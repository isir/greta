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

import greta.core.util.math.Matrix4d;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class Skeleton {

    String _name = "no";

    ArrayList<Joint> _joints = new ArrayList<Joint>();
    HashMap<String, Integer> _jointNameIds = new HashMap<String, Integer>();
    ArrayList<Integer> _parentJ = new ArrayList<Integer>();
    ArrayList<ArrayList<Integer>> _children = new ArrayList();
    ArrayList<Quaternion> _localRotations = new ArrayList<Quaternion>();
    ArrayList<Quaternion> _globalRotations = new ArrayList<Quaternion>();
    ArrayList<Vec3d> _localPosition = new ArrayList<Vec3d>();
    ArrayList<Vec3d> _globalPosition = new ArrayList<Vec3d>();
    ArrayList<Matrix4d> _localMs = new ArrayList<Matrix4d>();
    ArrayList<Matrix4d> _globalMs = new ArrayList<Matrix4d>();

    //body info
//    ArrayList<Body> _bodys = new ArrayList<Body>();
//    ArrayList<Integer> _connectedParentJoint = new ArrayList<Integer>();
//    ArrayList<Vec3d> _localCOM = new ArrayList<Vec3d>();
//    ArrayList<Float> _mass = new ArrayList<Float>();
    /**
     *
     * @param name
     */
    public Skeleton(String name) {
        _name = name;
    }

    public Skeleton(Skeleton s) {
        set(s);
    }

    public void set(Skeleton s){
        this._children = s._children;
        this._globalMs = s._globalMs;
        this._globalPosition = s._globalPosition;
        this._globalRotations = s._globalRotations;
        this._jointNameIds = s._jointNameIds;
        this._joints = s._joints;
        this._localMs = s._localMs;
        this._localPosition = s._localPosition;
        this._localRotations = s._localRotations;
        this._name = s._name;
        this._parentJ = s._parentJ;
    }

    @Override
    public Skeleton clone() {
        return new Skeleton(this);
    }

    public void reset() {
        for (Joint j : _joints) {
            j.setLocalRotation(new Quaternion());
        }
        update();
    }

    public void update() {
        for (Joint j : _joints) {
            j.update();
        }
    }

    public String getName() {
        return _name;
    }

    /**
     *
     * @param joint
     * @param parent defines parent id
     * @return index in joint list
     */
    public Joint createJoint(String name, int parentId) {
        int id = _joints.size();
        _parentJ.add(-1);
        _children.add(new ArrayList<Integer>());
        _localRotations.add(new Quaternion());
        _globalRotations.add(new Quaternion());
        _localPosition.add(new Vec3d());
        _globalPosition.add(new Vec3d());
        _localMs.add(new Matrix4d());
        _globalMs.add(new Matrix4d());

        Joint j = new Joint(name, id, parentId, this);
        _joints.add(j);
        _jointNameIds.put(name, id);
        return j;
    }

    /**
     * Removes the specified joint
     *
     * @param id the identifier of the joint
     * @return {@code true} if the remove succeed, {@code false} otherwise
     */
    public boolean removeJoint(int id) {
        if (0 < id && id < _joints.size()) {
            Joint j = _joints.get(id);
            int start = _parentJ.get(id);
            for (int i = start; i < _parentJ.size(); ++i) {
                if (_parentJ.get(i) > id) {
                    _parentJ.set(i, _parentJ.get(i) - 1);
                } else if (_parentJ.get(i) == id) {
                    _parentJ.set(i, _parentJ.get(id));
                }
            }
            _jointNameIds.remove(j.getName());
            _joints.remove(id);
            _parentJ.remove(id);
            _children.remove(id);
            _localRotations.remove(id);
            _globalRotations.remove(id);
            _localPosition.remove(id);
            _globalPosition.remove(id);
            _localMs.remove(id);
            _globalMs.remove(id);
            return true;
        }
        return false;
    }

    /**
     * Removes the specified joint
     *
     * @param name the name of the joint
     * @return {@code true} if the remove succeed, {@code false} otherwise
     */
    public boolean removeJoint(String name) {
        return removeJoint(_jointNameIds.get(name));
    }

    /**
     *
     * @param id
     * @return joint
     */
    public Joint getJoint(int id) {
        if (id < _joints.size() && id > -1) {
            Joint j = _joints.get(id);
            return j;
        }
        return null;
    }

    /**
     *
     * @param name
     * @return joint
     */
    public Joint getJoint(String name) {

        if (_jointNameIds.containsKey(name)) {
            int id = _jointNameIds.get(name);
            return _joints.get(id);
        }
        return null;
    }

    /**
     *
     * @param name
     * @return index
     */
    public int getJointIndex(String name) {
        if (_jointNameIds.containsKey(name)) {
            int id = _jointNameIds.get(name);
            return id;
        }
        return -1;
    }

    /**
     *
     * @return joint list
     */
    public ArrayList<Joint> getJoints() {
        return _joints;
    }

    /**
     * clear joint
     */
    public void clearAll() {
        _joints.clear();
        _jointNameIds.clear();
        _parentJ.clear();
        _children.clear();
        _localRotations.clear();
        _globalRotations.clear();
        _localPosition.clear();
        _globalPosition.clear();
        _localMs.clear();
        _globalMs.clear();
    }

    public HashMap<String, Integer> getJointNameIds() {
        return _jointNameIds;
    }

    public ArrayList<Integer> getParentJ() {
        return _parentJ;
    }

    public int getParent(int id){
        return _parentJ.get(id);
    }

    public ArrayList<ArrayList<Integer>> getChildren() {
        return _children;
    }

    public ArrayList<Integer> getChildren(int id) {
        return _children.get(id);
    }

    public ArrayList<Quaternion> getLocalRotations() {
        return _localRotations;
    }

    public Quaternion getLocalRotation(int id) {
        return _localRotations.get(id);
    }

    public ArrayList<Quaternion> getGlobalRotations() {
        return _globalRotations;
    }

    public Quaternion getGlobalRotation(int id) {
        return _globalRotations.get(id);
    }

    public ArrayList<Vec3d> getLocalPositions() {
        return _localPosition;
    }

    public Vec3d getLocalPosition(int id) {
        return _localPosition.get(id);
    }

    public ArrayList<Vec3d> getGlobalPositions() {
        return _globalPosition;
    }

    public Vec3d getGlobalPosition(int id) {
        return _globalPosition.get(id);
    }

    public ArrayList<Matrix4d> getLocalMatrixs() {
        return _localMs;
    }

    public Matrix4d getLocalMatrix(int id) {
        return _localMs.get(id);
    }

    public ArrayList<Matrix4d> getGlobalMatrixs() {
        return _globalMs;
    }

    public Matrix4d getGlobalMatrix(int id) {
        return _globalMs.get(id);
    }


    public void loadFrame(Frame frame) {
        Quaternion zero = new Quaternion();
        for (Joint j : _joints) {
            Quaternion rot = frame.getRotation(j.getName());
            j.setLocalRotation(rot != null ? rot : zero);
        }
    }

    public void loadRotations(HashMap<String, Quaternion> current) {
        for (String name : current.keySet()) {
            Joint j = getJoint(name);
            if (j != null) {
                j.setLocalRotation(current.get(name));
            }
        }
    }

    // a little optimisation when doing loadRotations(Map) followed by update()
    public void loadRotationsAndUpdate(HashMap<String, Quaternion> current) {
        for (Joint j : _joints) {
            Quaternion rot = current.get(j.getName());
            if (rot != null) {
                j.setLocalRotation(rot);//do also the update
            } else {
                j.update();//parent may changed
            }
        }
    }

    public void updateMatrix() {
        for (Joint j : _joints) {
            j.updateMatrix();
        }
    }

    public double[] computeUpBodyCOM() {
        Joint humanoid = getJoint("HumanoidRoot");

        Joint skullbase = getJoint("skullbase");
        double massHead = 4;
        Joint shoulder_l = getJoint("l_shoulder");
        Joint shoulder_r = getJoint("r_shoulder");
        double upperL = 2;
        double lowerL = 1.5;
        Joint elbow_l = getJoint("l_elbow");
        Joint elbow_r = getJoint("r_elbow");
        Joint wrist_l = getJoint("l_wrist");
        Joint wrist_r = getJoint("r_wrist");
        Joint vt5 = getJoint("vt5");
        double torso = 25;
        double zoffset = 0.1;

        Vec3d head = skullbase.getWorldPosition();
        head.add(new Vec3d(0, 0, 0.001f));
        Vec3d centerMassUpperArmL = Vec3d.addition(elbow_l.getWorldPosition(), shoulder_l.getWorldPosition());
        centerMassUpperArmL.divide(2);
        Vec3d centerMassUpperArmR = Vec3d.addition(elbow_r.getWorldPosition(), shoulder_r.getWorldPosition());
        centerMassUpperArmR.divide(2);

        Vec3d centerMassLowerArmL = Vec3d.addition(elbow_l.getWorldPosition(), wrist_l.getWorldPosition());
        centerMassLowerArmL.divide(2);
        Vec3d centerMassLowerArmR = Vec3d.addition(elbow_r.getWorldPosition(), wrist_r.getWorldPosition());
        centerMassLowerArmR.divide(2);

        Vec3d centerMassTorso = vt5.getWorldPosition();
        centerMassTorso.setZ((double) (centerMassTorso.z() + zoffset));

        double[] com = new double[4];
        com[0] = head.x() * massHead + centerMassUpperArmL.x() * upperL + centerMassUpperArmR.x() * upperL + centerMassLowerArmL.x() * lowerL + centerMassLowerArmR.x() * lowerL + centerMassTorso.x() * torso;
        com[1] = head.y() * massHead + centerMassUpperArmL.y() * upperL + centerMassUpperArmR.y() * upperL + centerMassLowerArmL.y() * lowerL + centerMassLowerArmR.y() * lowerL + centerMassTorso.y() * torso;
        com[2] = head.z() * massHead + centerMassUpperArmL.z() * upperL + centerMassUpperArmR.z() * upperL + centerMassLowerArmL.z() * lowerL + centerMassLowerArmR.z() * lowerL + centerMassTorso.z() * torso;
        com[3] = massHead + upperL + upperL + lowerL + lowerL + torso;
        com[0] = com[0] / com[3] - humanoid.getWorldPosition().x();
        com[1] = com[1] / com[3] - humanoid.getWorldPosition().y();
        com[2] = com[2] / com[3] - humanoid.getWorldPosition().z();
        return com;
    }
}
