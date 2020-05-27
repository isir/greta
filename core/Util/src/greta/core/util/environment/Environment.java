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

import greta.core.util.IniManager;
import greta.core.util.log.Logs;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Pierre Philippe
 * @author Andre-Marie Pez
 */
public class Environment{


    // add a global static variable: TIME ???

    private Root root = null;
    private List<Leaf> listleaf = null;
    private final List<EnvironmentEventListener> listeners = new ArrayList<EnvironmentEventListener>();

    // initialize the environment
    public Environment() {
        this(IniManager.getGlobals().getValueString("ENVIRONMENT"));
    }

    public Environment(String envName) {
         // initialize the time
        // xxx.setTimeMillis(0.0);

        // creating the tree (the root) with the landscape
        // load the environment from file
        listleaf = new ArrayList<Leaf>();

        if ((envName == null) || (envName.trim().isEmpty())) {
            load(IniManager.getGlobals().getValueString("ENVIRONMENT"));
        }
        else {
            load(envName);
        }


        //TODO must be better
//        move in the load function
//        for (Iterator<Node> iter = root.getChildren().iterator(); iter.hasNext();) {
//            Node child = iter.next();
//            if(child instanceof Leaf) {
//                listleaf.add((Leaf)child);
//            }
//        }
    }

    // add a leaf
    public void addLeaf (Leaf lf){
        this.listleaf.add(lf);
    }


    /*public void addLeaf(Leaf lf){
        this.listleaf.add(lf);
    }*/

    /**
     *
     * @param n
     */
    public void addNode(Node n) {
        addNode(n, root);
    }


    /**
     *
     * @param n
     * @param parent
     */
    public void addNode(Node n, TreeNode parent) {
        if (n == root) {
            return;
        }
        parent.addChildNode(n);
        TreeEvent event = new TreeEvent();
        event.childNode = n;
        event.newParentNode = parent;
        event.modifType = TreeEvent.MODIF_ADD;
        fireTreeEvent(event);
    }

    public void addNode(Node n, TreeNode parent, int index) {
        if (n == root) {
            return;
        }
        parent.addChildNode(n, index);
        TreeEvent event = new TreeEvent();
        event.childNode = n;
        event.newParentNode = parent;
        event.modifType = TreeEvent.MODIF_ADD;
        fireTreeEvent(event);
    }

    /**
     *
     * @param n
     */
    public void removeNode(Node n) {
        removeNode(n, root);
    }

    /**
     *
     * @param n
     * @param parent
     */
    public void removeNode(Node n, TreeNode parent) {
        parent.removeChild(n);

        TreeEvent event = new TreeEvent();
        event.childNode = n;
        event.oldParentNode = parent;
        event.modifType = TreeEvent.MODIF_REMOVE;
        fireTreeEvent(event);
    }

    /**
     *
     * @param n
     * @param newParent
     */
    public void moveNode(Node n, TreeNode newParent) {
        if (n == root) {
            return;
        }
        if (newParent != null) {
            if (n.parent == null) {
                addNode(n, newParent);
            } else {
                TreeEvent event = new TreeEvent();
                event.childNode = n;
                event.newParentNode = n.parent;
                event.oldParentNode = newParent;
                event.modifType = TreeEvent.MODIF_MOVE;
                n.parent.removeChild(n);
                newParent.addChildNode(n);
                fireTreeEvent(event);
            }
        } else {
            if (n.parent != null) {
                removeNode(n, n.parent);
            }
        }
    }

    /**
     * return the root of the environment
     *
     * @return TreeNode root
     */
    public TreeNode getRoot() {
        return root;
    }

    /**
     * return the list of all the leaves
     *
     * @return list of leaf
     */
    public List<Leaf> getListLeaf() {
        return listleaf;
    }

    /**
     * return the global tree
     *
     * @return TreeNode root
     */
    public TreeNode getTreeNode() {
        return root;
    }

