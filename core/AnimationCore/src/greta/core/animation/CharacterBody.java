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
import java.util.HashMap;

/**
 * each time after construction, apply initMassSystemByOriginalSkeleton
 *
 * @author Jing Huang
 */
public class CharacterBody {

    public static double EnergyEpsilon = 0.001f;
    Skeleton _sk_original = null;
    Skeleton _sk = null;
    ArrayList<Mass> _massList = new ArrayList<Mass>();
    HashMap<String, String> _table = new HashMap<String, String>();
    HashMap<String, Integer> _nameIdx = new HashMap<String, Integer>();
    HashMap<String, MassConstraint> _externalConstraintList = new HashMap<String, MassConstraint>();  //dont forget to reset constraint if end of key frame, the tracking is not needed
    Quaternion _upBase = new Quaternion();
    Quaternion _l_elbow = new Quaternion();
    Quaternion _r_elbow = new Quaternion();
    Quaternion _l_shoulder = new Quaternion();
    Quaternion _r_shoulder = new Quaternion();
    //since skeleton got for the elbow part is not always aligning in the x axis, it is difficult to compute, we make a virtual elbow for simplifying it.
    // please make sure next time using x aligning elbow skeleton
    Vec3d _virtualElbow_L, _virtualElbow_R;

    public CharacterBody(Skeleton sk) {
        _sk = sk.clone();
        _sk_original = sk.clone();
        setupNamingTable();
        Joint shoulder_l = _sk_original.getJoint("l_shoulder");
        Joint elbow_l = _sk_original.getJoint("l_elbow");
        Joint wrist_l = _sk_original.getJoint("l_wrist");
        _virtualElbow_L = Vec3d.interpolation(shoulder_l.getPosition(), wrist_l.getPosition(), elbow_l.getLocalPosition().length() / (elbow_l.getLocalPosition().length() + wrist_l.getLocalPosition().length()));
        Joint shoulder_r = _sk_original.getJoint("r_shoulder");
        Joint elbow_r = _sk_original.getJoint("r_elbow");
        Joint wrist_r = _sk_original.getJoint("r_wrist");
        _virtualElbow_R = Vec3d.interpolation(shoulder_r.getPosition(), wrist_r.getPosition(), elbow_r.getLocalPosition().length() / (elbow_r.getLocalPosition().length() + wrist_r.getLocalPosition().length()));
    }

    public Skeleton getOriginalSkeleton() {
        return _sk_original;
    }

    public Skeleton getSkeleton() {
        return _sk;
    }

    void setupNamingTable(HashMap<String, String> table) {
        _table = table;
    }

    void setupNamingTable() {
        _table.clear();
        _massList.clear();
        _nameIdx.clear();

        _table.put("vl5", "vl5");
        _nameIdx.put("vl5", 0);
        Mass mass = new Mass("vl5");
        mass.setMovable(false);
        _massList.add(mass);

        _table.put("l_shoulder", "l_shoulder");
        _nameIdx.put("l_shoulder", 1);
        mass = new Mass("l_shoulder");
        _massList.add(mass);
        mass.setMass(2);

        _table.put("r_shoulder", "r_shoulder");
        _nameIdx.put("r_shoulder", 2);
        mass = new Mass("r_shoulder");
        _massList.add(mass);
        mass.setMass(2);

        _table.put("r_elbow", "r_elbow");
        _nameIdx.put("r_elbow", 3);
        mass = new Mass("r_elbow");
        _massList.add(mass);

        _table.put("l_elbow", "l_elbow");
        _nameIdx.put("l_elbow", 4);
        mass = new Mass("l_elbow");
        _massList.add(mass);

        _table.put("r_wrist", "r_wrist");
        _nameIdx.put("r_wrist", 5);
        mass = new Mass("r_wrist");
        _massList.add(mass);

        _table.put("l_wrist", "l_wrist");
        _nameIdx.put("l_wrist", 6);
        mass = new Mass("l_wrist");
        _massList.add(mass);

    }

    public void resetSkeleton() {
        if (_sk_original == null) {
            return;
        }
        _sk = _sk_original.clone();
    }

