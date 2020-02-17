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
package greta.core.animation.common.IK;

import greta.core.animation.common.DOF;
import greta.core.animation.common.Joint;
import greta.core.animation.common.Target;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

/**
 *
 * @author Jing Huang
 */
public class MassSpringSolver extends IKSolver {

    IKMassSpring _massSpring = new IKMassSpring("");
    HashMap<String, Integer> _joint_node_map = new HashMap<String, Integer>();

    public MassSpringSolver() {
        super();
    }

    public MassSpringSolver(int maxTries, double targetTreshold) {
        super(maxTries, targetTreshold);
    }

    @Override
    public String getIKSolverName() {
        return "MassSpring";
    }

    @Override
    public boolean compute(ArrayList<Joint> list, Target target, boolean enableConstraints) {
        int size = list.size() - 1;
        if (size < 0) {
            return false;
        }
        Vec3d curEnd = list.get(size).getWorldPosition(), endPos = target.getPosition();

        setupChain(list);
        int tries = 0;
        endPos = checkForRetargetting(list, endPos);
        target.setPosition(endPos);
        while (tries < _maxTries
                && new Vec3d(curEnd, endPos).length() > _targetTreshold) {

            moveToTarget(list, target);
            list.get(size).setUpDirectionVector(target.getUpDirectionVector());
            for (int i = 1; i < list.size(); i++) {
                list.get(i).setWorldPosition(getMassOfJoint(list.get(i).getName()).getPosition());
            }
            //Integrate and apply changes
            for (int i = 1; i < list.size(); i++) {
                calculateLocalRotation(list.get(i));
            }
            if (enableConstraints) {
                checkJointsDOFsRestrictions(list, target);
            }
            list.get(0).update();
            updateMassesPosition(list);

            curEnd = list.get(size).getWorldPosition();
            ++tries;
        }

        if (tries == _maxTries) {
            return false;
        }

        return true;
    }

    @Override
    public boolean computeWithPriority(ArrayList<Joint> list, Target target, boolean enableConstraints, int priority) {
        int size = list.size() - 1;
        if (size < 0) {
            return false;
        }
        Vec3d curEnd = list.get(size).getWorldPosition(), endPos = target.getPosition();
        setupChain(list);
        int tries = 0;
        //endPos = checkForRetargetting(list, endPos);
//        if (checkForDistance(list, endPos)) {
//            _maxTries = 10;
//        }
        target.setPosition(endPos);
        while (tries < _maxTries
                && new Vec3d(curEnd, endPos).length() > _targetTreshold) {

            moveToTarget(list, target);
            list.get(size).setUpDirectionVector(target.getUpDirectionVector());

            for (int i = 1; i < list.size(); i++) {
                list.get(i).setWorldPosition(getMassOfJoint(list.get(i).getName()).getPosition());
            }
            //Integrate and apply changes
            for (int i = 1; i < list.size(); i++) {
                calculateLocalRotation(list.get(i));
            }
            if (enableConstraints) {
                checkJointsDOFsRestrictions(list, target);
            }
            list.get(0).updateWithPriority(priority);
            updateMassesPosition(list);

            curEnd = list.get(size).getWorldPosition();
            ++tries;
        }
        //System.out.println("try " + tries);
        if (tries == _maxTries) {
            return false;
        }
        return true;
    }

    @Override
    public void setupChain(ArrayList<Joint> joints) {
        _massSpring.clear();
        _joint_node_map.clear();
        //TO DO : define chain class and and add ass parameter the chain
        //Methodes to extract chains from skeletons or to create chains
        //getting the _links of the chain

        for (int i = 0; i < joints.size(); ++i) {
            int id = _massSpring.createMass(joints.get(i).getWorldPosition(), joints.get(i).getMass(), true);
            //System.out.println(joints.get(i).getName() + joints.get(i).getMass());
            _joint_node_map.put(joints.get(i).getName(), id);
        }

        IKMass baseNode = _massSpring.getMass(0);
        //System.out.println(baseNode.isMovable());
        //if the iteration calculation is started by 1 then do not need to setMovable false cause by relative rotation computation
        baseNode.setMovable(false);
        for (int i = 1; i < joints.size(); ++i) {
            _massSpring.createSpring(i - 1, i);
        }
    }

    public IKMass getMassOfJoint(String name) {
        if (_joint_node_map.containsKey(name)) {
            return _massSpring.getMass(_joint_node_map.get(name));
        }
        return null;
    }

