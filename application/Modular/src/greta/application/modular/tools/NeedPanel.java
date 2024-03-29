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
package greta.application.modular.tools;

import greta.application.modular.compilation.ZipMaker;
import greta.core.util.IniManager;
import greta.core.util.xml.XMLTree;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Andre-Marie Pez
 */
public class NeedPanel extends javax.swing.JPanel {

    XMLTree need;
    LibEditor editor;

    /**
     * Creates new form NeedPanel
     */
    public NeedPanel() {
        initComponents();

        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateNeed();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateNeed();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateNeed();
            }

        });
    }

    public NeedPanel(XMLTree need, LibEditor editor){
        this();
        setNeed(need);
        this.editor = editor;
    }

    public void setNeed(XMLTree need){
        this.need = need;
        jTextField1.setText(need.getAttribute("path"));
    }

    private void updateNeed(){
        if(need != null){
            need.setAttribute("path", jTextField1.getText());
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jFileChooser1 = new javax.swing.JFileChooser();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.choose");
        jButton2 = new javax.swing.JButton();
        jButton3 = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.delete");

        jButton1.setText("Choose");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setText("Files");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Delete");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addComponent(jTextField1, javax.swing.GroupLayout.DEFAULT_SIZE, 35, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton3))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addComponent(jButton1)
                .addComponent(jButton2)
                .addComponent(jButton3))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        ArrayList<String> regexPath = new ArrayList<String>(1);
        regexPath.add(jTextField1.getText());
        List<String> files = ZipMaker.getAllFilesToAdd(regexPath);
        JList l = new JList(files.toArray());

        JScrollPane sp = new JScrollPane();
        sp.setViewportView(l);

        JPanel p = new JPanel();
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(p);
        p.setLayout(layout);

        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sp)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(sp, javax.swing.GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
        );
        JOptionPane.showMessageDialog(this, p, "corresponding files", JOptionPane.PLAIN_MESSAGE);
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        File f = new File(jTextField1.getText());
        if(f.exists()) {
            jFileChooser1.setCurrentDirectory(f);
            if(f.isFile()){
                jFileChooser1.setSelectedFile(f);
            }
            else{
                jFileChooser1.setSelectedFile(null);
            }
        }
        else{
            while(f.getParent()!=null && !f.exists()){
                f = f.getParentFile();
            }
            if(f.exists()){
                jFileChooser1.setCurrentDirectory(f);
            } else {
                jFileChooser1.setCurrentDirectory(new File(IniManager.getProgramPath()));
            }
            jFileChooser1.setSelectedFile(null);
        }
        jFileChooser1.updateUI();
        if(jFileChooser1.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION){
            if(jFileChooser1.getSelectedFile() != null){
                String selectedPath = (new File(IniManager.getProgramPath())).toURI().relativize(jFileChooser1.getSelectedFile().toURI()).getPath();
                jTextField1.setText(selectedPath);
            }
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if(need != null){
            need.getParent().removeChild(need);
        }
        if(this.editor != null){
            editor.updateNeedsList();
        }
    }//GEN-LAST:event_jButton3ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
