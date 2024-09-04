/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.activemq.gui;

import greta.auxiliary.activemq.ConnectionListener;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.util.IniManager;
import java.awt.Color;
import java.util.Locale;

/**
 *
 * @author Andre-Marie Pez
 */
public class WhitboardFrame extends javax.swing.JFrame implements ConnectionListener{

    private static final Color green = new Color(0,150,0);

    private WhiteBoard whiteboard;
    protected String host = "";
    protected String port = "";
    protected String destination = "";
    protected boolean isQueue = false;

    private String statusProperty = "word.status";
    private String notConnectedProperty = "network.notconnected";
    private String connectedProperty = "network.connected";
    private String hostProperty = "network.host";
    private String portProperty = "network.port";
    private String topicProperty = "network.topic";
    private String destinationProperty = "network.destination";
    private String queueProperty = "network.queue";
    private String actualConnectedProperty;

    /** Creates new form WhitboardFrame */
    public WhitboardFrame() {
        initComponents();
        notConnected();
    }

    public void setConnected(boolean connected){
        if(connected){
            actualConnectedProperty = connectedProperty;
            connectedLabel.setForeground(green);
        }
        else{
            actualConnectedProperty = notConnectedProperty;
            connectedLabel.setForeground(Color.RED);
        }
        updateConnectedLabel();
    }

    @Override
    public void onDisconnection() {
        setConnected(false);
    }

    @Override
    public void onConnection() {
        setConnected(true);
    }

    public void setWhitboard(WhiteBoard wb){
        this.whiteboard = wb;
        setHostValue(whiteboard.getHost());
        setPortValue(whiteboard.getPort());
        setDestinationValue(whiteboard.getTopic());
        setIsQueueValue(whiteboard.isQueue());
        setConnected(whiteboard.isConnected());
        whiteboard.addConnectionListener(this);
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
        destination = value;
        destinationField.setText(destination);
    }

    protected void setIsQueueValue(boolean value){
        isQueue = value;
        isTopicButton.setSelected(!isQueue);
        isQueueButton.setSelected(isQueue);
    }

    private void updateConnection(){
        if( ! host.equals(hostField.getText()) ||
            ! port.equals(portField.getText())){
            host = hostField.getText();
            port = portField.getText();
            updateURL(host, port);
        }
        if( ! destination.equals(destinationField.getText()) || isQueue != isQueueButton.isSelected()){
            destination = destinationField.getText();
            isQueue = isQueueButton.isSelected();
            updateDestination(destination, isQueue);
        }
    }

    protected void changeDestinationLabel(String newProperty){
        destinationProperty = newProperty;
        updateLabelWithColon(destinationLabel, destinationProperty);
    }

    protected void removeTopicAndQueueOption(){
        jPanel1.remove(isTopicButton);
        jPanel1.remove(isQueueButton);
    }

    protected void updateURL(String host, String port){
        if(whiteboard != null) {
            whiteboard.setURL(host, port);
        }
    }

    protected void updateDestination(String name, boolean isQueue){
        if(whiteboard != null) {
            whiteboard.setDestination(name, isQueue);
        }
    }

    private void notConnected(){
        setConnected(false);
    }

    @Override
    public void setLocale(Locale l) {
        super.setLocale(l);
        updateConnectedLabel();
        updateLabelWithColon(statusLabel, statusProperty);
        updateLabelWithColon(hostLabel, hostProperty);
        updateLabelWithColon(portLabel, portProperty);
        updateLabelWithColon(destinationLabel, destinationProperty);
        if(isTopicButton!=null){
            isTopicButton.setText(IniManager.getLocaleProperty(topicProperty));
        }
        if(isQueueButton!=null){
            isQueueButton.setText(IniManager.getLocaleProperty(queueProperty));
        }
    }


    private void updateConnectedLabel(){
        if(connectedLabel!=null) {
            connectedLabel.setText(IniManager.getLocaleProperty(actualConnectedProperty));
        }
    }

