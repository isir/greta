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
