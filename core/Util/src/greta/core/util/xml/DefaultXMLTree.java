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

import greta.core.util.log.Logs;
import java.io.FileWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.DOMImplementation;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.bootstrap.DOMImplementationRegistry;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;

/**
 * This class is an implementation of XMLTree interface.
 * @see greta.core.util.xml.XMLTree XMLTree
 * @author Andre-Marie Pez
 */
public class DefaultXMLTree implements XMLTree{

    private Node myNode;

    public DefaultXMLTree(String rootName, String namespace){
        try{
            myNode = DefaultXMLParser.factory.newDocumentBuilder().getDOMImplementation().createDocument(namespace, rootName, null).getDocumentElement();
        }
        catch (ParserConfigurationException ex){
            Logs.error("DefaultXMLTree constructor :\n"+ex);
        }

    }

    public DefaultXMLTree(Node node){
        myNode = node;
    }

    @Override
    public boolean isTextNode() {
        return myNode.getNodeType() == Node.TEXT_NODE;
    }

    /**
     * Checks if this node is an element node
     * @return {@code true} if this is an element node, {@code false} otherwise.
     */
    private boolean isElementNode() {
        return myNode.getNodeType() == Node.ELEMENT_NODE;
    }

    /**
     * Checks if this node is a document node
     * @return {@code true} if this is a document node, {@code false} otherwise.
     */
    private boolean isDocumentNode(){
        return myNode.getNodeType() == Node.DOCUMENT_NODE;
    }

    /**
     * Compares two names.<br/>
     * Names are not case sensitives.
     * @param name1 a first name
     * @param name2 a second name
     * @return {@code true} if the two names the sames, {@code false} otherwise.
     */
    private boolean namesEquals(String name1, String name2){
        if(name1==null){
            return name2==null;
        }
        return name1.equalsIgnoreCase(name2);
    }

    /**
     * Compares two namespaces.<br/>
     * Namespaces are case sensitives.
     * @param ns1 a first name
     * @param ns2 a second name
     * @return {@code true} if the two namespaces the sames, {@code false} otherwise.
     */
    private boolean nameSpacesEquals(String ns1, String ns2){
        if(ns1==null){
            return ns2==null;
        }
        return ns1.equals(ns2);
    }

    @Override
    public void save(String fileName) {
        FileWriter out;
        try {
            out = new FileWriter(fileName);

            out.write(new String(getRootNode().toString().getBytes("UTF-8")));
            out.close();
        }
        catch (IOException ex) {
            Logs.error("DefaultXMLTree.Save("+fileName+") :\n"+ex);
        }
    }

    @Override
    public XMLTree findNodeCalled(String name) {
        return findNodeCalled(name, null);
    }

    @Override
    public XMLTree findNodeCalled(String name, String nameSpace) {
        NodeList children = myNode.getChildNodes();
        for (int i=0; i<children.getLength(); ++i) {
            Node child = children.item(i);
            if(child.getNodeType() == Node.TEXT_NODE && name.equals(XML.TEXT_NODE_NAME)) {
                return new DefaultXMLTree(child);
            }
            if (child.getNodeType() == Node.ELEMENT_NODE &&
                namesEquals(name, child.getLocalName()) &&
                (nameSpace==null || nameSpacesEquals(nameSpace, child.getNamespaceURI()))) {
                return new DefaultXMLTree(child);
            }

        }
        return null;
    }

    @Override
    public int getNumberOfChildren() {
        return myNode.getChildNodes().getLength();
    }

    @Override
    public XMLTree getParent() {
        Node parent = myNode.getParentNode();
        return parent == null ? null : new DefaultXMLTree(myNode.getParentNode());
    }

    @Override
    public String getName() {
        return myNode.getNodeType() == Node.TEXT_NODE ?
            XML.TEXT_NODE_NAME :
            myNode.getLocalName();
    }

    @Override
    public String getNameSpace(){
        return myNode.getNodeType() == Node.TEXT_NODE ?
            getParent().getNameSpace() :
            myNode.getNamespaceURI();
    }

    @Override
    public String getTextValue() {
        if(isTextNode()) {
            return myNode.getNodeValue();
        }
        return null;
    }

    @Override
    public double getAttributeNumber(String name) {
        return Double.parseDouble(getAttribute(name));
    }

    @Override
    public double getAttributeNumber(String name, String nameSpace) {
        return Double.parseDouble(getAttribute(name, nameSpace));
    }

