/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
