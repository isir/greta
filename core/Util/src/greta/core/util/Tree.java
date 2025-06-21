/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
