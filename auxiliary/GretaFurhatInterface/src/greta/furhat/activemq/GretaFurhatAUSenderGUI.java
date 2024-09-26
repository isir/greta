/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.furhat.activemq;

import greta.core.keyframes.face.AUPerformer;
import greta.core.repositories.AUAPFrame;
import greta.core.util.IniManager;
import greta.core.util.id.ID;
import java.awt.Color;
import java.util.Locale;
import greta.furhat.activemq.ConnectionListener;
import java.util.List;

/**
 *
 * @author Fousseyni Sangaré 04/2024-09/2024
 */
public class GretaFurhatAUSenderGUI extends javax.swing.JFrame implements ConnectionListener, AUPerformer {
    
    private GretaFurhatAUSender ausender; // = new GretaFurhatAUSender();
    
    
     private static final Color green = new Color(0,150,0);
    protected String host = "";
    protected String port = "";
    protected String topic = "";

    private String statusProperty = "word.status";
    private String notConnectedProperty = "network.notconnected";
    private String connectedProperty = "network.connected";
    private String hostProperty = "network.host";
    private String portProperty = "network.port";
    private String topicProperty = "network.topic";
    private String destinationProperty = "network.destination";
    private String actualConnectedProperty;

    
    /**
     * Creates new form GretaFurhatAUSenderGUI
     */
    public GretaFurhatAUSenderGUI() {
        initComponents();
        setAUSender();
        notConnected();
    }
    
    
    public void setConnected(boolean connected){
        if(connected){
            actualConnectedProperty = connectedProperty;
            statusLabel.setForeground(green);
        }
        else{
            actualConnectedProperty = notConnectedProperty;
            statusLabel.setForeground(Color.RED);
        }
        updateConnectedLabel();
    }

    @Override
    public void onDisconnection() {
        //setConnected(false);
    }

    @Override
    public void onConnection() {
        //setConnected(true);
    }
    
    public void setAUSender(){
        GretaFurhatAUSender wb = new GretaFurhatAUSender();
        this.ausender = wb;
        setHostValue(ausender.getHost());
        setPortValue(ausender.getPort());
        setDestinationValue(ausender.getTopic());
        setConnected(ausender.isConnected());
        ausender.addConnectionListener(this);
    }

    protected void setHostValue(String value){
        host = value;
        hostField.setText(host);
    }

    protected void setPortValue(String value){
        port = value;
        portField.setText(port);
    }

    protected void setDestinationValue(String value){
        topic = value;
        topicField.setText(topic);
    }


    private void updateConnection(){
        if( ! host.equals(hostField.getText()) ||
            ! port.equals(portField.getText())){
            host = hostField.getText();
            port = portField.getText();
            updateURL(host, port);
        }
        if( ! topic.equals(topicField.getText())){
            topic = topicField.getText();
            updateDestination(topic, false);
        }
        
    }

    protected void changeDestinationLabel(String newProperty){
        destinationProperty = newProperty;
        updateLabelWithColon(topicLabel, destinationProperty);
    }

   

    protected void updateURL(String host, String port){
        if(ausender != null) {
            ausender.setURL(host, port);
        }
    }

    protected void updateDestination(String name, boolean isQueue){
        if(ausender != null) {
            ausender.setDestination(name, false);
        }
    }

    private void notConnected(){
        if ((ausender.isConnected()) || (!activateButton.isSelected())){
            ausender.stopConnection();
        }
        
        setConnected(false);
    }
    
    
    @Override
    public void performAUAPFrame(AUAPFrame auapAnimation, ID requestId){
        
        if ((sendAUBox.isSelected()) && (ausender.isConnected())){
            //System.out.println("Hello from gretaFurhatAUPerformerGUI: "+auapAnimation.APVector.toString() + " ; id: "+requestId.toString()+ "url: "+ausender.getURL()+ " topic: "+ausender.getTopic());
            ausender.send(auapAnimation.APVector.toString());
        }
        
    }
    
    @Override
    public void performAUAPFrames(List<AUAPFrame> auapAnimation,  ID requestId){
        
        if ((sendAUBox.isSelected()) && (ausender.isConnected())){
            
            //System.out.println("Hello from gretaFurhatAUPerformsGUI: "+auapAnimation.size() + " ; id: "+requestId.toString()+ "url: "+ausender.getURL()+ " topic: "+ausender.getTopic());
            //System.out.println(auapAnimation.get(0).APVector.toString());
            for (AUAPFrame auFrame : auapAnimation) {
                ausender.send(auFrame.APVector.toString());
            }
        }
        
        
    }

    @Override
    public void setLocale(Locale l) {
        super.setLocale(l);
        updateConnectedLabel();
        updateLabelWithColon(statusLabel, statusProperty);
        updateLabelWithColon(hostLabel, hostProperty);
        updateLabelWithColon(portLabel, portProperty);
        updateLabelWithColon(topicLabel, destinationProperty);
        
    }
    

