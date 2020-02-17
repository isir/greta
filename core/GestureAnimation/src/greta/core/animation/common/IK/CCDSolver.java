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

/**
 *
 * @author Jing Huang
 */
public class CCDSolver extends IKSolver {

    @Override
    public String getIKSolverName() {
        return "CCD";
    }

    @Override
    public boolean compute(ArrayList<Joint> list, Target t, boolean enableConstraints) {
        for (int i = 0; i < list.size(); ++i) {
            list.get(i).reset();
        }
        //TO DO
		/*
         * add constraints do something when the cross product is 0 this happens
         * when the curVector and targetVector are collinears
         */

        // start at the last link in the chain
        Joint link = list.get(list.size() - 1).getParent();

        Vec3d rootPos = link.getWorldPosition();
        Vec3d curEnd = list.get(list.size() - 1).getWorldPosition();
        Vec3d targetVector = new Vec3d(), curVector = new Vec3d(), crossResult = new Vec3d(), endPos = t.getPosition();
        double cosAngle, turnAngle;

        int tries = 0;

        double distance = Vec3d.substraction(curEnd, endPos).length();

        while (++tries < _maxTries
                && distance > _targetTreshold) {
            // create the vector to the current effector pos
            curVector = Vec3d.substraction(curEnd, rootPos);
            // create the desired effector position vector
            targetVector = Vec3d.substraction(endPos, rootPos);

            // normalize the vectors (expensive, requires a sqrt)
            curVector.normalize();
            targetVector.normalize();

            // the dot product gives me the cosine of the desired angle
            cosAngle = curVector.dot3(targetVector);
            System.out.println(cosAngle);
            // if the dot product returns 1.0, i don't need to rotate as it is 0 degrees
            if (cosAngle < 0.9999999) {
                if (cosAngle < -0.9999999) {
                    //the 2 vectors are collinear
                    //TBD
                    //
                    // check if we can use cross product of source vector and [1, 0, 0]
                    //
                    crossResult.set(0, curVector.x(), -curVector.y());

                    if (crossResult.length() < 1.0e-10) {
                        //
                        // nope! we need cross product of source vector and [0, 1, 0]
                        //
                        crossResult.set(-curVector.z(), 0, curVector.x());
                    }
                } else {
                    // use the cross product to check which way to rotate
                    crossResult = curVector.cross3(targetVector);
                }

                crossResult.normalize();
                turnAngle = (double) Math.acos(cosAngle);	// get the angle

                Quaternion rotation = new Quaternion();
                rotation.setAxisAngle(crossResult, turnAngle * 0.5f);
                rotation.normalize();
                link.rotate(rotation);
                if (enableConstraints) {
                   //checkDOFsRestrictions(link, link.getDOFs());
                }

                link.update();
            }

            if (link.getParent() == null) {
                link = list.get(list.size() - 1).getParent();	// start of the chain, restart
            } else {
                link = link.getParent();
            }


            rootPos = link.getWorldPosition();
            curEnd = list.get(list.size() - 1).getWorldPosition();
            distance = Vec3d.substraction(curEnd, endPos).length();

        }

        if (tries == _maxTries) {
            return false;
        }

        return true;
    }

    @Override
    public boolean computeWithPriority(ArrayList<Joint> list, Target t, boolean b, int i) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    void checkDOFsRestrictions(Joint joint, DOF[] dofs) {
        Quaternion _localRotation = joint.getLocalRotation();
        Vec3d angle = _localRotation.getEulerAngleXYZ();

        boolean modified = false;

        if (angle.get(0) < dofs[DOF.DOFType.ROTATION_X.ordinal()].minValue()) {
            angle.set(0, dofs[DOF.DOFType.ROTATION_X.ordinal()].minValue());
            modified = true;
        } else if (angle.get(0) > dofs[DOF.DOFType.ROTATION_X.ordinal()].maxValue()) {
            angle.set(0, dofs[DOF.DOFType.ROTATION_X.ordinal()].maxValue());
            modified = true;
        }

        if (angle.get(1) < dofs[DOF.DOFType.ROTATION_Y.ordinal()].minValue()) {
            angle.set(1, dofs[DOF.DOFType.ROTATION_Y.ordinal()].minValue());
            modified = true;
        } else if (angle.get(1) > dofs[DOF.DOFType.ROTATION_Y.ordinal()].maxValue()) {
            angle.set(1, dofs[DOF.DOFType.ROTATION_Y.ordinal()].maxValue());
            modified = true;
        }

        if (angle.get(2) < dofs[DOF.DOFType.ROTATION_Z.ordinal()].minValue()) {
            angle.set(2, dofs[DOF.DOFType.ROTATION_Z.ordinal()].minValue());
            modified = true;
        } else if (angle.get(2) > dofs[DOF.DOFType.ROTATION_Z.ordinal()].maxValue()) {
            angle.set(2, dofs[DOF.DOFType.ROTATION_Z.ordinal()].maxValue());
            modified = true;
        }

        if (modified) {
            _localRotation.fromEulerXYZ(angle.x(), angle.y(), angle.z());
            joint.setLocalRotation(_localRotation);
        }

    }
}
