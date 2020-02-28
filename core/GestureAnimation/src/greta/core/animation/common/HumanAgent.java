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
import greta.core.animation.common.IK.MassSpringSolver;
import greta.core.animation.common.body.Arm;
import greta.core.animation.common.body.Torse;
import greta.core.animation.common.symbolic.SymbolicConverter;
import greta.core.util.CharacterManager;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 * body IK
 */
public class HumanAgent {

    SymbolicConverter _symbolicConverter;

    public SymbolicConverter getSymbolicConverter() {
        return _symbolicConverter;
    }

    public void setSymbolicConverter(SymbolicConverter _symbolicConverter) {
        this._symbolicConverter = _symbolicConverter;
    }
    HashMap<String, Skeleton> _chains = new HashMap<String, Skeleton>();
    double _leftArmLength = 0;
    double _rightArmLength = 0;
    String _leftArmIK = "leftArmIK";
    String _rightArmIK = "rightArmIK";
    double openness_left = 0;
    double openness_right = 0;
    ExpressiveTorso _expTorso = new ExpressiveTorso();

    public ExpressiveTorso getExpressiveTorso() {
        return _expTorso;
    }

    public HumanAgent(CharacterManager cm) {
        _symbolicConverter = new SymbolicConverter(cm);
    }

    Vec3d getOriginalJointPosition(String name) {
        return _symbolicConverter.getOriginalSkeleton().getJoint(name).getWorldPosition();
    }

    Vec3d getCurrentJointPosition(String name) {
        return _symbolicConverter.getSkeleton().getJoint(name).getWorldPosition();
    }

    Quaternion getCurrentJointGlobalRotation(String name) {
        return _symbolicConverter.getSkeleton().getJoint(name).getWorldRotation();
    }

    Skeleton getSkeletonChain(String name) {
        return _symbolicConverter.getSkeletonChain(name);
    }

    /**
     * computer process torso -> hands -> head
     *
     * @param leftarm
     * @param rightarm
     * @param torse
     */
    public void compute(Arm leftarm, Arm rightarm, Torse torse) {

        _expTorso.setArms(leftarm, rightarm);
        double p = 0;

        if (leftarm != null) {
            if (leftarm.getExpressivityParameters() != null) {
                openness_left = (double) leftarm.getOpenness();
                p += leftarm.getExpressivityParameters().pwr;
            }
        }
        if (rightarm != null) {
            if (rightarm.getExpressivityParameters() != null) {
                openness_right = (double) rightarm.getOpenness();
                p += rightarm.getExpressivityParameters().pwr;
            }
        }
        _expTorso.compute((1 + p) / 2.0);

        _symbolicConverter.getSkeleton().reset();
        reTargetingTorseForIKWithRotation(torse, leftarm, -1, rightarm, -1);

        if (leftarm != null) {
            if (leftarm.getRestPosName().equalsIgnoreCase("")) {
                applyLeftArmIK(leftarm);
            }
        } else {
        }
        if (rightarm != null) {
            if (rightarm.getRestPosName().equalsIgnoreCase("")) {
                applyRightArmIK(rightarm);
            }
        }
    }