    public void initMassSystemByOriginalSkeleton() {
        if (_sk_original == null) {
            return;
        }
        setupNamingTable();
        Joint shoulder_l = _sk_original.getJoint("l_shoulder");
        Joint elbow_l = _sk_original.getJoint("l_elbow");
        Joint wrist_l = _sk_original.getJoint("l_wrist");
        _virtualElbow_L = Vec3d.interpolation(shoulder_l.getPosition(), wrist_l.getPosition(), elbow_l.getLocalPosition().length() / (elbow_l.getLocalPosition().length() + wrist_l.getLocalPosition().length()));
        Joint shoulder_r = _sk_original.getJoint("r_shoulder");
        Joint elbow_r = _sk_original.getJoint("r_elbow");
        Joint wrist_r = _sk_original.getJoint("r_wrist");
        _virtualElbow_R = Vec3d.interpolation(shoulder_r.getPosition(), wrist_r.getPosition(), elbow_r.getLocalPosition().length() / (elbow_r.getLocalPosition().length() + wrist_r.getLocalPosition().length()));

        for (Mass mass : _massList) {
            mass.setPosition(_sk_original.getJoint(_table.get(mass.getName())).getPosition());
            if (mass.getName().equalsIgnoreCase("r_elbow")) {
                mass.setPosition(_virtualElbow_R);
            } else if (mass.getName().equalsIgnoreCase("l_elbow")) {
                mass.setPosition(_virtualElbow_L);
            }
        }
        initSpringConstraints();
        _externalConstraintList.clear();
    }

    public void initMassSystemBySkeleton() {
        if (_sk == null) {
            return;
        }
        setupNamingTable();

        for (Mass mass : _massList) {
            mass.setPosition(_sk.getJoint(_table.get(mass.getName())).getPosition());
//            if (mass.getName().equalsIgnoreCase("r_elbow")) {
//                mass.setPosition(_virtualElbow_R);
//            } else if (mass.getName().equalsIgnoreCase("l_elbow")) {
//                mass.setPosition(_virtualElbow_L);
//            }
        }
        initSpringConstraints();
        _externalConstraintList.clear();
    }

    public void setSkeletonValues(Frame frame) {
        if (_sk == null) {
            return;
        }
        _sk.loadRotationsAndUpdate(frame.getRotations());
//        for (String name : frame.getRotations().keySet()) {
//            Joint j = _sk.getJoint(name);
//            if (j != null) {
//                j.setLocalRotation(frame.getRotations().get(name).clone());
//            }
//        }
    }

    public void setSkeletonValues(HashMap<String, Quaternion> r) {
        if (_sk == null) {
            return;
        }
        _sk.loadRotationsAndUpdate(r);
//        for (String name : r.keySet()) {
//            Joint j = _sk.getJoint(name);
//            if (j != null) {
//                j.setLocalRotation(r.get(name));
//            }
//        }
//        _sk.update();
    }

    public ArrayList<Mass> getMasses() {
        return _massList;
    }

    public HashMap<String, Integer> getMassesNameId() {
        return _nameIdx;
    }

    void initSpringConstraints() {
        MassSpringConstraint mc = new MassSpringConstraint(getMassByName("vl5"),
                getMassByName("l_shoulder"));
        getMassByName("vl5").addInternalConstraint(mc);
        mc.setStiffness(0.8f);
        mc = new MassSpringConstraint(getMassByName("vl5"),
                getMassByName("r_shoulder"));
        getMassByName("vl5").addInternalConstraint(mc);
        mc.setStiffness(0.8f);

        mc = new MassSpringConstraint(getMassByName("l_shoulder"),
                getMassByName("vl5"));
        getMassByName("l_shoulder").addInternalConstraint(mc);
        mc = new MassSpringConstraint(getMassByName("l_shoulder"),
                getMassByName("l_elbow"));
        getMassByName("l_shoulder").addInternalConstraint(mc);
        mc = new MassSpringConstraint(getMassByName("l_shoulder"),
                getMassByName("r_shoulder"));
        getMassByName("l_shoulder").addInternalConstraint(mc);

        mc = new MassSpringConstraint(getMassByName("r_shoulder"),
                getMassByName("vl5"));
        getMassByName("r_shoulder").addInternalConstraint(mc);
        mc = new MassSpringConstraint(getMassByName("r_shoulder"),
                getMassByName("r_elbow"));
        getMassByName("r_shoulder").addInternalConstraint(mc);
        mc = new MassSpringConstraint(getMassByName("r_shoulder"),
                getMassByName("l_shoulder"));
        getMassByName("r_shoulder").addInternalConstraint(mc);


        mc = new MassSpringConstraint(getMassByName("l_elbow"),
                getMassByName("l_shoulder"));
        getMassByName("l_elbow").addInternalConstraint(mc);
        mc = new MassSpringConstraint(getMassByName("l_elbow"),
                getMassByName("l_wrist"));
        getMassByName("l_elbow").addInternalConstraint(mc);

        mc = new MassSpringConstraint(getMassByName("r_elbow"),
                getMassByName("r_shoulder"));
        getMassByName("r_elbow").addInternalConstraint(mc);
        mc = new MassSpringConstraint(getMassByName("r_elbow"),
                getMassByName("r_wrist"));
        getMassByName("r_elbow").addInternalConstraint(mc);

        mc = new MassSpringConstraint(getMassByName("r_wrist"),
                getMassByName("r_elbow"));
        getMassByName("r_wrist").addInternalConstraint(mc);

        mc = new MassSpringConstraint(getMassByName("l_wrist"),
                getMassByName("l_elbow"));
        getMassByName("l_wrist").addInternalConstraint(mc);
    }

