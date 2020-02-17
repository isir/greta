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
