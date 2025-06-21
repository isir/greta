/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
