/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.xml;

import vib.core.util.log.Logs;
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class defines the behavior to use when an error occurs in the parsing of an XML document.
 * @see vib.core.util.xml.DefaultXMLParser
 * @author Andre-Marie Pez
 */
public class DefaultXMLErrorHandler implements ErrorHandler{

    public void warning(SAXParseException e) throws SAXException {
        Logs.warning("XML : "+(e.getSystemId()==null?"":e.getSystemId()+" ")+"in line "+e.getLineNumber()+": "+e.getMessage());
    }

    public void error(SAXParseException e) throws SAXException {
        Logs.error("XML : "+(e.getSystemId()==null?"":e.getSystemId()+" ")+"in line "+e.getLineNumber()+": "+e.getMessage());
        //throw e;
    }

    public void fatalError(SAXParseException e) throws SAXException {
        Logs.error("XML : fatal error "+(e.getSystemId()==null?"":e.getSystemId()+" ")+"in line "+e.getLineNumber()+": "+e.getMessage());
        throw e;
    }

}
