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
package greta.application.modular.tools;

import greta.core.util.xml.XMLTree;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

/**
 *
 * @author Andre-Marie Pez
 */
public class TreeDragAndDrop extends TransferHandler {

    DataFlavor nodesFlavor;
    DataFlavor[] flavors = new DataFlavor[1];
    XMLTreeNode[] nodesToRemove;

    public TreeDragAndDrop() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType
                    + ";class=\""
                    + XMLTreeNode[].class.getName()
                    + "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch (ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }

    @Override
    public boolean canImport(TransferHandler.TransferSupport support) {
        if (!support.isDrop()) {
            return false;
        }
        support.setShowDropLocation(true);
        if (!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }
        // Do not allow a drop on the drag source selections.
        JTree.DropLocation dl
                = (JTree.DropLocation) support.getDropLocation();
        JTree tree = (JTree) support.getComponent();
        int dropRow = tree.getRowForPath(dl.getPath());
        int[] selRows = tree.getSelectionRows();
        for (int i = 0; i < selRows.length; i++) {
            if (selRows[i] == dropRow) {
                return false;
            }
        }
        // Do not allow MOVE-action drops if a non-leaf node is
        // selected unless all of its children are also selected.
        int action = support.getDropAction();
        if (action != MOVE) {
            return false;
        }

        TreePath dest = dl.getPath();
        XMLTreeNode target = (XMLTreeNode) dest.getLastPathComponent();
        if (target.isLeaf()) {
            return false;
        }

        TreePath path = tree.getPathForRow(selRows[0]);
        XMLTreeNode toMove = (XMLTreeNode) path.getLastPathComponent();
        if (toMove.node.isNamed("menus")) {
            return false;
        }
        if (isAncestor(toMove.node, target.node)) {
            return false;
        }

        return true;
    }

    private boolean isAncestor(XMLTree ancestor, XMLTree descendant) {
        XMLTree parent = descendant.getParent();
        while (parent != null) {
            if (parent.equals(ancestor)) {
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }

    @Override
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree) c;
        TreePath[] paths = tree.getSelectionPaths();
        if (paths != null) {
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            List<XMLTreeNode> copies
                    = new ArrayList<XMLTreeNode>();
            List<XMLTreeNode> toRemove
                    = new ArrayList<XMLTreeNode>();
            XMLTreeNode node
                    = (XMLTreeNode) paths[0].getLastPathComponent();
            XMLTreeNode copy = copy(node);
            copies.add(copy);
            toRemove.add(node);
            for (int i = 1; i < paths.length; i++) {
                XMLTreeNode next
                        = (XMLTreeNode) paths[i].getLastPathComponent();
                // Do not allow higher level nodes to be added to list.
                if (next.getLevel() < node.getLevel()) {
                    break;
                } else if (next.getLevel() > node.getLevel()) {  // child node
                    copy(next).setParent(copy);
                    // node already contains child
                } else {                                        // sibling
                    copies.add(copy(next));
                    toRemove.add(next);
                }
            }
            XMLTreeNode[] nodes
                    = copies.toArray(new XMLTreeNode[copies.size()]);
            nodesToRemove
                    = toRemove.toArray(new XMLTreeNode[toRemove.size()]);
            return new NodesTransferable(nodes);
        }
        return null;
    }

    /**
     * Defensive copy used in createTransferable.
     */
    private XMLTreeNode copy(XMLTreeNode node) {
        return new XMLTreeNode(node.node, node.parent);
    }

    @Override
    protected void exportDone(JComponent source, Transferable data, int action) {
        if((action & MOVE) == MOVE) {
            source.updateUI();
        }
    }

    @Override
    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    @Override
    public boolean importData(TransferHandler.TransferSupport support) {
        if (!canImport(support)) {
            return false;
        }
        // Extract transfer data.
        XMLTreeNode[] nodes = null;
        try {
            Transferable t = support.getTransferable();
            nodes = (XMLTreeNode[]) t.getTransferData(nodesFlavor);
        } catch (UnsupportedFlavorException ufe) {
            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
        } catch (java.io.IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
        }
        // Get drop location info.
        JTree.DropLocation dl
                = (JTree.DropLocation) support.getDropLocation();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        XMLTreeNode parent
                = (XMLTreeNode) dest.getLastPathComponent();
        JTree tree = (JTree) support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
        // Configure for drop mode.
        int index = childIndex;    // DropMode.INSERT
        if (childIndex == -1) {     // DropMode.ON
            index = parent.getChildCount();
        }
        for (XMLTreeNode node : nodes) {
            model.insertNodeInto(node, parent, index++);
        }
        return true;
    }

    @Override
    public String toString() {
        return getClass().getName();
    }

    public class NodesTransferable implements Transferable {

        XMLTreeNode[] nodes;

        public NodesTransferable(XMLTreeNode[] nodes) {
            this.nodes = nodes;
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
                throws UnsupportedFlavorException {
            if (!isDataFlavorSupported(flavor)) {
                throw new UnsupportedFlavorException(flavor);
            }
            return nodes;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }
}
