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