    public void calculateGlobalRotation(Joint j) {

        Joint parent = j.getParent();
        Quaternion rotation = new Quaternion();
        if (parent != null) {
            j.updateUpDirectionVector();
            Vec3d a_current = j.getDirectionalVector();
            Vec3d a_original = j.getOriginalDirectionalVector();
            Vec3d b_original = j.getOriginalUpDirectionVector();
            Vec3d b_current = j.getUpDirectionVector();
            rotation.fromRotatedPlane(a_current, b_current, a_original, b_original);
            //rotation.fromRotatedBasis(a_current, b_current, Vec3d.cross3(a_current, b_current), a_original, b_original, Vec3d.cross3(a_original, b_original));
            parent.setWorldRotation(rotation);
        }
    }

    public void calculateLocalRotation(Joint j) {

        Quaternion rotation = new Quaternion();
        Joint parent = j.getParent();

        if (parent != null) {
            //globalway
            calculateGlobalRotation(j);
            Quaternion p = parent.getWorldRotation();
            Joint jpp = parent.getParent();
            //use Parent compare better than local recusive, they miss part of initial state
            if (jpp != null) {
                Quaternion pp = jpp.getWorldRotation();
                p = Quaternion.multiplication(pp.inverse(), p);
            } else {
                Quaternion pp = parent.getRestOrientation();
                p = Quaternion.multiplication(pp.inverse(), p);
            }

            rotation = p;
            rotation.normalize();
            parent.setLocalRotation(rotation.normalized());
        }

    }

    public void moveToTarget(ArrayList<Joint> list, Target target) {

        int updateNumber = 20;
        IKMass nodeCurrent;
        if (!target.getEffector().isEmpty()) {
            nodeCurrent = getMassOfJoint(target.getEffector());
        } else {
            nodeCurrent = getMassOfJoint(list.get(list.size() - 1).getName());
        }
        double dif = new Vec3d(nodeCurrent.getPosition(), target.getPosition()).length();
        nodeCurrent.setPosition(target.getPosition());
        nodeCurrent.setMovable(false);
        _massSpring.update(updateNumber);
        dif = new Vec3d(nodeCurrent.getPosition(), target.getPosition()).length();

//        {
//            int nb = 0;
//            Vec3d force = Vec3d.substraction(target.getPosition(), nodeCurrent.getPosition());
//            while (force.length() > 0.5 && nb < 30) {
//                //Vec3d f = Vec3d.multiplication(force.normalized() , target.getEnergy() / force.length());
//                nodeCurrent.addForce(force);
//                //System.out.println(f);
//                _massSpring.update(5);
//                force = Vec3d.substraction(target.getPosition(), nodeCurrent.getPosition());
//               // System.out.println(force);
//                nb++;
//            }
//        }

    }

    public void updateMassesPosition(ArrayList<Joint> list) {
        Iterator<Joint> itor = list.iterator();
        while (itor.hasNext()) {
            Joint j = (itor.next());
            String name = j.getName();
            if (_joint_node_map.containsKey(name)) {
                _massSpring.getMass(_joint_node_map.get(name)).setPosition(j.getWorldPosition());
            }
        }
    }