    @Override
    public String getAttribute(String name) {
        if(isElementNode()) {
            return ((Element)myNode).getAttribute(name);
        }
        return "";
    }

    @Override
    public String getAttribute(String name, String nameSpace){
        if(isElementNode()) {
            return ((Element)myNode).getAttributeNS(nameSpace, name);
        }
        return "";
    }

    @Override
    public boolean hasAttribute(String name) {
        return isElementNode() && ((Element)myNode).hasAttribute(name);
    }

    @Override
    public boolean hasAttribute(String name, String nameSpace) {
        return isElementNode() && ((Element)myNode).hasAttributeNS(nameSpace, name);
    }

    @Override
    public void setTextValue(String text) throws Exception {
        if(isTextNode()) {
            myNode.setNodeValue(text);
        }
        else {
            throw new Exception("Can't set a text in this node");
        }
    }

    @Override
    public void setAttribute(String name, String value) {
        if(name.startsWith("xmlns:")){
            setAttribute(name, "http://www.w3.org/2000/xmlns/", value);
        }
        else{
            if(isElementNode()) {
                if(value!=null){
                    ((Element)myNode).setAttribute(name, value);
                }
                else{
                    ((Element)myNode).removeAttribute(name);
                }
            }
        }
    }

    @Override
    public void setAttribute(String name, String nameSpace, String value) {
        if(isElementNode()) {
            if(value!=null){
                ((Element)myNode).setAttributeNS(nameSpace, name, value);
            }
            else{
                ((Element)myNode).removeAttributeNS(nameSpace, name);
            }
        }
    }

    @Override
    public XMLTree addChild(XMLTree child) {
        if(isTextNode()){
            Logs.warning("DefaultXMLTree.AddChild(XMLTree) : Can't add any child in a text node");
            return null;
        }
        if(child instanceof DefaultXMLTree){
            Node newChild = ((DefaultXMLTree)child).myNode.cloneNode(true);
            myNode.getOwnerDocument().adoptNode(newChild);
            myNode.appendChild(newChild);
            return new DefaultXMLTree(newChild);
        }
        else{
            Logs.warning("DefaultXMLTree.AddChild(XMLTree) : Can't add an instance of "+child.getClass().getName());
            return null;
        }
    }

    @Override
    public void addText(String text) {
        if(isElementNode()) {
            myNode.appendChild(myNode.getOwnerDocument().createTextNode(text));
        }
        else {
            Logs.warning("DefaultXMLTree.AddText(String) : Can't add a text in this node.");
        }
    }

    @Override
    public XMLTree createChild(String name) {
        return this.createChild(name, this.getNameSpace());
    }

    @Override
    public XMLTree createChild(String name, String namespace) {
        if(isElementNode() || isDocumentNode()){
            String prefix = myNode.lookupPrefix(namespace);
            if(prefix!=null && !prefix.isEmpty()){
                name = prefix+":"+name;
            }
            return new DefaultXMLTree(myNode.appendChild(myNode.getOwnerDocument().createElementNS(namespace, name)));
        }
        else{
            Logs.warning("DefaultXMLTree.createChild() : Can't add any child in this node.");
            return null;
        }
    }

    @Override
    public String toString(){
        return this.toString(true);
    }

    @Override
    public String toString(boolean prettyPrint){
        if(isTextNode()){
            return myNode.getNodeValue();
        }

        LSSerializer serializer;
        DOMImplementationLS domImplLS;
        try {
            DOMImplementation implementation = DOMImplementationRegistry.newInstance().getDOMImplementation("XML 3.0");
            domImplLS = (DOMImplementationLS) implementation.getFeature("LS", "3.0");
            serializer = domImplLS.createLSSerializer();
            serializer.getDomConfig().setParameter("format-pretty-print", prettyPrint);
        } catch (Exception e) {
            Logs.error(e.toString());
            return "";
        }
        LSOutput output = domImplLS.createLSOutput();
        output.setEncoding("UTF-8");
        StringWriter buf = new StringWriter();
        output.setCharacterStream(buf);
        serializer.write(myNode, output);
        return buf.toString();
    }

    @Override
    public XMLTree getRootNode() {
        if(isDocumentNode()) {
            return new DefaultXMLTree(((Document)myNode).getDocumentElement());
        }
        return new DefaultXMLTree(myNode.getOwnerDocument().getDocumentElement());
    }