    /**
     * return a specifique Node
     *
     * @param id
     * @return the target Node
     */
    public Node getNode(String id) {
        if (id == null) {
            return null;
        }
        if (root.getIdentifier().equals(id)) {
            return root;
        } else {
            return search(id, root);
        }
    }

    public Vec3d getRelativeTargetVector(Vec3d target, Vec3d source, Quaternion sourceOrient) {
        //target look at vector
        Vec3d lookAtVector = Vec3d.substraction(target, source);

        //relative position from the position to the target
        Vec3d relativeTargetPos = //Vec3f.substraction(source,
                sourceOrient.inverseRotate(lookAtVector);//);
        return relativeTargetPos;
    }

    public Vec3d getTargetRelativeEulerAngles(Vec3d target, Vec3d source, Quaternion sourceOrient) {
        if (target == null || sourceOrient == null) {
            return new Vec3d();
        }
        Vec3d relativeTargetPos = getRelativeTargetVector(target, source, sourceOrient);

        Vec3d onHorizontalPlane = new Vec3d(relativeTargetPos.x(), 0.0, relativeTargetPos.z());
        onHorizontalPlane.normalize();
        double yawAngle = 0.0;

        // according to the head orientation and the target position the rotation angle will be different
        // the head it is supposed to be the center of the coordinate axis
        // according to the quadrant in which is positioned the target the rotation anlge will be changed

            if (onHorizontalPlane.x() >= 0 && onHorizontalPlane.z() >= 0){
                yawAngle = Math.toDegrees(Math.abs(Math.acos(onHorizontalPlane.z())));
            }else if (onHorizontalPlane.x() >= 0 && onHorizontalPlane.z() < 0){
                yawAngle = 90.0 + Math.toDegrees(Math.abs(Math.asin(onHorizontalPlane.z())));
            }else if (onHorizontalPlane.x() <= 0 && onHorizontalPlane.z() < 0){
                yawAngle = -90 -1*Math.toDegrees(Math.abs(Math.asin(onHorizontalPlane.z())));
            }else if (onHorizontalPlane.x() <= 0 && onHorizontalPlane.z() >= 0){
                yawAngle =  -1*Math.toDegrees(Math.abs(Math.acos(onHorizontalPlane.z())));
            }else{
                yawAngle = 0.0;
            }

        relativeTargetPos.normalize();
        double pitchAngle = Math.asin(relativeTargetPos.y());

        //theta (yaw, horizontal), phi (pitch, vertical), 0 (roll, "dutch angle")
        //if theta is positive, target to the left
        //if phi is positive, target is upwards
        return new Vec3d(Math.toRadians(yawAngle), pitchAngle, 0.0f);
    }

