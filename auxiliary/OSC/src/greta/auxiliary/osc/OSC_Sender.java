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
package greta.auxiliary.osc;

import com.illposed.osc.MessageSelector;
import com.illposed.osc.OSCBadDataEvent;
import com.illposed.osc.OSCMessage;
import com.illposed.osc.OSCMessageEvent;
import com.illposed.osc.OSCMessageListener;
import com.illposed.osc.OSCPacketEvent;
import com.illposed.osc.OSCPacketListener;
import com.illposed.osc.OSCSerializeException;
import com.illposed.osc.argument.OSCTimeTag64;
import com.illposed.osc.messageselector.OSCPatternAddressMessageSelector;
import com.illposed.osc.transport.udp.OSCPortIn;
import com.illposed.osc.transport.udp.OSCPortOut;
import greta.auxiliary.openface2.gui.OpenFaceOutputStreamReader;
import greta.auxiliary.openface2.util.StringArrayListener;
import greta.auxiliary.zeromq.ConnectionListener;
import greta.core.keyframes.face.AUEmitter;
import greta.core.keyframes.face.AUPerformer;
import greta.core.util.CharacterManager;
import greta.core.util.speech.Speech;
import greta.core.util.speech.TTS;
import greta.core.utilx.gui.CsvWriter;
import greta.core.utilx.gui.OpenAndLoad1;
import java.awt.Color;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextField;
import java.util.Date;
import java.io.IOException;
import java.io.*;
/**
 *
 * @author Andre-Marie Pez
 */
public class OSC_Sender extends javax.swing.JFrame implements AUEmitter, ConnectionListener, StringArrayListener, OSCPacketListener{
    
    private static final Logger LOGGER = Logger.getLogger(OpenFaceOutputStreamReader.class.getName());

    private static final Color green = new Color(0, 150, 0);
    private static final Color red = Color.RED;

    private static final String fileProperty = "GUI.file";
    private static final String statusProperty = "word.status";
    private static final String connectedProperty = "network.connected";
    private static final String notConnectedProperty = "network.notconnected";
    private static final String hostProperty = "network.host";
    private static final String portProperty = "network.port";
    public List<String> list_object= new ArrayList<String>();

    private String actualConnectedProperty;
    // OSC is use to monitor AUs signal, mainly for debug
    protected boolean useOSC = false;
    protected OSCPortOut oscOut = null;
    protected int oscPort = 9000;
    CsvWriter Csvfile1 = new CsvWriter();
    long starttime=0;
    boolean send1 =true;

    /**
     * Creates new customizer OSC
     */
    public OSC_Sender() {
        initComponents();
    }
    
    
     /*
    OSC
    */
    public void setUseOSC(boolean b){        
        if(b){              
            startOSCOut(oscPort);            
        }
        else{
            stopOSCOut();
        }
    }
    
    protected void startOSCOut(int port){        
        try {            
            oscOut = new OSCPortOut(InetAddress.getLocalHost(), port);            
            useOSC = true;
        } catch (IOException ex) {
            useOSC = false;
            LOGGER.log(Level.WARNING, null, ex);
        }
        LOGGER.log(Level.INFO, String.format("startOSCOut port %d : %b", port, useOSC));
    }
    
    protected void stopOSCOut(){        
        useOSC = false;
        if(oscOut!=null){
            try {
                oscOut.disconnect();
            } catch (IOException ex) {           
                LOGGER.log(Level.WARNING, null, ex);
            }
        }
        LOGGER.log(Level.INFO, String.format("stopOSCOut : %b",  !useOSC));
    }
    
    protected int getOscOutPort(){
        return oscPort;
    }
    
    protected void setOscOutPort(int port){
        LOGGER.log(Level.INFO, String.format("setOscOutPort : %d",  port));
        oscPort = port;      
    }

