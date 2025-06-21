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
package correctionmesh.util;

import greta.core.util.environment.Node;
import greta.core.util.environment.TreeNode;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Andre-Marie Pez
 */
public class Bone extends TreeNode {

    public int ogreID = -1;

    public void setToZeroOrientation() {
        Quaternion orient = getOrientation();
        for (Node child : getChildren()) {
            if (child instanceof Bone) {
                Bone childBone = (Bone) child;
                childBone.setCoordinates(orient.rotate(childBone.getCoordinates()));
                childBone.setOrientation(Quaternion.multiplication(orient, childBone.getOrientation()));
            }
        }
        setOrientation(0, 0, 0, 1);
    }

    public void setToZeroOrientationAndPropagateToChildren() {
        setToZeroOrientation();
        for (Node child : getChildren()) {
            if (child instanceof Bone) {
                ((Bone) child).setToZeroOrientationAndPropagateToChildren();
            }
        }
    }

    public void scale(double factor) {
        Vec3d pos = getCoordinates();
        pos.multiply(factor);
        this.setCoordinates(pos);
    }

    public void scaleAndPropagateToChildren(double factor) {
        scale(factor);
        for (Node child : getChildren()) {
            if (child instanceof Bone) {
                ((Bone) child).scaleAndPropagateToChildren(factor);
            }
        }
    }

    public void appendChild(Bone child) {
        addChildNode(child);
    }

    public int reIndex(int indexToStart) {
        ogreID = indexToStart;
        indexToStart++;
        for (Node child : getChildren()) {
            if (child instanceof Bone) {
                indexToStart = ((Bone) child).reIndex(indexToStart);
            }
        }
        return indexToStart;
    }
}