    /* This should work for two eyes... However, best use symbolic notations
     * for target and source eyes : for more reusability
     * for instance, we could then look with left eye to foot of target...
     *
     *
     *
     public Vec3f getTargetRelativePosition(String targetId, Vec3f pos, Quaternion orient){
     TreeNode target = (TreeNode) search(targetId, root);
     //target look at vector
     Vec3f lookAtVector = Vec3f.substraction(pos, target.getGlobalCoordinates());

     //relative position from the position to the target
     Vec3f relativeTargetPos = Vec3f.substraction(pos, orient.inverseRotate(lookAtVector));
     return relativeTargetPos;
     }

     public Vec3f[] getTargetRelativeEulerAngles(String targetId, String originId){
     TreeNode source = (TreeNode) search(originId, root);

     //TODO : coordinates should be adjusted with scale parameters !!!
     // this only works for a 1.0 scale character...
     Vec3f l_pos = new Vec3f(source.getGlobalCoordinates().x()+0.035f,
     source.getGlobalCoordinates().y()+1.575f,
     source.getGlobalCoordinates().z()+0.080f);

     Vec3f r_pos = new Vec3f(source.getGlobalCoordinates().x()-0.035f,
     source.getGlobalCoordinates().y()+1.575f,
     source.getGlobalCoordinates().z()+0.080f);

     Vec3f relativeTargetPos_l = getTargetRelativePosition(targetId, l_pos, source.getGlobalOrientation());
     Vec3f relativeTargetPos_r = getTargetRelativePosition(targetId, r_pos, source.getGlobalOrientation());

     //this is from -PI/2 to PI/2...
     double yawAngle_l = Math.atan(relativeTargetPos_l.x()/Math.abs(relativeTargetPos_l.z()));
     double pitchAngle_l = Math.atan(relativeTargetPos_l.y()/Math.abs(relativeTargetPos_l.z()));

     double yawAngle_r = Math.atan(relativeTargetPos_r.x()/Math.abs(relativeTargetPos_r.z()));
     double pitchAngle_r = Math.atan(relativeTargetPos_r.y()/Math.abs(relativeTargetPos_r.z()));

     //...so we check if the target is behind...
     if(relativeTargetPos_l.z()<0)
     {
     if(yawAngle_l>0)
     yawAngle_l+= Math.PI/2;
     else
     yawAngle_l-= Math.PI/2;
     }

     if(relativeTargetPos_r.z()<0)
     {
     if(yawAngle_l>0)
     yawAngle_r+= Math.PI/2;
     else
     yawAngle_r-= Math.PI/2;
     }

     Vec3f[] lrEulerAngles = new Vec3f[2];
     //theta (yaw, horizontal), phi (pitch, vertical), 0 (roll, "dutch angle")
     //if theta is positive, target to the left
     //if phi is positive, target is upwards
     lrEulerAngles[0] = new Vec3f( (float)yawAngle_l, (float)pitchAngle_l, 0.0f );
     lrEulerAngles[1] = new Vec3f( (float)yawAngle_r, (float)pitchAngle_r, 0.0f );
     return lrEulerAngles;
     }
     */

    private Node search(String id, TreeNode parent) {
        for (Node child : parent.getChildren()) {
            if (child.getIdentifier().equals(id)) {
                return child;
            }
            if (child instanceof TreeNode) {
                TreeNode childTreeNode = (TreeNode) child;
                Node found = search(id, childTreeNode);
                if (found != null) {
                    return found;
                }
            }
        }
        return null;
    }

    /**
     *
     * @param fileName
     */
    public void load(String fileName) {
        XMLParser parser = XML.createParser();
        parser.setValidating(false);
        XMLTree xmlRoot = parser.parseFile(fileName);
        // verify the file
        root = new Root(this);
        if (xmlRoot == null) {
            Logs.warning(this.getClass().getName() + ": problem with loading file " + fileName);
            return;
        }
        load(xmlRoot, root);
    }

    /**
     *
     * @param buffer the XML content
     */
    public void loadBuffer(String buffer) {
        XMLParser parser = XML.createParser();
        parser.setValidating(false);
        XMLTree xmlRoot = parser.parseBuffer(buffer);
        // verify the file
        root = new Root(this);
        if (xmlRoot == null) {
            Logs.warning(this.getClass().getName() + ": problem with loading buffer");
            return;
        }
        load(xmlRoot, root);
    }

