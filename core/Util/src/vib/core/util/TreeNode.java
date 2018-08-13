/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util;

/**
 *
 * @author Philippe Gauthier <philippe.gauthier@upmc.fr>
 */
import java.util.ArrayList;
import java.util.List;

public class TreeNode<T>{
    private T data = null;
    private List<TreeNode> children = new ArrayList<>();
    private TreeNode parent = null;

    public TreeNode(){
        
    }
    public TreeNode(T data) {
        this.data = data;
    }
    
    public void clear(){
        for(TreeNode child : children){
            child.clear();
        }
        children.clear();
    }

    public void addChild(TreeNode child) {
        child.setParent(this);
        this.children.add(child);
    }

    public TreeNode<T> addChild(T data) {
        TreeNode<T> newChild = new TreeNode<>(data);
        newChild.setParent(this);
        children.add(newChild);
        return newChild;
    }

    public void addChildren(List<TreeNode> children) {
        for(TreeNode t : children) {
            t.setParent(this);
        }
        this.children.addAll(children);
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    private void setParent(TreeNode parent) {
        this.parent = parent;
    }

    public TreeNode getParent() {
        return parent;
    }
}
