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
package greta.core.util.xml;

/**
 * This class alows to create XMLTree and XMLParser with the default implementation.
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @navassoc - instanciate - greta.core.util.xml.XMLTree
 * @navassoc - instanciate - greta.core.util.xml.XMLParser
 */
public final class XML {
    /**
     * Don't let anyone instantiate this class.
     */
    private XML(){};

    /**
     * Creates and returns an instance of current implementation of XMLTree interface
     *
     * @param rootName the name of the root node
     * @return an instance of XMLTree
     */
    public static XMLTree createTree(String rootName){
        return new greta.core.util.xml.DefaultXMLTree(rootName, null);
    }

    /**
     * Creates and returns an instance of current implementation of XMLTree interface
     *
     * @param rootName the name of the root node
     * @param nameSpace the name space of the root node
     * @return an instance of XMLTree
     */
    public static XMLTree createTree(String rootName, String nameSpace){
        return new greta.core.util.xml.DefaultXMLTree(rootName, nameSpace);
    }

    /**
     * Creates and returns an instance of current implementation of XMLParser interface
     *
     * @return an instance of XMLParser
     */
    public static XMLParser createParser(){
        return new greta.core.util.xml.DefaultXMLParser();
    }

    /**
     * The name of a text node.
     */
    public static final String TEXT_NODE_NAME = "#text";
}