    /**
     *
     * @param xmlTree
     * @param tn
     */
    private void load(XMLTree xmlTree, TreeNode tn) {

        if (xmlTree.isNamed("node")) {
            XMLTree positionXML = xmlTree.findNodeCalled("position");
            if (positionXML != null) {
                float x = positionXML.hasAttribute("x") ? (float) positionXML.getAttributeNumber("x") : 0;
                float y = positionXML.hasAttribute("y") ? (float) positionXML.getAttributeNumber("y") : 0;
                float z = positionXML.hasAttribute("z") ? (float) positionXML.getAttributeNumber("z") : 0;
                tn.setCoordinates(x, y, z);
            }
            XMLTree orientationXML = xmlTree.findNodeCalled("orientation");
            if (orientationXML != null) {
                boolean radian = orientationXML.hasAttribute("type") && orientationXML.getAttribute("type").equalsIgnoreCase("radian");
                float x = (float) orientationXML.getAttributeNumber("x");
                float y = (float) orientationXML.getAttributeNumber("y");
                float z = (float) orientationXML.getAttributeNumber("z");
                if (!radian) { //degree
                    x = (float) Math.toRadians(x);
                    y = (float) Math.toRadians(y);
                    z = (float) Math.toRadians(z);
                }
                tn.setOrientation(x, y, z);
            }
            XMLTree scaleXML = xmlTree.findNodeCalled("scale");
            if (scaleXML != null) {
                float x = scaleXML.hasAttribute("x") ? (float) scaleXML.getAttributeNumber("x") : 1;
                float y = scaleXML.hasAttribute("y") ? (float) scaleXML.getAttributeNumber("y") : 1;
                float z = scaleXML.hasAttribute("z") ? (float) scaleXML.getAttributeNumber("z") : 1;
                tn.setScale(x, y, z);
            }
        }

        for (XMLTree xmlchild : xmlTree.getChildrenElement()) {
            Node childNode = null;
            // control in xmlchild if its a leaf, an animatable or a node
            if (xmlchild.isNamed("node")) {
                TreeNode mynewnode = new TreeNode();
                tn.addChildNode(mynewnode);
                load(xmlchild, mynewnode);
                childNode = mynewnode;

            } else if (xmlchild.isNamed("leaf")) {
                String ref = xmlchild.getAttribute("reference");
                float x = 1, y = 1, z = 1;
                XMLTree leafsize = xmlchild.findNodeCalled("size");
                if (leafsize != null) {
                    x = (float) leafsize.getAttributeNumber("x");
                    y = (float) leafsize.getAttributeNumber("y");
                    z = (float) leafsize.getAttributeNumber("z");
                }
                Leaf lf = new Leaf();
                lf.setSize(x, y, z);
                lf.setReference(ref);
                if (tn instanceof Animatable) {
                    ((Animatable) tn).setAttachedLeaf(lf);
                } else {
                    tn.addChildNode(lf);
                }
                listleaf.add(lf);
                childNode = lf;

            } else if (xmlchild.isNamed("animatable")) {
                Animatable anim = new Animatable();
                tn.addChildNode(anim);
                load(xmlchild, anim);
                childNode = anim;
            }

            if (childNode != null && xmlchild.hasAttribute("id")) {
                childNode.setIdentifier(xmlchild.getAttribute("id"));
            }
        }
    }

    public XMLTree asXML() {
        return root.asXML(true, false);
    }

    public List<Node> getGuests() {
        List<Node> res = new ArrayList<Node>(root.getChildren().size());
        for (Node node : root.getChildren()) {
            if (node.isGuest()) {
                res.add(node);
            }
        }
        return res;
    }

    public List<XMLTree> getGuestsAsXML() {
        List<Node> guests = getGuests();
        List<XMLTree> res = new ArrayList<XMLTree>(guests.size());
        for (Node guest : guests) {
            res.add(guest.asXML(true, true));
        }
        return res;
    }

    /**
     *
     * @param listener
     */
    public void addEnvironementListener(EnvironmentEventListener listener) {
        synchronized(getListeners()){
            getListeners().add(listener);
        }
    }

    /**
     *
     * @param listener
     */
    public void removeEnvironementListener(EnvironmentEventListener listener) {
        synchronized(getListeners()){
            getListeners().remove(listener);
        }
    }

    /**
     *
     * @param event
     */
    private void fireTreeEvent(TreeEvent event) {
        synchronized(getListeners()){
            for (EnvironmentEventListener listener : getListeners()) {
                listener.onTreeChange(event);
            }
        }
    }

    /**
     *
     * @param event
     */
    protected void fireNodeEvent(NodeEvent event) {
        synchronized(getListeners()){
            for (EnvironmentEventListener listener : getListeners()) {
                listener.onNodeChange(event);
            }
        }
    }

    protected void fireLeafEvent(LeafEvent event) {
        synchronized(getListeners()){
            for (EnvironmentEventListener listener : getListeners()) {
                listener.onLeafChange(event);
            }
        }
    }

    /**
     * @return the listeners
     */
    public List<EnvironmentEventListener> getListeners() {
        return listeners;
    }
}
