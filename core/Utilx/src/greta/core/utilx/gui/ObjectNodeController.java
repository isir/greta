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

import greta.core.signals.GazeSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.IniManager;
import greta.core.util.environment.TreeNode;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import static greta.core.util.enums.CompositionType.blend;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import static greta.core.util.id.IDProvider.createID;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Timer;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andre-Marie Pez
 */
public class ObjectNodeController extends javax.swing.JFrame implements SignalEmitter{

    TreeNode node;
    TreeNode node_gaze;
    private CharacterManager cm;
    private List<SignalPerformer> signalPerformers;
    private List<Signal> selectedSignals;
    /** Creates new form TreeNodeController */
    public ObjectNodeController(CharacterManager cm) {
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
        this.cm=cm;
        idField.setHorizontalAlignment(javax.swing.JTextField.TRAILING);
        setTreeNode((TreeNode) this.cm.getEnvironment().getNode("Andre_chair"));
        setTreeNode_Gaze((TreeNode) this.cm.getEnvironment().getNode("Gaze_target"));
        signalPerformers = new ArrayList<SignalPerformer>();
        MyThreadGaze t = new MyThreadGaze(this);
        t.start();
        
    }

    public void setTreeNode(TreeNode node){
        this.node = node;
        readPosition();
        readOrientation();
        readScale();
        readId();
        
    }
    
      

    public void setTreeNode_Gaze(TreeNode node){
        this.node_gaze = node;
        readPosition_g();
        readOrientation_g();
        readScale_g();
        readId_g();
        
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
        posXField1.setEnabled(b);
        posYField1.setEnabled(b);
        posZField1.setEnabled(b);
        orientXField1.setEnabled(b);
        orientYField1.setEnabled(b);
        orientZField1.setEnabled(b);
        scaleXField1.setEnabled(b);
        scaleYField1.setEnabled(b);
        scaleZField1.setEnabled(b);
    }
    
    


    private void isDoubleTyped(java.awt.event.KeyEvent evt, javax.swing.JFormattedTextField field){
        ToolBox.checkDouble(evt, field);
    }
// GAZE TO OBJECT
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

    
//GAZE 


    private void updatePosition_g(){
        if(node_gaze!=null){
            Vec3d pos = node_gaze.getCoordinates();
            node_gaze.setCoordinates(
                    valueOf(posXField1, pos.x()),
                    valueOf(posYField1, pos.y()),
                    valueOf(posZField1, pos.z()));
        }
    }

    private void readPosition_g(){
        if(node_gaze!=null){
            Vec3d pos = node_gaze.getCoordinates();
            posXField1.setText(""+pos.x());
            posYField1.setText(""+pos.y());
            posZField1.setText(""+pos.z());
        }
    }

    private void updateScale_g(){
        if(node_gaze!=null){
            Vec3d scale = node_gaze.getScale();
            node_gaze.setScale(
                    valueOf(scaleXField1, scale.x()),
                    valueOf(scaleYField1, scale.y()),
                    valueOf(scaleZField1, scale.z()));
        }
    }

    private void readScale_g(){
        if(node_gaze!=null){
            Vec3d scale = node_gaze.getScale();
            scaleXField1.setText(""+scale.x());
            scaleYField1.setText(""+scale.y());
            scaleZField1.setText(""+scale.z());
        }
    }

    private void updateOrientation_g(){
        if(node_gaze!=null){
            Vec3d orient = node_gaze.getOrientation().getEulerAngleXYZByAngle();
            Quaternion q = new Quaternion();
            q.fromEulerXYZByAngle(
                    valueOf(orientXField1, orient.x()),
                    valueOf(orientYField1, orient.y()),
                    valueOf(orientZField1, orient.z()));
            node_gaze.setOrientation(q);
        }
    }

    private void readOrientation_g(){
        if(node_gaze!=null){
            Vec3d orient = node_gaze.getOrientation().getEulerAngleXYZByAngle();
            orientXField1.setText(""+orient.x());
            orientYField1.setText(""+orient.y());
            orientZField1.setText(""+orient.z());
        }
    }

