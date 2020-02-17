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
import greta.application.modular.modules.Style;
import greta.core.util.IniManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JColorChooser;
import javax.swing.JList;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Andre-Marie Pez
 */
public class StyleEditor extends javax.swing.JPanel implements Updatable{


    StyleListModel model  = new StyleListModel();
    StyleListModel.StyleElement style;

    /**
     * Creates new form StyleEditor
     */
    public StyleEditor() {
        initComponents();
        setName(IniManager.getLocaleProperty("modular.edit.style"));
        jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if( ! jTextField1.hasFocus()){
                    StyleListModel.StyleElement newSelection = (StyleListModel.StyleElement)jList1.getSelectedValue();
                    if(newSelection != style){
                        setCurrentStyle(newSelection);
                    }
                }
            }
        });

        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateStyleName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateStyleName();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateStyleName();
            }

        });

        jList1.setCellRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component toReturn = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null || ! (value instanceof StyleListModel.StyleElement)){
                    return toReturn;
                }
                StyleListModel.StyleElement element = (StyleListModel.StyleElement)value;
                boolean unicname = true;
                if(index > 0){
                    unicname = ! (model.getElementAt(index-1).getName().equals(element.getName()));
                }
                if(index < model.getSize()-1){
                    unicname = unicname && ! (model.getElementAt(index+1).getName().equals(element.getName()));
                }
                boolean valid = ModularXMLFile.checkOneStyle(element.style);
                toReturn.setForeground(valid ? (unicname ? toReturn.getForeground() : Colors.warning) : Colors.error);
                return toReturn;
            }

        });


        jTextField4.addKeyListener(new KeyAdapter() {
            boolean isPrintableChar(char c) {
                Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
                return (!Character.isISOControl(c))
                        && c != KeyEvent.CHAR_UNDEFINED
                        && block != null
                        && block != Character.UnicodeBlock.SPECIALS;
            }

            @Override
            public void keyTyped(KeyEvent e) {
                if(Character.isWhitespace(e.getKeyChar())){
                    e.setKeyChar(' ');
                }

                else if(isPrintableChar(e.getKeyChar())){
                    e.setKeyChar('-');
                }
            }
        });

        jTextField4.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateDash();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateDash();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateDash();
            }

        });

        setCurrentStyle(null);
    }

    private void updateStyleName() {
        if(style != null){
            style.setName(jTextField1.getText());
            jList1.setSelectedValue(style, true);
        }
    }

    private void setCurrentStyle(StyleListModel.StyleElement style){
        this.style = null;

        boolean enable = style!=null;
        jLabel1.setEnabled(enable);
        jLabel2.setEnabled(enable);
        jLabel3.setEnabled(enable);
        jLabel4.setEnabled(enable);
        jLabel5.setEnabled(enable);
        jLabel6.setEnabled(enable);
        jTextField1.setEnabled(enable);
        jTextField2.setEnabled(enable);
        jTextField3.setEnabled(enable);
        jTextField4.setEnabled(enable);
        jButton1.setEnabled(enable);
        jButton2.setEnabled(enable);
        jButton3.setEnabled(enable);
        jButton4.setEnabled(enable);
        jRadioButton1.setEnabled(enable);
        jRadioButton2.setEnabled(enable);
        jRadioButton3.setEnabled(enable);
        jRadioButton4.setEnabled(enable);
        jRadioButton5.setEnabled(enable);
        jRadioButton6.setEnabled(enable);
        jRadioButton7.setEnabled(enable);
        jRadioButton8.setEnabled(enable);
        deleteStyleButton.setEnabled(enable);
        jCheckBox1.setEnabled(enable);

        if(style==null){
            jTextField1.setText("");
            jTextField2.setText("");
            jTextField3.setText("");
            jTextField4.setText("");

            jTextField2.setBackground(Style.convertColor(Style.DEFAULT_COLOR));
            jTextField3.setBackground(Style.convertColor(Style.ensureEdgeColor(Style.DEFAULT_COLOR, null)));

            buttonGroup1.clearSelection();
            buttonGroup2.clearSelection();
        }
        else{
            jTextField1.setText(style.getName());

            String start = style.getStart();
            jRadioButton1.setSelected(start.equals("none"));
            jRadioButton2.setSelected(start.equals("arrow"));
            jRadioButton3.setSelected(start.equals("oval"));
            jRadioButton4.setSelected(start.equals("diamond"));
            String end  = style.getEnd();
            jRadioButton5.setSelected(end.equals("none"));
            jRadioButton6.setSelected(end.equals("arrow"));
            jRadioButton7.setSelected(end.equals("oval"));
            jRadioButton8.setSelected(end.equals("diamond"));
            jTextField4.setText(style.getDash());
            style.updateNameChanger(jCheckBox1.isSelected());

        }
        this.style = style;

        updateMainColor();
        updateEdgeColor();
    }


    private void applyMainColor(Color c){
        style.setColor(c);
        if( ! style.isEdgeColorSet()){
            updateEdgeColor();
        }
    }
    private void updateMainColor(){
        if(style==null)return ;
        updateColorField(style.getColor(), jTextField2);
        jButton1.setEnabled(style.isColorSet());
    }
    private void applyEdgeColor(Color c){
        style.setEdgeColor(c);
    }
    private void updateEdgeColor(){
        if(style==null)return ;
        updateColorField(style.getEdgeColor(), jTextField3);
        jButton4.setEnabled(style.isEdgeColorSet());
    }

    private void updateColorField(Color c, JTextField field){
        if(c == null){
            return;
        }
        field.setBackground(c);
        int brightness = (Math.max(Math.max(c.getRed(), c.getGreen()), c.getBlue())+Math.min(Math.min(c.getRed(), c.getGreen()), c.getBlue()))/2;
        field.setForeground(brightness>127 ? Color.black : Color.white);
        field.setText(Style.convertColor(c));
    }


    private void updateDash() {
        if(style!=null){
            style.setDash(jTextField4.getText());
        }
    }

    @Override
    public void update() {
        jList1.clearSelection();
        setCurrentStyle(null);
    }

    @Override
    public void reload() {
        jList1.setEnabled(false);
        update();
        model.reload();
        jList1.setEnabled(true);
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        buttonGroup1 = new javax.swing.ButtonGroup();
        buttonGroup2 = new javax.swing.ButtonGroup();
        newStyleButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("modular.edit.style.new")  ;
        deleteStyleButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("modular.edit.style.delete")  ;
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jLabel4 = new javax.swing.JLabel();
        jTextField2 = new javax.swing.JTextField();
        jButton2 = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.choose");
        jLabel5 = new javax.swing.JLabel();
        jTextField3 = new javax.swing.JTextField();
        jButton3 = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.choose");
        jButton1 = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.default");
        jButton4 = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.default");
        jLabel2 = new javax.swing.JLabel();
        jTextField4 = new javax.swing.JTextField();
        jLabel3 = new javax.swing.JLabel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        jRadioButton4 = new javax.swing.JRadioButton();
        jLabel6 = new javax.swing.JLabel();
        jRadioButton5 = new javax.swing.JRadioButton();
        jRadioButton6 = new javax.swing.JRadioButton();
        jRadioButton7 = new javax.swing.JRadioButton();
        jRadioButton8 = new javax.swing.JRadioButton();
        jCheckBox1 = new javax.swing.JCheckBox();

        newStyleButton.setText("New");
        newStyleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newStyleButtonActionPerformed(evt);
            }
        });

        deleteStyleButton.setText("Delete");
        deleteStyleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteStyleButtonActionPerformed(evt);
            }
        });

        jList1.setModel(model);
        jScrollPane1.setViewportView(jList1);

        jLabel1.setText("Name");

        jLabel4.setText("Main Color");

        jTextField2.setEditable(false);

        jButton2.setText("Choose");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jLabel5.setText("Edge Color");

        jTextField3.setEditable(false);

        jButton3.setText("Choose");
        jButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton3ActionPerformed(evt);
            }
        });

        jButton1.setText("Reset");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton4.setText("Reset");
        jButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton4ActionPerformed(evt);
            }
        });

        jLabel2.setText("Dash");

        jLabel3.setText("Start");

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setText("none");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("arrow");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("oval");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton4);
        jRadioButton4.setText("diamond");
        jRadioButton4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton4ActionPerformed(evt);
            }
        });

        jLabel6.setText("End");

        buttonGroup2.add(jRadioButton5);
        jRadioButton5.setText("none");
        jRadioButton5.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton5ActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButton6);
        jRadioButton6.setText("arrow");
        jRadioButton6.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton6ActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButton7);
        jRadioButton7.setText("oval");
        jRadioButton7.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton7ActionPerformed(evt);
            }
        });

        buttonGroup2.add(jRadioButton8);
        jRadioButton8.setText("diamond");
        jRadioButton8.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton8ActionPerformed(evt);
            }
        });

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
                                    .addComponent(jLabel2)
                                    .addComponent(jLabel3)
                                    .addComponent(jLabel6)
                                    .addComponent(jLabel5))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jRadioButton5)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jRadioButton6)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jRadioButton7)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jRadioButton8))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                                .addComponent(jButton3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jButton4))
                                            .addGroup(layout.createSequentialGroup()
                                                .addComponent(jRadioButton1)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jRadioButton2)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jRadioButton3)
                                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                                .addComponent(jRadioButton4)))
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addComponent(jTextField4)))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addComponent(jLabel4))
                                .addGap(6, 6, 6)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 70, javax.swing.GroupLayout.PREFERRED_SIZE)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jButton2)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                        .addComponent(jButton1)
                                        .addGap(0, 0, Short.MAX_VALUE))
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jCheckBox1))))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newStyleButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteStyleButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newStyleButton)
                    .addComponent(deleteStyleButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox1))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton2)
                            .addComponent(jButton1)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel4)
                                .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jButton3)
                            .addComponent(jButton4)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(jLabel5)
                                .addComponent(jTextField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel2)
                            .addComponent(jTextField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel3)
                            .addComponent(jRadioButton1)
                            .addComponent(jRadioButton2)
                            .addComponent(jRadioButton3)
                            .addComponent(jRadioButton4))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel6)
                            .addComponent(jRadioButton5)
                            .addComponent(jRadioButton6)
                            .addComponent(jRadioButton7)
                            .addComponent(jRadioButton8))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newStyleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newStyleButtonActionPerformed
        StyleListModel.StyleElement style = model.createStyle();
        jList1.setSelectedValue(style, true);
        setCurrentStyle(style);
        jTextField1.requestFocusInWindow();
    }//GEN-LAST:event_newStyleButtonActionPerformed

    private void deleteStyleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteStyleButtonActionPerformed
        if(style != null){
            model.deleteStyle(style);
        }
    }//GEN-LAST:event_deleteStyleButtonActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        Color chosen = JColorChooser.showDialog(this, "Main Color", jTextField2.getBackground());
        if(chosen == null){
            return ;
        }
        applyMainColor(chosen);
        updateMainColor();
    }//GEN-LAST:event_jButton2ActionPerformed

    private void jButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton3ActionPerformed
        Color chosen = JColorChooser.showDialog(this, "Edge Color", jTextField3.getBackground());
        if(chosen == null){
            return ;
        }
        applyEdgeColor(chosen);
        updateEdgeColor();
    }//GEN-LAST:event_jButton3ActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        applyMainColor(null);
        updateMainColor();
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        if(style!=null){
            style.setStart("none");
        }
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton4ActionPerformed
        applyEdgeColor(null);
        updateEdgeColor();
    }//GEN-LAST:event_jButton4ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        if(style!=null){
            style.setStart("arrow");
        }
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        if(style!=null){
            style.setStart("oval");
        }
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jRadioButton4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton4ActionPerformed
        if(style!=null){
            style.setStart("diamond");
        }
    }//GEN-LAST:event_jRadioButton4ActionPerformed

    private void jRadioButton5ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton5ActionPerformed
        if(style!=null){
            style.setEnd("none");
        }
    }//GEN-LAST:event_jRadioButton5ActionPerformed

    private void jRadioButton6ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton6ActionPerformed
        if(style!=null){
            style.setEnd("arrow");
        }
    }//GEN-LAST:event_jRadioButton6ActionPerformed

    private void jRadioButton7ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton7ActionPerformed
        if(style!=null){
            style.setEnd("oval");
        }
    }//GEN-LAST:event_jRadioButton7ActionPerformed

    private void jRadioButton8ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton8ActionPerformed
        if(style!=null){
            style.setEnd("diamond");
        }
    }//GEN-LAST:event_jRadioButton8ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        if(style != null){
            style.updateNameChanger(jCheckBox1.isSelected());
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.ButtonGroup buttonGroup2;
    private javax.swing.JButton deleteStyleButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JButton jButton3;
    private javax.swing.JButton jButton4;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JList jList1;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JRadioButton jRadioButton4;
    private javax.swing.JRadioButton jRadioButton5;
    private javax.swing.JRadioButton jRadioButton6;
    private javax.swing.JRadioButton jRadioButton7;
    private javax.swing.JRadioButton jRadioButton8;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    private javax.swing.JTextField jTextField3;
    private javax.swing.JTextField jTextField4;
    private javax.swing.JButton newStyleButton;
    // End of variables declaration//GEN-END:variables

}
