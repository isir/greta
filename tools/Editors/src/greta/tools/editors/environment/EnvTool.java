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
package greta.tools.editors.environment;

import greta.core.util.IniManager;
import greta.core.util.environment.Environment;
import greta.core.util.environment.EnvironmentEventListener;
import greta.core.util.environment.Leaf;
import greta.core.util.environment.LeafEvent;
import greta.core.util.environment.Node;
import greta.core.util.environment.NodeEvent;
import greta.core.util.environment.TreeEvent;
import greta.core.util.environment.TreeNode;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import greta.core.utilx.gui.TreeNodeController;
import java.awt.event.KeyEvent;
import java.io.File;
import javax.swing.DropMode;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.tree.TreeSelectionModel;

/**
 *
 * @author Andre-Marie Pez
 */
public class EnvTool extends javax.swing.JFrame implements EnvironmentEventListener, TreeSelectionListener{

    TreeNode currentNode;
    Leaf currentLeaf;

    Environment env;

    TreeNodeController controller;
    LeafController leafController;

    XMLParser parser;

    /**
     * Creates new form EnvTool
     */
    public EnvTool() {
        initComponents();
        controller = new TreeNodeController();
        jScrollPane1.setViewportView(controller.getContentPane());
        controller.setEnabled(false);

        leafController = new LeafController();
        jScrollPane3.setViewportView(leafController.getContentPane());
        leafController.setEnabled(false);
        parser = XML.createParser();
        parser.setValidating(false);
        jFileChooser1.setCurrentDirectory(new File(IniManager.getProgramPath()));
        jFileChooser1.removeChoosableFileFilter(jFileChooser1.getAcceptAllFileFilter());
        jFileChooser1.setAcceptAllFileFilterUsed(false);
        jFileChooser1.addChoosableFileFilter(new FileFilter() {

            @Override
            public boolean accept(File f) {
                if(f.isDirectory()) {
                    return true;
                }
                if(! f.getName().toLowerCase().endsWith(".xml")) {
                    return false;
                }
                XMLTree xmltree = parser.parseFile(f.getAbsolutePath());
                return xmltree!=null && xmltree.isNamed("environment");
            }

            @Override
            public String getDescription() {
                return "Environment (xml)";
            }
        });
    }