    private void readId_g(){
        if(node_gaze!=null){
            idField1.setText(node_gaze.getIdentifier());
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

        jPanel1 = new javax.swing.JPanel();
        scaleZField1 = new javax.swing.JFormattedTextField();
        scaleYField1 = new javax.swing.JFormattedTextField();
        scaleXField1 = new javax.swing.JFormattedTextField();
        meterLabel1 = new javax.swing.JLabel();
        degreeLabel1 = new javax.swing.JLabel();
        xLabel1 = new javax.swing.JLabel();
        yLabel1 = new javax.swing.JLabel();
        zLabel1 = new javax.swing.JLabel();
        idField1 = new javax.swing.JFormattedTextField();
        posLabel1 = new javax.swing.JLabel();
        posXField1 = new javax.swing.JFormattedTextField();
        posYField1 = new javax.swing.JFormattedTextField();
        posZField1 = new javax.swing.JFormattedTextField();
        oreintLabel1 = new javax.swing.JLabel();
        orientZField1 = new javax.swing.JFormattedTextField();
        orientYField1 = new javax.swing.JFormattedTextField();
        orientXField1 = new javax.swing.JFormattedTextField();
        idLabel1 = new javax.swing.JLabel();
        scaleLabel1 = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();
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
        posLabel = new javax.swing.JLabel();
        posXField = new javax.swing.JFormattedTextField();
        posYField = new javax.swing.JFormattedTextField();
        posZField = new javax.swing.JFormattedTextField();
        oreintLabel = new javax.swing.JLabel();
        orientZField = new javax.swing.JFormattedTextField();
        orientYField = new javax.swing.JFormattedTextField();
        orientXField = new javax.swing.JFormattedTextField();
        idLabel = new javax.swing.JLabel();
        sliding_object = new javax.swing.JCheckBox();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Gaze", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        scaleZField1.setText("0.0");
        scaleZField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleZField1ActionPerformed(evt);
            }
        });
        scaleZField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                scaleZField1FocusLost(evt);
            }
        });
        scaleZField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                scaleZField1KeyTyped(evt);
            }
        });

        scaleYField1.setText("0.0");
        scaleYField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleYField1ActionPerformed(evt);
            }
        });
        scaleYField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                scaleYField1FocusLost(evt);
            }
        });
        scaleYField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                scaleYField1KeyTyped(evt);
            }
        });

        scaleXField1.setText("0.0");
        scaleXField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                scaleXField1ActionPerformed(evt);
            }
        });
        scaleXField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                scaleXField1FocusLost(evt);
            }
        });
        scaleXField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                scaleXField1KeyTyped(evt);
            }
        });

        meterLabel1.setText("("+IniManager.getLocaleProperty("unit.meter").toLowerCase() +") ");

        degreeLabel1.setText("("+IniManager.getLocaleProperty("unit.degree").toLowerCase() +") ");

        xLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        xLabel1.setText("X");

        yLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        yLabel1.setText("Y");

        zLabel1.setHorizontalAlignment(javax.swing.SwingConstants.TRAILING);
        zLabel1.setText("Z");

        idField1.setEditable(false);
        idField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                idField1ActionPerformed(evt);
            }
        });
        idField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                idField1FocusLost(evt);
            }
        });
        idField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                idField1KeyTyped(evt);
            }
        });

        posLabel1.setText(IniManager.getLocaleProperty("word.position"));

        posXField1.setText("0.0");
        posXField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posXField1ActionPerformed(evt);
            }
        });
        posXField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                posXField1FocusLost(evt);
            }
        });
        posXField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                posXField1KeyTyped(evt);
            }
        });

        posYField1.setText("0.0");
        posYField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posYField1ActionPerformed(evt);
            }
        });
        posYField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                posYField1FocusLost(evt);
            }
        });
        posYField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                posYField1KeyTyped(evt);
            }
        });

        posZField1.setText("0.0");
        posZField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                posZField1ActionPerformed(evt);
            }
        });
        posZField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                posZField1FocusLost(evt);
            }
        });
        posZField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                posZField1KeyTyped(evt);
            }
        });

        oreintLabel1.setText(IniManager.getLocaleProperty("word.orientation"));

        orientZField1.setText("0.0");
        orientZField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orientZField1ActionPerformed(evt);
            }
        });
        orientZField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                orientZField1FocusLost(evt);
            }
        });
        orientZField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                orientZField1KeyTyped(evt);
            }
        });

        orientYField1.setText("0.0");
        orientYField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orientYField1ActionPerformed(evt);
            }
        });
        orientYField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                orientYField1FocusLost(evt);
            }
        });
        orientYField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                orientYField1KeyTyped(evt);
            }
        });

        orientXField1.setText("0.0");
        orientXField1.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                orientXField1FocusLost(evt);
            }
        });
        orientXField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orientXField1ActionPerformed(evt);
            }
        });
        orientXField1.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                orientXField1KeyTyped(evt);
            }
        });

        idLabel1.setText(IniManager.getLocaleProperty("word.identifier"));

        scaleLabel1.setText(IniManager.getLocaleProperty("word.scale"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(posLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(oreintLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(scaleLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(idLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(orientXField1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orientYField1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(orientZField1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(degreeLabel1))
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(xLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(posXField1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(yLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(posYField1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(zLabel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(posZField1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(meterLabel1))
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(idField1, javax.swing.GroupLayout.Alignment.LEADING)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel1Layout.createSequentialGroup()
                            .addComponent(scaleXField1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(scaleYField1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(scaleZField1, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xLabel1)
                    .addComponent(yLabel1)
                    .addComponent(zLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(posXField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(posYField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(posZField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(posLabel1)
                    .addComponent(meterLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(orientXField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(orientYField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(orientZField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(oreintLabel1)
                    .addComponent(degreeLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scaleXField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scaleYField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scaleZField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scaleLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(idField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(idLabel1))
                .addGap(0, 28, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Gaze Object", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

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
        orientXField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                orientXFieldFocusLost(evt);
            }
        });
        orientXField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                orientXFieldActionPerformed(evt);
            }
        });
        orientXField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                orientXFieldKeyTyped(evt);
            }
        });

        idLabel.setText(IniManager.getLocaleProperty("word.identifier"));

        sliding_object.setText("sliding_object");
        sliding_object.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sliding_objectActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addGap(0, 0, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(sliding_object)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(posLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(oreintLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(scaleLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(idLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(orientXField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(orientYField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(orientZField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(degreeLabel))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(xLabel, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(posXField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(yLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(posYField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                    .addComponent(zLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                    .addComponent(posZField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(meterLabel))
                            .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(idField, javax.swing.GroupLayout.Alignment.LEADING)
                                .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                                    .addComponent(scaleXField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(scaleYField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                    .addComponent(scaleZField, javax.swing.GroupLayout.PREFERRED_SIZE, 100, javax.swing.GroupLayout.PREFERRED_SIZE)))))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(xLabel)
                    .addComponent(yLabel)
                    .addComponent(zLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(posXField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(posYField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(posZField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(posLabel)
                    .addComponent(meterLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(orientXField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(orientYField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(orientZField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(oreintLabel)
                    .addComponent(degreeLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(scaleXField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scaleYField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scaleZField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(scaleLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(idField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(idLabel))
                .addGap(18, 18, 18)
                .addComponent(sliding_object)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 24, Short.MAX_VALUE))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(31, 31, 31)
                .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(96, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void scaleZField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleZField1ActionPerformed
        updateScale_g();
    }//GEN-LAST:event_scaleZField1ActionPerformed

    private void scaleZField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_scaleZField1FocusLost
         updateScale_g();
    }//GEN-LAST:event_scaleZField1FocusLost

    private void scaleZField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_scaleZField1KeyTyped
        isDoubleTyped(evt,scaleZField1);
    }//GEN-LAST:event_scaleZField1KeyTyped

    private void scaleYField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleYField1ActionPerformed
        updateScale_g();
    }//GEN-LAST:event_scaleYField1ActionPerformed

    private void scaleYField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_scaleYField1FocusLost
        updateScale_g();
    }//GEN-LAST:event_scaleYField1FocusLost

    private void scaleYField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_scaleYField1KeyTyped
        isDoubleTyped(evt,scaleYField1);
    }//GEN-LAST:event_scaleYField1KeyTyped

    private void scaleXField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleXField1ActionPerformed
        updateScale_g();
    }//GEN-LAST:event_scaleXField1ActionPerformed

    private void scaleXField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_scaleXField1FocusLost
         updateScale_g();
    }//GEN-LAST:event_scaleXField1FocusLost

    private void scaleXField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_scaleXField1KeyTyped
        isDoubleTyped(evt,scaleXField1);
    }//GEN-LAST:event_scaleXField1KeyTyped

    private void idField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_idField1ActionPerformed

    private void idField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_idField1FocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_idField1FocusLost

    private void idField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idField1KeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_idField1KeyTyped

    private void posXField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posXField1ActionPerformed
        updatePosition_g();
    }//GEN-LAST:event_posXField1ActionPerformed

    private void posXField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_posXField1FocusLost
        updatePosition_g();
    }//GEN-LAST:event_posXField1FocusLost

    private void posXField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_posXField1KeyTyped
        isDoubleTyped(evt,posXField1);
    }//GEN-LAST:event_posXField1KeyTyped

    private void posYField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posYField1ActionPerformed
        updatePosition_g();
    }//GEN-LAST:event_posYField1ActionPerformed

    private void posYField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_posYField1FocusLost
        updatePosition_g();
    }//GEN-LAST:event_posYField1FocusLost

    private void posYField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_posYField1KeyTyped
        isDoubleTyped(evt,posYField1);
    }//GEN-LAST:event_posYField1KeyTyped

    private void posZField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posZField1ActionPerformed
        updatePosition_g();
    }//GEN-LAST:event_posZField1ActionPerformed

    private void posZField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_posZField1FocusLost
        updatePosition_g();
    }//GEN-LAST:event_posZField1FocusLost

    private void posZField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_posZField1KeyTyped
        isDoubleTyped(evt,posZField1);
    }//GEN-LAST:event_posZField1KeyTyped

    private void orientZField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientZField1ActionPerformed
        updateOrientation_g();
    }//GEN-LAST:event_orientZField1ActionPerformed

    private void orientZField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_orientZField1FocusLost
        updateOrientation_g();
    }//GEN-LAST:event_orientZField1FocusLost

    private void orientZField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_orientZField1KeyTyped
        isDoubleTyped(evt,orientZField1);
    }//GEN-LAST:event_orientZField1KeyTyped

    private void orientYField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientYField1ActionPerformed
        updateOrientation_g();
    }//GEN-LAST:event_orientYField1ActionPerformed

    private void orientYField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_orientYField1FocusLost
        updateOrientation_g();
    }//GEN-LAST:event_orientYField1FocusLost

    private void orientYField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_orientYField1KeyTyped
        isDoubleTyped(evt,orientYField1);
    }//GEN-LAST:event_orientYField1KeyTyped

    private void orientXField1FocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_orientXField1FocusLost
        updateOrientation_g();
    }//GEN-LAST:event_orientXField1FocusLost

    private void orientXField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientXField1ActionPerformed
        updateOrientation_g();
    }//GEN-LAST:event_orientXField1ActionPerformed

    private void orientXField1KeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_orientXField1KeyTyped
        isDoubleTyped(evt,orientXField1);
    }//GEN-LAST:event_orientXField1KeyTyped

    private void sliding_objectActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sliding_objectActionPerformed
        // TODO add your handling code here:
        MyThread t = new MyThread(this);
        t.start();
    }//GEN-LAST:event_sliding_objectActionPerformed

    private void orientXFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_orientXFieldKeyTyped
        isDoubleTyped(evt,orientXField);
    }//GEN-LAST:event_orientXFieldKeyTyped

    private void orientXFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientXFieldActionPerformed
        updateOrientation();
    }//GEN-LAST:event_orientXFieldActionPerformed

    private void orientXFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_orientXFieldFocusLost
        updateOrientation();
    }//GEN-LAST:event_orientXFieldFocusLost

    private void orientYFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_orientYFieldKeyTyped
        isDoubleTyped(evt,orientYField);
    }//GEN-LAST:event_orientYFieldKeyTyped

    private void orientYFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_orientYFieldFocusLost
        updateOrientation();
    }//GEN-LAST:event_orientYFieldFocusLost

    private void orientYFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientYFieldActionPerformed
        updateOrientation();
    }//GEN-LAST:event_orientYFieldActionPerformed

    private void orientZFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_orientZFieldKeyTyped
        isDoubleTyped(evt,orientZField);
    }//GEN-LAST:event_orientZFieldKeyTyped

    private void orientZFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_orientZFieldFocusLost
        updateOrientation();
    }//GEN-LAST:event_orientZFieldFocusLost

    private void orientZFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_orientZFieldActionPerformed
        updateOrientation();
    }//GEN-LAST:event_orientZFieldActionPerformed

    private void posZFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_posZFieldKeyTyped
        isDoubleTyped(evt,posZField);
    }//GEN-LAST:event_posZFieldKeyTyped

    private void posZFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_posZFieldFocusLost
        updatePosition();
    }//GEN-LAST:event_posZFieldFocusLost

    private void posZFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posZFieldActionPerformed
        updatePosition();
    }//GEN-LAST:event_posZFieldActionPerformed

    private void posYFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_posYFieldKeyTyped
        isDoubleTyped(evt,posYField);
    }//GEN-LAST:event_posYFieldKeyTyped

    private void posYFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_posYFieldFocusLost
        updatePosition();
    }//GEN-LAST:event_posYFieldFocusLost

    private void posYFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posYFieldActionPerformed
        updatePosition();
    }//GEN-LAST:event_posYFieldActionPerformed

    private void posXFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_posXFieldKeyTyped
        isDoubleTyped(evt,posXField);
    }//GEN-LAST:event_posXFieldKeyTyped

    private void posXFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_posXFieldFocusLost
        updatePosition();
    }//GEN-LAST:event_posXFieldFocusLost

    private void posXFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_posXFieldActionPerformed
        updatePosition();
    }//GEN-LAST:event_posXFieldActionPerformed

    private void idFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_idFieldKeyTyped
        // TODO add your handling code here:
    }//GEN-LAST:event_idFieldKeyTyped

    private void idFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_idFieldFocusLost
        // TODO add your handling code here:
    }//GEN-LAST:event_idFieldFocusLost

    private void idFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_idFieldActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_idFieldActionPerformed

    private void scaleXFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_scaleXFieldKeyTyped
        isDoubleTyped(evt,scaleXField);
    }//GEN-LAST:event_scaleXFieldKeyTyped

    private void scaleXFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_scaleXFieldFocusLost
        updateScale();
    }//GEN-LAST:event_scaleXFieldFocusLost

    private void scaleXFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleXFieldActionPerformed
        updateScale();
    }//GEN-LAST:event_scaleXFieldActionPerformed

    private void scaleYFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_scaleYFieldKeyTyped
        isDoubleTyped(evt,scaleYField);
    }//GEN-LAST:event_scaleYFieldKeyTyped

    private void scaleYFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_scaleYFieldFocusLost
        updateScale();
    }//GEN-LAST:event_scaleYFieldFocusLost

    private void scaleYFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleYFieldActionPerformed
        updateScale();
    }//GEN-LAST:event_scaleYFieldActionPerformed

    private void scaleZFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_scaleZFieldKeyTyped
        isDoubleTyped(evt,scaleZField);
    }//GEN-LAST:event_scaleZFieldKeyTyped

    private void scaleZFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_scaleZFieldFocusLost
        updateScale();
    }//GEN-LAST:event_scaleZFieldFocusLost

    private void scaleZFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_scaleZFieldActionPerformed
        updateScale();
    }//GEN-LAST:event_scaleZFieldActionPerformed

    @Override
    public void addSignalPerformer(SignalPerformer performer) {
        if (performer != null) {
            signalPerformers.add(performer);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer performer) {
        if (performer != null) {
            signalPerformers.remove(performer);
        }
    }
    
        protected void sendSignals(List<Signal> signals, ID id, Mode mode) {
        if (signals != null) {
            System.out.println("greta.core.behaviorplanner.Planner.sendSignals()");
            for(Signal s:signals){
                System.out.println("Signals Class "+s.getClass());
            }
            for (SignalPerformer performer : signalPerformers) {
                performer.performSignals(signals, id, mode);
            }
        }
    }
/*
    public void slide_object(){
        if(sliding_object.isEnabled()){
            double value= -10;
            while(value<10){
            if(node!=null){
                Vec3d pos = node.getCoordinates();
                posXField.setText(Double.toString(value));
                node.setCoordinates(
                        valueOf(posXField, pos.x()),
                        valueOf(posYField, pos.y()),
                        valueOf(posZField, pos.z()));
            }
                try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ObjectNodeController.class.getName()).log(Level.SEVERE, null, ex);
                }
            value+=0.05;
                System.out.println("VALORE:"+value);
            }     
        }

    }
  */  
     public class MyThreadGaze extends Thread{
         
         public ObjectNodeController obj;
         public MyThreadGaze(ObjectNodeController obj){
             this.obj=obj;
         }
         
         public void run(){
             while(true){
            if(this.obj.cm.getGaze_t().isGaze_object()){
               if(this.obj.node!=null){
                Vec3d pos = this.obj.node.getCoordinates();
                double x = this.obj.cm.getGaze_t().getPosX();
                double y = this.obj.cm.getGaze_t().getPosY();
                double z = this.obj.cm.getGaze_t().getPosZ();
                this.obj.cm.getGaze_t().setPosX(pos.x()+x);
                this.obj.cm.getGaze_t().setPosY(pos.y()+y);
                this.obj.cm.getGaze_t().setPosZ(pos.z());
                this.obj.posXField1.setText(Double.toString(this.obj.cm.getGaze_t().getPosX()));
                this.obj.posYField1.setText(Double.toString(this.obj.cm.getGaze_t().getPosY()));
                this.obj.posZField1.setText(Double.toString(this.obj.cm.getGaze_t().getPosZ()));
               }
            }
            else{
            this.obj.posXField1.setText(Double.toString(this.obj.cm.getGaze_t().getPosX()));
            this.obj.posYField1.setText(Double.toString(this.obj.cm.getGaze_t().getPosY()));
            
            }
             }
         
         }
         
     }
     public class MyThread extends Thread {
         
        public ObjectNodeController obj;
        public MyThread(ObjectNodeController obj){
            this.obj=obj;
            
        }
        public void run(){
            selectedSignals = new ArrayList<Signal>();
            if(this.obj.sliding_object.isSelected()){
            double value= -10;
            boolean flag_0=false;
            boolean flag_m5=false;
            boolean flag_5=false;
            boolean flag_10=false;
            int time_start=0;
            int time_end=0;
            while(value<10){
            selectedSignals.clear();
            if(this.obj.node!=null){
                Vec3d pos = this.obj.node.getCoordinates();
                this.obj.posXField.setText(Double.toString(value));
                this.obj.node.setCoordinates(
                        valueOf(this.obj.posXField, pos.x()),
                        valueOf(this.obj.posYField, pos.y()),
                        valueOf(this.obj.posZField, pos.z()));
                        GazeSignal gaze = new GazeSignal("1");
                        gaze.setStartValue("1");
                        gaze.setTarget("Andre_chair0");
                        gaze.setGazeShift(true);
                        selectedSignals=new ArrayList<Signal>();
                        selectedSignals.add(gaze);
                        CompositionType mode = blend;
                        Mode mod= new Mode(mode);
                        IDProvider idp= new IDProvider();
                        ID id = createID(",lmsdvsdv,m");
                        //System.out.println("VALORE:"+(int) pos.x()+"   "+flag_0+"   "+flag_m5+"   "+flag_5+"   "+flag_10);
                        //if((int)pos.x()==0 && !flag_0){
                        //sendSignals(selectedSignals,id , mod);
                        //flag_0=true;
                        //}
                        //if((int)pos.x()==-5 && !flag_m5){
                        time_end++;
                        int fin=time_end-time_start;
                        System.out.println(time_end+"  "+time_start+"   "+fin);
                        if(fin==2000){
                            sendSignals(selectedSignals,id , mod);
                            time_start=time_end;
                        try {
                    TimeUnit.MICROSECONDS.sleep(5);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ObjectNodeController.class.getName()).log(Level.SEVERE, null, ex);
                }
                        
                        }
                        //}
                        //if((int)pos.x()==5 && !flag_5){
                        //sendSignals(selectedSignals,id , mod);
                        //flag_5=true;
                        //}
                        //if((int)pos.x()==10 && !flag_10){
                        //sendSignals(selectedSignals,id , mod);
                        //flag_10=true;
                       // }
            }
               /* try {
                    TimeUnit.MILLISECONDS.sleep(10);
                } catch (InterruptedException ex) {
                    Logger.getLogger(ObjectNodeController.class.getName()).log(Level.SEVERE, null, ex);
                }
*/
            value+=0.00005;
            }     
        }
       
    }
  }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel degreeLabel;
    private javax.swing.JLabel degreeLabel1;
    private javax.swing.JFormattedTextField idField;
    private javax.swing.JFormattedTextField idField1;
    private javax.swing.JLabel idLabel;
    private javax.swing.JLabel idLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JLabel meterLabel;
    private javax.swing.JLabel meterLabel1;
    private javax.swing.JLabel oreintLabel;
    private javax.swing.JLabel oreintLabel1;
    private javax.swing.JFormattedTextField orientXField;
    private javax.swing.JFormattedTextField orientXField1;
    private javax.swing.JFormattedTextField orientYField;
    private javax.swing.JFormattedTextField orientYField1;
    private javax.swing.JFormattedTextField orientZField;
    private javax.swing.JFormattedTextField orientZField1;
    private javax.swing.JLabel posLabel;
    private javax.swing.JLabel posLabel1;
    private javax.swing.JFormattedTextField posXField;
    private javax.swing.JFormattedTextField posXField1;
    private javax.swing.JFormattedTextField posYField;
    private javax.swing.JFormattedTextField posYField1;
    private javax.swing.JFormattedTextField posZField;
    private javax.swing.JFormattedTextField posZField1;
    private javax.swing.JLabel scaleLabel;
    private javax.swing.JLabel scaleLabel1;
    private javax.swing.JFormattedTextField scaleXField;
    private javax.swing.JFormattedTextField scaleXField1;
    private javax.swing.JFormattedTextField scaleYField;
    private javax.swing.JFormattedTextField scaleYField1;
    private javax.swing.JFormattedTextField scaleZField;
    private javax.swing.JFormattedTextField scaleZField1;
    private javax.swing.JCheckBox sliding_object;
    private javax.swing.JLabel xLabel;
    private javax.swing.JLabel xLabel1;
    private javax.swing.JLabel yLabel;
    private javax.swing.JLabel yLabel1;
    private javax.swing.JLabel zLabel;
    private javax.swing.JLabel zLabel1;
    // End of variables declaration//GEN-END:variables
}