    public Mass getMassByName(String name) {
        return _massList.get(_nameIdx.get(name));
    }

    public void applyLeftHand(Vec3d target) {
        getMassByName("l_wrist").setPosition(target);
        getMassByName("l_wrist").setMovable(false);
    }

    public void applyRightHand(Vec3d target) {
        getMassByName("r_wrist").setPosition(target);
        getMassByName("r_wrist").setMovable(false);
    }

    public void applyLeftHandByTrack(Vec3d target, double openness) {
        if (target == null) {
            return;
        }
        Mass m = getMassByName("l_wrist");
//        double distance = Vec3d.substraction(target, m.getPosition()).length();
        PositionMassConstraint mc = new PositionMassConstraint(m, target, 0);
        //mc.setStiffness(0.3f * distance / 100.0f);
        _externalConstraintList.put("l_wrist", mc);
        m.setMovable(true);

//        Mass elbow = getMassByName("l_elbow");
        DirectionalMassConstraint dmc = new DirectionalMassConstraint(new Vec3d(1, 0, 0), (double)(0.2f * openness));
        _externalConstraintList.put("l_elbow", dmc);
        //elbow.addExternalConstraint(dmc);
    }

    public void applyRightHandByTrack(Vec3d target, double openness) {
        if (target == null) {
            return;
        }
        Mass m = getMassByName("r_wrist");
//        double distance = Vec3d.substraction(target, m.getPosition()).length();
        PositionMassConstraint mc = new PositionMassConstraint(m, target, 0);
        //mc.setStiffness(0.3f * distance / 100.0f);
        _externalConstraintList.put("r_wrist", mc);
        m.setMovable(true);

//        Mass elbow = getMassByName("r_elbow");
        DirectionalMassConstraint dmc = new DirectionalMassConstraint(new Vec3d(-1, 0, 0), (double)(0.2f * openness));
        _externalConstraintList.put("r_elbow", dmc);
        //elbow.addExternalConstraint(dmc);
    }

    public void updateAll() {
        double energy = 0;
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
        if (energy > EnergyEpsilon) {
            computeAngles();
            // System.out.println("class CharacterBody: system iternal energy : " + energy);
        }
//        for (String name : _externalConstraintList.keySet()) {
//            Mass mass = getMassByName(name);
//            MassConstraint mc = _externalConstraintList.get(name);
//            System.out.println(mass.getName() + "  " + mass.getPosition() + "  " + ((PositionMassConstraint) (mc)).getDragpoint());
//            System.out.println("sk:" + _sk.getJoint(mass.getName()).getPosition());
//
//        }

    }

