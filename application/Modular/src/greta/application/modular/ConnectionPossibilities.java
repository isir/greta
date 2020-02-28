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
package greta.application.modular;

import greta.application.modular.modules.Connector;
import greta.application.modular.modules.ModuleFactory;
import greta.application.modular.modules.ModuleFactory.ModuleInfo;
import greta.application.modular.modules.Style;
import greta.core.util.IniManager;
import java.io.Reader;
import java.io.StringReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Element;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.StyleSheet;

/**
 *
 * @author Andre-Marie Pez
 */
public class ConnectionPossibilities extends javax.swing.JDialog {
    String LocaleParam;
    private class HTMLDoc extends HTMLDocument{
        public void publicWriteLock(){
            writeLock();
        }
        public void publicWriteUnlock(){
            writeUnlock();
        }
    }
    private HTMLDoc doc;
    private HTMLDocument emptyDoc;
    /** Creates new form ConnectionPossibilities */
    public ConnectionPossibilities(java.awt.Frame parent, boolean modal, String title){
        super(parent, modal);
        LocaleParam=title;
        this.setTitle(IniManager.getLocaleProperty(LocaleParam));
        initComponents();
        jTextArea1.setContentType("text/html");

        Reader stringReader = new StringReader("<html><head/><body><ul id=\"content\" style=\"list-style-type: none;margin: 0;margin-left: 10;padding: 0\" /></body></html>");
        HTMLEditorKit htmlKit = new HTMLEditorKit();
        doc = new HTMLDoc();
        StyleSheet styleSheet = doc.getStyleSheet();
        styleSheet.addRule(".hidden {font-size: 0px;}");
        styleSheet.addRule(".shown {}");
        emptyDoc = (HTMLDocument)htmlKit.createDefaultDocument();
        doc.setParser(emptyDoc.getParser());
        doc.setAsynchronousLoadPriority(4);
        doc.setTokenThreshold(100);
        try {
            htmlKit.read(stringReader, doc, 0);
        } catch (Exception ex) {}
        buildConnectionPossibilities(doc);
        jTextArea1.setDocument(doc);
        jTextArea1.setBackground(parent.getBackground());
        jTextField1.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                showLinesContaining(jTextField1.getText(), true);
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                showLinesContaining(jTextField1.getText(), false);
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });
    }

    private void buildConnectionPossibilities(HTMLDocument doc) {

        Collections.sort(ModuleFactory.moduleInfos, new Comparator<ModuleInfo>(){
            @Override
            public int compare(ModuleInfo o1, ModuleInfo o2) {
                return o1.name.compareToIgnoreCase(o2.name);
            }
        });

        for(ModuleInfo mIn : ModuleFactory.moduleInfos){
            for(ModuleInfo mOut : ModuleFactory.moduleInfos){
                if(mIn!=mOut){
                    for (Connector connector : Connector.connectors) {
                        if (connector.getInClass().isAssignableFrom(mIn.objectClass) &&
                            connector.getOutClass().isAssignableFrom(mOut.objectClass)) {

                            Element e = doc.getElement("content");
                            try {
                                doc.insertBeforeEnd(e,
                                         "<li class=\"shown\"><span "+getColor(mIn.style)+">"
                                        + mIn.name + "</span>"
                                        + " \u2192 "
                                        + "<span "+getColor(mOut.style)+">"
                                        + mOut.name + "</span>"
                                        +"</li>\n"
                                );
                            } catch (Exception ex) {ex.printStackTrace();}
                            break;
                        }
                    }
                }
            }
        }
    }

    private String getColor(String styleName){
        String vertexStyleName = Style.getMapper("Original").getVertexStyle(styleName);
        for(Style style : Style.getAllStyles()){
            if(style.getName().equals(vertexStyleName)){
                return "style=\"background-color: #"+Integer.toHexString(Integer.decode((String)style.getMap().get(com.mxgraph.util.mxConstants.STYLE_FILLCOLOR)))+"\"";
            }
        }
        return "";
    }

    @Override
    public void setLocale(Locale l) {
        super.setLocale(l);
        this.setTitle(IniManager.getLocaleProperty(LocaleParam));
    }


    private void showLinesContaining(String content, boolean add) {
        String lowerContent = content.toLowerCase();
        Element root = doc.getDefaultRootElement();
        for (int i = 0; i < root.getElementCount(); i++) {
            Element e = root.getElement(i);
            if (e.getName().equals("body")) {
                for (int j = 0; j < e.getElementCount(); j++) {
                    Element e2 = e.getElement(j);
                    if (e2.getName().equals("ul")) {
                        for (int k = 0; k < e2.getElementCount(); k++) {
                            Element e3 = e2.getElement(k);
                            if (e3.getName().equals("li") && e3.getAttributes() instanceof HTMLDocument.BlockElement) {
                                HTMLDocument.BlockElement e4 = (HTMLDocument.BlockElement) e3;
                                boolean shown = e4.containsAttribute(HTML.Attribute.CLASS, "shown");
                                if(add == shown){
                                    String text = "";
                                    try {
                                        text = doc.getText(e3.getStartOffset(), e3.getEndOffset() - e3.getStartOffset()).toLowerCase();
                                    } catch (Exception ex) {}
                                    doc.publicWriteLock();
                                    if (text.contains(lowerContent)) {
                                        e4.addAttribute(HTML.Attribute.CLASS, "shown");
                                    } else {
                                        e4.addAttribute(HTML.Attribute.CLASS, "hidden");
                                    }
                                    doc.publicWriteUnlock();
                                }
                            }
                        }
                    }
                }
            }
        }
        jTextArea1.setDocument(emptyDoc);
        jTextArea1.setDocument(doc);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextPane();
        jTextField1 = new javax.swing.JTextField();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        jTextArea1.setEditable(false);
        jScrollPane1.setViewportView(jTextArea1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 400, Short.MAX_VALUE)
            .addComponent(jTextField1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 274, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextPane jTextArea1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables

}
