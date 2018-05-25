/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.environment;

import vib.core.util.xml.XML;
import vib.core.util.xml.XMLTree;

/**
 *
 * @author Pierre Philippe
 * @author Andre-Marie Pez
 */
public class Root extends TreeNode {

    private Environment env = null;

    public Root() {
    }

    Root(Environment env) {
        this.env = env;
    }

    public Environment getEnvironment() {
        return env;
    }

    @Override
    protected String getXMLNodeName() {
        return "environment";
    }

    @Override
    protected XMLTree asXML(boolean doNonGuest, boolean doGest) {

        XMLTree node = XML.createTree(getXMLNodeName());

        for (Node child : getChildren()) {
            if ((!child.isGuest() && doNonGuest) || (child.isGuest() && doGest)) {
                node.addChild(child.asXML(doNonGuest, doGest));
            }
        }

        return node;
    }
}
