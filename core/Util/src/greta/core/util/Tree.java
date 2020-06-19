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
package greta.core.util;

/**
 *
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 */
import java.util.ArrayList;
import java.util.List;

public class Tree<T> {

    private T data = null;
    private List<Tree> children = new ArrayList<>();
    private Tree parent = null;

    public Tree() {

    }
    public Tree(T data) {
        this.data = data;
    }

    public void clear() {
        for(Tree child : children) {
            child.clear();
        }
        children.clear();
    }

    public void addChild(Tree child) {
        child.setParent(this);
        this.children.add(child);
    }

    public Tree<T> addChild(T data) {
        Tree<T> newChild = new Tree<>(data);
        newChild.setParent(this);
        children.add(newChild);
        return newChild;
    }

    public void removeChild(T data) {
        Tree nodeToDelete = null;
        for(Tree c : children) {
            if(c.data.equals(data)) {
                nodeToDelete = c;
                break;
            }
            else
                c.removeChild(data);
        }
        if(nodeToDelete!=null)
            children.remove(nodeToDelete);
    }

    public void addChildren(List<Tree> children) {
        for(Tree t : children) {
            t.setParent(this);
        }
        this.children.addAll(children);
    }

    public List<Tree> getChildren() {
        return children;
    }

    public int getChildrenCount() {
        return children.size();
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private void setParent(Tree parent) {
        this.parent = parent;
    }

    public Tree getParent() {
        return parent;
    }
}
