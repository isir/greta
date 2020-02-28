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
package greta.core.animation.performer;

import greta.core.animation.CharacterBody;
import greta.core.animation.Frame;
import greta.core.animation.FrameSequence;
import greta.core.animation.IdleMovement;
import greta.core.animation.Skeleton;
import greta.core.animation.body.Arm;
import greta.core.animation.body.ExpressiveFrame;
import greta.core.animation.body.ExpressiveTorso;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.animation.mpeg4.bap.JointType;
import greta.core.animation.mpeg4.bap.file.BAPFileWriter;
import greta.core.keyframes.CancelableKeyframePerformer;
import greta.core.keyframes.GestureKeyframe;
import greta.core.keyframes.HeadKeyframe;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.ShoulderKeyframe;
import greta.core.keyframes.TorsoKeyframe;
import greta.core.signals.gesture.Hand;
import greta.core.signals.gesture.Position;
import greta.core.signals.gesture.TrajectoryDescription;
import greta.core.signals.gesture.UniformPosition;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
import greta.core.util.Constants;
import greta.core.util.Mode;
import greta.core.util.animation.AnimationFrameEmitter;
import greta.core.util.animation.AnimationFramePerformer;
import greta.core.util.enums.CompositionType;
import greta.core.util.enums.Side;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.math.Function;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.math.X;
import greta.core.util.math.easefunctions.EaseInOutSine;
import greta.core.util.math.easefunctions.EaseOutBack;
import greta.core.util.math.easefunctions.EaseOutBounce;
import greta.core.util.math.easefunctions.EaseOutQuad;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Jing Huang
 * @author Nawhal Sayarh
 */
public class AnimationKeyframePerformer extends CharacterDependentAdapter implements CancelableKeyframePerformer, BAPFrameEmitter, CharacterDependent, AnimationFrameEmitter {
    private SymbolicConverter symbolicConverter;
    private CharacterBody cb;
    private ExpressiveTorso exTorso = new ExpressiveTorso();
    private boolean expressiveTorso = true;
    private boolean useTraj = true;

    private Skeleton dynSk = null;
    private Vec3d gravity = new Vec3d(0, -9.8f, 0);
    private boolean useFakedDynamics = true;
    //BodyAnimationBAPFrameEmitter be = new BodyAnimationBAPFrameEmitter();
    BodyAnimationBapBlender be;
    private IdleMovement idle;
    private int incre = 0;
    private boolean usePropagation = true;
    private double weightPropagation = 0.1;
    private String propagationJoint = "vt6";

    private CharacterManager cm;

    private ArrayList<AnimationFramePerformer> afperformers = new ArrayList<>();

    public AnimationKeyframePerformer(CharacterManager cm) {
        symbolicConverter = new SymbolicConverter(cm);
        be = new BodyAnimationBapBlender(cm);
        setCharacterManager(cm);
        //cm.add(this);
        //To-do link it the on the graph level.
        cb = new CharacterBody(symbolicConverter.getOriginalSkeleton());
        cb.initMassSystemByOriginalSkeleton();
        idle = new IdleMovement(symbolicConverter.getOriginalSkeleton().clone());
        dynSk = symbolicConverter.getOriginalSkeleton().clone();

        //testFunction();
    }

    public void setCharacterManager(CharacterManager cm) {
        this.cm = cm;
    }

    @Override
    public void onCharacterChanged() {
        cb = new CharacterBody(symbolicConverter.getOriginalSkeleton());
        cb.initMassSystemByOriginalSkeleton();
        idle = new IdleMovement(symbolicConverter.getOriginalSkeleton().clone());
        dynSk = symbolicConverter.getOriginalSkeleton().clone();
    }

    public void setExpressiveTorso(boolean t) {
        expressiveTorso = t;
    }

    public boolean isUsePropagation() {
        return usePropagation;
    }

    public void setUsePropagation(boolean _usePropagation) {
        this.usePropagation = _usePropagation;
    }

    public double getWeightPropagation() {
        return weightPropagation;
    }

