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
import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * This class defines the behavior to use when an error occurs in the parsing of an XML document.
 * @see greta.core.util.xml.DefaultXMLParser
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
