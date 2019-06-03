/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package correctionmesh.util;

import vib.core.util.environment.Node;
import vib.core.util.environment.TreeNode;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;

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
