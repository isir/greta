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
 * This interface describes a generic XML parser object.<br/>
 * It can parse an XML document to return an XML tree.
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @depend - generate - greta.core.util.xml.XMLTree
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
     * @param fileName the name of the file
     * @return the root of the XML tree
     */
    public XMLTree parseFile(String fileName);

    /**
     * Parses a specific XML file using an XML schema file for validation, builds the tree and returns the root of the tree.<br/>
     * If the parsing fails (the file does't exist, validation fails...), it returns null.
     * @param fileName the name of the file
     * @param fileNameXSD the name of the XML schema file
     * @return the root of the XML tree
     */
    public XMLTree parseFileWithXSD(String fileName, String fileNameXSD);

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
     * @param fileNameXSD the name of the XML schema file
     * @return the root of the XML tree
     */
    public XMLTree parseBufferWithXSD(String buffer, String fileNameXSD);

}