    private void setConnected(boolean connected) {
        if (connected) {
            actualConnectedProperty = connectedProperty;

        } else {
            actualConnectedProperty = notConnectedProperty;

        }
    }
    
        
    private void sendOSC(String root, Map<String,Double> map){
        try {
            for (String key : map.keySet()) {
                final List<Double> args = new ArrayList<>();
                args.add(map.get(key));
                OSCMessage msg = new OSCMessage(root+key, args);  
                if(oscOut!=null)
                    oscOut.send(msg);            
            }
        } catch (OSCSerializeException | IOException ex) {
            LOGGER.warning(ex.getLocalizedMessage());
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
        jSpinner1 = new javax.swing.JSpinner();
        jLabel2 = new javax.swing.JLabel();
        jCheckBox1 = new javax.swing.JCheckBox();
        textField4 = new java.awt.TextField();
        jButton1 = new javax.swing.JButton();
        jPanel2 = new javax.swing.JPanel();
        jLabel3 = new javax.swing.JLabel();
        jLabel4 = new javax.swing.JLabel();
        jLabel5 = new javax.swing.JLabel();
        textField1 = new java.awt.TextField();
        jLabel6 = new javax.swing.JLabel();
        textField2 = new java.awt.TextField();
        jLabel7 = new javax.swing.JLabel();
        textField3 = new java.awt.TextField();
        choice1 = new java.awt.Choice();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        jCheckBox2 = new javax.swing.JCheckBox();

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "OSC Parameters"));

        jSpinner1.setValue(getOscOutPort());

        jLabel2.setText("UDP port");

