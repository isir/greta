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
package greta.core.behaviorrealizer.keyframegenerator;

import greta.core.behaviorrealizer.keyframegenerator.GestureKeyframeGenerator.GestureModifier;
import greta.core.keyframes.ExpressivityParameters;
import greta.core.keyframes.GestureKeyframe;
import greta.core.signals.gesture.GesturePose;
import greta.core.signals.gesture.GestureSignal;
import greta.core.signals.gesture.Hand;
import greta.core.signals.gesture.TouchPosition;
import greta.core.signals.gesture.UniformPosition;
import greta.core.util.math.Vec3d;
import java.util.List;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class ExpressiveGestureModifier implements GestureModifier {

    private void createPoseKeyFrame(List<ExpressivePose> out, double time, GesturePose pose, ExpressivityParameters ep) {
        ExpressivePose expose = new ExpressivePose();
        expose.pose = new GesturePose(pose);
        expose.time = time;
        expose.ep = ep;
        out.add(expose);
    }

    @Override
    public void generateKeyframesForOneGesture(GestureSignal gesture, List<GestureKeyframe> outputKeyframes) {

        ExpressivityParameters ep = new ExpressivityParameters();
        ep.spc = gesture.getSPC();
        ep.tension = gesture.getTension();
        ep.fld = gesture.getFLD();
        ep.pwr = gesture.getPWR();
        ep.tmp = gesture.getTMP();
        ExpressivityParameters epprep = new ExpressivityParameters();
        epprep.spc = gesture.getSPC();
        epprep.tension = gesture.getTension();
        epprep.fld = gesture.getFLD() / 2;
        epprep.pwr = gesture.getPWR() / 2;
        epprep.tmp = gesture.getTMP() / 2;
        ExpressivityParameters epretr = new ExpressivityParameters();
        epretr.spc = gesture.getSPC();
        epretr.tension = gesture.getTension();
        epretr.fld = gesture.getFLD() / 2;
        epretr.pwr = gesture.getPWR() / 2;
        epretr.tmp = gesture.getTMP() / 2;

        double startTime = gesture.getStart().getValue();
        double lastTime = startTime;
        createKeyframe(outputKeyframes, gesture.getId() + "-start", startTime, gesture.getStartRestPose(), epprep);
        List<GesturePose> poses = gesture.getPhases();
        if (!poses.isEmpty()) {
            double strokeTime = gesture.getTimeMarker("stroke-start").getValue();

            double readyTime = gesture.getTimeMarker("ready").getValue();
            if (readyTime <= startTime) {
                //start does not exist, we are ready or starting the stroke
                outputKeyframes.clear();
            }

            if (readyTime < strokeTime) {
                createKeyframe(outputKeyframes, gesture.getId() + "-ready", readyTime, poses.get(0), epprep);
            }

            ExpressivityParameters epstroke = epprep;
            for (GesturePose pose : poses) {
                createKeyframe(outputKeyframes, gesture.getId() + "-stroke", strokeTime + pose.getRelativeTime(), pose, epstroke);
                epstroke = ep;
            }

            lastTime = gesture.getTimeMarker("stroke-end").getValue();

            double relaxTime = gesture.getTimeMarker("relax").getValue();
            if (relaxTime > lastTime) {
                createKeyframe(outputKeyframes, gesture.getId() + "-relax", relaxTime, poses.get(poses.size() - 1), epretr);
                lastTime = relaxTime;
            }
        }

        double endTime = gesture.getEnd().getValue();
        if (lastTime < endTime) {
            createKeyframe(outputKeyframes, gesture.getId() + "-end", endTime, gesture.getEndRestPose(), epretr);
        }
    }

//
//    @Override
//    public void generateKeyframesForOneGesture(GestureSignal gesture, List<GestureKeyframe> outputKeyframes) {
//
//        ExpressivityParameters ep = new ExpressivityParameters();
//        ep.spc = gesture.getSPC();
//        ep.tension = gesture.getTension();
//        ep.fld = gesture.getFLD();
//        ep.pwr = gesture.getPWR();
//        ep.tmp = gesture.getTMP();
//        ExpressivityParameters epprep = new ExpressivityParameters();
//        epprep.spc = gesture.getSPC();
//        epprep.tension = gesture.getTension();
//        epprep.fld = gesture.getFLD() / 2;
//        epprep.pwr = gesture.getPWR() / 2;
//        epprep.tmp = gesture.getTMP() / 2;
//        ExpressivityParameters epretr = new ExpressivityParameters();
//        epretr.spc = gesture.getSPC();
//        epretr.tension = gesture.getTension();
//        epretr.fld = gesture.getFLD() / 2;
//        epretr.pwr = gesture.getPWR() / 2;
//        epretr.tmp = gesture.getTMP() / 2;
//
//        double startTime = gesture.getStart().getValue();
//        double lastTime = startTime;
//        List<GesturePose> poses = gesture.getPhases();
//
//        List<ExpressivePose> newposes = new ArrayList<ExpressivePose>();
//        createPoseKeyFrame(newposes, startTime, gesture.getStartRestPose(), epprep);
//
//        if (!poses.isEmpty()) {
//            double strokeTime = gesture.getTimeMarker("stroke-start").getValue();
//
//            double readyTime = gesture.getTimeMarker("ready").getValue();
//            if (readyTime <= startTime) {
//                //start does not exist, we are ready or starting the stroke
//                newposes.clear();
//            }
//
//            if (readyTime < strokeTime) {
//                createPoseKeyFrame(newposes, readyTime, poses.get(0), epprep);
////                    createKeyframe(outputKeyframes, gesture.getId() + "-ready", readyTime, poses.get(0), epprep);
//            }
//
//            ExpressivityParameters epstroke = epprep;
//            for (GesturePose pose : poses) {
//                createPoseKeyFrame(newposes, strokeTime + pose.getRelativeTime(), pose, epstroke);
//                epstroke = ep;
////                    createKeyframe(outputKeyframes, gesture.getId() + "-stroke", strokeTime + pose.getRelativeTime(), pose, ep);
//            }
//
//            lastTime = gesture.getTimeMarker("stroke-end").getValue();
//
//            double relaxTime = gesture.getTimeMarker("relax").getValue();
//            if (relaxTime > lastTime) {
//                createPoseKeyFrame(newposes, relaxTime, poses.get(poses.size() - 1), epretr);
////                    createKeyframe(outputKeyframes, gesture.getId() + "-relax", relaxTime, poses.get(poses.size() - 1), epretr);
//                lastTime = relaxTime;
//            }
//        }
//
//        double endTime = gesture.getEnd().getValue();
//        if (lastTime < endTime) {
//            createPoseKeyFrame(newposes, endTime, gesture.getEndRestPose(), epretr);
//        }
//
//        //modifyGesturePose(newposes);
//
//        for (ExpressivePose pose : newposes) {
//            createKeyframe(outputKeyframes, gesture.getId(), pose.time, pose.pose, pose.ep);
//        }
//    }
    void modifyGesturePose(List<ExpressivePose> poses) {
        int size = poses.size();
        if (size < 3) {
            return;
        }

        for (int i = 1; i < size - 1; ++i) {

            GesturePose posePrevious = poses.get(i - 1).pose;
            double tPrevious = poses.get(i - 1).time;
            Hand leftP = posePrevious.getLeftHand();
            Vec3d lP = new Vec3d(leftP.getPosition().getX(), leftP.getPosition().getY(), leftP.getPosition().getZ());
            Hand rightP = posePrevious.getRightHand();
            Vec3d rP = new Vec3d(rightP.getPosition().getX(), rightP.getPosition().getY(), rightP.getPosition().getZ());

            GesturePose poseNext = poses.get(i + 1).pose;
            double tNext = poses.get(i + 1).time;
            Hand leftN = poseNext.getLeftHand();
            Vec3d lN = new Vec3d(leftN.getPosition().getX(), leftN.getPosition().getY(), leftN.getPosition().getZ());
            Hand rightN = poseNext.getRightHand();
            Vec3d rN = new Vec3d(rightN.getPosition().getX(), rightN.getPosition().getY(), rightN.getPosition().getZ());

            ExpressivePose expose = poses.get(i);
            GesturePose poseCurrent = expose.pose;
            double t = expose.time;
            ExpressivityParameters ep = expose.ep;
            double fld = ep.fld;
            Hand left = poseCurrent.getLeftHand();
            if (!(left.getPosition() instanceof TouchPosition)) {
                Vec3d pos = new Vec3d(left.getPosition().getX(), left.getPosition().getY(), left.getPosition().getZ());
                Vec3d newPos = interpolate(lP, (fld), lN, (fld), pos, (5 - fld));
                UniformPosition up = new UniformPosition(newPos.x(), newPos.y(), newPos.z());
                left.setPosition(up);
                //System.out.println("old: " + pos + " new: " + newPos);
            }
            Hand right = poseCurrent.getRightHand();
            if (!(right.getPosition() instanceof TouchPosition)) {
                Vec3d pos = new Vec3d(right.getPosition().getX(), right.getPosition().getY(), right.getPosition().getZ());
                Vec3d newPos = interpolate(rP, (fld), rN, (fld), pos, (5 - fld));
                UniformPosition up = new UniformPosition(newPos.x(), newPos.y(), newPos.z());
                right.setPosition(up);
            }
        }

//        GesturePose poseS = poses.get(0).pose;
//        double tS = poses.get(0).time;
//        Hand leftS = poseS.getLeftHand();
//        Vec3d ls = new Vec3d(leftS.getPosition().getX(), leftS.getPosition().getY(), leftS.getPosition().getZ());
//        Hand rightS = poseS.getRightHand();
//        Vec3d rs = new Vec3d(rightS.getPosition().getX(), rightS.getPosition().getY(), rightS.getPosition().getZ());
//
//
//        GesturePose poseE = poses.get(size - 1).pose;
//        double tE = poses.get(size - 1).time;
//        Hand leftE = poseE.getLeftHand();
//        Vec3d le = new Vec3d(leftE.getPosition().getX(), leftE.getPosition().getY(), leftE.getPosition().getZ());
//        Hand rightE = poseE.getRightHand();
//        Vec3d re = new Vec3d(rightE.getPosition().getX(), rightE.getPosition().getY(), rightE.getPosition().getZ());
//
//        double dur = tE - tS;
//        for (int i = 1; i < size - 1; ++i) {
//            ExpressivePose expose = poses.get(i);
//            GesturePose pose = expose.pose;
//            double t = expose.time;
//            ExpressivityParameters ep = expose.ep;
//            double fld = ep.fld;
//            Hand left = pose.getLeftHand();
//            if(!(left.getPosition() instanceof TouchPosition)){
//                Vec3d pos = new Vec3d(left.getPosition().getX(), left.getPosition().getY(), left.getPosition().getZ());
//                Vec3d newPos = interpolate(ls, (dur- (t - tS))/(dur) * (fld), le, (dur - (tE - t))/(dur)* (fld), pos, (3 - fld));
//                UniformPosition up = new UniformPosition(newPos.x(), newPos.y(), newPos.z());
//                left.setPosition(up);
//                System.out.println("old: " + pos + " new: " + newPos);
//            }
//            Hand right = pose.getRightHand();
//            if(!(right.getPosition() instanceof TouchPosition)){
//                Vec3d pos = new Vec3d(right.getPosition().getX(), right.getPosition().getY(), right.getPosition().getZ());
//                Vec3d newPos = interpolate(rs, (dur- (t - tS))/(dur)* (fld), re, (dur - (tE - t))/(dur)* (fld), pos, (3 - fld));
//                UniformPosition up = new UniformPosition(newPos.x(), newPos.y(), newPos.z());
//                right.setPosition(up);
//            }
//        }
    }

    Vec3d interpolate(Vec3d start, double weightS, Vec3d end, double weightE, Vec3d current, double weight) {
        Vec3d re = new Vec3d();
        Vec3d s = Vec3d.multiplication(start, weightS);
        Vec3d e = Vec3d.multiplication(end, weightE);
        Vec3d c = Vec3d.multiplication(current, weight);
        re.add(s);
        re.add(e);
        re.add(c);
        re.divide(weightS + weight + weightE);
        return re;
    }

    private class ExpressivePose {

        GesturePose pose;
        ExpressivityParameters ep;
        double time;
    }

    private void createKeyframe(List<GestureKeyframe> outputKeyframes, String id, double time, GesturePose pose, ExpressivityParameters params) {
        String stroke_type = "useless";
        if(pose.isIsStrokeEnd()){
            stroke_type = "stroke_end";
        }
        GestureKeyframe left = new GestureKeyframe(id + "-left", stroke_type, pose.getLeftHand().getTrajectory(), time, time, pose.getLeftHand(), null, false);
        left.setParameters(new ExpressivityParameters(params));
        outputKeyframes.add(left);
        GestureKeyframe right = new GestureKeyframe(id + "-right", stroke_type, pose.getRightHand().getTrajectory(), time, time, pose.getRightHand(), null, false);
        right.setParameters(new ExpressivityParameters(params));
        outputKeyframes.add(right);
    }

}
