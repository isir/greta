/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.deepasr;


import greta.core.intentions.IntentionPerformer;
import greta.core.signals.BMLTranslator;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import java.nio.charset.StandardCharsets;
import static greta.core.util.audio.Audio.ANSI_RESET;
import static greta.core.util.audio.Audio.ANSI_YELLOW;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.OutputStreamWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import greta.auxiliary.llm.LLMFrame;
import greta.core.feedbacks.Callback;
import java.io.InputStream;
import java.util.stream.Collectors;
import greta.core.feedbacks.FeedbackPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;

/**
 *
 * @author miche
 */
public class DeepASRFrame extends javax.swing.JFrame implements FeedbackPerformer {

    /**
     * Creates new form DeepGramFrame
     */
    
    private Server server;
    public Socket soc;
    public String answ;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    private ArrayList<LLMFrame> llms = new ArrayList<LLMFrame>();
    private static String markup = "fml-apml";
    
    private String DeepASR_python_env_checker_path = "Common\\Data\\DeepASR\\DeepGram\\check_env.py";
    private String DeepASR_python_env_installer_path = "Common\\Data\\DeepASR\\DeepGram\\init_env.bat";
    private String python_asr_path = "\\Common\\Data\\DeepASR\\DeepGram\\DeepGram.py ";
    private Process server_process;
    private Thread server_shutdownHook;
    private boolean automaticListenBool = false;
     public CharacterManager cm;
    
    public String getAnswer() {
        return answ;
    }

    public void setAnswer(String answer) {
        this.answ = answer;
    }
    
