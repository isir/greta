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

import java.util.List;
import java.util.ListIterator;

/**
 * This interface describes a generic XML tree object.<br/>
 * It can be the result of the parsing of an XML document<br/>
 * or can be construct to create an XML document.<br/>
 * @author Andre-Marie Pez
 */
public interface XMLTree {

    /**
     * Checks if this node is a text node
     * @return {@code true} if this is a text node, {@code false} otherwise.
     */
    public boolean isTextNode();

    /**
     * saves the XML content in file
     * @param fileName the name of the target file
     */
    public void save(String fileName);

    /**
     * Returns the root of the tree that contains this node
     * @return the root of the tree
     */
    public XMLTree getRootNode();

    /**
     * Returns the node called with some name.
     * @param name the name of the wanted node
     * @return the wanted node or null if not found
     */
    public XMLTree findNodeCalled(String name);

    /**
     * Returns the node called with some name and namespace.
     * @param name the name of the wanted node
     * @param nameSpace the namespace of the wanted node
     * @return the wanted node or null if not found
     */
    public XMLTree findNodeCalled(String name, String nameSpace);


    /**
     * Returns number of children node of this node.
     * @return number of children
     */
    public int getNumberOfChildren();

    /**
     * Returns the list of all children of this node.
     * @return the list of all children
     */
    public List<XMLTree> getChildren();

    /**
     * Returns the list of all child elements of this node.
     * @return the list of all child elements
     */
    public List<XMLTree> getChildrenElement();

    /**
     * Returns the list of all child texts of this node.
     * @return the list of all child texts
     */
    public List<XMLTree> getChildrenText();

    /**
     * Returns the parent of this.<br/>
     * if this has no parent, it returns null
     * @return the parent node
     */
    public XMLTree getParent();

    /**
     * returns the name of the node.<br/>
     * if this is a text node, it return the string {@code XML.TEXT_NODE_NAME}.
     * @return the name of the node
     */
    public String getName();

    /**
     * Checks if this {@code XMLTree} is named as the specified {@code String}.<br/>
     * this a convenient method for {@code getName().equalsIgnoreCase(name)}
     * @param name the name to check.
     * @return {@code true} if the names match, {@code false} otherwise.
     */
    public boolean isNamed(String name);

    /**
     * returns the namespace of the node.<br/>
     * if this is a text node, namespace of this parent.
     * @return the namespace of the node
     */
    public String getNameSpace();

    /**
     * Checks if the namaspace of this {@code XMLTree} equals to the specified {@code String}.<br/>
     * this a convenient method for {@code getNameSpace().equals(name)}
     * @param nameSpace the namespace to check.
     * @return {@code true} if the namespaces match, {@code false} otherwise.
     */
    public boolean isNameSpaced(String nameSpace);

    /**
     * returns the text content only if this is a txt node<br/>
     * else, returns null.
     * @return text content
     */
    public String getTextValue();

    /**
     * returns the double value of a specified attribute.
     * @param name the name of the attribute
     * @return the double value
     */
    public double getAttributeNumber(String name);

    /**
     * returns the double value of a specified attribute.
     * @param name the name of the attribute
     * @param nameSpace the namespace of the attribute
     * @return the double value
     */
    public double getAttributeNumber(String name, String nameSpace);

    /**
     * returns the value of a specified attribute.
     * @param name the name of the attribute
     * @return the string value
     */
    public String getAttribute(String name);

    /**
     * returns the value of a specified attribute.
     * @param name the name of the attribute
     * @param nameSpace the namespace of the attribute
     * @return the string value
     */
    public String getAttribute(String name, String nameSpace);

    /**
     * Checks if an attribute is present.
     * @param name the name of the attribute
     * @return {@code true} if the attribute exist, {@code false} otherwise.
     */
    public boolean hasAttribute(String name);

    /**
     * Checks if an attribute is present.
     * @param name the name of the attribute
     * @param nameSpace the namespace of the attribute
     * @return {@code true} if the attribute exist, {@code false} otherwise.
     */
    public boolean hasAttribute(String name, String nameSpace);

    /**
     * Returns the tree in XML text format
     * @return the tree in XML text format
     */
    @Override
    public String toString();

    /**
     * Sets a text only if this is a text node.
     *
     * @param text the text to set
     * @throws Exception if this is not a text node
     */
    public void setTextValue(String text) throws Exception ;

    /**
     * Changes the value of an attribute.<br/>
     * If the attribute does not exist, the function creates and adds it.
     * @param name the name of the attribute
     * @param value the new value to set
     */
    public void setAttribute(String name, String value);

    /**
     * Changes the value of an attribute.<br/>
     * If the attribute does not exist, the function creates and adds it.
     * @param name the name of the attribute
     * @param nameSpace the namespace of the attribute
     * @param value the new value to set
     */
    public void setAttribute(String name, String nameSpace, String value);

    /**
     * Copies and adds an existing tree as a child of this node.
     * @param child the tree to copy and add
     * @return the new child
     */
    public XMLTree addChild(XMLTree child);

    /**
     * Creates a text node as child.
     * @param text the text content
     */
    public void addText(String text);

    /**
     * Creates and returns a child with the specified name<br/>
     * The child's namespace should be the namespace of this.
     * @param name the name of the new child
     * @return the new child
     */
    public XMLTree createChild(String name);

    /**
     * Creates and returns a child with the specified name an namespace.<br/>
     * @param name the name of the new child
     * @param namespace the namespace off the child
     * @return the new child
     */
    public XMLTree createChild(String name, String namespace);

    public ListIterator<XMLTree> getIterator();

    public ListIterator<XMLTree> getElementIterator();

    public ListIterator<XMLTree> getTextIterator();

    public XMLTree removeChild(XMLTree child);

    public XMLTree removeChild(int index);

    public XMLTree removeChildElement(int index);

    public String toString(boolean prettyPrint);
}
