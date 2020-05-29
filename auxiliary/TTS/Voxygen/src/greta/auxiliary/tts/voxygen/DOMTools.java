/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.tts.voxygen;

import greta.core.util.log.Logs;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 *
 * @author montcheuil
 */


public class DOMTools {

    public static Document parseString(String xml) {
        // See http://www.mkyong.com/java/how-to-read-xml-file-in-java-dom-parser/
        DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
        dbFactory.setNamespaceAware(true); // (!) SEE https://stackoverflow.com/questions/42562866/why-does-getlocalname-return-null
        DocumentBuilder dBuilder;
        try {
            dBuilder = dbFactory.newDocumentBuilder(); // throw ParserConfigurationException
            Document doc = dBuilder.parse( new InputSource( new StringReader( xml ) )); // throw SAXException, IOException
            doc.getDocumentElement().normalize(); // see 
            return doc; // https://stackoverflow.com/questions/13786607/normalization-in-dom-parsing-with-java-how-does-it-work
        } catch (ParserConfigurationException ex) {
            //Logger.getLogger(DOMTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SAXException ex) {
            //Logger.getLogger(DOMTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            //Logger.getLogger(DOMTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
 
    private static TransformerFactory tf = TransformerFactory.newInstance();
    //private static Transformer liteTransformer = null, prettyPrintTransformer = null;
    private static Transformer[] transformers = {null, null, null, null};
    
    protected static Transformer getTransformer(boolean prettyPrint, boolean noXmlDecl) throws TransformerConfigurationException
    {
        int idx = prettyPrint ? 1 : 0;
        if (noXmlDecl) idx+=2;
        if (transformers[idx] ==null) {
            transformers[idx] = tf.newTransformer();
            transformers[idx].setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, noXmlDecl ? "yes" : "no");
            transformers[idx].setOutputProperty(OutputKeys.METHOD, "xml");
            transformers[idx].setOutputProperty(OutputKeys.INDENT, prettyPrint ? "yes" : "no");
            transformers[idx].setOutputProperty(OutputKeys.ENCODING, "UTF-8");
            transformers[idx].setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        }
        return transformers[idx];
    }
    
    // See https://stackoverflow.com/questions/2325388/what-is-the-shortest-way-to-pretty-print-a-org-w3c-dom-document-to-stdout
    public static void printDocument(Document doc, Writer out, boolean prettyPrint)
            throws IOException, TransformerException
    {
        Transformer transformer = getTransformer(prettyPrint, false);
        transformer.transform(new DOMSource(doc),
                new StreamResult(out));
    }
    
    public static String toXMLString(Document doc, boolean prettyPrint) {
        StringWriter sw = new StringWriter();
        
        try {
            printDocument(doc, sw, prettyPrint);
        } catch (IOException ex) {
            //Logger.getLogger(DOMTools.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TransformerException ex) {
            //Logger.getLogger(DOMTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sw.toString();
    }
    
    public static String toString(Node n, boolean prettyPrint) {
        StringWriter sw = new StringWriter();
        try {
            Transformer transformer = getTransformer(prettyPrint, true);
            transformer.transform(new DOMSource(n), new StreamResult(sw));
        } catch (TransformerException ex) {
            //Logger.getLogger(DOMTools.class.getName()).log(Level.SEVERE, null, ex);
        }
        return sw.toString();
    }
    
    
    /**
     * Replace a node by a new node.
     * @param src   the node to replace
     * @param name  the name of the new node
     * @param namespace the namespace of the new node
     * @param moveChildren  move src children to the new node
     * @return the new node
     */
    public static Element replaceNode(Element src, String name, String namespace, boolean moveChildren) {
        Document doc = src.getOwnerDocument();
        Node parent = src.getParentNode();
        // (a) create the new node
        Element repl = namespace==null ? doc.createElement(name) : //TMP: avoid xmlns="" attribute
                doc.createElementNS(namespace, name);
        // (c) move src children
        if (moveChildren) {
            NodeList children = src.getChildNodes();
            int nChildren = children.getLength();
            for (int i=0; i<nChildren; ++i) {
                repl.appendChild(children.item(i));
            }
        }        
        // (b) replace src by repl
        parent.replaceChild(repl, src);
        return repl;
    }

    /**
     * Look for node(s) of a specific name
     *  and insert one if required.
     * The created node become the only father's child, previous father's children become new node's children.
     * @param father    the father node
     * @param name  the node name
     * @param processor (optional) a function that take 2 arguments (the current node and a stack of ascendants with same name)
     *  and return a node
     * @param recursive    if true, look for all descendants
     * @return the node or <code>null</code>
     */
    public static List<Element> processNamedNodes(Element father, String name, BiFunction<Element, List<Element>, Element> processor, boolean recursive) {
        List<Element> results = new ArrayList<Element>();
        LinkedList<Element> ascendants = new LinkedList<Element>();
        processNamedNodesRec(father, name, processor, recursive, results, ascendants);
        return results;
    }
    
    /**
     * Recursive method for {@link #processNamedNodes(greta.core.util.xml.XMLTree, java.lang.String, java.util.function.BiFunction, boolean)}
     *
     */
    private static void processNamedNodesRec(Element father, String name, BiFunction<Element, List<Element>, Element> processor, boolean recursive, List<Element> results, LinkedList<Element> ascendants) {
        // Add the father in the ascendant
        if (name.equals(father.getLocalName())) {
            ascendants.push(father);
        }
        // search a <voice> child
        NodeList children = father.getChildNodes();
        int nChildren = children.getLength();
        for (int i=0; i<nChildren; ++i) {
            Node n = children.item(i);
            if (n instanceof Element) {
                Element c = (Element) n;
                boolean isName = name.equals(c.getLocalName());
                if (isName) {
                    // add processor(c,ascendant) to the results
                    results.add(processor == null ? c : processor.apply(c, ascendants)); // the ascendants <name> or null
                    ascendants.push(c);
                }
                // recursive call
                if (recursive) {
                    processNamedNodesRec(c, name, processor, recursive, results, ascendants);
                }
                // update ascendants stack
                if (isName) {
                    ascendants.pop();
                }
            }
        }
    }

    /**
     * Look for the 1st child of a specific name in the node children
     *  and insert one if required.
     * The created node become the only father's child, previous father's children become new node's children.
     * @param father    the father node
     * @param name  the node name
     * @param create    if true, create a node
     * @return the node or <code>null</code>
     */
    public static Element findCreateNode(Element father, String name, boolean create) {
        // search a <name> child
        NodeList children = father.getChildNodes();
        int nChildren = children.getLength();
        for (int i=0; i<nChildren; ++i) {
            Node n = children.item(i);
            if (n instanceof Element) {
                Element c = (Element) n;
            if (name.equals(c.getNodeName())) {
                return c;
            }
            }
        }
        //TODO: recursive
        if (!create) {
            return null;
        }
        Element node = father.getOwnerDocument().createElement(name);
        //if (insertPos==-2)
        {
            // Move children into the new node
            for (int i=0; i<nChildren; ++i) {
                Node n = children.item(i);
                node.appendChild(n);
            }
            // and node into father
            father.appendChild(node);
        }
        return node;
    }

    public static StringBuilder printNodes(StringBuilder sb, NodeList nodeList) {
        if (sb == null) {
            sb = new StringBuilder();
        }
        for (int count = 0; count < nodeList.getLength(); count++) {
            Node tempNode = nodeList.item(count);
            // make sure it's element node.
            if (tempNode.getNodeType() == Node.ELEMENT_NODE) {
                // get node name and value
                sb.append("\nNode Name =" + tempNode.getNodeName() + " [OPEN]\n");
                sb.append("Node Value =" + tempNode.getTextContent()+"\n");
                if (tempNode.hasAttributes()) {
                    // get attributes names and values
                    NamedNodeMap nodeMap = tempNode.getAttributes();
                    for (int i = 0; i < nodeMap.getLength(); i++) {
                        Node node = nodeMap.item(i);
                        sb.append("attr name : " + node.getNodeName()+"\n");
                        sb.append("attr value : " + node.getNodeValue()+"\n");
                    }
                }
                if (tempNode.hasChildNodes()) {
                    // loop again if has child nodes
                    printNodes(sb, tempNode.getChildNodes());
                }
                sb.append("Node Name =" + tempNode.getNodeName() + " [CLOSE]\n");
            }
        }
        return sb;
    }

    
}