    @Override
    public List<XMLTree> getChildren() {
        List<XMLTree> listToReturn = new ArrayList<XMLTree>();
        NodeList children = myNode.getChildNodes();
        for(int i=0;i<children.getLength();++i) {
            listToReturn.add(new DefaultXMLTree(children.item(i)));
        }
        return listToReturn;
    }

    @Override
    public List<XMLTree> getChildrenElement() {
        List<XMLTree> listToReturn = new ArrayList<XMLTree>();
        NodeList children = myNode.getChildNodes();
        for(int i=0;i<children.getLength();++i) {
            if(children.item(i).getNodeType() == Node.ELEMENT_NODE) {
                listToReturn.add(new DefaultXMLTree(children.item(i)));
            }
        }
        return listToReturn;
    }

    @Override
    public List<XMLTree> getChildrenText() {
        List<XMLTree> listToReturn = new ArrayList<XMLTree>();
        NodeList children = myNode.getChildNodes();
        for(int i=0;i<children.getLength();++i) {
            if(children.item(i).getNodeType() == Node.TEXT_NODE) {
                listToReturn.add(new DefaultXMLTree(children.item(i)));
            }
        }
        return listToReturn;
    }

    @Override
    public boolean isNamed(String name){
        return namesEquals(getName(), name);
    }

    @Override
    public boolean isNameSpaced(String nameSpace){
        return nameSpacesEquals(getNameSpace(), nameSpace);
    }

    /**
     *
     * @param obj
     * @return
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || myNode == null) {
            return false;
        }
        if (obj instanceof DefaultXMLTree == false) {
            return false;
        }
        return myNode.equals(((DefaultXMLTree) obj).myNode);
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 55 * hash + (this.myNode != null ? this.myNode.hashCode() : 0);
        return hash;
    }

    @Override
    public XMLTree removeChild(XMLTree child){
        if(child instanceof DefaultXMLTree){
            myNode.removeChild(((DefaultXMLTree)child).myNode);
        }
        return child;
    }

    @Override
    public XMLTree removeChild(int index){
        return removeChild(getChildren().get(index));
    }

    @Override
    public XMLTree removeChildElement(int index){
        return removeChild(getChildrenElement().get(index));
    }

    @Override
    public ListIterator<XMLTree> getIterator(){
        return new Iterator(getChildren());
    }

    @Override
    public ListIterator<XMLTree> getElementIterator(){
        return new Iterator(getChildrenElement());
    }

    @Override
    public ListIterator<XMLTree> getTextIterator(){
        return new Iterator(getChildrenText());
    }

    private class Iterator implements ListIterator<XMLTree>{

        ListIterator<XMLTree> children;
        XMLTree current;

        Iterator(List<XMLTree> childrenToIterate){
            children = childrenToIterate.listIterator();
        }

        @Override
        public boolean hasNext() {
            return children.hasNext();
        }

        @Override
        public XMLTree next() {
            current = children.next();
            return current;
        }

        @Override
        public boolean hasPrevious() {
            return children.hasPrevious();
        }

        @Override
        public XMLTree previous() {
            current = children.previous();
            return current;
        }

        @Override
        public int nextIndex() {
            return children.nextIndex();
        }

        @Override
        public int previousIndex() {
            return children.previousIndex();
        }

        @Override
        public void remove() {
            children.remove();
            DefaultXMLTree.this.removeChild(current);
        }

        @Override
        public void set(XMLTree e) {
            if(e instanceof DefaultXMLTree){
                children.set(e);
                Node newChild = ((DefaultXMLTree)e).myNode.cloneNode(true);
                myNode.getOwnerDocument().adoptNode(newChild);
                myNode.replaceChild(newChild, ((DefaultXMLTree)current).myNode);
                ((DefaultXMLTree)e).myNode = newChild;
                current = e;
            }
            else{
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }

        @Override
        public void add(XMLTree e) {
            if(e instanceof DefaultXMLTree){
                children.add(e);
                Node newChild = ((DefaultXMLTree)e).myNode.cloneNode(true);
                myNode.getOwnerDocument().adoptNode(newChild);
                ((DefaultXMLTree)e).myNode = newChild;
                if(! hasNext()){
                    myNode.appendChild(newChild);
                }
                else{
                    if(current==null){
                        myNode.insertBefore(newChild, myNode.getFirstChild());
                    }
                    else{
                        myNode.insertBefore(newChild, ((DefaultXMLTree)current).myNode.getNextSibling());
                    }
                }
                current = e;
            }
            else{
                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
            }
        }

    }
}
