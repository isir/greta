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
import java.awt.event.ItemEvent;
import java.awt.event.KeyEvent;
import java.util.Collections;
import java.util.LinkedList;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DropMode;
import javax.swing.JList;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Andre-Marie Pez
 */
public class MenuEditor extends javax.swing.JPanel implements Updatable{

    private XMLTree selectedMenu = null;

    /**
     * Creates new form MenuEditor
     */
    public MenuEditor() {

        initComponents();
        this.setName(IniManager.getLocaleProperty("modular.edit.menu"));
        jTree1 = new javax.swing.JTree(new XMLTreeNode(ModularXMLFile.getMenus(), null));
        jTree1.setCellRenderer(new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                Component comp = super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
                if (value instanceof XMLTreeNode) {
                    if (!((XMLTreeNode) value).isValid()) {
                        comp.setForeground(Colors.error);
                    } else {
                        if (!((XMLTreeNode) value).isChildrenValid()) {
                            comp.setForeground(Colors.warning);
                        }
                    }
                }
                return comp;
            }

        });
        jScrollPane1.setViewportView(jTree1);
        jTree1.setEnabled(true);
        jTree1.setEditable(false);
        jTree1.setExpandsSelectedPaths(true);
        jTree1.setDragEnabled(true);
        jTree1.setDropMode(DropMode.ON_OR_INSERT);
        jTree1.setTransferHandler(new TreeDragAndDrop());
        jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);

        jTree1.addTreeSelectionListener(new TreeSelectionListener() {

            @Override
            public void valueChanged(TreeSelectionEvent e) {

                Object o = e.getPath().getLastPathComponent();
                if (o instanceof XMLTreeNode) {
                    setSelectedMenu(((XMLTreeNode) o).node);
                } else {
                    setSelectedMenu(null);
                }
            }
        });

        jTree1.addKeyListener(new java.awt.event.KeyAdapter() {

            @Override
            public void keyPressed(java.awt.event.KeyEvent evt) {
                if (evt.getKeyCode() == KeyEvent.VK_DELETE) {
                    suppressSelected();
                }
            }
        });

        setSelectedMenu(null);
        updateCombobox();

        jComboBox1.setRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component returned = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

                if( ! ModularXMLFile.moduleHasItem(ModularXMLFile.getMenus(), value.toString())){
                    returned.setForeground(Colors.error);
                }
                return returned;
            }

        });

        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    if(selectedMenu !=null && selectedMenu.isNamed("item")){
                        selectedMenu.setAttribute("module", evt.getItem().toString());
                        updateListOfNotAccessibleModules();
                    }
                    updateJTreeUI();
                }
            }

        });

        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
              updateMenuName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
              updateMenuName();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
              updateMenuName();
            }
        });

        updateListOfNotAccessibleModules();
    }

    private void setSelectedMenu(XMLTree node) {
        selectedMenu = null;
        if(node == null || !(node.isNamed("item") || node.isNamed("menu"))){
            jLabel2.setEnabled(false);
            jComboBox1.setEnabled(false);

            jLabel1.setEnabled(false);
            jTextField1.setEnabled(false);
            jTextField1.setText("");

            createMenuButton.setEnabled(true);
            createItemButton.setEnabled(false);
            supprButton.setEnabled(false);

            selectedMenu = null;
        }
        else{
            boolean isItem = node.isNamed("item");

            jLabel2.setEnabled(isItem);
            jComboBox1.setEnabled(isItem);

            jLabel1.setEnabled(true);
            jTextField1.setEnabled(true);
            jTextField1.setText(node.getAttribute("name"));


            createMenuButton.setEnabled(!isItem);
            createItemButton.setEnabled(!isItem);
            supprButton.setEnabled(true);

            selectedMenu = node;
        }
        updateComboboxSelection();
    }


    private void updateJTreeUI() {
        java.awt.EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                jTree1.updateUI();
            }
        });
    }


    private void updateListOfNotAccessibleModules() {
        String list = "";
        for(XMLTree module : ModularXMLFile.getModules().getChildrenElement()){
            if( ! ModularXMLFile.moduleHasItem(ModularXMLFile.getMenus(), module.getAttribute("name"))){
                list += "   "+module.getAttribute("name")+"\n";
            }
        }
        if(list.isEmpty()){
            jTextArea1.setText(IniManager.getLocaleProperty("modular.edit.menu.modules.access.ok"));
            jTextArea1.setForeground(Colors.normal);
        }
        else{
            jTextArea1.setText(IniManager.getLocaleProperty("modular.edit.menu.modules.access.ko")+"\n"+list);
            jTextArea1.setForeground(Colors.error);
        }
    }

    private void updateCombobox(){
        LinkedList<String> moduleNames = new LinkedList<String>();
        for (XMLTree moduleName : ModularXMLFile.getModules().getChildrenElement()) {
            if (moduleName.isNamed("module")) {
                moduleNames.add(moduleName.getAttribute("name"));
            }
        }
        Collections.sort(moduleNames, String.CASE_INSENSITIVE_ORDER);
        moduleNames.addFirst("");
        jComboBox1.removeAllItems();

        for (String moduleName : moduleNames) {
            jComboBox1.addItem(moduleName);
        }
        updateComboboxSelection();
    }

    private void updateComboboxSelection(){
        if (selectedMenu != null && selectedMenu.isNamed("item")) {
            if(ModularXMLFile.itemHasModule(selectedMenu)){
                jComboBox1.setSelectedItem(selectedMenu.getAttribute("module"));
            }
            else{
                jComboBox1.setSelectedIndex(0);
            }
        }
        else{
            jComboBox1.setSelectedIndex(0);
            jComboBox1.setEnabled(false);
        }

    }


    private void updateMenuName(){
        if(selectedMenu != null){
            selectedMenu.setAttribute("name", jTextField1.getText());
            updateJTreeUI();
        }
    }

    private void suppressSelected(){
        if(jTree1.getSelectionCount() > 0){
            Object o = jTree1.getSelectionPath().getLastPathComponent();
            if (o instanceof XMLTreeNode) {
                XMLTreeNode toDelete = (XMLTreeNode) o;
                XMLTreeNode parent = (XMLTreeNode) toDelete.getParent();
                if(parent !=null){
                    parent.remove(toDelete);
                    updateJTreeUI();
                    setSelectedMenu(null);
                    updateListOfNotAccessibleModules();
                }
            }
        }
    }

    @Override
    public void update() {
        jTree1.clearSelection();
        setSelectedMenu(null);
        updateCombobox();
        updateListOfNotAccessibleModules();
    }


    @Override
    public void reload() {
        jTree1.setEnabled(false);
        update();
        ((DefaultTreeModel)jTree1.getModel()).reload(new XMLTreeNode(ModularXMLFile.getMenus(), null));
        jTree1.setEnabled(true);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        createMenuButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("modular.edit.menu.new.menu");
        createItemButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("modular.edit.menu.new.item");
        jScrollPane1 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jTextField1 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel1 = new greta.core.utilx.gui.ToolBox.LocalizedJLabel("word.name");
        jLabel2 = new greta.core.utilx.gui.ToolBox.LocalizedJLabel("word.module");
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        supprButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.delete");

        createMenuButton.setText("New Menu");
        createMenuButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createMenuButtonActionPerformed(evt);
            }
        });

        createItemButton.setText("New Item");
        createItemButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createItemButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setVerifyInputWhenFocusTarget(false);
        jScrollPane1.setViewportView(jTree1);

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel1.setText("Name");

        jLabel2.setText("Module");

        jScrollPane2.setBorder(null);

        jTextArea1.setEditable(false);
        jTextArea1.setBackground(getBackground());
        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jTextArea1.setBorder(null);
        jScrollPane2.setViewportView(jTextArea1);

        supprButton.setText("Delete");
        supprButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                supprButtonActionPerformed(evt);
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
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jLabel1)
                                        .addGap(100, 100, 100))
                                    .addComponent(jTextField1))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jScrollPane2)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(createMenuButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(createItemButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(supprButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(createMenuButton)
                    .addComponent(createItemButton)
                    .addComponent(supprButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        updateMenuName();
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void createMenuButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createMenuButtonActionPerformed
        XMLTreeNode currentSelectedNode;
        if(selectedMenu == null || jTree1.getSelectionPath() == null){
            currentSelectedNode = (XMLTreeNode)(jTree1.getModel().getRoot());
        }
        else{
            currentSelectedNode = (XMLTreeNode)(jTree1.getSelectionPath().getLastPathComponent());
        }
        if(currentSelectedNode.isLeaf()){
            return ;
        }
        XMLTreeNode child = new XMLTreeNode(currentSelectedNode.node.createChild("menu"), currentSelectedNode);
        this.setSelectedMenu(child.node);
        jTree1.setSelectionPath(child.getTreePath());
        updateJTreeUI();
        jTextField1.requestFocusInWindow();
    }//GEN-LAST:event_createMenuButtonActionPerformed

    private void createItemButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createItemButtonActionPerformed
        XMLTreeNode currentSelectedNode;
        if(selectedMenu == null || jTree1.getSelectionPath() == null){
            currentSelectedNode = (XMLTreeNode)(jTree1.getModel().getRoot());
        }
        else{
            currentSelectedNode = (XMLTreeNode)(jTree1.getSelectionPath().getLastPathComponent());
        }
        if(currentSelectedNode.isLeaf() || currentSelectedNode.node.isNamed("menus")){
            return ;
        }
        XMLTreeNode child = new XMLTreeNode(currentSelectedNode.node.createChild("item"), currentSelectedNode);
        this.setSelectedMenu(child.node);
        jTree1.setSelectionPath(child.getTreePath());
        updateJTreeUI();
        jTextField1.requestFocusInWindow();
    }//GEN-LAST:event_createItemButtonActionPerformed

    private void supprButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_supprButtonActionPerformed
        suppressSelected();
    }//GEN-LAST:event_supprButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton createItemButton;
    private javax.swing.JButton createMenuButton;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTree jTree1;
    private javax.swing.JButton supprButton;
    // End of variables declaration//GEN-END:variables
}