    boolean reTargetingTorseForIKWithRotation(Torse torse, Arm leftarm, double leftdistance, Arm rightarm, double rightdistance) {
        _leftArmLength = _rightArmLength = _symbolicConverter.getOriginalSkeleton().getJoint("l_elbow").getLength() + _symbolicConverter.getOriginalSkeleton().getJoint("l_wrist").getLength();

        restoreTorso(torse);
        //j.setLocalRotation(torse.getRotation());
        //j.update();
        for(String name: torse.getJointFrames().keySet()){
            Joint j = _symbolicConverter.getSkeleton().getJoint(name);
            j.setLocalRotation(torse.getJointFrames().get(name)._localrotation.clone());
            j.update();
        }
        Vec3d displacements = new Vec3d();
        Vec3d center = new Vec3d();
        int w = 0;
        if (leftarm != null) {
            Vec3d pos = leftarm.getPosition();
            center = Vec3d.addition(center, pos);
            Vec3d shoulder = getCurrentJointPosition("l_shoulder");
            Vec3d dis = Vec3d.substraction(pos, shoulder);
            double lengthCurrent = dis.length();
            if (lengthCurrent > _leftArmLength) {
                double len = lengthCurrent - _leftArmLength;
                Vec3d displace = Vec3d.multiplication(dis, 1 - _leftArmLength / lengthCurrent);
                displacements = new Vec3d(displacements.x() + displace.x(), java.lang.Math.max(displacements.y(), displace.y()), java.lang.Math.max(displacements.z(), displace.z()));
                w++;
            }
        }
        if (rightarm != null) {

            Vec3d pos = rightarm.getPosition();
            center = Vec3d.addition(center, pos);
            Vec3d shoulder = getCurrentJointPosition("r_shoulder");
            Vec3d dis = Vec3d.substraction(pos, shoulder);
            double lengthCurrent = dis.length();
            if (lengthCurrent > _rightArmLength) {
                double len = lengthCurrent - _rightArmLength;
                Vec3d displace = Vec3d.multiplication(dis, 1 - _rightArmLength / lengthCurrent);
                displacements = new Vec3d(displacements.x() + displace.x(), java.lang.Math.max(displacements.y(), displace.y()), java.lang.Math.max(displacements.z(), displace.z()));
                w++;
            }
        }
        Vec3d vt1 = new Vec3d(getOriginalJointPosition("vt1"));
        //vt1.set(2, 0);
        Target t = null;
        Vec3d newPos = null;
        if (w != 0) {
            newPos = Vec3d.addition(vt1, Vec3d.multiplication(displacements, (1)));
            Vec3d dir = Vec3d.substraction(Vec3d.division(center, 2), vt1);
            dir.set(1, 0);
            dir.set(2, 1);
            t = new Target(newPos, dir.normalized());
        }
        applyTorseIK(torse, t);
        return true;
    }

    void applyTorseIK(Torse torso, Target t) {
        if (t != null) {
            Quaternion q0P = new Quaternion();
            if (t.getPosition() != null) {
                //pencher
                double rotate = (double) java.lang.Math.atan(t.getPosition().y() / (java.lang.Math.sqrt(t.getPosition().x() * t.getPosition().x() + t.getPosition().z() * t.getPosition().z()))) / (1 + java.lang.Math.max(openness_left, openness_right) * 0.15f);
                double angleP = (double) (java.lang.Math.PI * 0.5 - java.lang.Math.min(rotate, 1.3));
                q0P.setAxisAngle(new Vec3d(1, 0, 0), angleP);
            }
            Quaternion q0 = new Quaternion();
            //selfrotation
            Vec3d original = new Vec3d(0, 0, 1);
            Vec3d dir = t.getUpDirectionVector();
            Vec3d newP = new Vec3d(dir.x(), 0, dir.z()).normalized();
            //System.out.println(newP);
            double cosTheta = original.dot3(newP);
            cosTheta = 1 < cosTheta ? 1 : cosTheta;
            double angle = (double) java.lang.Math.acos(cosTheta);
            q0.setAxisAngle(new Vec3d(0, 1, 0), angle * java.lang.Math.signum(newP.x()) * (openness_left + openness_right + 1) * 0.1f);
            Quaternion q = Quaternion.multiplication(q0, q0P);
            Quaternion finalresul = Quaternion.multiplication(q, _expTorso.getRotation());

            String list[] = {"vt2", "vt5", "vt12", "vl5"};
            Quaternion each = Quaternion.slerp(new Quaternion(), finalresul, 0.25f, true);
            for (String name : list) {
                JointFrame jf = new JointFrame();
                jf._localrotation = new Quaternion(each);
                Joint j = _symbolicConverter.getSkeleton().getJoint(name);
                j.setLocalRotation(each);
                j.updateLocally();
                torso.addJointFrame(name, jf);
            }
        } else {
            //add exprssive torso
            Quaternion finalresul = _expTorso.getRotation();

            String list[] = {"vt2", "vt5", "vt12", "vl5"};
            Quaternion each = Quaternion.slerp(new Quaternion(), finalresul, 0.25f, true);
            for (String name : list) {
                JointFrame jf = new JointFrame();
                Joint j = _symbolicConverter.getSkeleton().getJoint(name);
                //j.setLocalRotation(each);
                j.rotate(each);
                j.updateLocally();
                jf._localrotation = new Quaternion(j.getLocalRotation());
                torso.addJointFrame(name, jf);
            }

        }
    }