    public FrameSequence updateAllFrames() {
        FrameSequence frames = new FrameSequence();
        double energy = 10000;
        while (energy > EnergyEpsilon) {
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
            //System.out.println("class CharacterBody: system iternal energy : " + energy);
            frames.add(computeAngles());
        }
        return frames;
    }

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
        return frame;
    }

    public void setMassPositions(HashMap<String, Vec3d> massposition) {
        for (Mass mass : _massList) {
            Vec3d pos = massposition.get(mass.getName());
            if (pos != null) {
                mass.setPosition(pos);
            }
        }
    }

    public Frame computeAngles() {
        Frame angles = new Frame();
        angles.addRotation("vl5", computeUpBase());
        angles.addRotation("l_shoulder", computeLeftShoulder());
        angles.addRotation("r_shoulder", computeRightShoulder());
        angles.addRotation("l_elbow", computeLeftElbow());
        angles.addRotation("r_elbow", computeRightElbow());
        angles.getRootTranslation();
        return angles;
    }

    public Quaternion computeLeftElbow() {
        Vec3d oldUpper = Vec3d.substraction(_virtualElbow_L, _sk_original.getJoint("l_shoulder").getPosition());
        oldUpper.normalize();
        Vec3d oldLower = Vec3d.substraction(_sk_original.getJoint("l_wrist").getPosition(), _virtualElbow_L);
        oldLower.normalize();
        double dotangle = Math.max(Math.min(1,oldUpper.dot3(oldLower)), -1);
        double angle = Math.acos(dotangle);

        Vec3d Upper = Vec3d.substraction(getMassByName("l_elbow").getPosition(), getMassByName("l_shoulder").getPosition());
        Upper.normalize();
        Vec3d Lower = Vec3d.substraction(getMassByName("l_wrist").getPosition(), getMassByName("l_elbow").getPosition());
        Lower.normalize();
        double angle2 = Math.acos(Upper.dot3(Lower));
        _l_elbow.setAxisAngle(new Vec3d(1, 0, 0), (double)(angle - angle2));
//        _l_elbow.normalize();//useless : setAxisAngle did it
        _sk.getJoint("l_elbow").setLocalRotation(_l_elbow);
        //System.out.println("_l_elbow " + _l_elbow);
        return _l_elbow.clone();
    }

    public Quaternion computeRightElbow() {
        Vec3d oldUpper = Vec3d.substraction(_virtualElbow_R, _sk_original.getJoint("r_shoulder").getPosition());
        oldUpper.normalize();
        Vec3d oldLower = Vec3d.substraction(_sk_original.getJoint("r_wrist").getPosition(), _virtualElbow_R);
        oldLower.normalize();
        double dotangle = Math.max(Math.min(1,oldUpper.dot3(oldLower)), -1);
        double angle = Math.acos(dotangle);

        Vec3d Upper = Vec3d.substraction(getMassByName("r_elbow").getPosition(), getMassByName("r_shoulder").getPosition());
        Upper.normalize();
        Vec3d Lower = Vec3d.substraction(getMassByName("r_wrist").getPosition(), getMassByName("r_elbow").getPosition());
        Lower.normalize();
        double angle2 = Math.acos(Upper.dot3(Lower));
        _r_elbow.setAxisAngle(new Vec3d(1, 0, 0), (double)(angle - angle2));
//        _r_elbow.normalize(); //useless : setAxisAngle did it
        _sk.getJoint("r_elbow").setLocalRotation(_r_elbow);
        return _r_elbow.clone();
    }

    public Quaternion computeUpBase() {
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
        //System.out.println("vl5 " + _upBase);
        return _upBase.clone();
    }

    public Quaternion computeLeftShoulder() {
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
        //System.out.println("l_shoulder " + _l_shoulder);
        return _l_shoulder.clone();
    }

    public Quaternion computeRightShoulder() {
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
        return _r_shoulder.clone();
    }

    public void addConstraint(String name, MassConstraint mc) {
        Mass mass = getMassByName(name);
        if (mass != null) {
            mass.addInternalConstraint(mc);
        }
    }

    public void removeConstraint(String name, String constraintName) {
        Mass mass = getMassByName(name);
        if (mass != null) {
            mass.removeInternalConstraint(constraintName);
        }
    }

    public void addLeftWristConstraint(MassConstraint mc) {
        addConstraint("l_wrist", mc);
    }

    public void addRightWristConstraint(String name, MassConstraint mc) {
        addConstraint("r_wrist", mc);
    }

    public void addLeftElbowConstraint(MassConstraint mc) {
        addConstraint("l_elbow", mc);
    }

    public void addRightElbowConstraint(String name, MassConstraint mc) {
        addConstraint("r_elbow", mc);
    }

    public void addLeftShoulderConstraint(MassConstraint mc) {
        addConstraint("l_shoulder", mc);
    }

    public void addRightShoulderConstraint(String name, MassConstraint mc) {
        addConstraint("r_shoulder", mc);
    }

    public void enableBody(boolean enable) {
        getMassByName("l_shoulder").setMovable(enable);
        getMassByName("r_shoulder").setMovable(enable);
    }

    public Quaternion getL_Elbow() {
        return _l_elbow;
    }

    public Quaternion getL_Shoulder() {
        return _l_shoulder;
    }

    public Quaternion getR_Elbow() {
        return _r_elbow;
    }

    public Quaternion getR_Shoulder() {
        return _r_shoulder;
    }

    public Quaternion getUpBaseVl5() {
        return _upBase;
    }

    public Quaternion getLeftElbowGlobalOrientation(){
        return _sk.getJoint("l_elbow").getWorldOrientation();
    }

    public Quaternion getRightElbowGlobalOrientation(){
        return _sk.getJoint("r_elbow").getWorldOrientation();
    }
}
