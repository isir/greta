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
package greta.application.modular;

import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.File;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Andre-Marie Pez
 */
public class ModularXMLFileFilter extends FileFilter{

    private XMLParser parser;
    private static String markup = "modulated";
    private static String description = "Modular (xml)";
    private static String xml = ".xml";

    public ModularXMLFileFilter(){
        parser = XML.createParser();
        parser.setValidating(false);
    }

    @Override
    public boolean accept(File f) {
        if(f.isDirectory()) {
            return true;
        }
        if(! f.getName().toLowerCase().endsWith(xml)) {
            return false;
        }
        XMLTree xmltree = parser.parseFile(f.getAbsolutePath());
        return xmltree!=null && xmltree.getName().equalsIgnoreCase(markup);
    }

    @Override
    public String getDescription() {
        return description;
    }

}