    private void updateLabelWithColon(javax.swing.JLabel label, String property){
        if(label!=null) {
            label.setText(IniManager.getLocaleProperty(property)+":");
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        hostLabel = new javax.swing.JLabel();
        hostField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        destinationLabel = new javax.swing.JLabel();
        destinationField = new javax.swing.JTextField();
        statusLabel = new javax.swing.JLabel();
        connectedLabel = new javax.swing.JLabel();
        isTopicButton = new javax.swing.JRadioButton();
        isQueueButton = new javax.swing.JRadioButton();

        hostLabel.setText(IniManager.getLocaleProperty(hostProperty)+":");

        hostField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                hostFieldActionPerformed(evt);
            }
        });
        hostField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                hostFieldFocusLost(evt);
            }
        });

        portLabel.setText(IniManager.getLocaleProperty(portProperty)+":");

        portField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portFieldActionPerformed(evt);
            }
        });
        portField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                portFieldFocusLost(evt);
            }
        });
        portField.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyTyped(java.awt.event.KeyEvent evt) {
                portFieldKeyTyped(evt);
            }
        });

        destinationLabel.setText(IniManager.getLocaleProperty(destinationProperty)+":");

        destinationField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                destinationFieldActionPerformed(evt);
            }
        });
        destinationField.addFocusListener(new java.awt.event.FocusAdapter() {
            public void focusLost(java.awt.event.FocusEvent evt) {
                destinationFieldFocusLost(evt);
            }
        });

        statusLabel.setText(IniManager.getLocaleProperty(statusProperty)+":");

        connectedLabel.setText(IniManager.getLocaleProperty(actualConnectedProperty));

        buttonGroup1.add(isTopicButton);
        isTopicButton.setSelected(true);
        isTopicButton.setText(IniManager.getLocaleProperty(topicProperty)
        );
        isTopicButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isTopicButtonActionPerformed(evt);
            }
        });

        buttonGroup1.add(isQueueButton);
        isQueueButton.setText(IniManager.getLocaleProperty(queueProperty)
        );
        isQueueButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                isQueueButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(destinationField)
                    .addComponent(portField)
                    .addComponent(hostField)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(statusLabel)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(connectedLabel))
                            .addComponent(hostLabel)
                            .addComponent(portLabel)
                            .addGroup(jPanel1Layout.createSequentialGroup()
                                .addComponent(destinationLabel)
                                .addGap(18, 18, 18)
                                .addComponent(isTopicButton)
                                .addGap(18, 18, 18)
                                .addComponent(isQueueButton)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(statusLabel)
                    .addComponent(connectedLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hostLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(hostField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(portLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(portField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(isQueueButton)
                    .addComponent(destinationLabel)
                    .addComponent(isTopicButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(destinationField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void hostFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_hostFieldActionPerformed
        updateConnection();
    }//GEN-LAST:event_hostFieldActionPerformed

    private void portFieldKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_portFieldKeyTyped
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

    private void portFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portFieldActionPerformed
        updateConnection();
    }//GEN-LAST:event_portFieldActionPerformed

    private void portFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_portFieldFocusLost
        updateConnection();
    }//GEN-LAST:event_portFieldFocusLost

    private void destinationFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_destinationFieldActionPerformed
        updateConnection();
    }//GEN-LAST:event_destinationFieldActionPerformed

    private void destinationFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_destinationFieldFocusLost
        updateConnection();
    }//GEN-LAST:event_destinationFieldFocusLost

    private void hostFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hostFieldFocusLost
        updateConnection();
    }//GEN-LAST:event_hostFieldFocusLost

    private void isTopicButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isTopicButtonActionPerformed
        updateConnection();
    }//GEN-LAST:event_isTopicButtonActionPerformed

    private void isQueueButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_isQueueButtonActionPerformed
        updateConnection();
    }//GEN-LAST:event_isQueueButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JLabel connectedLabel;
    private javax.swing.JTextField destinationField;
    private javax.swing.JLabel destinationLabel;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JRadioButton isQueueButton;
    private javax.swing.JRadioButton isTopicButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables

}