    public void setEnvironment(Environment env){
        if(this.env!=null){
            this.env.removeEnvironementListener(this);
        }
        this.env = env;
        jButton1.setEnabled(false);
        jButton3.setEnabled(false);
        if(env == null){
            leafController.setEnabled(false);
            controller.setEnabled(false);
            jTree1.setEnabled(false);
            jTree1.removeTreeSelectionListener(this);
        }
        else{
            jTree1 = new javax.swing.JTree(new EnvironmentEmbededTreeNode(env.getRoot()));
            jTree1.setEnabled(true);
            jScrollPane2.setViewportView(jTree1);
            this.env.addEnvironementListener(this);
            jTree1.setDragEnabled(true);
            jTree1.setDropMode(DropMode.ON_OR_INSERT);
            jTree1.setTransferHandler(new TreeDragAndDrop());
            jTree1.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
//            jTree1.setTransferHandler(new TransferHandler(null));
            jTree1.addTreeSelectionListener(this);
            jTree1.setEditable(true);
            jTree1.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent evt) {
                    if(evt.getKeyCode() == KeyEvent.VK_DELETE && jTree1.getSelectionCount()>0){
                        Object o = jTree1.getSelectionPath().getLastPathComponent();
                        if(o instanceof EnvironmentEmbededTreeNode){
                            EnvironmentEmbededTreeNode toDelete = (EnvironmentEmbededTreeNode)o;
                            EnvironmentEmbededTreeNode parent = (EnvironmentEmbededTreeNode)toDelete.getParent();
                            EnvTool.this.env.removeNode(toDelete.envNode, (TreeNode)parent.envNode);
                        }
                    }
                }
            });
        }
    }


    File currentFile = null;
    private void save(){
        if(env == null){
            return;
        }
        if(currentFile == null){
            saveAs();
            return;
        }

        XMLTree envXML = env.asXML();
        envXML.save(currentFile.getAbsolutePath());
    }

    private void saveAs(){
        if(env == null){
            return;
        }
        if(currentFile!=null) {
            jFileChooser1.setCurrentDirectory(currentFile);
        }
        jFileChooser1.updateUI();
        if(jFileChooser1.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION){
            currentFile = jFileChooser1.getSelectedFile();
            save();
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
        jButton1 = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jButton3 = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTree1 = new javax.swing.JTree();
        jScrollPane3 = new javax.swing.JScrollPane();
        jMenuBar1 = new javax.swing.JMenuBar();
        jMenu1 = new greta.core.utilx.gui.ToolBox.LocalizedJMenu("GUI.file");
        jMenuItem1 = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("GUI.save");
        jMenuItem2 = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("GUI.saveAs");

        jButton1.setText("create node");
        jButton1.setEnabled(false);
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton3.setText("create object");
        jButton3.setEnabled(false);
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jScrollPane2.setViewportView(jTree1);

        jMenu1.setToolTipText("");

        jMenuItem1.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem1ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem1);

        jMenuItem2.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.SHIFT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        jMenuItem2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jMenuItem2ActionPerformed(evt);
            }
        });
        jMenu1.add(jMenuItem2);

        jMenuBar1.add(jMenu1);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jButton3)
                        .addGap(0, 858, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane2)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 478, Short.MAX_VALUE)
                            .addComponent(jScrollPane3))))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton1)
                    .addComponent(jButton3))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 276, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane3)
                        .addGap(29, 29, 29))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 487, Short.MAX_VALUE))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        EnvironmentEmbededTreeNode currentSelectedNode = (EnvironmentEmbededTreeNode)(jTree1.getSelectionPath().getLastPathComponent());
        (new EnvironmentEmbededTreeNode(new TreeNode())).setParent(currentSelectedNode);
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        EnvironmentEmbededTreeNode currentSelectedNode = (EnvironmentEmbededTreeNode)(jTree1.getSelectionPath().getLastPathComponent());
        (new EnvironmentEmbededTreeNode(new Leaf())).setParent(currentSelectedNode);
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jMenuItem1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem1ActionPerformed
        save();
    }//GEN-LAST:event_jMenuItem1ActionPerformed

    private void jMenuItem2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jMenuItem2ActionPerformed
        saveAs();
    }//GEN-LAST:event_jMenuItem2ActionPerformed


    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
        /* Set the Nimbus look and feel */
        //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /* If Nimbus (introduced in Java SE 6) is not available, stay with the default look and feel.
         * For details see http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException ex) {
            java.util.logging.Logger.getLogger(EnvTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (InstantiationException ex) {
            java.util.logging.Logger.getLogger(EnvTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (IllegalAccessException ex) {
            java.util.logging.Logger.getLogger(EnvTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        } catch (javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(EnvTool.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>

        /* Create and display the form */
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                new EnvTool().setVisible(true);
            }
        });
    }

    @Override
    public void onTreeChange(TreeEvent e) {
        java.awt.EventQueue.invokeLater(new Runnable() {
            public void run() {
                jTree1.updateUI();
            }
        });
    }

    @Override
    public void onNodeChange(NodeEvent e) {
    }

    @Override
    public void onLeafChange(LeafEvent event) {
    }

    @Override
    public void valueChanged(TreeSelectionEvent e) {
        Object o = e.getPath().getLastPathComponent();
        if(o instanceof EnvironmentEmbededTreeNode){
            Node node = ((EnvironmentEmbededTreeNode)o).envNode;
            if(node!=null){
                if(node instanceof Leaf){
                    jButton1.setEnabled(false);
                    jButton3.setEnabled(false);
                    currentLeaf = (Leaf)node;
                    currentNode = node.getParent();
                    leafController.setLeaf(currentLeaf);
                    controller.setTreeNode(currentNode);
                    leafController.setEnabled(true);
                    controller.setEnabled(false);
                }
                if(node instanceof TreeNode){
                    jButton1.setEnabled(true);
                    jButton3.setEnabled(true);
                    currentLeaf = null;
                    currentNode = (TreeNode)node;
                    leafController.setLeaf(null);
                    controller.setTreeNode(currentNode);
                    leafController.setEnabled(false);
                    controller.setEnabled(true);
                }
            }
            else{
                jButton1.setEnabled(false);
                jButton3.setEnabled(false);
                currentLeaf = null;
                currentNode = null;
                leafController.setLeaf(null);
                leafController.setEnabled(false);
                controller.setEnabled(false);
            }
        }
    }


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton3;
    private javax.swing.JFileChooser jFileChooser1;
    private javax.swing.JMenu jMenu1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JMenuItem jMenuItem1;
    private javax.swing.JMenuItem jMenuItem2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTree jTree1;
    // End of variables declaration//GEN-END:variables
}
