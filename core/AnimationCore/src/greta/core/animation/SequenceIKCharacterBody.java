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
package greta.core.animation;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class SequenceIKCharacterBody extends CharacterBody {

    PositionFrame _finalPosFrame = new PositionFrame();

    public SequenceIKCharacterBody(Skeleton sk) {
        super(sk);
    }

    public void setStartFrame(PositionFrame frame) {
        if (_sk == null) {
            return;
        }
        this.resetSkeleton();
        this.initMassSystemByOriginalSkeleton();
        for (String name : frame.getValues().keySet()) {
            Mass mass = getMassByName(name);
            mass.setPosition(frame.getValue(name));
//            if (mass.getName().equalsIgnoreCase("l_shoulder")) {
//                System.out.println(mass.getPosition() + mass.getName() + mass._movable);
//            }
        }
    }

    public PositionFrame getFinalPositionFrame() {
        return _finalPosFrame;
    }

    public void updateAllFrames(ArrayList<Frame> torso, ArrayList<Frame> left, ArrayList<Frame> right, ArrayList<Vec3d> lefthand, ArrayList<Vec3d> righthand) {
        double energy = 10000;
        while (energy > EnergyEpsilon) {
            for (int i = 0; i < 3; ++i) {
                energy = 0;
                for (String name : _externalConstraintList.keySet()) {
                    Mass mass = getMassByName(name);
                    MassConstraint mc = _externalConstraintList.get(name);
                    mass.addForce(mc.getForce());
                    //System.out.println(mass.getPosition() + mass.getName() + mc.getForce());
                }
                for (Mass mass : _massList) {
                    mass.applyConstraints();
                    energy += Math.abs(mass.getAcceleration().length());
                    mass.move();
                    mass.resetAcceleration();
                }
            }
            //System.out.println("SequenceIKCharacterBody class:system iternal energy : " + energy);

            Frame t = new Frame();
            Frame l = new Frame();
            Frame r = new Frame();

            Quaternion vl5 = new Quaternion();
            computeUpBase(vl5);
            t.addRotation("vl5", vl5);
            Quaternion l_shoulder = new Quaternion();
            computeLeftShoulder(l_shoulder);
            l.addRotation("l_shoulder", l_shoulder);
            Quaternion r_shoulder = new Quaternion();
            computeRightShoulder(r_shoulder);
            r.addRotation("r_shoulder", r_shoulder);
            Quaternion l_elbow = new Quaternion();
            Vec3d posL = new Vec3d();
            computeLeftElbow(l_elbow, posL);
            l.addRotation("l_elbow", l_elbow);
            Quaternion r_elbow = new Quaternion();
            Vec3d posR = new Vec3d();
            computeRightElbow(r_elbow, posR);
            r.addRotation("r_elbow", r_elbow);

            //System.out.println(t.getRotation("vl5").getEulerAngleXYZByAngle());
            torso.add(t);
            left.add(l);
            right.add(r);
            lefthand.add(posL);
            righthand.add(posR);
        }

        for (Mass mass : _massList) {
            _finalPosFrame.setPoint(mass.getName(), mass.getPosition());
        }
    }

    @Override
    public Frame updateFinalFrame() {
        FrameSequence frames = new FrameSequence();
        double energy = 10000;
        int loop = 0;
        int maxLoop = 150;
        while (energy > EnergyEpsilon && loop < maxLoop) {
            energy = 0;
            for (String name : _externalConstraintList.keySet()) {
                Mass mass = getMassByName(name);
                MassConstraint mc = _externalConstraintList.get(name);
                mass.addForce(mc.getForce());
            }
            for (Mass mass : _massList) {
                mass.applyConstraints();
                energy += Math.abs(mass.getAcceleration().length());
                mass.move();
                mass.resetAcceleration();
            }
            loop++;
        }

        Frame frame = computeAngles();
        for (Mass mass : _massList) {
            _finalPosFrame.setPoint(mass.getName(), mass.getPosition());
        }
        return frame;
    }

    public FrameSequence warp(FrameSequence torso, FrameSequence left, FrameSequence right, ArrayList<Vec3d> lefthand, ArrayList<Vec3d> righthand, double factor) {
        FrameSequence output = new FrameSequence(torso.getStartTime(), torso.getEndTime());
        if (torso.getSequence().size() < 3) {
            for (int i = 0; i < torso.getSequence().size(); ++i) {
                Frame f = new Frame();
                f.addRotations(torso.getSequence().get(i).getRotations());
                f.addRotations(left.getSequence().get(i).getRotations());
                f.addRotations(right.getSequence().get(i).getRotations());
                output.add(f);
            }
            return output;
        }

        ArrayList<Double> distanceLeft = new ArrayList<Double>();
        distanceLeft.add(0.);
        ArrayList<Double> distanceRight = new ArrayList<Double>();
        distanceRight.add(0.);
        for (int i = 1; i < lefthand.size(); ++i) {
            distanceLeft.add(Vec3d.substraction(lefthand.get(i), lefthand.get(i - 1)).length());
        }

        for (int i = 1; i < righthand.size(); ++i) {
            distanceRight.add(Vec3d.substraction(righthand.get(i), righthand.get(i - 1)).length());
        }

        FrameSequence warppedleft = SequenceWarpper.getDistanceWarppedSequence(left, distanceLeft, (int) (left.getSequence().size() * factor));
        FrameSequence warppedright = SequenceWarpper.getDistanceWarppedSequence(right, distanceRight, (int) (right.getSequence().size() * factor));

        for (int i = 0; i < (int) (torso.getSequence().size() * (factor)); ++i) {
            Frame f = new Frame();
            f.addRotations(torso.getSequence().get((int) ((double)i / factor)).getRotations());
            f.addRotations(warppedleft.getSequence().get(i).getRotations());
            f.addRotations(warppedright.getSequence().get(i).getRotations());
            output.add(f);
        }

        return output;
    }

    public ArrayList<Frame> warp(ArrayList<Frame> torso, ArrayList<Frame> left, ArrayList<Frame> right, ArrayList<Vec3d> lefthand, ArrayList<Vec3d> righthand, double factor) {
        ArrayList<Frame> output = new ArrayList<Frame>();
        if (torso.size() < 3) {
            for (int i = 0; i < torso.size(); ++i) {
                Frame f = new Frame();
                f.addRotations(torso.get(i).getRotations());
                f.addRotations(left.get(i).getRotations());
                f.addRotations(right.get(i).getRotations());
                output.add(f);
            }
            return output;
        }


        ArrayList<Double> distanceLeft = new ArrayList<Double>();
        distanceLeft.add(0.);
        ArrayList<Double> distanceRight = new ArrayList<Double>();
        distanceRight.add(0.);
        for (int i = 1; i < lefthand.size(); ++i) {
            distanceLeft.add(Vec3d.substraction(lefthand.get(i), lefthand.get(i - 1)).length());
        }

        for (int i = 1; i < righthand.size(); ++i) {
            distanceRight.add(Vec3d.substraction(righthand.get(i), righthand.get(i - 1)).length());
        }

        ArrayList<Frame> warppedleft = SequenceWarpper.getDistanceWarppedSequence(left, distanceLeft, (int) (left.size() * factor));
        ArrayList<Frame> warppedright = SequenceWarpper.getDistanceWarppedSequence(right, distanceRight, (int) (right.size() * factor));

        for (int i = 0; i < (int) (torso.size() * factor); ++i) {
            Frame f = new Frame();
            f.addRotations(torso.get((int) (i / factor)).getRotations());
            //System.out.println(torso.get((int) (i / factor)).getRotations().get("v15"));
            f.addRotations(warppedleft.get(i).getRotations());
            f.addRotations(warppedright.get(i).getRotations());
            output.add(f);
        }
        return output;
    }

    public ArrayList<Frame> output(ArrayList<Frame> torso, ArrayList<Frame> left, ArrayList<Frame> right) {
        ArrayList<Frame> output = new ArrayList<Frame>();
        for (int i = 0; i < torso.size(); ++i) {
            Frame f = new Frame();
            f.addRotations(torso.get(i).getRotations());
            f.addRotations(left.get(i).getRotations());
            f.addRotations(right.get(i).getRotations());
            output.add(f);
        }
        return output;
    }

    public void computeLeftElbow(Quaternion q, Vec3d pos) {
        Vec3d oldUpper = Vec3d.substraction(_virtualElbow_L, _sk_original.getJoint("l_shoulder").getPosition()).normalized();
        Vec3d oldLower = Vec3d.substraction(_sk_original.getJoint("l_wrist").getPosition(), _virtualElbow_L).normalized();
        double angle = (double) Math.acos(oldUpper.dot3(oldLower));

        Vec3d Upper = Vec3d.substraction(getMassByName("l_elbow").getPosition(), getMassByName("l_shoulder").getPosition()).normalized();
        Vec3d Lower = Vec3d.substraction(getMassByName("l_wrist").getPosition(), getMassByName("l_elbow").getPosition()).normalized();
        double angle2 = (double) Math.acos(Upper.dot3(Lower));
        _l_elbow.setAxisAngle(new Vec3d(1, 0, 0), angle - angle2);
        _l_elbow.normalize();
        _sk.getJoint("l_elbow").setLocalRotation(_l_elbow);
        q.setValue(_l_elbow.clone());
        Mass m = getMassByName("l_wrist");
        pos.add(m.getPosition());
    }

    public void computeRightElbow(Quaternion q, Vec3d pos) {
        Vec3d oldUpper = Vec3d.substraction(_virtualElbow_R, _sk_original.getJoint("r_shoulder").getPosition()).normalized();
        Vec3d oldLower = Vec3d.substraction(_sk_original.getJoint("r_wrist").getPosition(), _virtualElbow_R).normalized();
        double angle = (double) Math.acos(oldUpper.dot3(oldLower));

        Vec3d Upper = Vec3d.substraction(getMassByName("r_elbow").getPosition(), getMassByName("r_shoulder").getPosition()).normalized();
        Vec3d Lower = Vec3d.substraction(getMassByName("r_wrist").getPosition(), getMassByName("r_elbow").getPosition()).normalized();
        double angle2 = (double) Math.acos(Upper.dot3(Lower));
        _r_elbow.setAxisAngle(new Vec3d(1, 0, 0), angle - angle2);
        _r_elbow.normalize();
        _sk.getJoint("r_elbow").setLocalRotation(_r_elbow);
        q.setValue(_r_elbow.clone());
        Mass m = getMassByName("r_wrist");
        pos.add(m.getPosition());
    }

    public void computeUpBase(Quaternion q) {
        Vec3d oldLeft = Vec3d.substraction(_sk_original.getJoint("l_shoulder").getPosition(), _sk_original.getJoint("vl5").getPosition()).normalized();
        Vec3d oldRight = Vec3d.substraction(_sk_original.getJoint("r_shoulder").getPosition(), _sk_original.getJoint("vl5").getPosition()).normalized();
        Vec3d oldAxis0 = Vec3d.cross3(oldLeft, oldRight).normalized();
        Vec3d oldAxis1 = Vec3d.addition(oldLeft, oldRight).normalized();
        Vec3d oldAxis2 = Vec3d.cross3(oldAxis0, oldAxis1).normalized();

        Vec3d left = Vec3d.substraction(getMassByName("l_shoulder").getPosition(), getMassByName("vl5").getPosition()).normalized();
        Vec3d right = Vec3d.substraction(getMassByName("r_shoulder").getPosition(), getMassByName("vl5").getPosition()).normalized();
        Vec3d axis0 = Vec3d.cross3(left, right).normalized();
        Vec3d axis1 = Vec3d.addition(left, right).normalized();
        Vec3d axis2 = Vec3d.cross3(axis0, axis1).normalized();

        _upBase.fromRotatedBasis(axis0, axis1, axis2, oldAxis0, oldAxis1, oldAxis2);
        _upBase.normalize();
        _sk.getJoint("vl5").setLocalRotation(_upBase);
        q.setValue(_upBase.clone());
    }

    public void computeLeftShoulder(Quaternion q) {
        Vec3d oldUpper = Vec3d.substraction(_virtualElbow_L, _sk_original.getJoint("l_shoulder").getPosition()).normalized();
        //Vec3d oldLower = Vec3d.substraction(_sk_original.getJoint("l_wrist").getPosition(), _virtualElbow_L).normalized();
        Vec3d oldAxis0 = oldUpper.normalized();

        //Vec3d oldAxis1 = Vec3d.cross3(oldUpper, oldLower).normalized();
        Vec3d oldAxis1 = new Vec3d(-1, 0, 0);
        Vec3d oldAxis2 = Vec3d.cross3(oldAxis0, oldAxis1).normalized();
        oldAxis1 = Vec3d.cross3(oldAxis2, oldAxis0).normalized();

        Vec3d upper = Vec3d.substraction(getMassByName("l_elbow").getPosition(), getMassByName("l_shoulder").getPosition()).normalized();
        Vec3d lower = Vec3d.substraction(getMassByName("l_wrist").getPosition(), getMassByName("l_elbow").getPosition()).normalized();
        Vec3d axis0 = upper.normalized();
        Vec3d axis1 = Vec3d.cross3(upper, lower).normalized();
        double v = upper.dot3(lower);
        if (v > 0.99) {
            axis1 = new Vec3d(-1, 0, 0);
        }
        Vec3d axis2 = Vec3d.cross3(axis0, axis1).normalized();
        axis1 = Vec3d.cross3(axis2, axis0).normalized();

        _l_shoulder.fromRotatedBasis(axis0, axis1, axis2, oldAxis0, oldAxis1, oldAxis2);
        _l_shoulder = Quaternion.multiplication(_upBase.inverse(), _l_shoulder);
        _l_shoulder.normalize();
        _sk.getJoint("l_shoulder").setLocalRotation(_l_shoulder);
        q.setValue(_l_shoulder.clone());
    }

    public void computeRightShoulder(Quaternion q) {
        Vec3d oldUpper = Vec3d.substraction(_virtualElbow_R, _sk_original.getJoint("r_shoulder").getPosition()).normalized();
        //Vec3d oldLower = Vec3d.substraction(_sk_original.getJoint("r_wrist").getPosition(), _virtualElbow_R).normalized();
        Vec3d oldAxis0 = oldUpper.normalized();

        //Vec3d oldAxis1 = Vec3d.cross3(oldUpper, oldLower).normalized();
        Vec3d oldAxis1 = new Vec3d(-1, 0, 0);
        Vec3d oldAxis2 = Vec3d.cross3(oldAxis0, oldAxis1).normalized();
        oldAxis1 = Vec3d.cross3(oldAxis2, oldAxis0).normalized();

        Vec3d upper = Vec3d.substraction(getMassByName("r_elbow").getPosition(), getMassByName("r_shoulder").getPosition()).normalized();
        Vec3d lower = Vec3d.substraction(getMassByName("r_wrist").getPosition(), getMassByName("r_elbow").getPosition()).normalized();
        Vec3d axis0 = upper.normalized();
        Vec3d axis1 = Vec3d.cross3(upper, lower).normalized();
        double v = upper.dot3(lower);
        if (v > 0.99) {
            axis1 = new Vec3d(-1, 0, 0);
        }
        Vec3d axis2 = Vec3d.cross3(axis0, axis1).normalized();
        axis1 = Vec3d.cross3(axis2, axis0).normalized();

        _r_shoulder.fromRotatedBasis(axis0, axis1, axis2, oldAxis0, oldAxis1, oldAxis2);
        _r_shoulder = Quaternion.multiplication(_upBase.inverse(), _r_shoulder);
        _r_shoulder.normalize();
        _sk.getJoint("r_shoulder").setLocalRotation(_r_shoulder);
        q.setValue(_r_shoulder.clone());
    }
}
