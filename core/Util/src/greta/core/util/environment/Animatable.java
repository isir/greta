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
package greta.core.util.environment;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Pierre Philippe
 * @author Andre-Marie Pez
 */
public class Animatable extends TreeNode {

    private TreeNode rotationnode = null;
    private TreeNode scalenode = null;
    private Leaf attachedleaf = null;

    public Animatable() {
        init();
    }

    public Animatable(String id) {
        identifier = id;
        init();
    }

    private void init() {
        rotationnode = new TreeNode();
        rotationnode.setIdentifier(identifier + "_RotationTreeNode");
        this.addChildNode(rotationnode);

        scalenode = new TreeNode();
        scalenode.setIdentifier(identifier + "_ScaleTreeNode");
        rotationnode.addChildNode(scalenode);

        attachedleaf = new Leaf();
        attachedleaf.setIdentifier(identifier + "_AttachedLeaf");
        scalenode.addChildNode(attachedleaf);
    }

    /**
     * Changes the identifier of this {@code Node}<br/> never use this until you
     * know what you are doing.
     *
     * @param id the new identifier
     */
    @Override
    public void setIdentifier(String id) {
        super.setIdentifier(id);
        rotationnode.setIdentifier(identifier + "_RotationTreeNode");
        scalenode.setIdentifier(identifier + "_ScaleTreeNode");
        attachedleaf.setIdentifier(identifier + "_AttachedLeaf");
    }

    public Leaf getAttachedLeaf() {
        return attachedleaf;
    }

    public void setAttachedLeaf(Leaf oneleaf) {
        if (attachedleaf != null) {
            scalenode.removeChild(attachedleaf);
        }
        scalenode.addChildNode(oneleaf);
        this.attachedleaf = oneleaf;
    }

    @Override
    public void setOrientation(double x, double y, double z, double w) {
        rotationnode.setOrientation(x, y, z, w);
    }

    @Override
    public void setOrientation(Vec3d orient, double a) {
        rotationnode.setOrientation(orient, a);
    }

    @Override
    public void setOrientation(Quaternion qorient) {
        rotationnode.setOrientation(qorient.axis(), qorient.angle());
    }

    @Override
    public void setOrientation(double x, double y, double z) {
        rotationnode.setOrientation(x, y, z);
    }

    @Override
    public void setOrientationEulerX(double x) {
        rotationnode.setOrientationEulerX(x);
    }

    @Override
    public void setOrientationEulerY(double y) {
        rotationnode.setOrientationEulerY(y);
    }

    @Override
    public void setOrientationEulerZ(double z) {
        rotationnode.setOrientationEulerZ(z);
    }

    @Override
    public void setOrientationW(double w) {
        rotationnode.setOrientationW(w);
    }

    @Override
    public void setOrientationX(double x) {
        rotationnode.setOrientationX(x);
    }

    @Override
    public void setOrientationY(double y) {
        rotationnode.setOrientationY(y);
    }

    @Override
    public void setOrientationZ(double z) {
        rotationnode.setOrientationZ(z);
    }

    @Override
    public void setScaleX(double x) {
        scalenode.setScaleX(x);
    }

    @Override
    public void setScaleY(double y) {
        scalenode.setScaleY(y);
    }

    @Override
    public void setScaleZ(double z) {
        scalenode.setScaleZ(z);
    }

    @Override
    public void setScale(double x, double y, double z) {
        scalenode.setScale(x, y, z);
    }

    @Override
    public void setScale(Vec3d pos) {
        scalenode.setScale(pos.x(), pos.y(), pos.z());
    }

    @Override
    public Vec3d getScale() {
        return scalenode.getScale();
    }

    @Override
    public Quaternion getOrientation() {
        return rotationnode.getOrientation();
    }

    public TreeNode getScaleNode() {
        return scalenode;
    }

    public TreeNode getRotationNode() {
        return rotationnode;
    }

    public TreeNode getPositionNode() {
        return this;
    }

    public void addChildOnScaleNode(Node node) {
        scalenode.addChildNode(node);
    }

    public void addChildOnRotationNode(Node node) {
        rotationnode.addChildNode(node);
    }

    public void addChildOnPositionNode(Node node) {
        this.addChildNode(node);
    }

    @Override
    protected String getXMLNodeName() {
        return "animatable";
    }
}
