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
package greta.core.utilx.gui;

import greta.core.util.IniManager;
import greta.core.util.environment.TreeNode;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.Locale;

/**
 *
 * @author Andre-Marie Pez
 */
public class TreeNodeController extends javax.swing.JFrame {

    TreeNode node;

    /** Creates new form TreeNodeController */
    public TreeNodeController() {
        initComponents();
        initField(posXField);
        initField(posYField);
        initField(posZField);
        initField(orientXField);
        initField(orientYField);
        initField(orientZField);
        initField(scaleXField);
        initField(scaleYField);
        initField(scaleZField);

        idField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    }

    public void setTreeNode(TreeNode node){
        this.node = node;
        readPosition();
        readOrientation();
        readScale();
        readId();
    }

    private void initField(javax.swing.JFormattedTextField field){
        field.setFormatterFactory(
                new javax.swing.text.DefaultFormatterFactory(
                        new javax.swing.text.NumberFormatter(
                                greta.core.util.IniManager.getNumberFormat())));
        field.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
    }

    @Override
    public void setEnabled(boolean b) {
        super.setEnabled(b);
        posXField.setEnabled(b);
        posYField.setEnabled(b);
        posZField.setEnabled(b);
        orientXField.setEnabled(b);
        orientYField.setEnabled(b);
        orientZField.setEnabled(b);
        scaleXField.setEnabled(b);
        scaleYField.setEnabled(b);
        scaleZField.setEnabled(b);
    }


    private void isDoubleTyped(java.awt.event.KeyEvent evt, javax.swing.JFormattedTextField field){
        ToolBox.checkDouble(evt, field);
    }

    private void updatePosition(){
        if(node!=null){
            Vec3d pos = node.getCoordinates();
            node.setCoordinates(
                    valueOf(posXField, pos.x()),
                    valueOf(posYField, pos.y()),
                    valueOf(posZField, pos.z()));
        }
    }

    private void readPosition(){
        if(node!=null){
            Vec3d pos = node.getCoordinates();
            posXField.setText(""+pos.x());
            posYField.setText(""+pos.y());
            posZField.setText(""+pos.z());
        }
    }

    private void updateScale(){
        if(node!=null){
            Vec3d scale = node.getScale();
            node.setScale(
                    valueOf(scaleXField, scale.x()),
                    valueOf(scaleYField, scale.y()),
                    valueOf(scaleZField, scale.z()));
        }
    }

    private void readScale(){
        if(node!=null){
            Vec3d scale = node.getScale();
            scaleXField.setText(""+scale.x());
            scaleYField.setText(""+scale.y());
            scaleZField.setText(""+scale.z());
        }
    }

    private void updateOrientation(){
        if(node!=null){
            Vec3d orient = node.getOrientation().getEulerAngleXYZByAngle();
            Quaternion q = new Quaternion();
            q.fromEulerXYZByAngle(
                    valueOf(orientXField, orient.x()),
                    valueOf(orientYField, orient.y()),
                    valueOf(orientZField, orient.z()));
            node.setOrientation(q);
        }
    }

    private void readOrientation(){
        if(node!=null){
            Vec3d orient = node.getOrientation().getEulerAngleXYZByAngle();
            orientXField.setText(""+orient.x());
            orientYField.setText(""+orient.y());
            orientZField.setText(""+orient.z());
        }
    }

    private void readId(){
        if(node!=null){
            idField.setText(node.getIdentifier());
        }
    }

    private double valueOf(javax.swing.JFormattedTextField field, double defaultValue){
        try{
            return Double.parseDouble(field.getText());
        }
        catch(Throwable t){}
        field.setText(""+defaultValue);
        return defaultValue;
    }

    @Override
    public void setLocale(Locale l) {
        super.setLocale(l);
        if(degreeLabel!=null){
            degreeLabel.setText("("+IniManager.getLocaleProperty("unit.degree", l).toLowerCase() +") ");
        }
        if(meterLabel!=null){
            meterLabel.setText("("+IniManager.getLocaleProperty("unit.meter", l).toLowerCase() +") ");
        }
        if(posLabel!=null){
            posLabel.setText(IniManager.getLocaleProperty("word.position", l));
        }
        if(oreintLabel!=null){
            oreintLabel.setText(IniManager.getLocaleProperty("word.orientation", l));
        }
        if(scaleLabel!=null){
            scaleLabel.setText(IniManager.getLocaleProperty("word.scale", l));
        }
        if(idLabel!=null){
            idLabel.setText(IniManager.getLocaleProperty("word.identifier", l));
        }
    }


    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        posLabel = new javax.swing.JLabel();
        posXField = new javax.swing.JFormattedTextField();
        posYField = new javax.swing.JFormattedTextField();
        posZField = new javax.swing.JFormattedTextField();
        oreintLabel = new javax.swing.JLabel();
        orientZField = new javax.swing.JFormattedTextField();
        orientYField = new javax.swing.JFormattedTextField();
        orientXField = new javax.swing.JFormattedTextField();
        scaleLabel = new javax.swing.JLabel();
        scaleZField = new javax.swing.JFormattedTextField();
        scaleYField = new javax.swing.JFormattedTextField();
        scaleXField = new javax.swing.JFormattedTextField();
        meterLabel = new javax.swing.JLabel();
        degreeLabel = new javax.swing.JLabel();
        xLabel = new javax.swing.JLabel();
        yLabel = new javax.swing.JLabel();
        zLabel = new javax.swing.JLabel();
        idField = new javax.swing.JFormattedTextField();
        idLabel = new javax.swing.JLabel();

