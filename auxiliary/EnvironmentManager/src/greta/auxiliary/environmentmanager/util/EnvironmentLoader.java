/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.environmentmanager.util;

import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.util.CharacterManager;
import greta.core.util.audio.AudioTreeNode;
import greta.core.util.environment.Animatable;
import greta.core.util.environment.Environment;
import greta.core.util.environment.Leaf;
import greta.core.util.environment.Node;
import greta.core.util.environment.TreeNode;
import greta.core.util.log.Logs;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author Brice Donval
 */
public class EnvironmentLoader {

    private static int loadingCount = 0;
    private static String loadingLog = "";

    /**
     *
     * @param environment
     * @param fileName
     */
    public static void loadEnvironmentWithFile(Environment environment, String fileName) {

        ++loadingCount;
        loadingLog = "Loading environment from file \"" + fileName + "\" ...\n\n";

        // parse the file
        XMLTree xmlRoot = parseFile(fileName);

        // verify the XML content
        if ((xmlRoot == null) || (!xmlRoot.isNamed("environment"))) {

            loadingLog += "    [EnvironmentLoader] loadEnvironmentWithFile() : problem with loading environment with file \"" + fileName + "\" !\n";
            Logs.warning("[EnvironmentLoader] loadEnvironmentWithFile() : problem with loading environment with file \"" + fileName + "\" !");

        } else {

            // clear the environment
            clearEnvironment(environment);

            // load the environment
            for (XMLTree xmlChild : xmlRoot.getChildrenElement()) {
                createAndAddChildToEnvironmentWithXMLChild(environment, environment.getRoot(), xmlChild);
            }
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/Environment_Load_" + loadingCount + ".txt");
    }

    /**
     *
     * @param environment
     * @param buffer
     */
    public static void loadEnvironmentWithBuffer(Environment environment, String buffer) {

        ++loadingCount;
        loadingLog = "Loading environment from buffer ...\n\n";

        // parse the buffer
        XMLTree xmlRoot = parseBuffer(buffer);

        // verify the XML content
        if ((xmlRoot == null) || (!xmlRoot.isNamed("environment"))) {

            loadingLog += "    [EnvironmentLoader] loadEnvironmentWithBuffer() : problem with loading environment with buffer !\n";
            Logs.warning("[EnvironmentLoader] loadEnvironmentWithBuffer() : problem with loading environment with buffer !");

        } else {

            // clear the environment
            clearEnvironment(environment);

            // load the environment
            for (XMLTree xmlChild : xmlRoot.getChildrenElement()) {
                createAndAddChildToEnvironmentWithXMLChild(environment, environment.getRoot(), xmlChild);
            }
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/Environment_Load_" + loadingCount + ".txt");
    }

    /**
     *
     * @param environment
     * @param parentNode
     * @param fileName
     * @return the loaded node
     */
    public static TreeNode loadNodeWithFile(Environment environment, TreeNode parentNode, String fileName) {

        ++loadingCount;
        loadingLog = "Loading node with file \"" + fileName + "\" ...\n\n";

        TreeNode node = null;

        // parse the file
        XMLTree xmlRoot = parseFile(fileName);

        // verify the file
        if ((xmlRoot == null) || (!xmlRoot.isNamed("node"))) {

            loadingLog += "    [EnvironmentLoader] loadNodeWithFile() : problem with loading node with file \"" + fileName + "\" !\n";
            Logs.warning("[EnvironmentLoader] loadNodeWithFile() : problem with loading node with file \"" + fileName + "\" !");

        } else {

            // load the node
            node = (TreeNode) createAndAddChildToEnvironmentWithXMLChild(environment, parentNode, xmlRoot);
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/Node_Load_" + loadingCount + ".txt");
        return node;
    }

    /**
     *
     * @param environment
     * @param parentNode
     * @param buffer
     * @return the loaded node
     */
    public static TreeNode loadNodeWithBuffer(Environment environment, TreeNode parentNode, String buffer) {

        ++loadingCount;
        loadingLog = "Loading node with buffer ...\n\n";

        TreeNode node = null;

        // parse the buffer
        XMLTree xmlRoot = parseBuffer(buffer);

        // verify the buffer
        if ((xmlRoot == null) || (!xmlRoot.isNamed("node"))) {

            loadingLog += "    [EnvironmentLoader] loadNodeWithBuffer() : problem with loading node with buffer !\n";
            Logs.warning("[EnvironmentLoader] loadNodeWithBuffer() : problem with loading node with buffer !");

        } else {

            // load the node
            node = (TreeNode) createAndAddChildToEnvironmentWithXMLChild(environment, parentNode, xmlRoot);
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/Node_Load_" + loadingCount + ".txt");
        return node;
    }

    /**
     *
     * @param environment
     * @param parentNode
     * @param fileName
     * @return the loaded leaf
     */
    public static Leaf loadLeafWithFile(Environment environment, TreeNode parentNode, String fileName) {

        ++loadingCount;
        loadingLog = "Loading leaf with file \"" + fileName + "\" ...\n\n";

        Leaf leaf = null;

        // parse the file
        XMLTree xmlRoot = parseFile(fileName);

        // verify the file
        if ((xmlRoot == null) || (!xmlRoot.isNamed("leaf"))) {

            loadingLog += "    [EnvironmentLoader] loadLeafWithFile() : problem with loading leaf with file \"" + fileName + "\" !\n";
            Logs.warning("[EnvironmentLoader] loadLeafWithFile() : problem with loading leaf with file \"" + fileName + "\" !");

        } else {

            // load the leaf
            leaf = (Leaf) createAndAddChildToEnvironmentWithXMLChild(environment, parentNode, xmlRoot);
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/Leaf_Load_" + loadingCount + ".txt");
        return leaf;
    }

    /**
     *
     * @param environment
     * @param parentNode
     * @param buffer
     * @return the loaded leaf
     */
    public static Leaf loadLeafWithBuffer(Environment environment, TreeNode parentNode, String buffer) {

        ++loadingCount;
        loadingLog = "Loading leaf with buffer ...\n\n";

        Leaf leaf = null;

        // parse the buffer
        XMLTree xmlRoot = parseBuffer(buffer);

        // verify the buffer
        if ((xmlRoot == null) || (!xmlRoot.isNamed("leaf"))) {

            loadingLog += "    [EnvironmentLoader] loadLeafWithBuffer() : problem with loading leaf with buffer !\n";
            Logs.warning("[EnvironmentLoader] loadLeafWithBuffer() : problem with loading leaf with buffer !");

        } else {

            // load the leaf
            leaf = (Leaf) createAndAddChildToEnvironmentWithXMLChild(environment, parentNode, xmlRoot);
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/Leaf_Load_" + loadingCount + ".txt");
        return leaf;
    }

    /**
     *
     * @param environment
     * @param parentNode
     * @param fileName
     * @return the loaded animatable
     */
    public static Animatable loadAnimatableWithFile(Environment environment, TreeNode parentNode, String fileName) {

        ++loadingCount;
        loadingLog = "Loading animatable with file \"" + fileName + "\" ...\n\n";

        Animatable animatable = null;

        // parse the file
        XMLTree xmlRoot = parseFile(fileName);

        // verify the file
        if ((xmlRoot == null) || (!xmlRoot.isNamed("animatable"))) {

            loadingLog += "    [EnvironmentLoader] loadAnimatableWithFile() : problem with loading animatable with file \"" + fileName + "\" !\n";
            Logs.warning("[EnvironmentLoader] loadAnimatableWithFile() : problem with loading animatable with file \"" + fileName + "\" !");

        } else {

            // load the animatable
            animatable = (Animatable) createAndAddChildToEnvironmentWithXMLChild(environment, parentNode, xmlRoot);
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/Animatable_Load_" + loadingCount + ".txt");
        return animatable;
    }

    /**
     *
     * @param environment
     * @param parentNode
     * @param buffer
     * @return the loaded animatable
     */
    public static Animatable loadAnimatableWithBuffer(Environment environment, TreeNode parentNode, String buffer) {

        ++loadingCount;
        loadingLog = "Loading animatable with buffer ...\n\n";

        Animatable animatable = null;

        // parse the buffer
        XMLTree xmlRoot = parseBuffer(buffer);

        // verify the buffer
        if ((xmlRoot == null) || (!xmlRoot.isNamed("animatable"))) {

            loadingLog += "    [EnvironmentLoader] loadAnimatableWithBuffer() : problem with loading animatable with buffer !\n";
            Logs.warning("[EnvironmentLoader] loadAnimatableWithBuffer() : problem with loading animatable with buffer !");

        } else {

            // load the animatable
            animatable = (Animatable) createAndAddChildToEnvironmentWithXMLChild(environment, parentNode, xmlRoot);
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/Animatable_Load_" + loadingCount + ".txt");
        return animatable;
    }

    /**
     *
     * @param environment
     * @param parentNode
     * @param fileName
     * @return the loaded mpeg4animatable
     */
    public static MPEG4Animatable loadMPEG4AnimatableWithFile(Environment environment, TreeNode parentNode, String fileName) {

        ++loadingCount;
        loadingLog = "Loading mpeg4animatable with file \"" + fileName + "\" ...\n\n";

        MPEG4Animatable mpeg4Animatable = null;

        // parse the file
        XMLTree xmlRoot = parseFile(fileName);

        // verify the file
        if ((xmlRoot == null) || (!xmlRoot.isNamed("mpeg4animatable"))) {

            loadingLog += "    [EnvironmentLoader] loadMPEG4AnimatableWithFile() : problem with loading mpeg4animatable with file \"" + fileName + "\" !\n";
            Logs.warning("[EnvironmentLoader] loadMPEG4AnimatableWithFile() : problem with loading mpeg4animatable with file \"" + fileName + "\" !");

        } else {

            // load the mpeg4animatable
            mpeg4Animatable = (MPEG4Animatable) createAndAddChildToEnvironmentWithXMLChild(environment, parentNode, xmlRoot);
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/MPEG4Animatable_Load_" + loadingCount + ".txt");
        return mpeg4Animatable;
    }

    /**
     *
     * @param environment
     * @param parentNode
     * @param buffer
     * @return the loaded mpeg4animatable
     */
    public static MPEG4Animatable loadMPEG4AnimatableWithBuffer(Environment environment, TreeNode parentNode, String buffer) {

        ++loadingCount;
        loadingLog = "Loading mpeg4animatable with buffer ...\n\n";

        MPEG4Animatable mpeg4Animatable = null;

        // parse the buffer
        XMLTree xmlRoot = parseBuffer(buffer);

        // verify the buffer
        if ((xmlRoot == null) || (!xmlRoot.isNamed("mpeg4animatable"))) {

            loadingLog += "    [EnvironmentLoader] loadMPEG4AnimatableWithBuffer() : problem with loading mpeg4animatable with buffer !\n";
            Logs.warning("[EnvironmentLoader] loadMPEG4AnimatableWithBuffer() : problem with loading mpeg4animatable with buffer !");

        } else {

            // load the mpeg4animatable
            mpeg4Animatable = (MPEG4Animatable) createAndAddChildToEnvironmentWithXMLChild(environment, parentNode, xmlRoot);
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/MPEG4Animatable_Load_" + loadingCount + ".txt");
        return mpeg4Animatable;
    }

    /**
     *
     * @param environment
     * @param parentNode
     * @param fileName
     * @return the loaded unknown child
     */
    public static Node loadUnknownChildWithFile(Environment environment, TreeNode parentNode, String fileName) {

        ++loadingCount;
        loadingLog = "Loading unknown child with file \"" + fileName + "\" ...\n\n";

        Node node = null;

        // parse the file
        XMLTree xmlRoot = parseFile(fileName);

        // verify the file
        if ((xmlRoot == null) || (!xmlRoot.isNamed("node") && !xmlRoot.isNamed("leaf") && !xmlRoot.isNamed("animatable") && !xmlRoot.isNamed("mpeg4animatable"))) {

            loadingLog += "    [EnvironmentLoader] loadUnknownChildWithFile() : problem with loading unknown child with file \"" + fileName + "\" !\n";
            Logs.warning("[EnvironmentLoader] loadUnknownChildWithFile() : problem with loading unknown child with file \"" + fileName + "\" !");

        } else {

            // load the unknown child
            node = createAndAddChildToEnvironmentWithXMLChild(environment, parentNode, xmlRoot);
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/UnknownChild_Load_" + loadingCount + ".txt");
        return node;
    }

    /**
     *
     * @param environment
     * @param parentNode
     * @param buffer
     * @return the loaded unknown child
     */
    public static Node loadUnknownChildWithBuffer(Environment environment, TreeNode parentNode, String buffer) {

        ++loadingCount;
        loadingLog = "Loading unknown child with buffer ...\n\n";

        Node node = null;

        // parse the buffer
        XMLTree xmlRoot = parseBuffer(buffer);

        // verify the buffer
        if ((xmlRoot == null) || (!xmlRoot.isNamed("node") && !xmlRoot.isNamed("leaf") && !xmlRoot.isNamed("animatable") && !xmlRoot.isNamed("mpeg4animatable"))) {

            loadingLog += "    [EnvironmentLoader] loadUnknownChildWithBuffer() : problem with loading unknown child with buffer !\n";
            Logs.warning("[EnvironmentLoader] loadUnknownChildWithBuffer() : problem with loading unknown child with buffer !");

        } else {

            // load the unknown child
            node = createAndAddChildToEnvironmentWithXMLChild(environment, parentNode, xmlRoot);
        }

        Toolbox.logIntoFile(loadingLog + "\n... END OF FILE.", Config.EnvironmentManager_DebugFolder + "/UnknownChild_Load_" + loadingCount + ".txt");
        return node;
    }

    /* ---------------------------------------------------------------------- */

    /**
     *
     * @param fileName
     */
    private static XMLTree parseFile(String fileName) {

        // parse the file
        XMLParser parser = XML.createParser();
        parser.setValidating(false);
        XMLTree xmlRoot = parser.parseFile(fileName);

        // verify the file
        if (xmlRoot == null) {
            Logs.warning("[EnvironmentLoader] parseFile() : parse error with loading content of file \"" + fileName + "\" !");
        }

        return xmlRoot;
    }

    /**
     *
     * @param buffer
     */
    private static XMLTree parseBuffer(String buffer) {

        // parse the buffer
        XMLParser parser = XML.createParser();
        parser.setValidating(false);
        XMLTree xmlRoot = parser.parseBuffer(buffer);

        // verify the buffer
        if (xmlRoot == null) {
            Logs.warning("[EnvironmentLoader] parseBuffer() : parse error with loading content of buffer !");
        }

        return xmlRoot;
    }

    /**
     *
     * @param environment
     */
    private static void clearEnvironment(Environment environment) {

        loadingLog += "    - Clearing environment :\n";

        List<Node> environmentNodes = new ArrayList<Node>(environment.getRoot().getChildren());
        List<Node> environmentGuests = new ArrayList<Node>(environment.getGuests());

        for (Node node : environmentNodes) {
            if (!environmentGuests.contains(node)) {
                loadingLog += "        - removing node " + node.getIdentifier() + ".\n";
                environment.removeNode(node);
            }
        }

        loadingLog += "\n";

        // update the environment list of leaves
        updateEnvironmentListLeaf(environment);
    }

    /**
     *
     * @param environment
     */
    private static void updateEnvironmentListLeaf(Environment environment) {

        loadingLog += "    - Removing obsolete leaves from list of leaves :\n";
        removeObsoleteLeavesFromEnvironmentListLeaf(environment);
        loadingLog += "\n";

        loadingLog += "    - Adding found leaves into list of leaves :\n";
        addFoundLeavesToEnvironmentListLeaf(environment, environment.getRoot());
        loadingLog += "\n";
    }

    /**
     *
     * @param environment
     */
    private static void removeObsoleteLeavesFromEnvironmentListLeaf(Environment environment) {

        ListIterator<Leaf> environmentLeafIterator = environment.getListLeaf().listIterator();

        while (environmentLeafIterator.hasNext()) {
            Leaf leaf = environmentLeafIterator.next();
            if (leaf.getRoot() != environment.getRoot()) {
                loadingLog += "        - removing leaf " + leaf.getIdentifier() + " from list of leaves.\n";
                environmentLeafIterator.remove();
            }
        }
    }

    /**
     *
     * @param environment
     * @param treeNode
     */
    private static void addFoundLeavesToEnvironmentListLeaf(Environment environment, TreeNode treeNode) {

        List<Node> nodes = treeNode.getChildren();

        for (Node node : nodes) {
            if (node instanceof Leaf) {
                Leaf leaf = (Leaf) node;
                if (!environment.getListLeaf().contains(leaf)) {
                    loadingLog += "        - adding leaf " + leaf.getIdentifier() + " into list of leaves.\n";
                    environment.getListLeaf().add(leaf);
                } else {
                    loadingLog += "        - finding leaf " + leaf.getIdentifier() + " in list of leaves.\n";
                }
            } else if (node instanceof TreeNode) {
                addFoundLeavesToEnvironmentListLeaf(environment, (TreeNode) node);
            }
        }
    }

    /* ---------------------------------------------------------------------- */

    /**
     *
     * @param xmlChild
     */
    private static Node createChildWithXMLChild(XMLTree xmlChild) {

        if (xmlChild.isNamed("node")) {

            TreeNode childTreeNode = new TreeNode();
            fillNodeWithXMLNode(childTreeNode, xmlChild);
            return childTreeNode;

        } else if (xmlChild.isNamed("leaf")) {

            Leaf childLeaf = new Leaf();
            fillLeafWithXMLLeaf(childLeaf, xmlChild);
            return childLeaf;

        } else if (xmlChild.isNamed("animatable")) {

            Animatable childAnimatable = new Animatable();
            fillAnimatableWithXMLAnimatable(childAnimatable, xmlChild);
            return childAnimatable;

        } else if (xmlChild.isNamed("mpeg4animatable")) {

            MPEG4Animatable childMPEG4animatable = new MPEG4Animatable(CharacterManager.getStaticInstance());
            fillAnimatableWithXMLAnimatable(childMPEG4animatable, xmlChild);
            return childMPEG4animatable;

        }

        return null;
    }

    /**
     *
     * @param parentNode
     * @param xmlChild
     */
    private static Node createAndAddChildToNodeWithXMLChild(TreeNode parentNode, XMLTree xmlChild) {

        if (xmlChild.isNamed("node")) {

            TreeNode childTreeNode = (TreeNode) createChildWithXMLChild(xmlChild);
            loadingLog += "    - adding node " + childTreeNode.getIdentifier() + " to parent node " + parentNode.getIdentifier() + ".\n";
            parentNode.addChildNode(childTreeNode);
            return childTreeNode;

        } else if (xmlChild.isNamed("leaf")) {

            Leaf childLeaf = (Leaf) createChildWithXMLChild(xmlChild);
            loadingLog += "    - adding leaf " + childLeaf.getIdentifier() + " to parent node " + parentNode.getIdentifier() + ".\n";
            parentNode.addChildNode(childLeaf);
            return childLeaf;

        } else if (xmlChild.isNamed("animatable")) {

            Animatable childAnimatable = (Animatable) createChildWithXMLChild(xmlChild);
            loadingLog += "    - adding animatable " + childAnimatable.getIdentifier() + " to parent node " + parentNode.getIdentifier() + ".\n";
            parentNode.addChildNode(childAnimatable);
            return childAnimatable;

        } else if (xmlChild.isNamed("mpeg4animatable")) {

            MPEG4Animatable childMPEG4animatable = (MPEG4Animatable) createChildWithXMLChild(xmlChild);
            loadingLog += "    - adding mpeg4animatable " + childMPEG4animatable.getIdentifier() + " to parent node " + parentNode.getIdentifier() + ".\n";
            parentNode.addChildNode(childMPEG4animatable);
            return childMPEG4animatable;

        }

        return null;
    }

    /**
     *
     * @param parentNode
     * @param xmlChild
     */
    private static Node createAndAddChildToEnvironmentWithXMLChild(Environment environment, TreeNode parentNode, XMLTree xmlChild) {

        Node node = null;

        if (xmlChild.isNamed("node")) {

            TreeNode childTreeNode = (TreeNode) createChildWithXMLChild(xmlChild);
            loadingLog += "    - adding node " + childTreeNode.getIdentifier() + " to environment parent node " + parentNode.getIdentifier() + " into .\n";
            environment.addNode(childTreeNode, parentNode);
            node = childTreeNode;

        } else if (xmlChild.isNamed("leaf")) {

            Leaf childLeaf = (Leaf) createChildWithXMLChild(xmlChild);
            loadingLog += "    - adding leaf " + childLeaf.getIdentifier() + " to environment parent node " + parentNode.getIdentifier() + ".\n";
            environment.addNode(childLeaf, parentNode);
            node = childLeaf;

        } else if (xmlChild.isNamed("animatable")) {

            Animatable childAnimatable = (Animatable) createChildWithXMLChild(xmlChild);
            loadingLog += "    - adding animatable " + childAnimatable.getIdentifier() + " to environment parent node " + parentNode.getIdentifier() + ".\n";
            environment.addNode(childAnimatable, parentNode);
            node = childAnimatable;

        } else if (xmlChild.isNamed("mpeg4animatable")) {

            MPEG4Animatable childMPEG4animatable = (MPEG4Animatable) createChildWithXMLChild(xmlChild);
            loadingLog += "    - adding mpeg4animatable " + childMPEG4animatable.getIdentifier() + " to environment parent node " + parentNode.getIdentifier() + ".\n";
            environment.addNode(childMPEG4animatable, parentNode);
            node = childMPEG4animatable;

        }

        // update the environment list of leaves
        updateEnvironmentListLeaf(environment);

        return node;
    }

    /* -------------------------------------------------- */

    /**
     *
     * @param treeNode
     * @param xmlTree
     */
    private static void fillNodeWithXMLNode(TreeNode treeNode, XMLTree xmlTree) {

        // setting identifier
        treeNode.setIdentifier(xmlTree.getAttribute("id"));

        XMLTree positionXML = xmlTree.findNodeCalled("position");
        XMLTree orientationXML = xmlTree.findNodeCalled("orientation");
        XMLTree scaleXML = xmlTree.findNodeCalled("scale");

        // setting coordinates
        if (positionXML != null) {
            double x = positionXML.hasAttribute("x") ? (double) positionXML.getAttributeNumber("x") : 0;
            double y = positionXML.hasAttribute("y") ? (double) positionXML.getAttributeNumber("y") : 0;
            double z = positionXML.hasAttribute("z") ? (double) positionXML.getAttributeNumber("z") : 0;
            loadingLog += "    - setting coordinates (" + x + " ; " + y + " ; " + z + ") into node " + treeNode.getIdentifier() + ".\n";
            treeNode.setCoordinates(x, y, z);
        }

        // removing position node from node children to be load
        xmlTree.removeChild(positionXML);

        // setting orientation
        if (orientationXML != null) {
            boolean radian = orientationXML.hasAttribute("type") && orientationXML.getAttribute("type").equalsIgnoreCase("radian");
            double x = (double) orientationXML.getAttributeNumber("x");
            double y = (double) orientationXML.getAttributeNumber("y");
            double z = (double) orientationXML.getAttributeNumber("z");
            if (!radian) {
                x = (double) Math.toRadians(x);
                y = (double) Math.toRadians(y);
                z = (double) Math.toRadians(z);
            }
            loadingLog += "    - setting orientation (" + x + " ; " + y + " ; " + z + ") into node " + treeNode.getIdentifier() + ".\n";
            treeNode.setOrientation(x, y, z);
        }

        // removing orientation node from node children to be load
        xmlTree.removeChild(orientationXML);

        // setting scale
        if (scaleXML != null) {
            double x = scaleXML.hasAttribute("x") ? (double) scaleXML.getAttributeNumber("x") : 1;
            double y = scaleXML.hasAttribute("y") ? (double) scaleXML.getAttributeNumber("y") : 1;
            double z = scaleXML.hasAttribute("z") ? (double) scaleXML.getAttributeNumber("z") : 1;
            loadingLog += "    - setting scale (" + x + " ; " + y + " ; " + z + ") into node " + treeNode.getIdentifier() + ".\n";
            treeNode.setScale(x, y, z);
        }

        // removing scale node from node children to be load
        xmlTree.removeChild(scaleXML);

        // loading node children
        for (XMLTree xmlChild : xmlTree.getChildrenElement()) {
            createAndAddChildToNodeWithXMLChild(treeNode, xmlChild);
        }
    }

    /* -------------------------------------------------- */

    /**
     *
     * @param leaf
     * @param xmlTree
     */
    private static void fillLeafWithXMLLeaf(Leaf leaf, XMLTree xmlTree) {

        // setting identifier
        leaf.setIdentifier(xmlTree.getAttribute("id"));

        // setting reference
        leaf.setReference(xmlTree.getAttribute("reference"));
        loadingLog += "    - setting reference \"" + leaf.getReference() + "\" into leaf " + leaf.getIdentifier() + ".\n";

        // setting size
        XMLTree leafsize = xmlTree.findNodeCalled("size");
        if (leafsize != null) {
            double x = leafsize.hasAttribute("x") ? (double) leafsize.getAttributeNumber("x") : 1;
            double y = leafsize.hasAttribute("y") ? (double) leafsize.getAttributeNumber("y") : 1;
            double z = leafsize.hasAttribute("z") ? (double) leafsize.getAttributeNumber("z") : 1;
            loadingLog += "    - setting size (" + x + " ; " + y + " ; " + z + ") into leaf " + leaf.getIdentifier() + ".\n";
            leaf.setSize(x, y, z);
        }
    }

    /* -------------------------------------------------- */

    /**
     *
     * @param animatable
     * @param xmlTree
     */
    private static void fillAnimatableWithXMLAnimatable(Animatable animatable, XMLTree xmlTree) {

        // setting identifier
        animatable.setIdentifier(xmlTree.getAttribute("id"));

        XMLTree animatablePositionXMLNode = getAnimatablePositionXMLNode(xmlTree);
        XMLTree animatableRotationXMLNode = getAnimatableRotationXMLNode(xmlTree);
        XMLTree animatableScaleXMLNode = getAnimatableScaleXMLNode(xmlTree);
        XMLTree animatableAttachedLeafXMLNode = getAnimatableAttachedLeafXMLNode(xmlTree);

        if (xmlTree.isNamed("mpeg4animatable")) {

            // loading head node
            XMLTree mpeg4AnimatableHeadXMLNode = getMPEG4AnimatableHeadXMLNode(xmlTree);
            AudioTreeNode mpeg4AnimatableHeadNode = ((MPEG4Animatable) animatable).getHeadNode();
            fillNodeWithXMLNode(mpeg4AnimatableHeadNode, mpeg4AnimatableHeadXMLNode);

            // removing head node from scale node children to be load
            animatableScaleXMLNode.removeChild(mpeg4AnimatableHeadXMLNode);
        }

        // loading attached leaf
        fillLeafWithXMLLeaf(animatable.getAttachedLeaf(), animatableAttachedLeafXMLNode);

        // removing attached leaf from scale node children to be load
        animatableScaleXMLNode.removeChild(animatableAttachedLeafXMLNode);

        // loading scale node
        fillNodeWithXMLNode(animatable.getScaleNode(), animatableScaleXMLNode);

        // removing scale node from rotation node children to be load
        animatableRotationXMLNode.removeChild(animatableScaleXMLNode);

        // loading rotation node
        fillNodeWithXMLNode(animatable.getRotationNode(), animatableRotationXMLNode);

        // removing rotation node from position node children to be load
        animatablePositionXMLNode.removeChild(animatableRotationXMLNode);

        // loading position node
        fillNodeWithXMLNode(animatable.getPositionNode(), animatablePositionXMLNode);
    }

    /**
     *
     * @param animatableXMLNode
     */
    private static XMLTree getAnimatablePositionXMLNode(XMLTree animatableXMLNode) {
        return animatableXMLNode;
    }

    /**
     *
     * @param animatableXMLNode
     */
    private static XMLTree getAnimatableRotationXMLNode(XMLTree animatableXMLNode) {

        XMLTree animatablePositionXMLNode = getAnimatablePositionXMLNode(animatableXMLNode);

        if (animatablePositionXMLNode != null) {

            String animatableId = animatableXMLNode.getAttribute("id");

            for (XMLTree xmlChild : animatablePositionXMLNode.getChildrenElement()) {
                String childId = xmlChild.getAttribute("id");
                if (childId.equalsIgnoreCase(animatableId + "_RotationTreeNode")) {
                    return xmlChild;
                }
            }
        }

        return null;
    }

    /**
     *
     * @param animatableXMLNode
     */
    private static XMLTree getAnimatableScaleXMLNode(XMLTree animatableXMLNode) {

        XMLTree animatableRotationXMLNode = getAnimatableRotationXMLNode(animatableXMLNode);

        if (animatableRotationXMLNode != null) {

            String animatableId = animatableXMLNode.getAttribute("id");

            for (XMLTree xmlChild : animatableRotationXMLNode.getChildrenElement()) {
                String childId = xmlChild.getAttribute("id");
                if (childId.equalsIgnoreCase(animatableId + "_ScaleTreeNode")) {
                    return xmlChild;
                }
            }
        }

        return null;
    }

    /**
     *
     * @param animatableXMLNode
     */
    private static XMLTree getAnimatableAttachedLeafXMLNode(XMLTree animatableXMLNode) {

        XMLTree animatableScaleXMLNode = getAnimatableScaleXMLNode(animatableXMLNode);

        if (animatableScaleXMLNode != null) {

            String animatableId = animatableXMLNode.getAttribute("id");

            for (XMLTree xmlChild : animatableScaleXMLNode.getChildrenElement()) {
                String childId = xmlChild.getAttribute("id");
                if (childId.equalsIgnoreCase(animatableId + "_AttachedLeaf")) {
                    return xmlChild;
                }
            }
        }

        return null;
    }

    /**
     *
     * @param animatableXMLNode
     */
    private static XMLTree getMPEG4AnimatableHeadXMLNode(XMLTree animatableXMLNode) {

        XMLTree animatableScaleXMLNode = getAnimatableScaleXMLNode(animatableXMLNode);

        if (animatableScaleXMLNode != null) {

            String animatableId = animatableXMLNode.getAttribute("id");

            for (XMLTree xmlChild : animatableScaleXMLNode.getChildrenElement()) {
                String childId = xmlChild.getAttribute("id");
                if (childId.equalsIgnoreCase(animatableId + "_AudioTreeNode")) {
                    return xmlChild;
                }
            }
        }

        return null;
    }

}
