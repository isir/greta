/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors;

import java.io.File;

/**
 *
 * @author Andre-Marie
 */
public class XMLFileChooser extends javax.swing.filechooser.FileFilter{

    @Override
    public boolean accept(File f) {
        return f.getName().endsWith(".xml") || f.isDirectory();
    }

    @Override
    public String getDescription() {
        return "XML files";
    }

}
