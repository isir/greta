/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.xml;

/**
 * This interface describes a generic XML parser object.<br/>
 * It can parse an XML document to return an XML tree.
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @depend - generate - vib.core.util.xml.XMLTree
 */
public interface XMLParser {

    /**
     * Activates or desactivates the validation in the parsing of an XML.<br/>
     * Set {@code true} to Activate the validation, {@code false} to desactivate.<br/>
     * The default value is {@code true}.
     * @param validating activate/desactivate the validation
     */
    public void setValidating(boolean validating);

    /**
     * Parses a specific XML file, builds the tree and returns the root of the tree.<br/>
     * If the parsing fails (the file does't exist, validation fails...), it returns null.
     * @param filename the name of the file
     * @return the root of the XML tree
     */
    public XMLTree parseFile(String filename);

    /**
     * Parses a specific XML file using an XML schema file for validation, builds the tree and returns the root of the tree.<br/>
     * If the parsing fails (the file does't exist, validation fails...), it returns null.
     * @param filename the name of the file
     * @param filenameXSD the name of the XML schema file
     * @return the root of the XML tree
     */
    public XMLTree parseFileWithXSD(String filename, String filenameXSD);

    /**
     * Parses an XML content, builds the tree and returns the root of the tree.<br/>
     * If the parsing fails (the file does't exist, validation fails...), it returns null.
     * @param buffer the XML content
     * @return the root of the XML tree
     */
    public XMLTree parseBuffer(String buffer);

    /**
     * Parses an XML content using an XML schema file for validation, builds the tree and returns the root of the tree.<br/>
     * If the parsing fails (the file does't exist, validation fails...), it returns null.
     * @param buffer the XML content
     * @param filenameXSD the name of the XML schema file
     * @return the root of the XML tree
     */
    public XMLTree parseBufferWithXSD(String buffer, String filenameXSD);

}
