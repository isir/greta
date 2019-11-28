/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package vib.core.util.xml;

import vib.core.util.log.Logs;
import java.io.File;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * This class is an implementation of XMLParser interface.
 * @see vib.core.util.xml.XMLParser XMLParser
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @navassoc - use - vib.core.util.xml.DefaultXMLErrorHandler
 * @navassoc - generate - vib.core.util.xml.DefaultXMLTree
 */
public class DefaultXMLParser implements XMLParser{

    protected static DocumentBuilderFactory factory;
    private static Map<String,Schema> schemas;
    private static ErrorHandler handler;
    private DocumentBuilder parser;
    private boolean val;

    static {
        factory = DocumentBuilderFactory.newInstance();
        factory.setExpandEntityReferences(true);
        factory.setNamespaceAware(true);
        schemas = new HashMap<String, Schema>();
        handler = new DefaultXMLErrorHandler();
    }

    public DefaultXMLParser(){
        parser = null;
        val = true;
    }

    private void instanciateParser(Schema schema) throws ParserConfigurationException{
        factory.setSchema(schema);
        //factory.setFeature("http://apache.org/xml/features/validation/schema",schema!=null);
        factory.setValidating(val && schema==null); // validation is done by the schema
        parser = factory.newDocumentBuilder();
        parser.setErrorHandler(handler);
    }

    private Schema getSchema(String filename){
        Schema schem = schemas.get(filename);
        if(schem == null){
            try {
                schem = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI).newSchema(new File(filename));
            }
            catch (SAXException ex) {
                Logs.error("DefaultXMLParser.getSchema("+filename+") :\n"+ex.toString());
            }
            schemas.put(filename, schem);
        }
        return schem;
    }

    public void setValidating(boolean validating){
        val = validating;
    }

    public XMLTree parseFile(String filename) {
        try {
            instanciateParser(null);
            return new DefaultXMLTree(parser.parse(new File(filename)).getDocumentElement());
        }
        catch (Exception ex) {
            Logs.error("DefaultXMLParser.parseFile("+filename+") :\n"+ex.toString());
        }
        return null;
    }

    public XMLTree parseFileWithXSD(String filename, String filenameXSD) {
        try {
            instanciateParser(getSchema(filenameXSD));
            return new DefaultXMLTree(parser.parse(new File(filename)).getDocumentElement());
        }
        catch (Exception ex) {
            Logs.error("DefaultXMLParser.parseFileWithXSD("+filename+", "+filenameXSD+") :\n"+ex.toString());
        }
        return null;
    }

    public XMLTree parseBuffer(String buffer) {
        try {
            instanciateParser(null);
            return new DefaultXMLTree(parser.parse(new InputSource(new StringReader(buffer))).getDocumentElement());
        }
        catch (Exception ex) {
            Logs.error("DefaultXMLParser.parseBuffer(\"...\") :\n"+ex.toString());
        }
        return null;
    }

    public XMLTree parseBufferWithXSD(String buffer, String filenameXSD) {
        try {
            instanciateParser(getSchema(filenameXSD));
            return new DefaultXMLTree(parser.parse(new InputSource(new StringReader(buffer))).getDocumentElement());
        }
        catch (Exception ex) {
            Logs.error("DefaultXMLParser.parseBufferWithXSD(\"...\", "+filenameXSD+") :\n"+ex.toString());
        }
        return null;
    }

}
