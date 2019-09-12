/*
 * This file is part of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
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