        posLabel.setText(IniManager.getLocaleProperty("word.position"));

        posXField.setText("0.0");
        posXField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posXFieldActionPerformed(evt);
            }
        });
        posXField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                posXFieldFocusLost(evt);
            }
        });
        posXField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                posXFieldKeyTyped(evt);
            }
        });

        posYField.setText("0.0");
        posYField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posYFieldActionPerformed(evt);
            }
        });
        posYField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                posYFieldFocusLost(evt);
            }
        });
        posYField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                posYFieldKeyTyped(evt);
            }
        });

        posZField.setText("0.0");
        posZField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posZFieldActionPerformed(evt);
            }
        });
        posZField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                posZFieldFocusLost(evt);
            }
        });
        posZField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                posZFieldKeyTyped(evt);
            }
        });

        oreintLabel.setText(IniManager.getLocaleProperty("word.orientation"));

        orientZField.setText("0.0");
        orientZField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orientZFieldActionPerformed(evt);
            }
        });
        orientZField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                orientZFieldFocusLost(evt);
            }
        });
        orientZField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                orientZFieldKeyTyped(evt);
            }
        });

        orientYField.setText("0.0");
        orientYField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orientYFieldActionPerformed(evt);
            }
        });
        orientYField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                orientYFieldFocusLost(evt);
            }
        });
        orientYField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                orientYFieldKeyTyped(evt);
            }
        });

        orientXField.setText("0.0");
        orientXField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orientXFieldActionPerformed(evt);
            }
        });
        orientXField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                orientXFieldFocusLost(evt);
            }
        });
        orientXField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                orientXFieldKeyTyped(evt);
            }
        });

        scaleLabel.setText(IniManager.getLocaleProperty("word.scale"));

        scaleZField.setText("0.0");
        scaleZField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleZFieldActionPerformed(evt);
            }
        });
        scaleZField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                scaleZFieldFocusLost(evt);
            }
        });
        scaleZField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                scaleZFieldKeyTyped(evt);
            }
        });

        scaleYField.setText("0.0");
        scaleYField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleYFieldActionPerformed(evt);
            }
        });
        scaleYField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                scaleYFieldFocusLost(evt);
            }
        });
        scaleYField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                scaleYFieldKeyTyped(evt);
            }
        });

        scaleXField.setText("0.0");
        scaleXField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleXFieldActionPerformed(evt);
            }
        });
        scaleXField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                scaleXFieldFocusLost(evt);
            }
        });
        scaleXField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                scaleXFieldKeyTyped(evt);
            }
        });

        meterLabel.setText("("+IniManager.getLocaleProperty("unit.meter").toLowerCase() +") ");

        degreeLabel.setText("("+IniManager.getLocaleProperty("unit.degree").toLowerCase() +") ");

        xLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        xLabel.setText("X");

        yLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        yLabel.setText("Y");

        zLabel.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        zLabel.setText("Z");

        idField.setEditable(false);
        idField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idFieldActionPerformed(evt);
            }
        });
        idField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                idFieldFocusLost(evt);
            }
        });
        idField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                idFieldKeyTyped(evt);
            }
        });

        idLabel.setText(IniManager.getLocaleProperty("word.identifier"));

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(posLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(oreintLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scaleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(idLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(orientXField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orientYField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orientZField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(degreeLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(xLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(posXField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(yLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(posYField, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(zLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(posZField, javax.swing.GroupLayout.DEFAULT_SIZE, 100, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(meterLabel))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(idField, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addComponent(scaleXField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(scaleYField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(scaleZField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xLabel)
                    .addComponent(yLabel)
                    .addComponent(zLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(posXField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(posYField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(posZField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(posLabel)
                    .addComponent(meterLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(orientXField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(orientYField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(orientZField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(oreintLabel)
                    .addComponent(degreeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scaleXField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scaleYField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scaleZField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scaleLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(idLabel))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void posXFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posXFieldActionPerformed
        updatePosition();
    }//GEN-LAST:event_posXFieldActionPerformed

    private void posZFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posZFieldActionPerformed
        updatePosition();
    }//GEN-LAST:event_posZFieldActionPerformed

    private void orientZFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientZFieldActionPerformed
        updateOrientation();
    }//GEN-LAST:event_orientZFieldActionPerformed

    private void orientXFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientXFieldActionPerformed
        updateOrientation();
    }//GEN-LAST:event_orientXFieldActionPerformed

    private void scaleZFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleZFieldActionPerformed
        updateScale();
    }//GEN-LAST:event_scaleZFieldActionPerformed

    private void scaleXFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleXFieldActionPerformed
        updateScale();
    }//GEN-LAST:event_scaleXFieldActionPerformed

    private void posXFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_posXFieldKeyTyped
        isDoubleTyped(evt,posXField);
    }//GEN-LAST:event_posXFieldKeyTyped

    private void orientXFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_orientXFieldKeyTyped
        isDoubleTyped(evt,orientXField);
    }//GEN-LAST:event_orientXFieldKeyTyped

    private void scaleXFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_scaleXFieldKeyTyped
        isDoubleTyped(evt,scaleXField);
    }//GEN-LAST:event_scaleXFieldKeyTyped

    private void posYFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_posYFieldKeyTyped
        isDoubleTyped(evt,posYField);
    }//GEN-LAST:event_posYFieldKeyTyped

    private void orientYFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_orientYFieldKeyTyped
        isDoubleTyped(evt,orientYField);
    }//GEN-LAST:event_orientYFieldKeyTyped

    private void scaleYFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_scaleYFieldKeyTyped
        isDoubleTyped(evt,scaleYField);
    }//GEN-LAST:event_scaleYFieldKeyTyped

    private void posZFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_posZFieldKeyTyped
        isDoubleTyped(evt,posZField);
    }//GEN-LAST:event_posZFieldKeyTyped

    private void orientZFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_orientZFieldKeyTyped
        isDoubleTyped(evt,orientZField);
    }//GEN-LAST:event_orientZFieldKeyTyped

    private void scaleZFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_scaleZFieldKeyTyped
        isDoubleTyped(evt,scaleZField);
    }//GEN-LAST:event_scaleZFieldKeyTyped

    private void posYFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posYFieldActionPerformed
        updatePosition();
    }//GEN-LAST:event_posYFieldActionPerformed

    private void scaleYFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleYFieldActionPerformed
        updateScale();
    }//GEN-LAST:event_scaleYFieldActionPerformed

    private void orientYFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientYFieldActionPerformed
        updateOrientation();
    }//GEN-LAST:event_orientYFieldActionPerformed

    private void posXFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_posXFieldFocusLost
        updatePosition();
    }//GEN-LAST:event_posXFieldFocusLost

    private void posYFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_posYFieldFocusLost
        updatePosition();
    }//GEN-LAST:event_posYFieldFocusLost

    private void posZFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_posZFieldFocusLost
        updatePosition();
    }//GEN-LAST:event_posZFieldFocusLost

    private void orientXFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_orientXFieldFocusLost
        updateOrientation();
    }//GEN-LAST:event_orientXFieldFocusLost

    private void orientYFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_orientYFieldFocusLost
        updateOrientation();
    }//GEN-LAST:event_orientYFieldFocusLost

    private void orientZFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_orientZFieldFocusLost
        updateOrientation();
    }//GEN-LAST:event_orientZFieldFocusLost

    private void scaleXFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_scaleXFieldFocusLost
        updateScale();
    }//GEN-LAST:event_scaleXFieldFocusLost

    private void scaleYFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_scaleYFieldFocusLost
        updateScale();
    }//GEN-LAST:event_scaleYFieldFocusLost

    private void scaleZFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_scaleZFieldFocusLost
        updateScale();
    }//GEN-LAST:event_scaleZFieldFocusLost

    private void idFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_idFieldActionPerformed

    private void idFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_idFieldFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_idFieldFocusLost

    private void idFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idFieldKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_idFieldKeyTyped

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel degreeLabel;
    private javax.swing.JFormattedTextField idField;
    private javax.swing.JLabel idLabel;
    private javax.swing.JLabel meterLabel;
    private javax.swing.JLabel oreintLabel;
    private javax.swing.JFormattedTextField orientXField;
    private javax.swing.JFormattedTextField orientYField;
    private javax.swing.JFormattedTextField orientZField;
    private javax.swing.JLabel posLabel;
    private javax.swing.JFormattedTextField posXField;
    private javax.swing.JFormattedTextField posYField;
    private javax.swing.JFormattedTextField posZField;
    private javax.swing.JLabel scaleLabel;
    private javax.swing.JFormattedTextField scaleXField;
    private javax.swing.JFormattedTextField scaleYField;
    private javax.swing.JFormattedTextField scaleZField;
    private javax.swing.JLabel xLabel;
    private javax.swing.JLabel yLabel;
    private javax.swing.JLabel zLabel;
    // End of variables declaration//GEN-END:variables
}
