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
package greta.tools.editors.environment;

import greta.core.util.environment.Node;
import greta.core.util.environment.Root;
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
    EnvironmentEmbededTreeNode[] nodesToRemove;

    public TreeDragAndDrop() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                              ";class=\"" +
                EnvironmentEmbededTreeNode[].class.getName() +
                              "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch(ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
    }

    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop()) {
            return false;
        }
        support.setShowDropLocation(true);
        if(!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }
        // Do not allow a drop on the drag source selections.
        JTree.DropLocation dl =
                (JTree.DropLocation)support.getDropLocation();
        JTree tree = (JTree)support.getComponent();
        int dropRow = tree.getRowForPath(dl.getPath());
        int[] selRows = tree.getSelectionRows();
        for(int i = 0; i < selRows.length; i++) {
            if(selRows[i] == dropRow) {
                return false;
            }
        }
        // Do not allow MOVE-action drops if a non-leaf node is
        // selected unless all of its children are also selected.
        int action = support.getDropAction();
        if(action != MOVE) {
            return false;
        }

        TreePath dest = dl.getPath();
        EnvironmentEmbededTreeNode target = (EnvironmentEmbededTreeNode)dest.getLastPathComponent();
        if(target.isLeaf()){
            return false;
        }

        TreePath path = tree.getPathForRow(selRows[0]);
        EnvironmentEmbededTreeNode toMove = (EnvironmentEmbededTreeNode)path.getLastPathComponent();
        if(toMove.envNode instanceof Root){
            return false;
        }
        if(isAncestor(toMove.envNode, target.envNode)){
            return false;
        }

        return true;
    }

    private  boolean isAncestor(Node ancestor, Node descendant){
        Node parent = descendant.getParent();
        while(parent != null){
            if(ancestor == parent){
                return true;
            }
            parent = parent.getParent();
        }
        return false;
    }
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree)c;
        TreePath[] paths = tree.getSelectionPaths();
        if(paths != null) {
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            List<EnvironmentEmbededTreeNode> copies =
                new ArrayList<EnvironmentEmbededTreeNode>();
            List<EnvironmentEmbededTreeNode> toRemove =
                new ArrayList<EnvironmentEmbededTreeNode>();
            EnvironmentEmbededTreeNode node =
                (EnvironmentEmbededTreeNode)paths[0].getLastPathComponent();
            EnvironmentEmbededTreeNode copy = copy(node);
            copies.add(copy);
            toRemove.add(node);
            for(int i = 1; i < paths.length; i++) {
                EnvironmentEmbededTreeNode next =
                    (EnvironmentEmbededTreeNode)paths[i].getLastPathComponent();
                // Do not allow higher level nodes to be added to list.
                if(next.getLevel() < node.getLevel()) {
                    break;
                } else if(next.getLevel() > node.getLevel()) {  // child node
                    copy(next).setParent(copy);
                    // node already contains child
                } else {                                        // sibling
                    copies.add(copy(next));
                    toRemove.add(next);
                }
            }
            EnvironmentEmbededTreeNode[] nodes =
                copies.toArray(new EnvironmentEmbededTreeNode[copies.size()]);
            nodesToRemove =
                toRemove.toArray(new EnvironmentEmbededTreeNode[toRemove.size()]);
            return new NodesTransferable(nodes);
        }
        return null;
    }

    /** Defensive copy used in createTransferable. */
    private EnvironmentEmbededTreeNode copy(EnvironmentEmbededTreeNode node) {
        return new EnvironmentEmbededTreeNode(node.envNode);
    }

    protected void exportDone(JComponent source, Transferable data, int action) {
//        if((action & MOVE) == MOVE) {
//            JTree tree = (JTree)source;
//            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
//            // Remove nodes saved in nodesToRemove in createTransferable.
//            for(int i = 0; i < nodesToRemove.length; i++) {
//                model.removeNodeFromParent(nodesToRemove[i]);
//            }
//        }
    }

    public int getSourceActions(JComponent c) {
        return COPY_OR_MOVE;
    }

    public boolean importData(TransferHandler.TransferSupport support) {
        if(!canImport(support)) {
            return false;
        }
        // Extract transfer data.
        EnvironmentEmbededTreeNode[] nodes = null;
        try {
            Transferable t = support.getTransferable();
            nodes = (EnvironmentEmbededTreeNode[])t.getTransferData(nodesFlavor);
        } catch(UnsupportedFlavorException ufe) {
            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
        } catch(java.io.IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
        }
        // Get drop location info.
        JTree.DropLocation dl =
                (JTree.DropLocation)support.getDropLocation();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        EnvironmentEmbededTreeNode parent =
            (EnvironmentEmbededTreeNode)dest.getLastPathComponent();
        JTree tree = (JTree)support.getComponent();
        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        // Configure for drop mode.
        int index = childIndex;    // DropMode.INSERT
        if(childIndex == -1) {     // DropMode.ON
            index = parent.getChildCount();
        }
        // Add data to model.
        for(int i = 0; i < nodes.length; i++) {
            model.insertNodeInto(nodes[i], parent, index++);
        }
        return true;
    }

    public String toString() {
        return getClass().getName();
    }

    public class NodesTransferable implements Transferable {
        EnvironmentEmbededTreeNode[] nodes;

        public NodesTransferable(EnvironmentEmbededTreeNode[] nodes) {
            this.nodes = nodes;
         }

        public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
            if(!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            return nodes;
        }

        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }

        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }
}
