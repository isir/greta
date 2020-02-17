/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.animation.common;

import greta.core.animation.common.Frame.JointFrame;
import greta.core.animation.common.Frame.KeyFrame;
import greta.core.animation.common.body.Arm;
import greta.core.animation.common.easingcurve.EquationFunctions;
import greta.core.animation.common.symbolic.EaseTimeFunction;
import greta.core.keyframes.ExpressivityParameters;
import greta.core.signals.gesture.TrajectoryDescription;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.LinkedList;

/**
 *
 * @author Jing Huang
 */
public class GesturePathInterpolator {

    int _iteration = 1;

    public int getIteration() {
        return _iteration;
    }

    public void setIteration(int _iteration) {
        this._iteration = _iteration;
    }


    public ArrayList<Arm> interpolateTCB(ArrayList<Arm> gestures) {
        ArrayList<Arm> arms = new ArrayList<Arm>();
        if (_iteration > 1) {
            for (int segment = 0; segment < gestures.size() - 1; ++segment) {
                // System.out.println("PathInterpolation segment "+segment);
                String phase = gestures.get(segment).getPhase();
                if (phase == null) {
                    arms.add(gestures.get(segment));
                } else if ((phase.equalsIgnoreCase("STROKE-START") || phase.equalsIgnoreCase("STROKE")) && gestures.get(segment).getExpressivityParameters().pwr >= 0.1) {
                    Arm vertices[] = new Arm[4];

                    if (segment == 0) {
                        vertices[0] = (gestures.get(0));
                    } else {
                        vertices[0] = (gestures.get(segment - 1));
                    }
                    if (segment < gestures.size() - 2) {
                        vertices[1] = gestures.get(segment);
                        vertices[2] = gestures.get(segment + 1);
                        vertices[3] = gestures.get(segment + 2);
                    } else if (segment < gestures.size() - 1) {
                        vertices[1] = gestures.get(segment);
                        vertices[2] = gestures.get(segment + 1);
                        vertices[3] = gestures.get(segment + 0);
                    } else if (segment < gestures.size()) {
                        vertices[1] = gestures.get(segment);

                        if (segment > 1) {
                            vertices[2] = gestures.get(segment - 1);
                            vertices[3] = gestures.get(segment - 1);
                        } else {
                            vertices[2] = gestures.get(segment);
                            vertices[3] = gestures.get(segment);
                        }

                    }

                    for (int iteration = 0; iteration < _iteration; ++iteration) {
                        //System.out.println("PathInterpolation p"+vertices[1].getExpressivityParameters());
                        Arm newVertex = compute((double) iteration / (double) _iteration, vertices[0], vertices[1], vertices[2], vertices[3]);
                        arms.add(newVertex);
                    }
                } else {
                    arms.add(gestures.get(segment));
                }
            }

            if (gestures.size() > 0) {
                arms.add(gestures.get(gestures.size() - 1));
            }
        } else {
            arms = gestures;
        }
//        for (Arm arm : arms) {
//            System.out.println(arm.getTime() + " " + arm.getSide() + arm._isRest + arm.getTarget());
//        }

        return arms;
    }

    Arm compute(double s, Arm p1, Arm p2, Arm p3, Arm p4) {
        if (s == 0) {
            // System.out.println("PathInterpolation Original"+p1.getTarget());
            return p2;
        }

        double time = p2.getTime() + (p3.getTime() - p2.getTime()) * s;
        double newS = s;
        if (p3.getExpressivityParameters().pwr > 0) {
            newS = EquationFunctions.easeOutCubic(s);
        } else if (p3.getExpressivityParameters().pwr < 0) {
            newS = EquationFunctions.easeInCubic(s);
        }

        Vec3d pos = Vec3d.interpolation(p2.getPosition(), p3.getPosition(), newS);
        Vec3d up = Vec3d.interpolation(p2.getUpDirectionVector(), p3.getUpDirectionVector(), newS);

        Arm a = new Arm(time);
        a.setPosition(pos);
        a.setUpDirectionVector(up);
        a.setSide(p2.getSide());
        Quaternion q1 = p2.getWrist();
        if (q1 == null) {
            q1 = new Quaternion();
        }
        Quaternion q2 = p3.getWrist();
        if (q2 == null) {
            q2 = new Quaternion();
        }
        a.setWrist(Quaternion.slerp(q1, q2, newS, true), p2.isWristLocalOrientation());
        a.setExpressivityParameters(null);
        //System.out.println("PathInterpolation added"+a.getTarget());
        return a;
    }

