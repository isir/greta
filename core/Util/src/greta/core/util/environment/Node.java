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

import greta.core.util.xml.XMLTree;
import java.util.Random;

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
