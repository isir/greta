/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.xml;

/**
 * This class alows to create XMLTree and XMLParser with the default implementation.
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @navassoc - instanciate - vib.core.util.xml.XMLTree
 * @navassoc - instanciate - vib.core.util.xml.XMLParser
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
        return new vib.core.util.xml.DefaultXMLTree(rootName, null);
    }

    /**
     * Creates and returns an instance of current implementation of XMLTree interface
     *
     * @param rootName the name of the root node
     * @param nameSpace the name space of the root node
     * @return an instance of XMLTree
     */
    public static XMLTree createTree(String rootName, String nameSpace){
        return new vib.core.util.xml.DefaultXMLTree(rootName, nameSpace);
    }

    /**
     * Creates and returns an instance of current implementation of XMLParser interface
     *
     * @return an instance of XMLParser
     */
    public static XMLParser createParser(){
        return new vib.core.util.xml.DefaultXMLParser();
    }

    /**
     * The name of a text node.
     */
    public static final String TEXT_NODE_NAME = "#text";
}