        jCheckBox1.setText("OSC");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        textField4.setText("/unity/fruit1/name");
        textField4.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textField4ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addGap(66, 66, 66)
                .addComponent(textField4, javax.swing.GroupLayout.PREFERRED_SIZE, 109, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 80, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel2)
                    .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, 146, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(textField4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel1Layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(15, 15, 15)
                        .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox1))))
                .addContainerGap(46, Short.MAX_VALUE))
        );

        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(javax.swing.BorderFactory.createLineBorder(new java.awt.Color(0, 0, 0)), "Gaze Object/Coordinates"));

        jLabel3.setText("Object");

        jLabel4.setText("Coordinates");

        jLabel5.setText("X");

        textField1.setText("");
        textField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                textField1ActionPerformed(evt);
            }
        });

        jLabel6.setText("Y");

        textField2.setText("");

        jLabel7.setText("Z");

        textField3.setText("");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jLabel5)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textField1, javax.swing.GroupLayout.PREFERRED_SIZE, 116, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jLabel6, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(jLabel3)
                    .addComponent(jLabel4)
                    .addComponent(choice1, javax.swing.GroupLayout.PREFERRED_SIZE, 179, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 21, Short.MAX_VALUE)
                .addComponent(textField2, javax.swing.GroupLayout.PREFERRED_SIZE, 110, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 42, Short.MAX_VALUE)
                .addComponent(jLabel7, javax.swing.GroupLayout.PREFERRED_SIZE, 7, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textField3, javax.swing.GroupLayout.PREFERRED_SIZE, 98, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(choice1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(29, 29, 29)
                .addComponent(jLabel4)
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel5)
                    .addComponent(textField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel6)
                    .addComponent(textField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel7)
                    .addComponent(textField3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(36, Short.MAX_VALUE))
        );

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 15)); // NOI18N
        jLabel1.setText("OSC Communication");

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane1.setViewportView(jTextArea1);

        jCheckBox2.setText("OSC Receiver");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(237, 237, 237)
                        .addComponent(jButton1))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(31, 31, 31)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 106, Short.MAX_VALUE)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 283, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jCheckBox2))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(24, 24, 24)
                .addComponent(jLabel1)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(53, 53, 53)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(28, 28, 28))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jCheckBox2)
                        .addGap(47, 47, 47)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jButton1)
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
                        List<Integer> li= new ArrayList<Integer>();
                li.add(2);
                li.add(1);
                OSCMessage msg = new OSCMessage(textField4.getText(),li );  
               Map map=new HashMap();  
               map.put("unity/fruit1/name", choice1.getSelectedItem());
                System.out.println("[INFO] "+hostProperty.toString());
                sendOSC("/", map);
                try {
                    System.out.println(oscOut+"  "+msg.toString());
                    oscOut.send(msg);
                } catch (IOException ex) {
                    Logger.getLogger(OpenFaceOutputStreamReader.class.getName()).log(Level.SEVERE, null, ex);
                } catch (OSCSerializeException ex) {
                    Logger.getLogger(OpenFaceOutputStreamReader.class.getName()).log(Level.SEVERE, null, ex);
                }
                jTextArea1.setText("Received message");
                 
                if(send1==true){
                    starttime = System.currentTimeMillis();
                    SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd'at'HH-mm-ss");
                    Date dates = new Date(System.currentTimeMillis());
                    String heure = formatter.format(dates);
                    String gaze = "gaze" ;
                    CsvWriter newCsvfile1 = new CsvWriter("D:\\Csvfiles",gaze,heure);
                    Csvfile1 = newCsvfile1;
                    System.err.println("CREATION DU FICHIER CSV n'A pas ECHOUE");
                            try {
                                Csvfile1.sendToCSV("start","0");
                            } catch (IOException ex) {
                                Logger.getLogger(OSC_Sender.class.getName()).log(Level.SEVERE, null, ex);
                            }
                    send1=false;
                }
                if(send1==false){
                    double tps = (double) System.currentTimeMillis()-starttime;
                    String item = choice1.getSelectedItem();
                    String[] parts = item.split("1");
                    String target = parts[1];
                    try {
                        Csvfile1.sendToCSV(target,Double.toString(tps/1000.0));
                    } catch (IOException ex) {
                        Logger.getLogger(OpenAndLoad1.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
                
                
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        setUseOSC(jCheckBox1.isSelected());
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void textField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textField1ActionPerformed

    private void textField4ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_textField4ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_textField4ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
Thread thread = new Thread() {
		@Override
		public void run() {
			try {
				SocketAddress socketAddress = new InetSocketAddress("127.0.0.1",9100) ;
                            OSCPortIn receiver = new OSCPortIn(socketAddress);
				//SocketAddress socketAddress1 = receiver.getRemoteAddress();
				MessageSelector messageSelector = new OSCPatternAddressMessageSelector("/unity");
				OSCMessageListener messageListener = new OSCMessageListener() {
					
					@Override
					public void acceptMessage(OSCMessageEvent arg0) {
						// TODO Auto-generated method stub
						//System.out.println("message recieved");
                                                //System.out.println("[INFO]:"+arg0.getMessage().getArguments());
                                                System.out.println("RECEIVED MESSAGE");
                                                String obj =arg0.getMessage().getArguments().toString();
                                                String [] list = obj.replace("[","").replace("]","").split(",");
                                                    for(String object: list){
                                                         System.out.println("[INFO_2]:   "+object.trim());
                                                    boolean flag = false;
                                                    for(String o:list_object){
                                                            System.out.println("[INFO_3]:"+o+"   "+object);
                                                            if(object.equals(o))
                                                                System.out.println("[INFO_4]"+o+"  "+object);
                                                                flag=true;
                                                        }
                                                    
                                                    if(object.length()>0){
                                                       
                                                        boolean var=true;
                                                        for(int i=0;i<choice1.getItemCount();i++){
                                                        if(choice1.getItem(i).equals(object)){
                                                            var=false;
                                                        }
                                                        
                                                    }
                                                        if(var){
                                                        System.out.println("ELEMENT A AJOUTER:"+object);
                                                        list_object.add(object.trim());
                                                        choice1.add(object.trim());
                                                    }
                                                        for(int i=0;i<choice1.getItemCount();i++){
                                                            int count=0;
                                                            for(int j=0;j<choice1.getItemCount();j++){
                                                        if(choice1.getItem(i).equals(choice1.getItem(j))){
                                                            count++;
                                                            if(count>1){
                                                                choice1.remove(i);
                                                            }
                                                        }
                                                       }
                                                    }
					}
                                                    }
                                        }
				};
				OSCPacketListener listener = new OSCPacketListener() {
					
					@Override
					public void handlePacket(OSCPacketEvent arg0) {
						// TODO Auto-generated method stub
						//System.out.println("[INFO_4]:recieved");
						//System.out.println(arg0.getSource().toString());
						//System.out.println(arg0.getPacket().toString());
					}
					
					@Override
					public void handleBadData(OSCBadDataEvent arg0) {
						// TODO Auto-generated method stub
						
					}
				};

				receiver.getDispatcher().addListener(messageSelector, messageListener);
				receiver.addPacketListener(listener);
				receiver.startListening();
				if (receiver.isListening())
					System.out.println("Server is listening");
				receiver.run();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				System.out.println("error " + e);
			}
		}
	};

	thread.start();
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.Choice choice1;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel6;
    private javax.swing.JLabel jLabel7;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextArea jTextArea1;
    private java.awt.TextField textField1;
    private java.awt.TextField textField2;
    private java.awt.TextField textField3;
    private java.awt.TextField textField4;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addAUPerformer(AUPerformer aup) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeAUPerformer(AUPerformer aup) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onConnection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onDisconnection() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void stringArrayChanged(String[] stringArray) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handlePacket(OSCPacketEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void handleBadData(OSCBadDataEvent event) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
