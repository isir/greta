/*
 * This file is a part of the Modular application.
 */

package vib.application.modular;

import java.io.File;
import javax.swing.filechooser.FileFilter;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;

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