    public void setWeightPropagation(double _weightPropagation) {
        this.weightPropagation = _weightPropagation;
    }

    private double timeThreasure = 0.01;

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId) {
        // TODO : Mode management in progress
        performKeyframes(keyframes, requestId, new Mode(CompositionType.replace));
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId, Mode mode) {
        int size = keyframes.size();
        if (size < 2) {
            return;
        }
        double start = keyframes.get(0).getOffset();
        double end = keyframes.get(size - 1).getOffset();
        LinkedList<GestureKeyframe> leftGestureKeyframe = new LinkedList<>();
        LinkedList<GestureKeyframe> rightGestureKeyframe = new LinkedList<>();

        LinkedList<ExpressiveFrame> torso = new LinkedList<>();
        LinkedList<ExpressiveFrame> head = new LinkedList<>();
        LinkedList<ExpressiveFrame> leftShoulder = new LinkedList<>();
        LinkedList<ExpressiveFrame> rightShoulder = new LinkedList<>();
        double previousT_left = -1;
        double previousT_right = -1;
        double previousT_torse = -1;
        double previousT_head = -1;
        double previousT_leftShoulder = -1;
        double previousT_rightShoulder = -1;
        //double timelong = (double) (keyframes.get(keyframes.size() - 1).getOffset() - keyframes.get(0).getOffset());
        for (Keyframe kf : keyframes) {
            if (kf instanceof GestureKeyframe) {
                GestureKeyframe keyframe = (GestureKeyframe) kf;
                Side side = keyframe.getSide();
                if (side == Side.LEFT) {
                    if (Math.abs(previousT_left - keyframe.getOffset()) < timeThreasure) {
                        continue;
                    }
                    previousT_left = keyframe.getOffset();
                    leftGestureKeyframe.add(keyframe);
                } else if (side == Side.RIGHT) {
                    if (Math.abs(previousT_right - keyframe.getOffset()) < timeThreasure) {
                        continue;
                    }
                    previousT_right = keyframe.getOffset();
                    rightGestureKeyframe.add(keyframe);
                   // System.out.println(keyframe.getHand().getPosition().getX()+" "+keyframe.getHand().getPosition().getY()+" "+keyframe.getHand().getPosition().getZ());
                } else {
                    System.out.println("IKKeyFramePerformer: GestureKeyframe side error:" + side);
                }
            } else if (kf instanceof TorsoKeyframe) {
                TorsoKeyframe keyframe = (TorsoKeyframe) kf;
                if (Math.abs(previousT_torse - keyframe.getOffset()) < timeThreasure) {
                    continue;
                }
                previousT_torse = keyframe.getOffset();
                torso.add(symbolicConverter.getTorse(keyframe));
            } else if (kf instanceof HeadKeyframe) {
                HeadKeyframe keyframe = (HeadKeyframe) kf;
                if (Math.abs(previousT_head - keyframe.getOffset()) < timeThreasure) {
                    continue;
                }
                previousT_head = keyframe.getOffset();
                head.add(symbolicConverter.getHead(keyframe));
            } else if (kf instanceof ShoulderKeyframe) {
                ShoulderKeyframe keyframe = (ShoulderKeyframe) kf;
                String side = keyframe.getSide();
                if (side.equalsIgnoreCase("LEFT")) {
                    if (Math.abs(previousT_leftShoulder - keyframe.getOffset()) < timeThreasure) {
                        continue;
                    }
                    previousT_leftShoulder = keyframe.getOffset();
                    leftShoulder.add(symbolicConverter.getShoulder(keyframe));
                } else if (side.equalsIgnoreCase("RIGHT")) {
                    if (Math.abs(previousT_rightShoulder - keyframe.getOffset()) < timeThreasure) {
                        continue;
                    }
                    previousT_rightShoulder = keyframe.getOffset();
                    rightShoulder.add(symbolicConverter.getShoulder(keyframe));
                }
            } else {
                //System.out.println("IKKeyFramePerformer: Keyframe type error : "+kf.getClass().getSimpleName());
            }
        }

        if(useTraj){
            applyTraj(leftGestureKeyframe);
            applyTraj(rightGestureKeyframe);
        }

        //replanify the position
        applyTCB(leftGestureKeyframe);
        applyTCB(rightGestureKeyframe);

        //build by IK
        LinkedList<ExpressiveFrame> leftArm = new LinkedList<>();
        LinkedList<ExpressiveFrame> rightArm = new LinkedList<>();
        propagateArm(leftGestureKeyframe, leftArm);
        propagateArm(rightGestureKeyframe, rightArm);

        //sendTotally(leftArm, rightArm, torso, head, leftShoulder, rightShoulder, requestId);
//        for(ExpressiveFrame arm : rightArm){
//            System.out.println(((Arm)arm).getTime() + " "+((Arm)arm).getTarget());
//        }
        sendByFrame(start, end, leftArm, rightArm, torso, head, leftShoulder, rightShoulder, requestId, mode);
    }

    private void propagateArm (List<GestureKeyframe> gestureKeyframes, LinkedList<ExpressiveFrame> side) {
        boolean first = true;
        Arm previous = null;
        for (GestureKeyframe keyframe : gestureKeyframes) {
            Arm arm = symbolicConverter.getArm(keyframe);
            side.add(arm);
            if (first) {
                first = false;
            } else {
                applyPropagation(previous, arm);
            }
            previous = arm;
        }
    }

    private void applyPropagation(Arm armprevious, Arm arm) {
        if (usePropagation) {
            Vec3d t0 = armprevious.getTarget();
            Vec3d t1 = arm.getTarget();
            double scale = 1;
            Vec3d dir = Vec3d.substraction(t1, t0);
            Vec3d axisX = new Vec3d(1, 0, 0);
            Quaternion q = new Quaternion();
            if(Math.abs(dir.z()) > 1 || Math.abs(dir.x()) > 1) scale = 170;
            //System.out.println(dir.z() +" "+ dir.x());
            q.setAxisAngle(axisX, dir.z() * weightPropagation / scale /* arm.getExpressivityParameters().spc*/);
            arm.accumulateRotation(propagationJoint, q);

            Vec3d axisY = new Vec3d(0, 1, 0);
            Quaternion q2 = new Quaternion();
            q2.setAxisAngle(axisY, dir.x() * weightPropagation / scale /* arm.getExpressivityParameters().spc*/);
            arm.accumulateRotation(propagationJoint, q2);

//            if (arm.getSide() == Side.LEFT) {
//                Vec3d axisZ = new Vec3d(0, 0, 1);
//                Quaternion q3 = new Quaternion();
//                q3.setAxisAngle(axisZ, dir.x() * weightPropagation * arm.getExpressivityParameters().spc);
//                arm.accumulateRotation(propagationJoint, q3);
//
//            } else if (arm.getSide() == Side.RIGHT) {
//                Vec3d axisZ = new Vec3d(0, 0, 1);
//                Quaternion q3 = new Quaternion();
//                q3.setAxisAngle(axisZ, dir.x() * weightPropagation * arm.getExpressivityParameters().spc);
//                arm.accumulateRotation(propagationJoint, q3);
//
//            }
        }
    }

    private void applyTraj(LinkedList<GestureKeyframe> gests){
        if(gests.isEmpty())
            return ;
        LinkedList<GestureKeyframe> result = new LinkedList<>();
        for(int i = 0; i < gests.size() - 1; ++i){
            GestureKeyframe first = gests.get(i);
            GestureKeyframe next = gests.get(i + 1);
            result.add(first);
            TrajectoryDescription trj = first.getTrajectoryType();
            if(trj == null || trj.getName().equalsIgnoreCase("Linear")) continue;
            double duration = next.getOffset() - first.getOffset();
            if(duration < 0.1) continue;
            int frameNB = (int) (10 * duration);
            if (frameNB > 10) {
                frameNB *= 0.9;  //for avoiding the pb with precision  when frameNB = keyframe * dure < 1 then will not create the frame in the interpolator
            }
            if(frameNB < 1) continue;
            if(!trj.getName().equalsIgnoreCase("Linear") && !trj.getName().isEmpty() && trj.isUsed() && duration >= 0.1){
                TrajectoryDescription.Variation v;
                if (first.getParameters().pwr > 0) {
                    v = TrajectoryDescription.Variation.GREATER;
                } else if (first.getParameters().pwr < 0) {
                    v = TrajectoryDescription.Variation.SMALLER;
                } else {
                    v = TrajectoryDescription.Variation.NONE;
                }
                Position pos1= first.getHand().getPosition();
                //System.out.println(first.getOffset());
                Position pos2= next.getHand().getPosition();
                ArrayList<Vec3d> poses = trj.compute(new Vec3d(pos1.getX(), pos1.getY(), pos1.getZ()), new Vec3d(pos2.getX(), pos2.getY(), pos2.getZ()),frameNB,v);
                for (int j = 0; j < poses.size(); ++j) {
                    double time = first.getOffset() + (next.getOffset() - first.getOffset()) * ((double) (j + 1) / (double) (poses.size() + 1));
                    //System.out.println(time+" " +poses.get(j) + "");
                    double intefactor = (time - first.getOffset())  / (next.getOffset() - first.getOffset());
                    Quaternion q = Quaternion.slerp(first.getHand().getWristOrientation(), next.getHand().getWristOrientation(), intefactor, true);

                    Vec3d p = poses.get(j);
                    Hand hand = new Hand(first.getHand());
                    UniformPosition pos = new UniformPosition();
                    pos.setX(p.x());
                    pos.setY(p.y());
                    pos.setZ(p.z());
                    hand.setPosition(pos);
                    hand.setWristOrientation(q);
                    GestureKeyframe gest = new GestureKeyframe(first.getId(), first.getPhaseType(), null,time, time, hand,first.getScriptName(),first.isIsScript() );
                    result.add(gest);
                }
            }
        }
        result.add(gests.getLast());
        if(gests.size() != result.size()){
            gests.clear();
            gests.addAll(result);
        }
    }

    private void applyTCB(LinkedList<GestureKeyframe> gestureKeyframe) {
        if (gestureKeyframe.size() > 4) {
            ArrayList<Vec3d> newPos = new ArrayList<Vec3d>();
            for (int i = 2; i < gestureKeyframe.size() - 2; ++i) {
                UniformPosition ps1 = (UniformPosition) gestureKeyframe.get(i - 2).getHand().getPosition();
                Vec3d p1 = new Vec3d(ps1.getX(), ps1.getY(), ps1.getZ());

                UniformPosition ps2 = (UniformPosition) gestureKeyframe.get(i - 1).getHand().getPosition();
                Vec3d p2 = new Vec3d(ps2.getX(), ps2.getY(), ps2.getZ());

                UniformPosition ps = (UniformPosition) gestureKeyframe.get(i).getHand().getPosition();
                Vec3d p = new Vec3d(ps.getX(), ps.getY(), ps.getZ());
                //System.out.println("orig: " + p);
                UniformPosition ps3 = (UniformPosition) gestureKeyframe.get(i + 1).getHand().getPosition();
                Vec3d p3 = new Vec3d(ps3.getX(), ps3.getY(), ps3.getZ());

                UniformPosition ps4 = (UniformPosition) gestureKeyframe.get(i + 2).getHand().getPosition();
                Vec3d p4 = new Vec3d(ps4.getX(), ps4.getY(), ps4.getZ());

                Vec3d pM = TCB.computePosition(0.5, p1, p2, p3, p4, 0.5, 0.5, 0.5);

                double fld = gestureKeyframe.get(i).getParameters().fld;

                Vec3d finalP = Vec3d.addition(Vec3d.multiplication(p, 1 - fld), Vec3d.multiplication(pM, fld));
                newPos.add(finalP);
//                ps.setX(finalP.x());
//                ps.setY(finalP.y());
//                ps.setZ(finalP.z());
                //System.out.println("final: " + finalP);
            }
            int n = 0;
            for (int i = 2; i < gestureKeyframe.size() - 2; ++i) {
                UniformPosition ps = (UniformPosition) gestureKeyframe.get(i).getHand().getPosition();
                Vec3d finalP = newPos.get(n);
                ps.setX(finalP.x());
                ps.setY(finalP.y());
                ps.setZ(finalP.z());
                n++;
            }
        }
    }

    /* void sendTotally(LinkedList<ExpressiveFrame> left, LinkedList<ExpressiveFrame> right,
     LinkedList<ExpressiveFrame> torso, LinkedList<ExpressiveFrame> head,
     LinkedList<ExpressiveFrame> leftShoulder, LinkedList<ExpressiveFrame> rightShoulder, ID requestId) {
     {
     FrameSequence fsHead = this.generateExpressiveSequence(head, Constants.FRAME_PER_SECOND, null);
     FrameSequence fsTorso = this.generateExpressiveSequence(torso, Constants.FRAME_PER_SECOND, null);
     FrameSequence fsShoulderLeft = this.generateExpressiveSequence(leftShoulder, Constants.FRAME_PER_SECOND, null);
     FrameSequence fsShoulderRight = this.generateExpressiveSequence(rightShoulder, Constants.FRAME_PER_SECOND, null);
     FrameSequence fsarmr = generateExpressiveSequence(right, Constants.FRAME_PER_SECOND, null);
     FrameSequence fsarml = generateExpressiveSequence(left, Constants.FRAME_PER_SECOND, null);

     if (fsarmr != null) {
     be.updateFrameList(fsarmr, requestId);
     }

     if (fsarml != null) {
     be.updateFrameList(fsarml, requestId);
     }

     if (fsHead != null) {
     be.updateFrameList(fsHead, requestId);
     }
     if (fsTorso != null) {
     be.updateFrameList(fsTorso, requestId);
     }
     if (fsShoulderLeft != null) {
     be.updateFrameList(fsShoulderLeft, requestId);
     }
     if (fsShoulderRight != null) {
     be.updateFrameList(fsShoulderRight, requestId);
     }
     //            if (_useFakedDynamics) {
     //                ArrayList<AnimationFrame> afs = new ArrayList<AnimationFrame>();
     //                int first = (int) (fsBody.getStartTime() * Constants.FRAME_PER_SECOND + 0.5);
     //                for (Frame frame : fsBody.getSequence()) {
     //                    AnimationFrame af = getAnimationFrame(frame, first);
     //                    first++;
     //                    afs.add(af);
     //                }
     //                for(AnimationFramePerformer performer : afperformers){
     //                    performer.performAnimationFrames(afs, requestId);
     //                }
     //            }
     }
     }*/

    private ExpressiveFrame fillFrame(double time, LinkedList<ExpressiveFrame> expressiveFrames, ExpressiveFrame expressiveFrame, Frame frame) {
        if (expressiveFrame != null && expressiveFrames.size() > 0) {
            double first = expressiveFrame.getTime();
            double second = expressiveFrames.get(0).getTime();
            if (time > second) {
                expressiveFrame = expressiveFrames.poll();
                first = expressiveFrame.getTime();
                if (expressiveFrames.size() > 0) {
                    second = expressiveFrames.get(0).getTime();
                } else {
                    frame.interpolation(expressiveFrame, expressiveFrame, 1);
                    second = -1;
                }
            }
            if (second > 0) {
                Function function = getFunction(expressiveFrames.get(0));
                double ratio = function.f((time - first) / (second - first));
                if (first <= time && second > time) {
                    frame.interpolation(expressiveFrame, expressiveFrames.get(0), ratio);
                }
            }
        }
        return expressiveFrame;
    }

    private ExpressiveFrame fillFrame2(double time, LinkedList<ExpressiveFrame> expressiveFrames, ExpressiveFrame expressiveFrame, Frame frame) {
        while (expressiveFrames.size() > 0 && time > expressiveFrames.get(0).getTime()) {
            expressiveFrame = expressiveFrames.poll();
        }
        if (expressiveFrame != null) {
            if (expressiveFrames.size() > 0) {

                Function function = getFunction(expressiveFrames.get(0));
                double first = expressiveFrame.getTime();
                double second = expressiveFrames.get(0).getTime();
                double ratio = function.f((time - first) / (second - first));
                if (first <= time && second > time) {
                    frame.interpolation(expressiveFrame, expressiveFrames.get(0), ratio);
                }
            } else {
                frame.addRotations(expressiveFrame.getRotations());
                //expressiveFrame = null;
            }
        }
        return expressiveFrame;
    }

    private void sendByFrame(double start, double end,
            LinkedList<ExpressiveFrame> left,
            LinkedList<ExpressiveFrame> right,
            LinkedList<ExpressiveFrame> torso,
            LinkedList<ExpressiveFrame> head,
            LinkedList<ExpressiveFrame> leftShoulder,
            LinkedList<ExpressiveFrame> rightShoulder,
            ID requestId,
            Mode mode) {

        double diff = Constants.FRAME_DURATION_SECONDS;
        ExpressiveFrame left1 = null;//left.poll();
        ExpressiveFrame right1 = null;//right.poll();
        ExpressiveFrame torso1 = null;//torso.poll();
        ExpressiveFrame head1 = null;//head.poll();
        ExpressiveFrame ls1 = null;//leftShoulder.poll();
        ExpressiveFrame rs1 = null;//rightShoulder.poll();

        List<BAPFrame> bapFrames = new ArrayList<>();
        for (double i = start; i <= end + diff; i = i + diff) {
            Frame f = new Frame();
            left1 = fillFrame2(i, left, left1, f);
            right1 = fillFrame2(i, right, right1, f);
            torso1 = fillFrame2(i, torso, torso1, f);
            head1 = fillFrame2(i, head, head1, f);
            ls1 = fillFrame2(i, leftShoulder, ls1, f);
            rs1 = fillFrame2(i, rightShoulder, rs1, f);

            BAPFrame bf = writeBAPFrame(i, f);
            bapFrames.add(bf);
        }

        be.updateFrames(bapFrames, requestId, mode);
    }

    @Override
    public void cancelKeyframesById(ID id) {
        be.cancelFramesWithId(id);
    }

    private FrameSequence generateExpressiveSequence(LinkedList<ExpressiveFrame> typeframes, int framePerSecond, Function function) {
        if (typeframes.size() < 1) {
            return null;
        } else if (typeframes.size() == 1) {
            FrameSequence fs = new FrameSequence();
            fs.setStartTime(typeframes.get(0).getTime());
            fs.setEndTime(typeframes.get(0).getTime());
            fs.add(typeframes.get(0));
            return fs;
        }
        FrameSequence fs = new FrameSequence();
        fs.setStartTime(typeframes.get(0).getTime());
        fs.setEndTime(typeframes.get(typeframes.size() - 1).getTime());
        for (int i = 0; i < typeframes.size() - 1; i++) {
            Frame f0 = typeframes.get(i);
            Frame f1 = typeframes.get(i + 1);
            function = getFunction(typeframes.get(i + 1));
            double time0 = typeframes.get(i).getTime();
            double time1 = typeframes.get(i + 1).getTime();
            double duration = time1 - time0;
            int fNb = (int) (duration * framePerSecond);
            for (int idx = 0; idx <= fNb; ++idx) {
                Frame f = new Frame();
                double r = function.f((double) idx / (double) fNb);
                f.interpolation(f0, f1, r);
                fs.add(f);
            }
        }
        return fs;
    }

    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapfp) {
        be.addBAPFramePerformer(bapfp);
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapfp) {
        be.removeBAPFramePerformer(bapfp);
    }

    private Function getFunction(ExpressiveFrame expFrame) {
        Function toReturn = X.x;
        double pwr = 0;
                if (expFrame.getExpressivityParameters() != null) {
                    pwr = expFrame.getExpressivityParameters().pwr;
                }
        if (pwr >= 0.1) {
            if (pwr < 0.3) {
                toReturn = new EaseInOutSine();
            } else if (pwr < 0.5) {
                toReturn = new EaseOutQuad();
            } else if (pwr < 0.7) {
                toReturn = new EaseOutQuad();
            } else if (pwr < 0.95) {
                toReturn = new EaseOutBack(); //overshoot
            } else if (pwr >= 0.95) {
                toReturn = new EaseOutBounce(); //rebounce
            }
        }
        if(expFrame.isIsStrokeEnd()){
            toReturn = new EaseOutBack();
        }
        return toReturn;

    }

    private int getFrameNb(double current, double start, double secondPerFrame) {
        return (int) ((current - start) / secondPerFrame);
    }

    private boolean isKeyframesSorted(List<Keyframe> keyframes) {
        Keyframe k0 = keyframes.get(0);
        for (int i = 1; i < keyframes.size(); ++i) {
            Keyframe k1 = keyframes.get(i);
            if (k0.getOffset() > k1.getOffset()) {
                return false;
            }
            k0 = k1;
        }
        return true;
    }

    private ArrayList<Skeleton> getSkeletons(FrameSequence fs) {
        ArrayList<Skeleton> sks = new ArrayList<Skeleton>();
        for (Frame f : fs.getSequence()) {
            sks.add(getSkeleton(f));
        }
        return sks;
    }

    private Skeleton getSkeleton(Frame f) {
        Skeleton sk = cb.getOriginalSkeleton().clone();
        sk.getJoint(0).setLocalPosition(f.getRootTranslation());
        for (String name : f.getRotations().keySet()) {
            sk.getJoint(name).setLocalRotation(f.getRotation(name));
        }
        sk.update();
        return sk;
    }

    @Override
    public void addAnimationFramePerformer(AnimationFramePerformer afp) {
        afperformers.add(afp);
    }

    @Override
    public void removeAnimationFramePerformer(AnimationFramePerformer afp) {
        afperformers.remove(afp);
    }

    public static void writeBap(String name, BAPFrame bf, Vec3d angle) {
        BAPType z = JointType.get(name).rotationZ;
        BAPType y = JointType.get(name).rotationY;
        BAPType x = JointType.get(name).rotationX;
        bf.setRadianValue(z, angle.z());
        bf.setRadianValue(y, angle.y());
        bf.setRadianValue(x, angle.x());
    }

    public static BAPFrame writeBAPFrame(double time, Frame frame) {
        BAPFrame bf = new BAPFrame((int) (time * Constants.FRAME_PER_SECOND + 0.5));
        HashMap<String, Quaternion> results = frame.getRotations();
        Iterator iterator = results.keySet().iterator();
        while (iterator.hasNext()) {
            String name = (String) iterator.next();
            Quaternion q = results.get(name);
            Vec3d angle = q.getEulerAngleXYZ();
            JointType joint = JointType.get(name);
            BAPType z = joint.rotationZ;
            BAPType y = joint.rotationY;
            BAPType x = joint.rotationX;
            bf.setRadianValue(z, angle.z());
            bf.setRadianValue(y, angle.y());
            bf.setRadianValue(x, angle.x());
        }
        Vec3d t = frame.getRootTranslation();
        if (t.x() == 0 && t.y() == 0 && t.z() == 0) {

        } else {
            bf.applyValue(BAPType.HumanoidRoot_tr_lateral, (int) ((t.x()) * 1.9 * 10));
            bf.applyValue(BAPType.HumanoidRoot_tr_vertical, (int) ((t.y()) * 1.9 * 10));
            bf.applyValue(BAPType.HumanoidRoot_tr_frontal, (int) ((t.z()) * 1.9 * 10));
        }
        return bf;
    }

    private void testFunction() {
        RelaxArmDynamicsMotion ram = new RelaxArmDynamicsMotion("left", symbolicConverter.getOriginalSkeleton());
        ram.setDesireAngles(-0.3, -0.4);
        ram.setTime(0, 100);
        ram.setInputs(-0.5, -0.3);
        ram.generate();
        ArrayList<Double> elbowl = ram.getOutput_X_elbow();
        ArrayList<Double> shoulderl = ram.getOutput_X_shoulder();

        BAPFileWriter writer = new BAPFileWriter();
        ArrayList<BAPFrame> frames = new ArrayList<BAPFrame>();
        for (int i = 0; i < elbowl.size(); ++i) {
            BAPFrame bap = new BAPFrame(i);
            writeBap("l_shoulder", bap, new Vec3d((shoulderl.get(i).doubleValue()), 0, 0));
            writeBap("l_elbow", bap, new Vec3d((elbowl.get(i).doubleValue()), 0, 0));
            frames.add(bap);
        }
        ID id = IDProvider.createID("testdynamics in AnimationKeyFramePerformer");
        writer.performBAPFrames(frames, id);
    }

    private Vec3d getTorque(Vec3d shoulder, Vec3d elbow, Vec3d wrist, Vec3d gestureSpaceScale) {
        Vec3d low = Vec3d.addition(wrist, elbow);
        low.scale(0.5f, 0.5f, 0.5f);
        low.minus(shoulder);
        low.set(low.x() / gestureSpaceScale.x(), low.y() / gestureSpaceScale.x(), low.z() / gestureSpaceScale.z());
        Vec3d t0 = low.cross3(gravity);

        Vec3d up = Vec3d.addition(shoulder, elbow);
        up.scale(0.5f, 0.5f, 0.5f);
        up.minus(shoulder);
        up.set(up.x() / gestureSpaceScale.x(), up.y() / gestureSpaceScale.x(), up.z() / gestureSpaceScale.z());
        Vec3d t1 = up.cross3(gravity);
        t0.add(t1);
        return t0;
    }

    private void applyFakedDynamics(Frame current) {
        dynSk.reset();
        dynSk.loadFrame(current);
        dynSk.update();
        Vec3d torqueL = getTorque(
                dynSk.getJoint("l_shoulder").getWorldPosition(),
                dynSk.getJoint("l_elbow").getWorldPosition(),
                dynSk.getJoint("l_wrist").getWorldPosition(),
                symbolicConverter.gestureSpace_LeftScale);
        Vec3d torqueR = getTorque(
                dynSk.getJoint("r_shoulder").getWorldPosition(),
                dynSk.getJoint("r_elbow").getWorldPosition(),
                dynSk.getJoint("r_wrist").getWorldPosition(),
                symbolicConverter.gestureSpace_RightScale);

        Vec3d torque = Vec3d.addition(torqueL, torqueR).opposite();
//        double tx = tx = compute(0, torque.x(), 0, 0);
//        double ty = compute(0, torque.y(), 0, 0);
//        double tz = compute(0, torque.z(), 0, 0);
//        torque.set(tx, ty, tz);

        double angle = torque.length();
        if (angle > 0) {
//            torque.divide(angle); //normalise //useless : Quaternion will do it
            double amp = 0.0015f;
            Quaternion rotation = new Quaternion(torque, angle * amp);
            current.accumulateRotation("vl5", rotation);
            current.accumulateRotation("vl1", rotation);
            current.accumulateRotation("vt7", rotation);
            //System.out.println(rotation);
        }
    }

//    double _friction = 0;
//    double _kP = 0.5;
//    double _kD = 2 * Math.sqrt(_kP);
//    public double compute(double desireAngle, double currentAngle, double desireVelocity, double currentvelocity) {
//        double _torqueOutput = (desireAngle - currentAngle) * _kP + (desireVelocity - currentvelocity) * _kD - _friction * currentvelocity;
//        //double _torqueOutput = Math.signum(desireAngle - currentAngle) * _kP ;
//        return _torqueOutput;
//    }
}
