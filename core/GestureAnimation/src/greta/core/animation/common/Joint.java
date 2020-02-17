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

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Jing Huang
 */
public class Joint {

    double _mass = 1.0f;

    public double getMass() {
        return _mass;
    }

    public void setMass(double mass) {
        this._mass = mass;
    }
    Skeleton _pSkeleton;
    String _name = "no";
    int _id = -1;
    int _parent = -1;
    ArrayList<Integer> _children = new ArrayList<Integer>();
    Quaternion _restOrientation = new Quaternion();
    Quaternion _localRotation = new Quaternion();
    Quaternion _worldRotation = new Quaternion();
    Vec3d _origine = new Vec3d();
    Vec3d _localPosition = new Vec3d();
    Vec3d _worldPosition = new Vec3d();
    Vec3d _originalDirectionalVector = new Vec3d();
    Vec3d _upDirection = new Vec3d(0, 0, 1);
    Vec3d _originalUpDirection = new Vec3d(0, 0, 1);
    double _length = -1;
    DOF _dofs[] = {new DOF(-3.1415926f, 3.1415926f), new DOF(-3.1415926f, 3.1415926f), new DOF(-3.1415926f, 3.1415926f), new DOF(-3.1415926f, 3.1415926f), new DOF(-3.1415926f, 3.1415926f), new DOF(-3.1415926f, 3.1415926f)};//new DOF[6];
    int _priority = 0;
    LocalCoordinateSystem _localCoordinateSystem = new LocalCoordinateSystem();

    public Joint(String name, Skeleton skeleton) {
        _name = name;
        _pSkeleton = skeleton;
    }

    public Joint(String name, Skeleton skeleton, int id, int parent) {
        _name = name;
        _pSkeleton = skeleton;
        _id = id;
        _parent = parent;
    }

    /**
     * reset to initial
     */
    public void reset() {
        _localRotation = new Quaternion();
        _restOrientation = new Quaternion();
        //_upDirection = new Vec3d(0, 0, 1);
        //_originalUpDirection = new Vec3d(0, 0, 1);
        //updateLocally(); //update position for updirectionvector
        //initUpDirectionVector();
        updateLocally();
        Iterator<Integer> itor = _children.iterator();
        while (itor.hasNext()) {
            _pSkeleton.getJoint(itor.next().intValue()).reset();
        }
    }

    public Skeleton getSkeleton() {
        return _pSkeleton;
    }

