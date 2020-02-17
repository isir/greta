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

import greta.application.modular.ModularXMLFile;
import greta.core.util.IniManager;
import greta.core.util.xml.XMLTree;
import java.awt.Component;
import java.io.File;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingWorker;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.filechooser.FileFilter;

/**
 *
 * @author Andre-Marie Pez
 */
public class LibEditor extends javax.swing.JPanel implements Updatable{


    LibListModel model  = new LibListModel();
    LibListModel.LibElement lib;

    /**
     * Creates new form LibEditor
     */
    public LibEditor() {
        initComponents();
        setName(IniManager.getLocaleProperty("modular.edit.lib"));
        jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if( ! jTextField1.hasFocus()){
                    LibListModel.LibElement newSelection = (LibListModel.LibElement)jList1.getSelectedValue();
                    if(newSelection != lib){
                        setCurrentLib(newSelection);
                    }
                }
            }
        });

        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateLibName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateLibName();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateLibName();
            }

        });

        jList1.setCellRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component toReturn = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null || ! (value instanceof LibListModel.LibElement)){
                    return toReturn;
                }
                LibListModel.LibElement element = (LibListModel.LibElement)value;
                boolean unicname = true;
                if(index > 0){
                    unicname = ! (model.getElementAt(index-1).getId().equals(element.getId()));
                }
                if(index < model.getSize()-1){
                    unicname = unicname && ! (model.getElementAt(index+1).getId().equals(element.getId()));
                }
                boolean valid = ModularXMLFile.checkOneLib(element.lib);
                toReturn.setForeground(valid ? (unicname ? toReturn.getForeground() : Colors.warning) : Colors.error);
                return toReturn;
            }

        });

        jTextField4.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                checkPath();
                updatePath();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkPath();
                updatePath();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkPath();
                updatePath();
            }

        });


        jFileChooser1.removeChoosableFileFilter(jFileChooser1.getAcceptAllFileFilter());
        jFileChooser1.setAcceptAllFileFilterUsed(false);
        jFileChooser1.addChoosableFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".jar");
            }

            @Override
            public String getDescription() {
                return "Jar File";
            }
        });

        setCurrentLib(null);
    }

    private void updateLibName() {
        if(lib != null){
            lib.setId(jTextField1.getText());
            jList1.setSelectedValue(lib, true);
        }
    }

    private void setCurrentLib(LibListModel.LibElement lib){
        this.lib = null;

        boolean enable = lib!=null;
        jLabel1.setEnabled(enable);
        jLabel2.setEnabled(enable);
        jTextField1.setEnabled(enable);
        jTextField4.setEnabled(enable);
        jButton1.setEnabled(enable);
        deleteLibButton.setEnabled(enable);
        jCheckBox1.setEnabled(enable);
        if(lib==null){
            jTextField1.setText("");
            jTextField4.setText("");
        }
        else{
            lib.updateNameChanger(jCheckBox1.isSelected());
            jTextField1.setText(lib.getId());
            jTextField4.setText(lib.getPath());
        }
        this.lib = lib;
        updateDependenciesList();
        updateNeedsList();
    }

    private void checkPath(){
        if(!(new File(jTextField4.getText())).exists()){
            jTextField4.setForeground(Colors.error);
        } else if(jTextField4.getText().toLowerCase().endsWith(".jar")){
            jTextField4.setForeground(Colors.normal);
        } else {
            jTextField4.setForeground(Colors.warning);
        }
    }
    private void updatePath() {
        if(lib!=null){
            lib.setPath(jTextField4.getText());
        }
    }

    @Override
    public void update() {
        jList1.clearSelection();
        setCurrentLib(null);
    }

    @Override
    public void reload() {
        jList1.setEnabled(false);
        update();
        model.reload();
        jList1.setEnabled(true);
    }


    public void updateDependenciesList(){
        boolean enable = lib!=null;

        jButton3.setEnabled(lib!=null && lib.exists() && lib.isJar());
        dependenciesPanel.setEnabled(enable);
        jButton2.setEnabled(enable);
        JPanel content = new JPanel();
        if(enable){
            LinkedList<DependencePanel> dependencies = new LinkedList<DependencePanel>();
            for(XMLTree child : lib.lib.getChildrenElement()){
                if(child.isNamed("depends")){
                    dependencies.add(new DependencePanel(child, this));
                }
            }
            if( ! dependencies.isEmpty()){
                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(content);
                content.setLayout(layout);
                ParallelGroup horizontal = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
                SequentialGroup vertical = layout.createSequentialGroup();
                for(DependencePanel depends : dependencies){
                    horizontal = horizontal.addComponent(depends, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
                    vertical = vertical.addComponent(depends, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
                }
                layout.setHorizontalGroup(horizontal);
                layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(vertical)
                );
            }
        }

        jScrollPane2.setViewportView(content);
    }

    public void updateNeedsList(){
        boolean enable = lib!=null;

        jButton4.setEnabled(enable);
        needsPanel.setEnabled(enable);
        JPanel content = new JPanel();
        if(enable){
            LinkedList<NeedPanel> needs = new LinkedList<NeedPanel>();
            for(XMLTree child : lib.lib.getChildrenElement()){
                if(child.isNamed("needs")){
                    NeedPanel need = new NeedPanel(child, this);
                    needs.add(need);
                }
            }
            if( ! needs.isEmpty()){
                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(content);
                content.setLayout(layout);
                ParallelGroup horizontal = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
                SequentialGroup vertical = layout.createSequentialGroup();
                for(NeedPanel need : needs){
                    horizontal = horizontal.addComponent(need, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
                    vertical = vertical.addComponent(need, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
                }
                layout.setHorizontalGroup(horizontal);
                layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(vertical)
                );
            }
        }

        jScrollPane3.setViewportView(content);
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
        newLibButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("modular.edit.lib.new");
        deleteLibButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("modular.edit.lib.delete");
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel2 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jButton1 = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.choose");
        dependenciesPanel = new javax.swing.JPanel();
        jButton2 = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.add");
        jButton3 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        needsPanel = new javax.swing.JPanel();
        jButton4 = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.add");
        jScrollPane3 = new javax.swing.JScrollPane();
        jCheckBox1 = new javax.swing.JCheckBox();

        newLibButton.setText("New");
        newLibButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newLibButtonActionPerformed(evt);
            }
        });

        deleteLibButton.setText("Delete");
        deleteLibButton.setToolTipText("");
        deleteLibButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteLibButtonActionPerformed(evt);
            }
        });

        jList1.setModel(model);
        jScrollPane1.setViewportView(jList1);

        jLabel1.setText("Id");

        jLabel2.setText("Path");

        jButton1.setText("Choose");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        dependenciesPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Dependencies"));

        jButton2.setText("Add");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jButton3.setText("Check");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jScrollPane2.setBorder(null);

        javax.swing.GroupLayout dependenciesPanelLayout = new javax.swing.GroupLayout(dependenciesPanel);
        dependenciesPanel.setLayout(dependenciesPanelLayout);
        dependenciesPanelLayout.setHorizontalGroup(
            dependenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dependenciesPanelLayout.createSequentialGroup()
                .addComponent(jButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jButton3))
            .addComponent(jScrollPane2)
        );
        dependenciesPanelLayout.setVerticalGroup(
            dependenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(dependenciesPanelLayout.createSequentialGroup()
                .addGroup(dependenciesPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 56, Short.MAX_VALUE))
        );

        needsPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Needs"));

        jButton4.setText("Add");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jScrollPane3.setBorder(null);

        javax.swing.GroupLayout needsPanelLayout = new javax.swing.GroupLayout(needsPanel);
        needsPanel.setLayout(needsPanelLayout);
        needsPanelLayout.setHorizontalGroup(
            needsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(needsPanelLayout.createSequentialGroup()
                .addComponent(jButton4)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.TRAILING)
        );
        needsPanelLayout.setVerticalGroup(
            needsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(needsPanelLayout.createSequentialGroup()
                .addComponent(jButton4)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 55, Short.MAX_VALUE))
        );

        jCheckBox1.setSelected(true);
        jCheckBox1.setText("Apply rename everywhere");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                        .addComponent(jLabel2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addGap(22, 22, 22)))
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField4)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton1))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jCheckBox1))))
                            .addComponent(dependenciesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(needsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newLibButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteLibButton)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newLibButton)
                    .addComponent(deleteLibButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jButton1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(dependenciesPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(needsPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newLibButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newLibButtonActionPerformed
        LibListModel.LibElement lib = model.createLib();
        jList1.setSelectedValue(lib, true);
        setCurrentLib(lib);
        jTextField1.requestFocusInWindow();
    }//GEN-LAST:event_newLibButtonActionPerformed

    private void deleteLibButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteLibButtonActionPerformed
        if(lib != null){
            model.deleteLib(lib);
        }
    }//GEN-LAST:event_deleteLibButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        File f = new File(jTextField4.getText());
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
                jTextField4.setText(selectedPath);
            }
        }

    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        if(lib!=null && lib.exists() && lib.isJar()){
            SwingWorker<List<String>, Void> task = new SwingWorker<List<String>, Void>(){

                @Override
                protected List<String> doInBackground() throws Exception {
                    ProgressMonitor progress = new ProgressMonitor(LibEditor.this, "Analyse "+lib.getId(), "", 0, 1);
                    progress.setMillisToPopup(0);
                    progress.setMillisToDecideToPopup(500);
                    List<String> errors = ToolBox.checkLibraryLoading(lib.lib, progress);
                    progress.close();
                    if (errors == null) {
                        JOptionPane.showMessageDialog(LibEditor.this, "No problem found", "Check Library", JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        String message = "Class not found :";
                        Collections.sort(errors);
                        int limit = 5;
                        int count = 0;
                        for (String err : errors) {
                            if (count >= limit) {
                                message += "\n" + (errors.size() - count) + " more...";
                                break;
                            } else {
                                message += "\n    " + err;
                            }
                            count++;
                        }
                        message += "\n\nDo you want to find missing dependencies?";
                        int doSearch = JOptionPane.showConfirmDialog(LibEditor.this, message, "Check Library", JOptionPane.YES_NO_OPTION, JOptionPane.ERROR_MESSAGE);

                        if(doSearch == JOptionPane.YES_OPTION){
                            progress.setMillisToPopup(0);
                            progress.setMillisToDecideToPopup(500);
                            List<String> found = ToolBox.findLibrariesFor(errors, progress);
                            progress.close();
                            found.remove(lib.getId());
                            if(found.isEmpty()){
                                JOptionPane.showMessageDialog(LibEditor.this, "No solution found", "Check Library", JOptionPane.ERROR_MESSAGE);

                            }
                            else {
                                boolean pbAgain = false;
                                message = "You may add:";
                                for (String s : found) {
                                    message += "\n    " + s;
                                }
                                if( ! errors.isEmpty()){
                                    message += "\n\n"+errors.size()+" errors can not be solved";
                                    pbAgain = true;
                                }

                                message += "\n\nDo you want to add these dependencies?";
                                int addDependencies = JOptionPane.showConfirmDialog(LibEditor.this, message, "Check Library", JOptionPane.YES_NO_OPTION, pbAgain?JOptionPane.ERROR_MESSAGE:JOptionPane.INFORMATION_MESSAGE);
                                if(addDependencies == JOptionPane.YES_OPTION){
                                    for (String s : found) {
                                        lib.addDependence(s);
                                    }
                                    updateDependenciesList();
                                }
                            }
                        }
                    }
                    return null;
                }
            };
            task.execute();
        }
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        if(lib != null){
            lib.updateNameChanger(jCheckBox1.isSelected());
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        lib.addDependence("");
        updateDependenciesList();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        lib.addNeed("");
        updateNeedsList();
    }//GEN-LAST:event_jButton4ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteLibButton;
    private javax.swing.JPanel dependenciesPanel;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JPanel needsPanel;
    private javax.swing.JButton newLibButton;
    // End of variables declaration//GEN-END:variables

}