    private void updateConnectedLabel(){
        if(statusLabel!=null) {
            statusLabel.setText(IniManager.getLocaleProperty(actualConnectedProperty));
        }
    }

    private void updateLabelWithColon(javax.swing.JLabel label, String property){
        if(label!=null) {
            label.setText(IniManager.getLocaleProperty(property)+":");
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

        jRadioButton1 = new javax.swing.JRadioButton();
        topicLabel = new javax.swing.JLabel();
        hostLabel = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        sendAUBox = new javax.swing.JCheckBox();
        statusLabel = new javax.swing.JLabel();
        hostField = new javax.swing.JTextField();
        topicField = new javax.swing.JTextField();
        activateButton = new javax.swing.JRadioButton();

        jRadioButton1.setText("jRadioButton1");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        topicLabel.setText("topic");

        hostLabel.setText("host");

        portField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                portFieldFocusLost(evt);
            }
        });
        portField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portFieldActionPerformed(evt);
            }
        });
        portField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                portFieldKeyTyped(evt);
            }
        });

        portLabel.setText("port");

        sendAUBox.setText("Send");
        sendAUBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendAUBoxActionPerformed(evt);
            }
        });

        statusLabel.setText("status");

        hostField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                hostFieldFocusLost(evt);
            }
        });
        hostField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostFieldActionPerformed(evt);
            }
        });

        topicField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                topicFieldFocusLost(evt);
            }
        });
        topicField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                topicFieldActionPerformed(evt);
            }
        });

        activateButton.setText("activate");
        activateButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                activateButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(hostLabel)
                        .addContainerGap())
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                                .addComponent(hostField, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(topicLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(portLabel, javax.swing.GroupLayout.Alignment.LEADING)
                                .addComponent(portField, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 70, Short.MAX_VALUE))
                            .addComponent(topicField, javax.swing.GroupLayout.PREFERRED_SIZE, 113, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 74, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addComponent(activateButton)
                                .addGap(108, 108, 108))
                            .addGroup(layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(statusLabel, javax.swing.GroupLayout.PREFERRED_SIZE, 114, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(sendAUBox))
                                .addContainerGap())))))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(65, 65, 65)
                        .addComponent(hostLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(topicLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(83, 83, 83)
                        .addComponent(activateButton)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(topicField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(portLabel))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(statusLabel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(46, 46, 46)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(sendAUBox))
                .addContainerGap(56, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void portFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portFieldActionPerformed
        // TODO add your handling code here:
        updateConnection();
    }//GEN-LAST:event_portFieldActionPerformed

    private void portFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_portFieldFocusLost
        // TODO add your handling code here:
        updateConnection();
    }//GEN-LAST:event_portFieldFocusLost

    private void hostFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostFieldActionPerformed
        // TODO add your handling code here:
        updateConnection();
    }//GEN-LAST:event_hostFieldActionPerformed

    private void hostFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hostFieldFocusLost
        // TODO add your handling code here:
        updateConnection();
    }//GEN-LAST:event_hostFieldFocusLost

    private void topicFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_topicFieldActionPerformed
        // TODO add your handling code here:
        updateConnection();
    }//GEN-LAST:event_topicFieldActionPerformed

    private void topicFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_topicFieldFocusLost
        // TODO add your handling code here:
        updateConnection();
    }//GEN-LAST:event_topicFieldFocusLost

    private void portFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_portFieldKeyTyped
        // TODO add your handling code here:
        char pressed = evt.getKeyChar();
        if(Character.isDigit(pressed)){
            String beforeselect = portField.getText().substring(0, portField.getSelectionStart());
            String afterSelect = portField.getText().substring(portField.getSelectionEnd());
            long value = Long.parseLong(beforeselect + pressed + afterSelect);
            if (0 <= value && value <= 65535) {
                return;//it's ok
            }
        }
        evt.consume();
    }//GEN-LAST:event_portFieldKeyTyped

    private void sendAUBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendAUBoxActionPerformed
        // TODO add your handling code here:
        updateConnection();
    }//GEN-LAST:event_sendAUBoxActionPerformed

    private void activateButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_activateButtonActionPerformed
        // TODO add your handling code here:       
        if (activateButton.isSelected()){
            ausender.startConnection();
            setConnected(true);
        }
        else{
            notConnected();
        } 
    }//GEN-LAST:event_activateButtonActionPerformed
                                                                                                                                                                                                                    

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JRadioButton activateButton;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JCheckBox sendAUBox;
    private javax.swing.JLabel statusLabel;
    private javax.swing.JTextField topicField;
    private javax.swing.JLabel topicLabel;
    // End of variables declaration//GEN-END:variables
}