    void checkJointsDOFsRestrictions(ArrayList<Joint> joints, Target target) {

        int start = joints.size() - 1;
        for (int i = start; i >= 0; --i) {
            Joint joint = joints.get(i);
            //	std::cout<<joint->getName()<<std::endl;
            Quaternion _localRotation = joint.getLocalRotation();
            DOF[] _dofs = joint.getDOFs();
            Vec3d angle = _localRotation.getEulerAngleXYZ();
            Vec3d difference = new Vec3d();
            boolean modified = false;

            Quaternion y_parent = new Quaternion();
            if (angle.x() < _dofs[DOF.DOFType.ROTATION_X.ordinal()].minValue()) {
                difference.setX(_dofs[DOF.DOFType.ROTATION_X.ordinal()].minValue() - angle.x());
                angle.setX(_dofs[DOF.DOFType.ROTATION_X.ordinal()].minValue());
                //angle[0] = _dofs[ROTATION_X]->maxValue();
                modified = true;
            } else if (angle.x() > _dofs[DOF.DOFType.ROTATION_X.ordinal()].maxValue()) {
                difference.setX(_dofs[DOF.DOFType.ROTATION_X.ordinal()].maxValue() - angle.x());
                angle.setX(_dofs[DOF.DOFType.ROTATION_X.ordinal()].maxValue());
                //angle[0] = _dofs[ROTATION_X]->minValue();
                modified = true;
            }

            if (angle.y() < _dofs[DOF.DOFType.ROTATION_Y.ordinal()].minValue()) {

                difference.setY(_dofs[DOF.DOFType.ROTATION_Y.ordinal()].minValue() - angle.y());
                angle.setY(_dofs[DOF.DOFType.ROTATION_Y.ordinal()].minValue());
                //angle[1]  = _dofs[ROTATION_Y]->minValue();
                //angle[1]  = _dofs[ROTATION_Y]->maxValue();
                y_parent.fromEulerXYZ(0, difference.y(), 0);
                modified = true;

            } else if (angle.y() > _dofs[DOF.DOFType.ROTATION_Y.ordinal()].maxValue()) {
                difference.setY(_dofs[DOF.DOFType.ROTATION_Y.ordinal()].maxValue() - angle.y());
                angle.setY(_dofs[DOF.DOFType.ROTATION_Y.ordinal()].maxValue());
                //angle[1]  = _dofs[ROTATION_Y]->maxValue();
                //angle[1]  = _dofs[ROTATION_Y]->minValue();
                y_parent.fromEulerXYZ(0, difference.y(), 0);
                modified = true;
            }

            if (angle.z() < _dofs[DOF.DOFType.ROTATION_Z.ordinal()].minValue()) {
                difference.setZ(_dofs[DOF.DOFType.ROTATION_Z.ordinal()].minValue() - angle.z());
                angle.setZ(_dofs[DOF.DOFType.ROTATION_Z.ordinal()].minValue());
                modified = true;
            } else if (angle.z() > _dofs[DOF.DOFType.ROTATION_Z.ordinal()].maxValue()) {
                difference.setZ(_dofs[DOF.DOFType.ROTATION_Z.ordinal()].maxValue() - angle.z());
                angle.setZ(_dofs[DOF.DOFType.ROTATION_Z.ordinal()].maxValue());
                modified = true;
            }


            if (modified) {
                Quaternion old = new Quaternion(_localRotation);

                _localRotation.fromEulerXYZ(angle.x(), angle.y(), angle.z());
                joint.setLocalRotation(_localRotation);
                joint.update();
                if (i > 0) {
                    joint.updateLocally();
                    //joint.updateUpDirectionVector();
                    Quaternion q;
                    q = Quaternion.multiplication(_localRotation, old.inverse());
                    double distanceJoint = Vec3d.substraction(joints.get(i - 1).getWorldPosition(), joint.getWorldPosition()).length();
                    double distanceTarget = Vec3d.substraction(joint.getWorldPosition(), target.getPosition()).length();
                    double influenceFactor = distanceTarget / (distanceTarget + distanceJoint);
                  /*  double up = Vec3d.substraction(joints.get(i - 1).getWorldPosition() , joints.get(i - 1).getWorldPosition()).length();
                    double down = Vec3d.substraction( joints.get(i - 1).getWorldPosition(), joint.getWorldPosition()).length();
                    double influenceFactor = up/ down;*/
                    q.invert();
                    Quaternion parent = Quaternion.slerp(new Quaternion(), q, influenceFactor, true);
                    //joints.get(i - 1).rotate(y_parent);
                    joints.get(i - 1).rotate(parent);
                    joints.get(i - 1).updateLocally();
                    //joint->updateLocally();
                    //joints.get(i).setUpDirectionVector(Quaternion.multiplication(joints.get(i - 1).getWorldRotation(), joints.get(i).getOriginalUpDirectionVector()));

                }

            } else {
                joint.setLocalRotation(_localRotation);
            }


        }

    }

    Vec3d checkForRetargetting(ArrayList<Joint> joints, Vec3d endpos) {
        double sumdistance = 0;
        for (int i = 1; i < joints.size(); ++i) {
            sumdistance += joints.get(i).getLength();
        }
        Vec3d basePos = joints.get(0).getWorldPosition();
        Vec3d vecDistance = Vec3d.substraction(endpos, basePos);
        double distance = vecDistance.length();
        if (sumdistance < distance) {
            double ratio = sumdistance / (distance + 1.0f);
            return Vec3d.addition(basePos, Vec3d.multiplication(vecDistance, ratio));
        }
        return endpos;
    }

    boolean checkForDistance(ArrayList<Joint> joints, Vec3d endpos) {
        double sumdistance = 0;
        for (int i = 0; i < joints.size(); ++i) {
            sumdistance += joints.get(i).getLength();
        }
        Vec3d basePos = joints.get(0).getWorldPosition();
        Vec3d vecDistance = Vec3d.substraction(endpos, basePos);
        double distance = vecDistance.length();
        if (sumdistance < distance) {
            return false;
        }
        return true;
    }
}
