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

import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;

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