   public void performFeedback(String type){
       if (type == "end"){
       if (!IsListenning & automaticListenBool){
           Thread r3 = new Thread() {
            @Override
            public void run() {
       
                try {
                    String language= cm.getLanguage();
                    System.out.println("Language selected : "+language);
                    
                    server.sendMessage(language);
                    System.out.println("Listenning");
                    listen.setText("Stop");
                    IsListenning = Boolean.TRUE;
                    
                } catch (IOException ex) {
                    Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
           };
           r3.start();
                
                
                }
   }
   if (type == "start"){
       if (IsListenning){
           Thread r3 = new Thread() {
            @Override
            public void run() {
       
                    try{
                        server.sendMessage("STOP");
                        System.out.println("Stopping");
                        IsListenning = Boolean.FALSE;
                        listen.setText("Listen");
                    }catch (IOException ex) {
                    Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
           };
                    r3.start();
   }
   }
   }
   public void performFeedback(ID AnimId, String type, SpeechSignal speechSignal, TimeMarker tm){
    performFeedback(type);
   };

   public void performFeedback(ID AnimId, String type, List<Temporizable> listTmp){
       performFeedback(type);
   };

   public void performFeedback(Callback callback){
   performFeedback(callback.type());
};
   public void setDetailsOption(boolean detailed){
       
   };

   public boolean areDetailedFeedbacks(){
     return true  ;
   };

   public void setDetailsOnFace(boolean detailsOnFace){
     
   };

   public boolean areDetailsOnFace(){
       return false;  
   };

   public void setDetailsOnGestures(boolean detailsOnGestures){
       
   };

   public boolean areDetailsOnGestures(){
       return false;  
   };
    
    
    
    
  
    public DeepASRFrame(CharacterManager cm) {
        initComponents();
        server = new Server();
       
       this.cm=cm;
        
       
        TranscriptText.setLineWrap(true);
        TranscriptText.setWrapStyleWord(true);
    }
    
     
    
    
    
    

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel1 = new javax.swing.JPanel();
        jPanel2 = new javax.swing.JPanel();
        port = new javax.swing.JTextField();
        portLabel = new javax.swing.JLabel();
        addressLabel = new javax.swing.JLabel();
        address = new javax.swing.JTextField();
        listen = new javax.swing.JButton();
        answer = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        TranscriptText = new javax.swing.JTextArea();
        enable = new javax.swing.JCheckBox();
        automaticListen = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "DeepGram", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Socket Parametes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N

        port.setText("4040");
        port.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portActionPerformed(evt);
            }
        });

        portLabel.setText("Port");

        addressLabel.setText("Address");

        address.setText("localhost");

        listen.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        listen.setText("Listen");
        listen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                listenActionPerformed(evt);
            }
        });

        answer.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        answer.setText("Transcript");

        TranscriptText.setColumns(20);
        TranscriptText.setRows(5);
        jScrollPane3.setViewportView(TranscriptText);

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(listen)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addressLabel)
                            .addComponent(portLabel))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(44, 44, 44)
                        .addComponent(answer)
                        .addGap(18, 18, 18)
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(24, 26, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(portLabel)
                            .addComponent(answer))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addressLabel)
                            .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 158, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(listen)
                .addContainerGap())
        );

        enable.setText("Enable");
        enable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableActionPerformed(evt);
            }
        });

        automaticListen.setText("Automatic Listenning");
        automaticListen.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                automaticListenActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enable)
                .addGap(28, 28, 28)
                .addComponent(automaticListen)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(enable)
                    .addComponent(automaticListen))
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void enableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableActionPerformed
        
        if(enable.isSelected()){
            
            System.out.println("DeepGram port:"+server.getPort());
            server.setAddress(address.getText());
            server.setPort(port.getText());
            boolean python=true;
             try{
            server_process = new ProcessBuilder("python", DeepASR_python_env_checker_path).redirectErrorStream(true).start();
            server_process.waitFor();
        } catch (Exception e){
           e.printStackTrace();
        }
        

        InputStream inputStream = server_process.getInputStream();
        String result = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n")
                );
        System.out.println(".init_DeepGram_server(): DeepASR, python env exist: " + result);
        
        if(result.equals("0")){
            System.out.println(".init_DeepGram_server(): DeepASR, installing python environment...");
            try{
                server_process = new ProcessBuilder(DeepASR_python_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                server_process.waitFor();
            } catch (Exception e){
                e.printStackTrace();
            }
            
        }        
          
            
            
            if(python==false){
                System.out.println(ANSI_YELLOW+"[INFO]This warning appears because it seems that you enabled the DeepGram module which is optional. "
                        + "Python and/or openai seem to be not installed. You need to install them in order to use this module!"+ANSI_RESET);
                
  
                enable.setSelected(false);
                enable.setEnabled(false);
                
            }
            
            if(python){
                try{ 
                    System.out.println("Opening python DeepGram script");
                    
                    server.startConnection();
                    Thread r1 = new Thread() {
                    @Override
                    public void run() {
                        
                            try {
                                System.out.println("Checking new connections");
                                server.accept_new_connection();
                            } catch (IOException ex) {
                                Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        
                    };
                    
                    Thread r2 = new Thread() {
                    @Override
                    public void run() {
                        
                        try {
                            String[] cmd = {
                                    "cmd.exe","/C","conda","activate","greta_deepgram","&&","python","-u",
                                System.getProperty("user.dir")+python_asr_path,server.getPort(),
                            };
                            Runtime rt = Runtime.getRuntime();
                            System.out.println("command:"+cmd[0]+" "+cmd[1]+" "+cmd[2]);
                            
                            Process proc = rt.exec(cmd);
                            
                            BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(proc.getInputStream(), "ISO-8859-1"));
                            BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(proc.getErrorStream()));
                            
                            // Read the output from the command
                            System.out.println("Here is the standard output of the command:\n");
                            String s = null;
                           
                            while ((s = stdInput.readLine()) != null) {
                                System.out.println("READ INPUT PYTHON :"+s);
                                answ=s;
                                if(answ!=null && answ.length()>1){
                                    System.out.println("CLIENT:"+answ);
                                    if (answ.contains("Speech Final:")|answ.contains("Is Final:")){
                                    TranscriptText.setText(answ.replace("Speech Final:","").replace("Is Final:",""));
                                    if (IsListenning){
                                        for (LLMFrame llm : llms){
                                        
                                        llm.setRequestTextandSend(answ.replace("Speech Final:","").replace("Is Final:",""));
                                    }
                    try{
                        server.sendMessage("STOP");
                        System.out.println("Stopping");
                        IsListenning = Boolean.FALSE;
                        listen.setText("Listen");
                    }catch (IOException ex) {
                    Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                    
                }
                                    
                                    
                                }
                                    
                                }
                                if(TranscriptText.getText().length()>1){
                                    answ=null;
                                    System.out.println("TEXTE:"+TranscriptText.getText());        
                        }
                            }
                            
                            // Read any errors from the attempted command
                            System.out.println("Here is the standard error of the command (if any):\n");
                            while ((s = stdError.readLine()) != null) {
                                System.out.println(s);
                            }   } catch (IOException ex) {
                            Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } 
 } 
                        
                    };
                
                

                     
                        
                    r1.start();
                    r2.start();
                    System.out.println("greta.auxiliary.deepasr.DeepGram" + server.port + "   " + server.address);
                    
                    }
                    catch(Exception e)
                    {
                    e.printStackTrace(); 
                }
            }
        }else{
            try{
             if(IsListenning){
                 Thread r3 = new Thread() {
            @Override
            public void run() {
                    try{
                        server.sendMessage("STOP");
                        System.out.println("Stopping");
                        IsListenning = Boolean.FALSE;
                        listen.setText("Listen");
                    }catch (IOException ex) {
                    Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                
            }
        };

        r3.start();
             }
            server.stopConnection();
            }
            catch(Exception e)
                 {
                   e.printStackTrace(); 
                }
        }
    }//GEN-LAST:event_enableActionPerformed
   
    private Boolean IsListenning = Boolean.FALSE;
    private void listenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_listenActionPerformed
        // TODO add your handling code here:
         Thread r3 = new Thread() {
            @Override
            public void run() {
        
                String language= cm.getLanguage();
               
                
                if (IsListenning){
                    try{
                        server.sendMessage("STOP");
                        System.out.println("Stopping");
                        IsListenning = Boolean.FALSE;
                        listen.setText("Listen");
                    }catch (IOException ex) {
                    Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                }else{
                try {
                    
                    System.out.println("Language selected : "+language);
                    
                    server.sendMessage(language);
                    System.out.println("Listenning");
                    listen.setText("Stop");
                    IsListenning = Boolean.TRUE;
                    
                } catch (IOException ex) {
                    Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
                }
            }
        };

        r3.start();
    
    }//GEN-LAST:event_listenActionPerformed

    public void addLLMFrame(LLMFrame llm) {
        llms.add(llm);
    }
    
 
    public void removeLLMFrame(LLMFrame llm) {
        llms.remove(llm);
    }
    private void portActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portActionPerformed

    private void automaticListenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_automaticListenActionPerformed
        // TODO add your handling code here:
        automaticListenBool = !automaticListenBool;
        if(automaticListenBool){
         Thread r3 = new Thread() {
            @Override
            public void run() {
        
                String language= cm.getLanguage();
               
                
                if (IsListenning){
                    try{
                        server.sendMessage("STOP");
                        System.out.println("Stopping");
                        IsListenning = Boolean.FALSE;
                        listen.setText("Listen");
                    }catch (IOException ex) {
                    Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                }else{
                try {
                    
                    System.out.println("Language selected : "+language);
                    
                    server.sendMessage(language);
                    System.out.println("Listenning");
                    listen.setText("Stop");
                    IsListenning = Boolean.TRUE;
                    
                } catch (IOException ex) {
                    Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
                
                
                }
            }
        };

        r3.start();
        }
    }//GEN-LAST:event_automaticListenActionPerformed

    /**
     * @param args the command line arguments
     */


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea TranscriptText;
    private javax.swing.JTextField address;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JLabel answer;
    private javax.swing.JCheckBox automaticListen;
    private javax.swing.JCheckBox enable;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JButton listen;
    private javax.swing.JTextField port;
    private javax.swing.JLabel portLabel;
    // End of variables declaration//GEN-END:variables

}
