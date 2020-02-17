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

import greta.core.util.environment.Environment;
import greta.core.util.environment.Leaf;
import greta.core.util.environment.Node;
import greta.core.util.environment.Root;
import greta.core.util.environment.TreeNode;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.NoSuchElementException;
import javax.swing.tree.MutableTreeNode;

/**
 *
 * @author Andre-Marie Pez
 */
public class EnvironmentEmbededTreeNode implements javax.swing.tree.MutableTreeNode {

    final Node envNode;

    public EnvironmentEmbededTreeNode(Node envNode) {
        this.envNode = envNode;
    }

    @Override
    public javax.swing.tree.TreeNode getChildAt(int childIndex) {
        if (envNode instanceof TreeNode) {
            return new EnvironmentEmbededTreeNode(((TreeNode) envNode).getChildren().get(childIndex));
        }
        return null;
    }

    @Override
    public int getChildCount() {
        if (envNode instanceof TreeNode) {
            return ((TreeNode) envNode).getChildren().size();
        }
        return 0;
    }

    @Override
    public javax.swing.tree.TreeNode getParent() {
        return envNode.getParent() == null ? null : new EnvironmentEmbededTreeNode(envNode.getParent());
    }

    @Override
    public int getIndex(javax.swing.tree.TreeNode node) {
        if (node instanceof EnvironmentEmbededTreeNode && envNode instanceof TreeNode) {
            return ((TreeNode) envNode).getChildren().indexOf(((EnvironmentEmbededTreeNode) node).envNode);
        } else {
            return -1;
        }
    }

    @Override
    public boolean getAllowsChildren() {
        return !(envNode instanceof Leaf);
    }

    @Override
    public boolean isLeaf() {
        return envNode instanceof Leaf;
    }

    @Override
    public Enumeration children() {
        if (envNode instanceof TreeNode) {
            final Iterator<Node> iter = ((TreeNode) envNode).getChildren().iterator();
            return new Enumeration() {
                @Override
                public boolean hasMoreElements() {
                    return iter.hasNext();
                }

                @Override
                public Object nextElement() {
                    return new EnvironmentEmbededTreeNode(iter.next());
                }
            };
        }
        return new Enumeration() {

            @Override
            public boolean hasMoreElements() {
                return false;
            }

            @Override
            public Object nextElement() {
                throw new NoSuchElementException();
            }
        };
    }

    @Override
    public String toString() {
        return envNode.getIdentifier();
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        child.removeFromParent();
        if(envNode instanceof TreeNode && child instanceof EnvironmentEmbededTreeNode){
            Node root = envNode.getRoot();
            if(root instanceof Root){
                Environment e = ((Root)root).getEnvironment();
                e.addNode(((EnvironmentEmbededTreeNode)child).envNode, (TreeNode)envNode, index);
            }
            else{
                ((TreeNode)envNode).addChildNode(((EnvironmentEmbededTreeNode)child).envNode, index);
            }
        }
    }

    @Override
    public void remove(int index) {
        if(envNode instanceof TreeNode){
            remove(new EnvironmentEmbededTreeNode(((TreeNode)envNode).getChildren().get(index)));
        }
    }

    @Override
    public void remove(MutableTreeNode node) {
        if(envNode instanceof TreeNode && node instanceof EnvironmentEmbededTreeNode){
            Node root = envNode.getRoot();
            if(root instanceof Root){
                Environment e = ((Root)root).getEnvironment();
                e.removeNode(((EnvironmentEmbededTreeNode)node).envNode, (TreeNode)envNode);
            }
            else{
                ((TreeNode)envNode).removeChild(((EnvironmentEmbededTreeNode)node).envNode);
            }
        }
    }

    @Override
    public void setUserObject(Object object) {

        String newID = object.toString();
        if( ! isIdExists(newID)){
            EnvironmentEmbededTreeNode parent = (EnvironmentEmbededTreeNode)getParent();
            int index = 0;
            if(parent !=null){
                index = ((TreeNode)parent.envNode).getChildren().indexOf(envNode);
                removeFromParent();
            }
            envNode.setIdentifier(newID);
            if(parent !=null){
                parent.insert(this, index);
            }
        }
    }

    private boolean isIdExists(String id){
        return isIdExists(envNode.getRoot(), id);
    }
    private boolean isIdExists(Node searchIn, String id){
        if(searchIn.getIdentifier().equals(id)){
            return true;
        }
        if(searchIn instanceof TreeNode){
            for(Node child : ((TreeNode)searchIn).getChildren()){
                if(isIdExists(child, id)){
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void removeFromParent() {
        javax.swing.tree.TreeNode parent = this.getParent();
        if(parent instanceof javax.swing.tree.MutableTreeNode){
            ((javax.swing.tree.MutableTreeNode)parent).remove(this);
        }
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        if(newParent instanceof EnvironmentEmbededTreeNode){
            Node newEnvParent = ((EnvironmentEmbededTreeNode)newParent).envNode;
            if(newEnvParent instanceof TreeNode){
                Node oldRoot = envNode.getRoot();
                Node newRoot = newEnvParent.getRoot();
                if(oldRoot == newRoot){
                    //same tree
                    if(oldRoot instanceof Root){
                        Environment e = ((Root)oldRoot).getEnvironment();
                        e.moveNode(envNode, (TreeNode)newEnvParent);
                    }
                    else{
                        ((TreeNode)newEnvParent).addChildNode(envNode);
                    }
                }
                else{
                    //two different trees
                    removeFromParent();
                    if(newRoot instanceof Root){
                        Environment e = ((Root)newRoot).getEnvironment();
                        e.addNode(envNode, (TreeNode)newEnvParent);
                    }
                    else{
                        ((TreeNode)newEnvParent).addChildNode(envNode);
                    }
                }
            }
        }
    }

    int getLevel() {
        int parentCount = 0;
        TreeNode parent = envNode.getParent();
        while(parent != null){
            parent = parent.getParent();
            ++parentCount;
        }
        return parentCount;
    }

    boolean isNodeChild(EnvironmentEmbededTreeNode aNode) {
        return aNode.envNode.getParent()==envNode;
    }

}