    public void setSkeleton(Skeleton skeleton) {
        _pSkeleton = skeleton;
    }

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        _name = name;
    }

    public int getId() {
        return _id;
    }

    public void setId(int id) {
        _id = id;
    }

    public boolean addChild(int id) {
        if (_pSkeleton == null) {
            return false;
        }

        Joint joint = _pSkeleton.getJoint(id);

        if (joint != null) {
            _children.add(id);

            return true;
        }
        return false;
    }

    public boolean addChild(Joint joint) {
        if (_pSkeleton == null) {
            return false;
        }

        joint.setParentById(_id);
        int added = _pSkeleton.addJoint(joint, _id);
        if (added > 0) {
            _children.add(joint.getId());
        }
        return true;
    }

    public Joint getChild(int index) {
        if (index < _children.size()) {
            return _pSkeleton.getJoint(_children.get(index));
        }
        return null;
    }

    public void setParentById(int parent) {
        _parent = parent;
    }

    public int getParentId() {
        return _parent;
    }

    public Joint getParent() {
        if (_parent == -1) {
            return null;
        }
        return _pSkeleton.getJoint(_parent);
    }

    public ArrayList<Integer> getChildren() {
        return _children;
    }

    public void setOrigine(Vec3d pos) {
        _origine = new Vec3d(pos);
        _worldPosition =  new Vec3d(_origine);
    }

    //initialJoint restOriginalVector
    /**
     *
     * @param dir
     * set center for root joint, or direction vector for others
     */
    public void setOriginalDirectionalVector(Vec3d dir) {

        if (getParentId() >= 0) {
            _originalDirectionalVector = new Vec3d(dir);
            _originalDirectionalVector.normalize();
            _length = dir.length();
            _localPosition = new Vec3d(dir);
        } else {
            _origine = new Vec3d(dir);
        }
        updateLocally();
        initUpDirectionVector();
        updateLocally();
    }

    public Vec3d getOriginalDirectionalVector() {
        return _originalDirectionalVector;
    }

    public Vec3d getDirectionalVector() {
        Joint parent = getParent();
        if (parent != null) {
            Vec3d father = parent.getWorldPosition();
            Vec3d self = getWorldPosition();
            Vec3d dif = Vec3d.substraction(self, father);
            return (dif).normalized();
        }
//        Vec3d v = new Vec3d(getWorldPosition());
//        return (v).normalized();
        return new Vec3d();
    }

    public double getLength() {
        return _length;
    }

    public void initUpDirectionVector() {
        updateUpDirectionVector();
        _originalUpDirection = getUpDirectionVector();
    }

    public void setOriginalUpDirectionVector(Vec3d v) {
        _originalUpDirection = new Vec3d(v).normalized();
    }

    public void setUpDirectionVector(Vec3d v) {
        _upDirection = new Vec3d(v).normalized();
    }

    public Vec3d getUpDirectionVector() {
        return new Vec3d(_upDirection);
    }

    public Vec3d getOriginalUpDirectionVector() {
        return new Vec3d(_originalUpDirection);
    }

    public void updateUpDirectionVector() {
        //boneVector.normalize();
        Joint parent = getParent();
        if (parent == null) {
            return;
        }
        Vec3d boneVector = getDirectionalVector();
        Vec3d upVp = getUpDirectionVector().normalized();
        Vec3d vPerpendicular = boneVector.cross3(upVp);
        Vec3d upV = vPerpendicular.cross3(boneVector);
        _upDirection = upV.normalized();
    }

    public void setRestOrientation(Quaternion rest) {
        _restOrientation = new Quaternion(rest);
    }

    public Quaternion getRestOrientation() {
        return new Quaternion(_restOrientation);
    }

    public void setLocalRotation(Quaternion local) {
        _localRotation = new Quaternion(local);
    }

    public Quaternion getLocalRotation() {
        return new Quaternion(_localRotation);
    }

    public void setWorldRotation(Quaternion world) {
        _worldRotation = new Quaternion(world);
    }

    public Quaternion getWorldRotation() {
        return new Quaternion(_worldRotation);
    }

    public void setLocalPosition(Vec3d pos) {
        _localPosition =  new Vec3d(pos);
    }

    public Vec3d getLocalPosition() {
        return new Vec3d(_localPosition);
    }

    public void setWorldPosition(Vec3d pos) {
        _worldPosition =  new Vec3d(pos);
    }

    public Vec3d getWorldPosition() {
        return new Vec3d(_worldPosition);
    }

    public void rotate(Quaternion rotation) {
        Quaternion tmp = rotation;
        tmp.normalize();
        _localRotation.multiply(tmp);
        return;
    }

    /**
     * update joint info locally
     */
    public void updateLocally() {
        if (_parent == -1) {
            //_worldRotation = Quaternion.multiplication(_localRotation, _restOrientation );
            _worldRotation = Quaternion.multiplication(_restOrientation, _localRotation);
            _worldRotation.normalize();
            _worldPosition = Vec3d.addition(Quaternion.multiplication(_worldRotation, _localPosition), _origine);
            _upDirection = Quaternion.multiplication(_restOrientation, _originalUpDirection);
        } else {
            _worldPosition = Quaternion.multiplication(getParent().getWorldRotation(), _localPosition);
            //System.out.println(getName() + ""+ _worldPosition.x() + " " + _worldPosition.y() + " " + _worldPosition.z());
            _worldPosition.add(getParent().getWorldPosition());
            //Given two quaternions p, q and multiplying them to form the quaternion r = pq,
            //applying the quaternion r to a vector v first rotates the vector by q then by p.
            //still wonder which should be first local or global G1 = G0 * L   or G1 =  L * G0
            _worldRotation = Quaternion.multiplication(getParent().getWorldRotation(), _localRotation);
            _worldRotation.normalize();
            _upDirection = Quaternion.multiplication(getParent().getWorldRotation(), _originalUpDirection);
            //System.out.println(getName() + ""+ _worldPosition.x() + " " + _worldPosition.y() + " " + _worldPosition.z());
        }
    }

    /**
     * update iteratively for joint and its children
     */
    public void update() {
        if (_parent == -1) {
            //_worldRotation = Quaternion.multiplication(_localRotation, _restOrientation );
            _worldRotation = Quaternion.multiplication(_restOrientation, _localRotation);
            _worldRotation.normalize();
            _worldPosition = Vec3d.addition(Quaternion.multiplication(_worldRotation, _localPosition), _origine);
            _upDirection = Quaternion.multiplication(_restOrientation, _originalUpDirection);
        } else {
            _worldPosition = Quaternion.multiplication(getParent().getWorldRotation(), _localPosition);
            //System.out.println(getName() + ""+ _worldPosition.x() + " " + _worldPosition.y() + " " + _worldPosition.z());
            _worldPosition.add(getParent().getWorldPosition());
            //Given two quaternions p, q and multiplying them to form the quaternion r = pq,
            //applying the quaternion r to a vector v first rotates the vector by q then by p.
            //still wonder which should be first local or global G1 = G0 * L   or G1 =  L * G0
            _worldRotation = Quaternion.multiplication(getParent().getWorldRotation(), _localRotation);
            _worldRotation.normalize();
            _upDirection = Quaternion.multiplication(getParent().getWorldRotation(), _originalUpDirection);
            //System.out.println(getName() + ""+ _worldPosition.x() + " " + _worldPosition.y() + " " + _worldPosition.z());
        }
        //System.out.println("joint class :"+getName() + " " + _worldPosition.x() + " " + _worldPosition.y() + " " +  _worldPosition.z() + " ");
        Iterator<Integer> itor = _children.iterator();
        while (itor.hasNext()) {
            _pSkeleton.getJoint(itor.next().intValue()).update();
        }
    }

    /**
     * update iteratively for joint and its children with priority
     */
    public void updateWithPriority(int priority) {
        if (priority >= _priority) {
            if (_parent == -1) {
                //_worldRotation = Quaternion.multiplication(_localRotation, _restOrientation );
                _worldRotation = Quaternion.multiplication(_restOrientation, _localRotation);
                _worldRotation.normalize();
                _worldPosition = Vec3d.addition(Quaternion.multiplication(_worldRotation, _localPosition), _origine);
                _upDirection = Quaternion.multiplication(_restOrientation, _originalUpDirection);
            } else {
                _worldPosition = Quaternion.multiplication(getParent().getWorldRotation(), _localPosition);
                //System.out.println(getName() + ""+ _worldPosition.x() + " " + _worldPosition.y() + " " + _worldPosition.z());
                _worldPosition.add(getParent().getWorldPosition());
                //Given two quaternions p, q and multiplying them to form the quaternion r = pq,
                //applying the quaternion r to a vector v first rotates the vector by q then by p.
                //still wonder which should be first local or global G1 = G0 * L   or G1 =  L * G0
                _worldRotation = Quaternion.multiplication(getParent().getWorldRotation(), _localRotation);
                _worldRotation.normalize();
                _upDirection = Quaternion.multiplication(getParent().getWorldRotation(), _originalUpDirection);
                //System.out.println(getName() + ""+ _worldPosition.x() + " " + _worldPosition.y() + " " + _worldPosition.z());
            }
            Iterator<Integer> itor = _children.iterator();
            while (itor.hasNext()) {
                _pSkeleton.getJoint(itor.next().intValue()).updateWithPriority(priority);
            }
        }
    }

    public void checkDOFsRestrictions() {
        checkDOFsRestrictions(_localRotation);
    }

    /**
     *
     * @param q
     * check constraints for local rotation
     */
    public void checkDOFsRestrictions(Quaternion q) {

        Vec3d angle = q.getEulerAngleXYZ();

        boolean modified = false;

        if (angle.get(0) < _dofs[DOF.DOFType.ROTATION_X.ordinal()].minValue()) {
            angle.setX(_dofs[DOF.DOFType.ROTATION_X.ordinal()].minValue());
            modified = true;
        } else if (angle.get(0) > _dofs[DOF.DOFType.ROTATION_X.ordinal()].maxValue()) {
            angle.setX(_dofs[DOF.DOFType.ROTATION_X.ordinal()].maxValue());
            modified = true;
        }

        if (angle.get(1) < _dofs[DOF.DOFType.ROTATION_Y.ordinal()].minValue()) {
            angle.setY(_dofs[DOF.DOFType.ROTATION_Y.ordinal()].minValue());
            modified = true;
        } else if (angle.get(1) > _dofs[DOF.DOFType.ROTATION_Y.ordinal()].maxValue()) {
            angle.setY(_dofs[DOF.DOFType.ROTATION_Y.ordinal()].maxValue());
            modified = true;
        }

        if (angle.get(2) < _dofs[DOF.DOFType.ROTATION_Z.ordinal()].minValue()) {
            angle.setZ(_dofs[DOF.DOFType.ROTATION_Z.ordinal()].minValue());
            modified = true;
        } else if (angle.get(2) > _dofs[DOF.DOFType.ROTATION_Z.ordinal()].maxValue()) {
            angle.setZ(_dofs[DOF.DOFType.ROTATION_Z.ordinal()].maxValue());
            modified = true;
        }

        if (modified) {

            q.fromEulerXYZ(angle.get(0), angle.get(1), angle.get(2));

        }
    }

    public void setDOF(DOF.DOFType type, DOF dof) {
        _dofs[type.ordinal()] = dof;
    }

    public void setDOF(DOF.DOFType type, double min, double max) {
        //System.out.println(""+_dofs[type.ordinal()]);
        _dofs[type.ordinal()].maxValue(max);
        _dofs[type.ordinal()].minValue(min);
    }

    public DOF[] getDOFs() {
        return _dofs;
    }

    public void resetPriority() {
        _priority = 0;
    }

    public void setPriority(int priority) {
        if (priority <= _priority) {
            return;
        }
        _priority = priority;
    }

    public int getPriority() {
        return _priority;
    }

    public LocalCoordinateSystem getLocalCoordinateSystem() {
        return _localCoordinateSystem;
    }


}