    /**
     * check the trajectory rebuild the arm gestures list
     * // need to check the time  if the time is small, you can not apply the trajectory, ifnot you will get frame jump
     * @param gestures
     * @return
     */
    public LinkedList<Arm> mixPathDriven(LinkedList<Arm> gestures) {

        LinkedList<Arm> arms = new LinkedList<Arm>();

        for (int segment = 0; segment < gestures.size() - 1; ++segment) {
            Arm armStart = gestures.get(segment);

            Arm armEnd = gestures.get(segment + 1);
            double duration = armEnd.getTime() - armStart.getTime();
            if(duration<0.0000001)
            {
                System.out.println("Path INterpolation error: received two same time key frames!");
                continue;
            }
            arms.add(armStart);

            int frameNB = (int) (KeyFrame.getFramePerSecond() * duration);
            if (frameNB > 4) {
                frameNB *= 0.9;  //for avoiding the pb with precision  when frameNB = keyframe * dure < 1 then will not create the frame in the interpolator
            }
            String phase = gestures.get(segment + 1).getPhase();

            if (armEnd == null) {
                //System.out.println("fk");
            }
            TrajectoryDescription trj = armEnd.getTrajectory();
            // need to check the time  if the time is small, you can not apply the trajectory, ifnot you will get frame jump
            if (trj != null && !trj.getName().equalsIgnoreCase("Linear") && !trj.getName().equalsIgnoreCase("") && trj.isUsed() && duration >= 0.1) {
                TrajectoryDescription.Variation v;
                if (armEnd.getExpressivityParameters().pwr > 0) {
                    v = TrajectoryDescription.Variation.GREATER;
                } else if (armEnd.getExpressivityParameters().pwr < 0) {
                    v = TrajectoryDescription.Variation.SMALLER;
                } else {
                    v = TrajectoryDescription.Variation.NONE;
                }
                /**
                 * trajectory
                 */
                //trj.makeCircle(0, 1, 5);
                //ArrayList<Vec3d> poses = trj.computeCircle(armStart.getPosition(), armEnd.getPosition(),0, 1, 5,frameNB, v);
                //trj.makeCircle(0, 1, 10);  //if use circle  use this functions
                ArrayList<Vec3d> poses = trj.compute(armStart.getPosition(), armEnd.getPosition(),frameNB,v);
                //System.out.println("++++++  "+armStart.getPosition());
                for (int i = 0; i < poses.size(); ++i) {
                    Vec3d pos = poses.get(i);
                    double time = armStart.getTime() + (armEnd.getTime() - armStart.getTime()) * ((double) (i + 1) / (double) (poses.size() + 1));
                    //System.out.println(i+ "  "+time+" "+pos);
                    Arm a = new Arm(time);
                    for(String name: armEnd.getJointFrames().keySet()){
                        JointFrame jf2 = armEnd.getJointFrame(name);
                        JointFrame jf1 = armStart.getJointFrame(name);
                        if(jf1 == null) jf1 = new JointFrame();
                        a.addJointFrame(name, JointFrame.interpolate(jf1, jf2, (double)(i + 1) / (poses.size() + 1)));
                    }
                    a.setPosition(pos);
                    a.getTarget().setEnergy(armEnd.getTarget().getEnergy());
                    a.setUpDirectionVector(Vec3d.interpolation(armStart.getUpDirectionVector(), armEnd.getUpDirectionVector(), (double)(i + 1) / (poses.size() + 1) ));
                    a.setSide(armStart.getSide());
                    Quaternion q1 = armStart.getWrist();
                    if (q1 == null) {
                        q1 = new Quaternion();
                    }
                    Quaternion q2 = armEnd.getWrist().normalized();
                    if (q2 == null) {
                        q2 = new Quaternion();
                    }
                    a.setWrist(Quaternion.slerp(q1, q2, (double) i / (double) poses.size(), true), armEnd.isWristLocalOrientation());

                    ExpressivityParameters p = new ExpressivityParameters(armEnd.getExpressivityParameters());
                    p.pwr = 0;
                    a.setExpressivityParameters(p);
                    arms.add(a);
                }
                //System.out.println("-----  "+armEnd.getPosition());
                ++segment;
                //System.out.println(""+ poses.size());
            } else if ("TCB".contains("11") && phase.equalsIgnoreCase("STROKE-END")) {
                //System.out.println(frameNB);
                Arm vertices[] = new Arm[4];
                if (segment == 0) {
                    vertices[0] = (gestures.get(0));
                } else {
                    vertices[0] = (gestures.get(segment - 1));
                }
                if (segment < gestures.size() - 2) {
                    vertices[1] = gestures.get(segment);
                    vertices[2] = gestures.get(segment + 1);
                    vertices[3] = gestures.get(segment + 2);
                } else if (segment < gestures.size() - 1) {
                    vertices[1] = gestures.get(segment);
                    vertices[2] = gestures.get(segment + 1);
                    vertices[3] = gestures.get(segment + 0);
                } else if (segment < gestures.size()) {
                    vertices[1] = gestures.get(segment);
                    if (segment > 1) {
                        vertices[2] = gestures.get(segment - 1);
                        vertices[3] = gestures.get(segment - 1);
                    } else {
                        vertices[2] = gestures.get(segment);
                        vertices[3] = gestures.get(segment);
                    }
                }

                for (int iteration = 0; iteration < frameNB; ++iteration) {
                    double variationTimeL = EaseTimeFunction.getInstance().getTime((double) iteration / (double) frameNB, armEnd.getExpressivityParameters(), armEnd.getFunction());
                    Arm newVertex = compute(variationTimeL, vertices[0], vertices[1], vertices[2], vertices[3]);
                    arms.add(newVertex);
                    ExpressivityParameters p = new ExpressivityParameters(armEnd.getExpressivityParameters());
                    p.pwr = 0;
                    newVertex.setExpressivityParameters(p);
                }
            }

        }
        if (gestures.size() > 0 /*&& arms.size() < gestures.size()*/) {
            arms.add(gestures.get(gestures.size() - 1));
        }
        return arms;
    }
}
