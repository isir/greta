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

import greta.core.animation.common.Joint;
import greta.core.animation.common.Skeleton;
import greta.core.animation.common.Task;
import greta.core.util.math.Quaternion;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class IKTask extends Task {

    protected boolean _constraintsActived = true;
    protected Skeleton _originalSkeleton;
    protected Skeleton _skeleton = new Skeleton(_name);
    protected String _endJointName;
    protected IKSolver _pIKSolver = new MassSpringSolver();

    public IKTask(String name) {
        super(name);
    }

    public void bindOriginalSkeleton(Skeleton skeleton) {
        _originalSkeleton = skeleton;
    }

    public void useDOFConstraints(boolean active) {
        _constraintsActived = active;
    }

    public boolean isDOFConstraintsActived() {
        return _constraintsActived;
    }

    @Override
    public void launch() {
        if (_actived && _skeleton.getJoints().size() > 0 && _originalSkeleton != null) {
            synchonizeBegin();

            _pIKSolver.computeWithPriority(_skeleton.getJoints(), _target, _constraintsActived, _priority);

            synchonizeEnd();
        }
        _actived = false;
    }

    public boolean setChain(Skeleton skeleton) {
        _skeleton = skeleton;
        if (skeleton.getJoints().size() <= 0) {
            return false;
        }
        _endJointName = skeleton.getJoints().get(skeleton.getJoints().size() - 1).getName();
        return true;
    }

    public void setIKSolver(IKSolver IKSolver) {
        _pIKSolver = IKSolver;
    }

    public IKSolver getIKSolver() {
        return _pIKSolver;
    }

    public Skeleton getSkeleton() {
        return _skeleton;
    }

    //important part : need to reset the chain to initial pos for compare, the local rotation is relative to start initial state.
    void synchonizeBegin() {

        _skeleton.reset();
        ArrayList<Joint> joints = _skeleton.getJoints();
        //_originalSkeleton.getJoint(joints.get(0).getName()).reset();  //reset chaine

        for (int i = 0; i < joints.size(); ++i) {
            Joint joint = joints.get(i);
            Joint original = _originalSkeleton.getJoint(joint.getName());
            if (original != null) {
                if (i == 0) {
                    Quaternion worldr = original.getParent().getWorldRotation();
                    joint.setRestOrientation(worldr);
                    joint.setOrigine(original.getWorldPosition());
                }
            }
        }
    }

    void synchonizeEnd() {
        //_skeleton.reset();
        ArrayList<Joint> joints = _skeleton.getJoints();

        for (int i = 0; i < joints.size(); ++i) {
            Joint joint = joints.get(i);
            Joint original = _originalSkeleton.getJoint(joint.getName());
            if (original != null) {
                original.setLocalRotation(joint.getLocalRotation());

            }
        }
        //todo
        _originalSkeleton.getJoint(joints.get(0).getName()).update();//must for update all positions

    }
}
