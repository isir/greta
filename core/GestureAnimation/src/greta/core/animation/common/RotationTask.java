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

/**
 *
 * @author Jing Huang
 */
public class RotationTask extends Task {
    protected Skeleton _originalSkeleton;
    protected Skeleton _skeleton = new Skeleton(_name);
    public RotationTask(String name) {
        super(name);
    }

    public void bindOriginalSkeleton(Skeleton skeleton) {
        _originalSkeleton = skeleton;
    }

    public Skeleton getSkeleton() {
        return _skeleton;
    }

    @Override
    public void launch() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }


    public boolean setChain(Skeleton skeleton) {
        _skeleton = skeleton;
        if (skeleton.getJoints().size() <= 0) {
            return false;
        }
        return true;
    }

    public void rotateJoint(String name, Quaternion q){
        Joint j = _skeleton.getJoint(name);
        j.setLocalRotation(q);
    }


    public void rotateWholeChain(Quaternion q){
       for(Joint j : _skeleton.getJoints()){
           j.setLocalRotation(q);
       }
    }

    public void rotateWholeChainWithSphericalLinearInterpolation(Quaternion q){
        int size = _skeleton.getJoints().size();
        if(size == 0) return;
        Quaternion qr = Quaternion.slerp(new Quaternion(), q, (double)1 / (double)size, true);
        rotateWholeChain(qr);
    }
}
