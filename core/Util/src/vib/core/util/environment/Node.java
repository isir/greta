/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.environment;

import java.util.Random;
import vib.core.util.xml.XMLTree;

/**
 *
 * @author Pierre Philippe
 */
public abstract class Node {

    private static Random random = new Random();
    private static long increment = 0;
    protected TreeNode parent = null;
    protected String identifier = getClass().getSimpleName() + "_" + System.currentTimeMillis() + "_" + (increment++) + "_" + random.nextLong();
    private boolean guest = false;

    /**
     *
     * @return the identifier of this {@code Node}
     */
    public String getIdentifier() {
        return identifier;
    }

    /**
     * Changes the identifier of this {@code Node}<br/> never use this until you
     * know what you are doing.
     *
     * @param id the new identifier
     */
    public void setIdentifier(String id) {
        identifier = id;
    }

    public Node getRoot() {
        if (parent == null) {
            return this;
        } else {
            return parent.getRoot();
        }
    }

    protected void setParent(TreeNode p) {
        this.parent = p;
    }

    public boolean hasAncestor(Node n) {
        if(n==null){
            return false;
        }
        if(n==this){
            return true;
        }
        if( ! (n instanceof TreeNode)) {
            return false;
        }
        if(getParent()==null){
            return false;
        }
        return getParent().hasAncestor(n);
    }

    public boolean isAncestorOf(Node n) {
        if(n==null){
            return false;
        }
        return n.hasAncestor(this);
    }

    /**
     * @return parent node
     */
    public TreeNode getParent() {
        return parent;
    }

    public boolean isGuest() {
        return guest;
    }

    public void setGuest(boolean isGuest) {
        guest = isGuest;
    }

    protected abstract String getXMLNodeName();

    protected abstract XMLTree asXML(boolean doNonGuest, boolean doGest);

    public XMLTree asXML() {
        return asXML(true, true);
    }
}
