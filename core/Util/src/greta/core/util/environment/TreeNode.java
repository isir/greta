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
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Tree node for a general tree of Objects
 *
 * @author Pierre Philippe
 * @author Andre-Marie Pez
 */
public class TreeNode extends Node {

    // list of children of the current node
    private List<Node> children = null;
    // 3D coordonates
    private Vec3d position;
    // 3D orientation of an object
    private Quaternion orientation;
    // 3D scale of an object
    private Vec3d scale;

    public TreeNode() {
        this(0, 0, 0, 0, 0, 0, 1, 1, 1, 1);
    }

    public TreeNode(double x, double y, double z,
            double ox, double oy, double oz, double ow,
            double sx, double sy, double sz /*, parent????*/) {
        //this.parent = null;
        this.children = new ArrayList<Node>();
        position = new Vec3d(x, y, z);
        orientation = new Quaternion(ox, oy, oz, ow);
        scale = new Vec3d(sx, sy, sz);
    }

    ////////////////////////////////////////////////////////////////////////////
    ///   Cooradinates   ///////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * @return the local coordinate x
     */
    public double getCoordinateX() {
        return getCoordinates().x();
    }

    /**
     * @return the local coordinate y
     */
    public double getCoordinateY() {
        return getCoordinates().y();
    }

    /**
     * @return the local coordinate z
     */
    public double getCoordinateZ() {
        return getCoordinates().z();
    }

    /**
     * Returns the local coordinates of this {@code TreeNode}.
     *
     * @return the local coordinates.
     */
    public Vec3d getCoordinates() {
        return position;
    }

    /**
     * Returns the global coordinates of this {@code TreeNode}.
     *
     * @return the global coordinates.
     */
    public Vec3d getGlobalCoordinates() {
        if (parent == null) {
            return getCoordinates();
        } else {
            return Vec3d.addition(parent.getGlobalCoordinates(),
                    parent.getGlobalOrientation()
                    .rotate(Vec3d.multiplicationOfComponents(
                                    parent.getGlobalScale(),
                                    getCoordinates())));
        }
    }

    /**
     * set coordonates x y z
     */
    public void setCoordinates(double x, double y, double z) {
        if (x != position.x() || y != position.y() || z != position.z()) {
            position.set(x, y, z);
            this.fireNodeEvent(NodeEvent.MODIF_POSITION);
        }
    }

    public void setCoordinates(Vec3d pos) {
        setCoordinates(pos.x(), pos.y(), pos.z());
    }

    public void setCoordinateX(double x) {
        setCoordinates(x, getCoordinateY(), getCoordinateZ());
    }

    public void setCoordinateY(double y) {
        setCoordinates(getCoordinateX(), y, getCoordinateZ());
    }

    public void setCoordinateZ(double z) {
        setCoordinates(getCoordinateX(), getCoordinateY(), z);
    }