    void restoreTorso(Torse torso) {
        String list[] = {"vt2", "vt5", "vt12", "vl5"};
        for (String name : list) {
            JointFrame jf = new JointFrame();
            jf._localrotation = new Quaternion(new Quaternion());
            Joint j = _symbolicConverter.getSkeleton().getJoint(name);
            j.setLocalRotation(new Quaternion());
            j.updateLocally();
            //torso.addJointFrame(name, jf);
        }
    }

    /**
     * apply gaze
     *
     * @param gaze
     */
    /*
     * void applyHeadGaze(Target gaze) { if (gaze == null) { return; } if
     * (gaze.getPosition() == null) { return; }
     *
     * Vec3d pos = gaze.getPosition(); Vec3d dir = Vec3d.substraction(pos,
     * getCurrentJointPosition("vc7")).normalized(); Quaternion finalGlobe = new
     * Quaternion(); Quaternion vertical = new Quaternion(); Quaternion
     * horizontal = new Quaternion();
     *
     * //horizontal Vec3d originalH = new Vec3d(0, 0, 1); Vec3d newPH = new
     * Vec3d(dir.x(), 0, dir.z()).normalized(); double cosTheta =
     * originalH.dot3(newPH);//> bdif ? adif : bdif; cosTheta = 1 < cosTheta ? 1
     * : cosTheta; double angle = (double) java.lang.Math.acos(cosTheta);
     * horizontal.setAxisAngle(new Vec3d(0, 1, 0), angle *
     * java.lang.Math.signum(newPH.x()));
     *
     *
     * //vertical Vec3d originalV = new Vec3d(0, 0, 1); Vec3d newPV = new
     * Vec3d(0, dir.y(), dir.z()).normalized(); cosTheta =
     * originalV.dot3(newPV);//> bdif ? adif : bdif; cosTheta = 1 < cosTheta ? 1
     * : cosTheta; angle = (double) java.lang.Math.acos(cosTheta);
     * vertical.setAxisAngle(new Vec3d(1, 0, 0), -angle *
     * java.lang.Math.signum(newPV.y()));
     *
     * finalGlobe = Quaternion.multiplication(vertical, horizontal); Quaternion
     * q = getCurrentJointGlobalRotation("vc7");
     *
     * Quaternion finalR = Quaternion.division(finalGlobe, q); JointFrame jf =
     * new JointFrame(); jf._localrotation = new Quaternion(finalR);
     *
     * j.setLocalRotation(finalR); j.update();
     *
     * }
     */
    /**
     * apply left hand
     *
     * @param t
     */
    void applyLeftArmIK(Arm t) {

        Skeleton leftarm = getSkeletonChain(_leftArmIK);
        if (leftarm == null) {
            return;
        }

//        Shoulder shoulder = new Shoulder(0);
//        shoulder.setSide("LEFT");
//        Vec3d pos = _symbolicConverter.getOriginalSkeleton().getJoint("l_shoulder").getWorldPosition();
//        Vec3d dif = Vec3d.substraction(t.getTarget().getPosition(), pos);
//        shoulder.compute(dif.x()/50, dif.y()/50);
//        t.addJointFrames(shoulder.getJointFrames());
//        for(String name: shoulder.getJointFrames().keySet()){
//            Joint j = _symbolicConverter.getSkeleton().getJoint(name);
//            j.setLocalRotation(shoulder.getJointFrames().get(name)._localrotation);
//            j.updateLocally();
//        }
        _symbolicConverter.getSkeleton().update();
        beginSyncho(leftarm, _symbolicConverter.getSkeleton());  //cpy skeleton to chain
        MassSpringSolver massSpringSolver = new MassSpringSolver();
        massSpringSolver.compute(leftarm.getJoints(), t.getTarget(), true);
        for (Joint j : leftarm.getJoints()) {
            JointFrame jf = new JointFrame();
            jf._localrotation = j.getLocalRotation();
            t.addJointFrame(j.getName(), jf);
        }
        Quaternion leftwrist = t.getWrist();
        if (leftwrist != null) {
            if (!t.isWristLocalOrientation()) {
                Joint end = leftarm.getJoint(leftarm.getJoints().size() - 1);
                Quaternion world = end.getWorldRotation();
                Quaternion local = Quaternion.multiplication(world.inverse(), leftwrist);
                end.setLocalRotation(local.normalized());
                end.checkDOFsRestrictions();
                end.update();
                JointFrame jf = new JointFrame();
                jf._localrotation = new Quaternion(local);
                t.addJointFrame("l_wrist", jf);
            } else {
                JointFrame jf = new JointFrame();
                jf._localrotation = new Quaternion(leftwrist);
                t.addJointFrame("l_wrist", jf);
            }
        }
        //endSyncho(leftarm, _symbolicConverter.getSkeleton()); //cpy chain to skeleton
    }

