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

import greta.application.modular.ModularXMLFile;
import greta.core.util.xml.XMLTree;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

/**
 *
 * @author Andre-Marie Pez
 */
public class XMLTreeNode implements javax.swing.tree.MutableTreeNode {

    XMLTree node;
    MutableTreeNode parent;

    public XMLTreeNode(XMLTree node, MutableTreeNode parent) {
        this.node = node;
        this.parent = parent;
    }

    @Override
    public javax.swing.tree.TreeNode getChildAt(int childIndex) {
        if (node instanceof XMLTree) {
            return new XMLTreeNode(node.getChildrenElement().get(childIndex), this);
        }
        return null;
    }

    @Override
    public int getChildCount() {
        return node.getChildrenElement().size();
    }

    @Override
    public javax.swing.tree.TreeNode getParent() {
        return parent;
    }

    @Override
    public int getIndex(javax.swing.tree.TreeNode node) {
        if (node instanceof XMLTreeNode) {
            List<XMLTree> children = this.node.getChildrenElement();
            for (int i = 0; i < children.size(); i++) {
                XMLTree xMLTree = children.get(i);
                if (xMLTree.equals(((XMLTreeNode) node).node)) {
                    return i;
                }
            }
        }
        return -1;
    }

    @Override
    public boolean getAllowsChildren() {
        return node.getName().startsWith("menu");
    }

    @Override
    public boolean isLeaf() {
        return node.isNamed("item");
    }

    @Override
    public Enumeration children() {
        if (!isLeaf()) {
            final Iterator<XMLTree> iter = node.getChildrenElement().iterator();
            return new Enumeration() {
                @Override
                public boolean hasMoreElements() {
                    return iter.hasNext();
                }

                @Override
                public Object nextElement() {
                    return new XMLTreeNode(iter.next(), XMLTreeNode.this);
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
        return node.getAttribute("name");
    }

    @Override
    public void insert(MutableTreeNode child, int index) {
        child.removeFromParent();
        if (child instanceof XMLTreeNode) {
            ListIterator<XMLTree> iter = node.getElementIterator();
            while (iter.hasNext() && index != 0) {
                index--;
                iter.next();
            }
            iter.add(((XMLTreeNode) child).node);
        }
    }

    @Override
    public void remove(int index) {
        remove(new XMLTreeNode(node.getChildrenElement().get(index), this));
    }

    @Override
    public void remove(MutableTreeNode child) {
        if (child instanceof XMLTreeNode) {
            ListIterator<XMLTree> iter = node.getElementIterator();
            while (iter.hasNext()) {
                if (iter.next().equals(((XMLTreeNode) child).node)) {
                    iter.remove();
                    break;
                }
            }
        }
    }

    @Override
    public void setUserObject(Object object) {
        node.setAttribute("name", object.toString());
    }

    @Override
    public void removeFromParent() {
        if (parent != null) {
            parent.remove(this);
        }
        parent = null;
    }

    @Override
    public void setParent(MutableTreeNode newParent) {
        removeFromParent();
        if (newParent instanceof XMLTreeNode) {
            node = ((XMLTreeNode) newParent).node.addChild(node);
        }
        parent = newParent;
    }

    public int getLevel() {
        int parentCount = 0;
        XMLTree parent = node.getParent();
        while (parent != null && parent.getName().toLowerCase().startsWith("menu")) {
            parent = parent.getParent();
            ++parentCount;
        }
        return parentCount;
    }

    boolean isNodeChild(XMLTreeNode aNode) {
        return aNode.node.getParent().equals(node);
    }

    boolean isValid() {
        if (isLeaf()) {
            return ModularXMLFile.itemHasModule(node);
        } else {
            return this.children().hasMoreElements(); //empty menu
        }
    }

    boolean isChildrenValid() {
        if (!isValid()) {
            return false;
        }
        Enumeration children = this.children();
        while (children.hasMoreElements()) {
            Object child = children.nextElement();
            if (child instanceof XMLTreeNode) {
                if (!((XMLTreeNode) child).isChildrenValid()) {
                    return false;
                }
            }
        }

        return true;
    }

    public TreePath getTreePath() {
        int level = this.getLevel();
        TreeNode[] path = new TreeNode[level + 1];

        TreeNode cursor = this;
        for (int i = level; i >= 0; --i) {
            path[i] = cursor;
            cursor = cursor.getParent();
        }

        return new TreePath(path);
    }

}
