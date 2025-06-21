/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.auxiliary.activemq.gui;

import greta.auxiliary.activemq.Broker;
import greta.auxiliary.activemq.ConnectionListener;
import greta.core.util.IniManager;
import java.awt.Color;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Locale;

/**
 *
 * @author Andre-Marie Pez
 */
public class BrokerFrame extends javax.swing.JFrame implements ConnectionListener{

    private static final Color green = new Color(0,150,0);

    private Broker broker;
    protected String host = "";
    protected String port = "";

    private String statusProperty = "word.status";
    private String notConnectedProperty = "network.notconnected";
    private String connectedProperty = "network.connected";
    private String hostProperty = "network.host";
    private String portProperty = "network.port";
    private String actualConnectedProperty;

    private BufferedWriter p_stdin;
    
    /** Creates new form BrokerFrame */
    public BrokerFrame() throws Exception {
        initComponents();
        setConnected(false);
        
        broker = new Broker();
        setBroker(broker);
        setConnected(true);
        
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

    public void setBroker(Broker broker){
        
//        this.broker = broker;
//        setHostValue(this.broker.getHost());
//        setPortValue(this.broker.getPort());
//        setConnected(this.broker.isConnected());
//        this.broker.addConnectionListener(this);
        
        System.out.println("[CAUTION] greta.auxiliary.activemq.gui.BrokerFrame: you need to kill this broker manually by yourself.");
        System.out.println("[CAUTION] Since it is launched from commandline command, it is out of control from Java");

        // init shell
        ProcessBuilder builder = new ProcessBuilder("C:/Windows/System32/cmd.exe");
        Process p = null;
        try {
            p = builder.start();
            Runtime.getRuntime().addShutdownHook(new ShutdownHook(p));
        } catch (IOException e) {
            System.out.println(e);
        }
        // get stdin of shell
        p_stdin = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

        // execute commands
        executeCommand("cd Common\\Lib\\External\\apache-activemq-5.15.14\\bin");
        executeCommand("activemq start");
        System.out.println("ActiveMQ Broker started");
        
        
        
    }

    protected void setHostValue(String value){
        host = value;
        hostField.setText(host);
    }

    protected void setPortValue(String value){
        port = value;
        portField.setText(port);
    }

    private void updateConnection(){
        if(! port.equals(portField.getText())){
            port = portField.getText();
            updatePort(port);
        }
    }

    protected void updatePort(String port){
        if(broker != null) {
            broker.setPort(port);
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

        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();
        hostLabel = new javax.swing.JLabel();
        hostField = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        portField = new javax.swing.JTextField();
        statusLabel = new javax.swing.JLabel();
        connectedLabel = new javax.swing.JLabel();

        hostLabel.setText(IniManager.getLocaleProperty("network.host")+":");

        hostField.setEditable(false);
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

        portLabel.setText(IniManager.getLocaleProperty("network.port")+":");

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

        statusLabel.setText(IniManager.getLocaleProperty("word.status")+":");

        connectedLabel.setText(IniManager.getLocaleProperty("network.notconnected"));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(statusLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(connectedLabel))
                    .addComponent(hostLabel)
                    .addComponent(hostField, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE)
                    .addComponent(portLabel)
                    .addComponent(portField, javax.swing.GroupLayout.DEFAULT_SIZE, 238, Short.MAX_VALUE))
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

    private void hostFieldFocusLost(java.awt.event.FocusEvent evt) {//GEN-FIRST:event_hostFieldFocusLost
        updateConnection();
    }//GEN-LAST:event_hostFieldFocusLost

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel connectedLabel;
    private javax.swing.JTextField hostField;
    private javax.swing.JLabel hostLabel;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JTextField portField;
    private javax.swing.JLabel portLabel;
    private javax.swing.JLabel statusLabel;
    // End of variables declaration//GEN-END:variables
 
    private void executeCommand(String command) {
        try {
            // single execution
            p_stdin.write(command);
            p_stdin.newLine();
            p_stdin.flush();
        } catch (IOException e) {
            System.out.println("greta.auxiliary.activemq.BrokerFrame.executeCommand(): "+e);
        }
    } 
}