    void applyRightArmIK(Arm t) {

        Skeleton rightarm = getSkeletonChain(_rightArmIK);
        if (rightarm == null) {
            return;
        }

//        Shoulder shoulder = new Shoulder(0);
//        shoulder.setSide("RIGHT");
//        Vec3d pos = _symbolicConverter.getOriginalSkeleton().getJoint("r_shoulder").getWorldPosition();
//        Vec3d dif = Vec3d.substraction(t.getTarget().getPosition(), pos);
//        shoulder.compute(dif.x()/50, dif.y()/50);
//        t.addJointFrames(shoulder.getJointFrames());
//        for(String name: shoulder.getJointFrames().keySet()){
//            Joint j = _symbolicConverter.getSkeleton().getJoint(name);
//            j.setLocalRotation(shoulder.getJointFrames().get(name)._localrotation);
//            j.update();
//        }
        _symbolicConverter.getSkeleton().update();

        beginSyncho(rightarm, _symbolicConverter.getSkeleton());
        MassSpringSolver massSpringSolver = new MassSpringSolver();
        massSpringSolver.compute(rightarm.getJoints(), t.getTarget(), true);
        for (Joint j : rightarm.getJoints()) {
            JointFrame jf = new JointFrame();
            jf._localrotation = j.getLocalRotation();
            t.addJointFrame(j.getName(), jf);
        }
        Quaternion rightwrist = t.getWrist();
        if (rightwrist != null) {
            if (!t.isWristLocalOrientation()) {
                Joint end = rightarm.getJoint(rightarm.getJoints().size() - 1);
                Quaternion world = end.getWorldRotation();
                Quaternion local = Quaternion.multiplication(world.inverse(), rightwrist);
                end.setLocalRotation(local.normalized());
                end.checkDOFsRestrictions();
                end.update();
                JointFrame jf = new JointFrame();
                jf._localrotation = new Quaternion(local);
                t.addJointFrame("r_wrist", jf);
            } else {
                JointFrame jf = new JointFrame();
                jf._localrotation = new Quaternion(rightwrist);
                t.addJointFrame("r_wrist", jf);
            }
        }
        //endSyncho(rightarm, _symbolicConverter.getSkeleton());
    }

    void beginSyncho(Skeleton chain, Skeleton skeleton) {
        chain.reset();
        ArrayList<Joint> joints = chain.getJoints();

        for (int i = 0; i < joints.size(); ++i) {
            Joint joint = joints.get(i);
            Joint original = skeleton.getJoint(joint.getName());
            if (original != null) {
                if (i == 0) {
                    Quaternion worldr = original.getParent().getWorldRotation();
                    joint.setRestOrientation(worldr);
                    joint.setOrigine(original.getWorldPosition());
                }
            }
        }
        if (chain.getJoints().size() > 0) {
            chain.getJoint(0).update();
        }
    }

    /*
     * void endSyncho(Skeleton chain, Skeleton skeleton) { ArrayList<Joint>
     * joints = chain.getJoints(); for (int i = 0; i < joints.size(); ++i) {
     * Joint joint = joints.get(i); Joint original =
     * skeleton.getJoint(joint.getName()); if (original != null) {
     * original.setLocalRotation(joint.getLocalRotation()); } }
     * skeleton.getJoint(joints.get(0).getName()).update();//must for update all
     * positions }
     */
    public void setOpenness_left(double openness_left) {
        this.openness_left = openness_left;
    }

    public void setOpenness_right(double openness_right) {
        this.openness_right = openness_right;
    }
}