    ////////////////////////////////////////////////////////////////////////////
    ///   Orientation   ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    public double getOrientationX() {
        return getOrientation().x();
    }

    public double getOrientationY() {
        return getOrientation().y();
    }

    public double getOrientationZ() {
        return getOrientation().z();
    }

    public double getOrientationW() {
        return getOrientation().w();
    }

    public double getOrientationEulerX() {
        return getOrientationEuler().x();
    }

    public double getOrientationEulerY() {
        return getOrientationEuler().y();
    }

    public double getOrientationEulerZ() {
        return getOrientationEuler().z();
    }

    /**
     * Returns the local orientation of this {@code TreeNode}.
     *
     * @return the local orientation.
     */
    public Quaternion getOrientation() {
        return orientation;
    }

    public Vec3d getOrientationEuler() {
        return getOrientation().getEulerAngleXYZ();
    }

    /**
     * Returns the global orientation of this {@code TreeNode}.
     *
     * @return the global orientation.
     */
    public Quaternion getGlobalOrientation() {
        if (parent == null) {
            return getOrientation();
        } else {
            return Quaternion.multiplication(parent.getGlobalOrientation(), getOrientation());
        }
    }

    /**
     * set orientation with quaternion
     *
     * @param x component x of the quaternion
     * @param y component y of the quaternion
     * @param z component z of the quaternion
     * @param w component w of the quaternion
     */
    public void setOrientation(double x, double y, double z, double w) {
        if (x != getOrientation().x() || y != getOrientation().y() || z != getOrientation().z() || w != getOrientation().w()) {
            getOrientation().setValue(x, y, z, w);
            this.fireNodeEvent(NodeEvent.MODIF_ROTATION);
        }
    }

    /**
     * set orientation with euler angle in radian
     *
     * @param x angle around x axis
     * @param y angle around y axis
     * @param z angle around z axis
     */
    public void setOrientation(double x, double y, double z) {
        Quaternion q = new Quaternion();
        q.fromEulerXYZ(x, y, z);
        setOrientation(q);
    }

    public void setOrientation(Vec3d orient, double a) {
        getOrientation().setAxisAngle(orient, a);
        this.fireNodeEvent(NodeEvent.MODIF_ROTATION);
    }

    public void setOrientation(Quaternion qorient) {
        setOrientation(qorient.x(), qorient.y(), qorient.z(), qorient.w());
    }

    public void setOrientationX(double x) {
        setOrientation(x, getOrientation().y(), getOrientation().z(), getOrientation().w());
    }

    public void setOrientationY(double y) {
        setOrientation(getOrientation().x(), y, getOrientation().z(), getOrientation().w());
    }

    public void setOrientationZ(double z) {
        setOrientation(getOrientation().x(), getOrientation().y(), z, getOrientation().w());
    }

    public void setOrientationW(double w) {
        setOrientation(getOrientation().x(), getOrientation().y(), getOrientation().z(), w);
    }

    public void setOrientationEulerX(double x) {
        Vec3d euler = getOrientationEuler();
        setOrientation(x, euler.y(), euler.z());
    }

    public void setOrientationEulerY(double y) {
        Vec3d euler = getOrientationEuler();
        setOrientation(euler.x(), y, euler.z());
    }

    public void setOrientationEulerZ(double z) {
        Vec3d euler = getOrientationEuler();
        setOrientation(euler.x(), euler.y(), z);
    }

    ////////////////////////////////////////////////////////////////////////////
    ///   Scale   //////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    public double getScaleX() {
        return getScale().x();
    }

    public double getScaleY() {
        return getScale().y();
    }

    public double getScaleZ() {
        return getScale().z();
    }

    /**
     * Returns the local scaling of this {@code TreeNode}.
     *
     * @return the local scaling.
     */
    public Vec3d getScale() {
        return scale;
    }

    /**
     * Returns the global scaling of this {@code TreeNode}.
     *
     * @return the global scaling.
     */
    public Vec3d getGlobalScale() {
        if (parent == null) {
            return getScale();
        } else {
            return Vec3d.multiplicationOfComponents(parent.getGlobalScale(), getScale());
        }
    }

    /**
     * set scale x y z
     */
    public void setScale(double x, double y, double z) {
        if (x != getScale().x() || y != getScale().y() || z != getScale().z()) {
            getScale().set(x, y, z);
            this.fireNodeEvent(NodeEvent.MODIF_SCALE);
        }
    }

    public void setScale(Vec3d scale) {
        setScale(scale.x(), scale.y(), scale.z());
    }

    public void setScaleX(double x) {
        setScale(x, getScale().y(), getScale().z());
    }

    public void setScaleY(double y) {
        setScale(getScale().x(), y, getScale().z());
    }

    public void setScaleZ(double z) {
        setScale(getScale().x(), getScale().y(), z);
    }

    ////////////////////////////////////////////////////////////////////////////
    ///   Other   //////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////

    /**
     * remove node from tree
     */
    public void remove() {
        if (parent != null) {
            parent.removeChild(this);
        }
    }

    /**
     * remove child node
     *
     * @param child
     */
    public void removeChild(Node child) {
        if (child.parent == this) {
            child.parent = null;
        }
        if (children.contains(child)) {
            children.remove(child);
        }
    }

    /**
     * add child to node
     *
     * @param child node to be added
     */
    public void addChildNode(Node child) {
        if (child == this) {
            return;
        }
        if (child.parent != null) {
            child.parent.removeChild(child);
        }
        child.parent = this;
        if (!children.contains(child)) {
            children.add(child);
        }
    }

    public void addChildNode(Node child, int index) {
        if (child == this) {
            return;
        }
        if (child.parent != null) {
            child.parent.removeChild(child);
        }
        child.parent = this;
        if (!children.contains(child)) {
            children.add(index, child);
        }
    }

    /**
     * deep copy (clone)
     *
     * @return copy of TreeNode
     */
    public TreeNode deepCopy() {
        TreeNode newNode = new TreeNode(/*getReference()*/);
        for (Iterator iter = children.iterator(); iter.hasNext();) {
            TreeNode child = (TreeNode) iter.next();
            newNode.addChildNode(child.deepCopy());
        }
        return newNode;
    }

    /**
     * deep copy (clone) and prune
     *
     * @param depth - number of child levels to be copied
     * @return copy of TreeNode
     */
    public TreeNode deepCopyPrune(int depth) {
        if (depth < 0) {
            throw new IllegalArgumentException("Depth is negative");
        }
        TreeNode newNode = new TreeNode(/*getReference()*/);
        if (depth == 0) {
            return newNode;
        }
        for (Node child : children) {
            if (child instanceof TreeNode) {
                newNode.addChildNode(((TreeNode) child).deepCopyPrune(depth - 1));
            }
        }
        return newNode;
    }

    /**
     * @return level = distance from root
     */
    public int getLevel() {
        int level = 0;
        TreeNode p = parent;
        while (p != null) {
            ++level;
            p = p.parent;
        }
        return level;
    }

    /**
     * @return List of children
     */
    public List<Node> getChildren() {
        return children;
    }

    /**
     * return all or part of the nodes
     *
     * @return a tree of nodes
     */
    public TreeNode getPartTreeNode(int depth) {
        // following the level, give the nodes of the tree
        // TODO: don't copy it
        TreeNode parttreenode = deepCopyPrune(depth);
        return parttreenode;
    }

    /**
     * move a tree dependening of coordinates
     */
    public void moveTree(int x, int y, int z) {
        moveTree(new Vec3d(x, y, z));
    }

    public void moveTree(Vec3d offset) {
        this.position.add(offset);
        this.fireNodeEvent(NodeEvent.MODIF_POSITION);
    }

    /**
     * rotate a tree depending of the angle of the rotation
     */
    public void rotateTree(Vec3d axis, double angle) {
        rotateTree(new Quaternion(axis, angle));
    }

    public void rotateTree(Quaternion quat) {
        orientation.multiply(quat);
        this.fireNodeEvent(NodeEvent.MODIF_ROTATION);
    }

    public void reScale(double x, double y, double z) {
        setScale(scale.x() * x, scale.y() * y, scale.z() * z);
    }

    public void reScale(Vec3d pos) {
        reScale(pos.x(), pos.y(), pos.z());
    }

    protected void fireNodeEvent(int eventType) {
        Node root = getRoot();
        if (root instanceof Root) {
            NodeEvent event = new NodeEvent();
            event.node = this;
            event.modifType = eventType;
            Environment env = ((Root) root).getEnvironment();
            env.fireNodeEvent(event);
        }
    }

    @Override
    protected String getXMLNodeName() {
        return "node";
    }

    @Override
    protected XMLTree asXML(boolean doNonGuest, boolean doGest) {

        XMLTree node = XML.createTree(getXMLNodeName());
        node.setAttribute("id", identifier);

        if (position.x() != 0 || position.y() != 0 || position.z() != 0) {
            XMLTree pos = node.createChild("position");
            pos.setAttribute("x", "" + position.x());
            pos.setAttribute("y", "" + position.y());
            pos.setAttribute("z", "" + position.z());
        }

        Vec3d euler = orientation.getEulerAngleXYZByAngle();
        if (euler.x() != 0 || euler.y() != 0 || euler.z() != 0) {
            XMLTree ori = node.createChild("orientation");
            ori.setAttribute("x", "" + euler.x());
            ori.setAttribute("y", "" + euler.y());
            ori.setAttribute("z", "" + euler.z());
        }

        if (scale.x() != 1 || scale.y() != 1 || scale.z() != 1) {
            XMLTree sca = node.createChild("scale");
            sca.setAttribute("x", "" + scale.x());
            sca.setAttribute("y", "" + scale.y());
            sca.setAttribute("z", "" + scale.z());
        }

        for (Node child : children) {
            if ((!child.isGuest() && doNonGuest) || (child.isGuest() && doGest)) {
                node.addChild(child.asXML(doNonGuest, doGest));
            }
        }

        return node;
    }
}
