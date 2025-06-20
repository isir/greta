/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.llm;

import greta.auxiliary.MeaningMiner.ImageSchemaExtractor;
import greta.auxiliary.MeaningMiner.shutdownHook;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
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
import java.io.InputStream;
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
import java.util.stream.Collectors;
import javax.jms.JMSException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import greta.core.util.enums.CompositionType;

/**
 *
 * @author miche
 */
public class MICounselorIncrementalDA extends LLMFrame{

    /**
     * Creates new form MistralFrame
     */
    
    private Server server;
    
    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private ArrayList<SignalPerformer> signal_performers = new ArrayList<SignalPerformer>();
    private XMLParser fmlparser = XML.createParser();
    private XMLParser bmlparser = XML.createParser();
    private static String markup = "fml-apml";
    private static String endSentenceFile = System.getProperty("user.dir") + "\\fml_endSentence.xml";

    private boolean MM_parse_server_activated = false;
    private String MM_python_env_checker_path = "Common\\Data\\MeaningMiner\\python\\check_env.py";
    private String MM_python_env_installer_path = "Common\\Data\\MeaningMiner\\python\\init_env.bat";
    private String MM_parse_server_path         = "Common\\Data\\MeaningMiner\\python\\activate_server.bat";
    private String MM_parse_server_killer_path  = "Common\\Data\\MeaningMiner\\python\\kill_server.bat";
    private String LLM_python_env_checker_path = "Common\\Data\\LLM\\Mistral\\check_env.py";
    private String LLM_python_env_installer_path = "Common\\Data\\LLM\\Mistral\\init_env.bat";
    private String python_path_llm_drinking="\\Common\\Data\\LLM\\MICounselor\\MICounselorIncrementalDA - Drinking.py ";
    private String python_path_llm_sport="\\Common\\Data\\LLM\\MICounselor\\MICounselorIncrementalDA - Sport.py ";
    private String python_path_llm_smoking="\\Common\\Data\\LLM\\MICounselor\\MICounselorIncrementalDA - Smoking.py ";
    
    private Process server_process;
    private Thread server_shutdownHook;
    private Process server_process_mistral;
    private Thread server_shutdownHook_mistral;

    private final Object lock = new Object();

//    public CharacterManager cm;

    public MICounselorIncrementalDA(CharacterManager cm) throws InterruptedException {
        
        super(cm);
        initComponents();
        
        server = new Server();
        this.cm=cm;
        
        systemPrompt.setLineWrap(true);
        systemPrompt.setWrapStyleWord(true);
        request.setLineWrap(true);
        request.setWrapStyleWord(true);
        AnswerText.setLineWrap(true);
        AnswerText.setWrapStyleWord(true);
        
        init_MeaningMiner_server("greta.auxiliary.mistral.MICounselorIncremental");
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
        jScrollPane1 = new javax.swing.JScrollPane();
        request = new javax.swing.JTextArea();
        jLabel1 = new javax.swing.JLabel();
        jScrollPane2 = new javax.swing.JScrollPane();
        systemPrompt = new javax.swing.JTextArea();
        answer = new javax.swing.JLabel();
        answer1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        AnswerText = new javax.swing.JTextArea();
        addressLabel2 = new javax.swing.JLabel();
        modelBox = new javax.swing.JComboBox<>();
        enable = new javax.swing.JCheckBox();
        addressLabel3 = new javax.swing.JLabel();
        ThemeBox = new javax.swing.JComboBox<>();
        send = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setPreferredSize(new java.awt.Dimension(810, 290));

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Mistral", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Socket Parametes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N

        port.setText("4000");
        port.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portActionPerformed(evt);
            }
        });

        portLabel.setText("Port");

        addressLabel.setText("Address");

        address.setText("localhost");

        request.setColumns(20);
        request.setRows(5);
        jScrollPane1.setViewportView(request);

        jLabel1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel1.setText("Request");

        systemPrompt.setColumns(20);
        systemPrompt.setRows(5);
        jScrollPane2.setViewportView(systemPrompt);

        answer.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        answer.setText("Answer");

        answer1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        answer1.setText("System Prompt");

        AnswerText.setColumns(20);
        AnswerText.setRows(5);
        jScrollPane3.setViewportView(AnswerText);

        addressLabel2.setText("Model");

        modelBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Local", "Online" }));
        modelBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                modelBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addressLabel)
                            .addComponent(portLabel))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                                .addComponent(addressLabel2)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(modelBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addComponent(jLabel1))
                .addGap(57, 57, 57)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(answer)
                            .addComponent(answer1)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, jPanel2Layout.createSequentialGroup()
                        .addComponent(answer1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(portLabel)
                            .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(modelBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(addressLabel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addressLabel)
                            .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(answer))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 57, Short.MAX_VALUE)
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        enable.setText("Enable");
        enable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableActionPerformed(evt);
            }
        });

        addressLabel3.setText("Thème");

        ThemeBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Drinking", "Smoking", "Sport" }));
        ThemeBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                ThemeBoxActionPerformed(evt);
            }
        });

        send.setFont(new java.awt.Font("Tahoma", 1, 14)); // NOI18N
        send.setText("Send");
        send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enable)
                .addGap(18, 18, 18)
                .addComponent(addressLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(ThemeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(send)
                .addContainerGap())
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap(18, Short.MAX_VALUE)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(addressLabel3)
                        .addComponent(ThemeBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addComponent(send, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(enable))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
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
            .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel1.getAccessibleContext().setAccessibleName("Mi Counselor");

        pack();
    }// </editor-fold>//GEN-END:initComponents
    private void enableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableActionPerformed
        
        if(enable.isSelected()){
            
            System.out.println("Mistral port:"+server.getPort());
            server.setAddress(address.getText());
            server.setPort(port.getText());
            boolean python=true;
            
            try{
                server_process_mistral = new ProcessBuilder("python", LLM_python_env_checker_path).redirectErrorStream(true).start();
                server_process_mistral.waitFor();
            } catch (Exception e){
               e.printStackTrace();
            }
        

            InputStream inputStream = server_process_mistral.getInputStream();
            String result = new BufferedReader(
                    new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines()
                    .collect(Collectors.joining("\n")
                    );
            System.out.println(".init_Mistral_server(): Mistral, python env exist: " + result);

            if(result.equals("0")){
                
                System.out.println(".init_Mistral_server(): Mistral, installing python environment...");
                try{
                    server_process_mistral = new ProcessBuilder(LLM_python_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                    server_process_mistral.waitFor();
                } catch (Exception e){
                    e.printStackTrace();
                }

            }
            
            if(python){
                try{ 
                    System.out.println("Opening python Mistral script");
                    
                    server.startConnection();
                    
                    Thread server_launcher_thread = new Thread() {
                        @Override
                        public void run() {
                            
                            try {
                                System.out.println("greta.auxiliary.llm.MistralFrame.enableActionPerformed(): waiting for client connection (Mistral.py -> Mistral module)");
                                server.accept_new_connection();
                            } catch (IOException ex) {
                                Logger.getLogger(MICounselorIncremental.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        
                        }
                    };
                    server_launcher_thread.start();

                    Thread main_thread = new Thread() {
                        @Override
                        public void run() {

                            try {
                                
                                String theme = (String) ThemeBox.getSelectedItem();
                                String python_path_llm = "";
                                if (theme.contains("Drinking")){
                                    python_path_llm = python_path_llm_drinking;
                                }else if (theme.contains("Sport")){
                                    python_path_llm = python_path_llm_sport;
                                }else{
                                    python_path_llm = python_path_llm_smoking;
                                }
                                
                                String[] cmd = {
                                    "cmd.exe","/C","conda","activate","greta_mistral","&&","python","-u",
                                    System.getProperty("user.dir")+python_path_llm,server.getPort(),
                                };
                                Runtime rt = Runtime.getRuntime();
                                System.out.println("command:"+cmd[0]+" "+cmd[1]+" "+cmd[2]);

                                Process proc = rt.exec(cmd);

                                BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream(), "ISO-8859-1"));
                                BufferedReader stdError = new BufferedReader(new InputStreamReader(proc.getErrorStream()));

                                // Read the output from the command
                                System.out.println("Here is the standard output of the command:\n");
                                String s = null;

                                while ((s = stdInput.readLine()) != null) {
                                    
                                    System.out.println("READ INPUT PYTHON :"+s);

                                    if(s.contains("STOP")){

                                        load(endSentenceFile, CompositionType.append, false, true);

                                        synchronized (lock) {
                                            IsStreaming = Boolean.FALSE;                                            
                                        }                                        

                                    }else{

                                        if(s.length()>1){

                                            synchronized (lock) {
                                                System.out.println("Already on answer:"+AnswerText.getText());
                                                System.out.println("answer:"+s);
                                                AnswerText.setText(AnswerText.getText()+ s);                                                    
                                            }

                                            if(s.contains("START:")){

                                                System.out.println("TEXTE:"+s);
                                                s = s.replace("START:", "");
                                                String file=TextToFML(s, false);
                                                s=null;                                                        

                                                //load(file, CompositionType.append);
                                                load(file, CompositionType.replace, true, false);

                                            }
                                            else{

                                                System.out.println("TEXTE:"+s);
                                                String file=TextToFML(s, false);
                                                load(file, CompositionType.append, true, false);
                                            }
                                                
                                                /*if (s.contains("END_CONVO")) {
                                                    
                                                    s = s.replace("END_CONVO", "");
                                                    String file=TextToFML(s, false);
                                                    //s=null;                                            
                                                    load(file, CompositionType.append, true, false);

                                                    // String finalSentence = "This is the end of the session, see you later.";
                                                    // file=TextToFML(finalSentence, false);
                                                    // load(file, CompositionType.append, true, false);

                                                } else {
                                                    String file=TextToFML(s, false);
                                                    load(file, CompositionType.append, true, false);
                                                }*/
                                                
                                    
                                        }else{
                                             System.out.println("LE MESSAGE EST EMPTY*******************************************");
                                             AnswerText.setText("");
        Thread r1 = new Thread() {
            @Override
            public void run() {
                
                String text=request.getText();
                String language= cm.getLanguage();
                String model= (String) modelBox.getSelectedItem();
                String systemPromptText = systemPrompt.getText();
                System.out.println("Language selected : "+language);
                
                synchronized (lock) {
                    IsStreaming = Boolean.TRUE;                    
                }
                
                if(text.length()>0) {

                    try {
                        server.sendMessage(model+"#SEP#"+language+"#SEP#"+text+"#SEP#"+systemPromptText);
                        System.out.println("Sent message:"+text);
                    } catch (IOException ex) {
                        Logger.getLogger(MICounselorIncremental.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }

            }
        };

        r1.start();    
                                        }

                                    }                                        



                                }

                                // Read any errors from the attempted command
                                System.out.println("Here is the standard error of the command (if any):\n");
                                while ((s = stdError.readLine()) != null) {
                                    System.out.println(s);
                                }

                            } catch (IOException ex) {
                                Logger.getLogger(MICounselorIncremental.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (ParserConfigurationException ex) {
                                Logger.getLogger(MICounselorIncremental.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (SAXException ex) {
                                Logger.getLogger(MICounselorIncremental.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (TransformerException ex) {
                                Logger.getLogger(MICounselorIncremental.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (JMSException ex) {
                                Logger.getLogger(MICounselorIncremental.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } 
                        
                    };
                    main_thread.start();

                    System.out.println("greta.auxiliary.mistral.Mistral:" + server.port + "   " + server.address);
                    
                }
                catch(Exception e)
                {
                    e.printStackTrace(); 
                }
            }

            else {
                
                System.out.println(ANSI_YELLOW+"[INFO]This warning appears because it seems that you enabled the Mistral module which is optional. "
                        + "Python and/or openai seem to be not installed. You need to install them in order to use this module!"+ANSI_RESET);
  
                enable.setSelected(false);
                enable.setEnabled(false);
                
            }            
            
        }else{
            try{
                server.stopConnection();
            }
            catch(Exception e)
            {
                e.printStackTrace(); 
            }
        }
    }//GEN-LAST:event_enableActionPerformed

    private void ThemeBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_ThemeBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_ThemeBoxActionPerformed

    private void modelBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_modelBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_modelBoxActionPerformed

    private void sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendActionPerformed

        AnswerText.setText("");
        Thread r1 = new Thread() {
            @Override
            public void run() {

                String text=request.getText();
                String language= cm.getLanguage();
                String model= (String) modelBox.getSelectedItem();
                String systemPromptText = systemPrompt.getText();
                System.out.println("Language selected : "+language);

                synchronized (lock) {
                    IsStreaming = Boolean.TRUE;
                }

                if(text.length()>0) {

                    try {
                        server.sendMessage(model+"#SEP#"+language+"#SEP#"+text+"#SEP#"+systemPromptText);
                        System.out.println("Sent message:"+text);
                    } catch (IOException ex) {
                        Logger.getLogger(MICounselorIncremental.class.getName()).log(Level.SEVERE, null, ex);
                    }

                }

            }
        };

        r1.start();
    }//GEN-LAST:event_sendActionPerformed

    private void portActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portActionPerformed

    /**
     * @param args the command line arguments
     */
    public void setRequestText(String content){
        request.setText(content);
    }
     
    @Override
    public void setRequestTextandSend(String content){
     
        request.setText(content);
        AnswerText.setText("");
        
        synchronized (lock) {
            IsStreaming = Boolean.TRUE;        
        }

        Thread r1 = new Thread() {
            @Override
            public void run() {
                String text=request.getText();
                String language= cm.getLanguage();
                String model= (String) modelBox.getSelectedItem();
                String systemPromptText = systemPrompt.getText();
                System.out.println("Language selected : "+language);

                if(text.length()>0) {

                    try {
                        server.sendMessage(model+"#SEP#"+language+"#SEP#"+text+"#SEP#"+systemPromptText);
                        System.out.println("Sent message:"+text);
                    } catch (IOException ex) {
                        Logger.getLogger(LLMFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    
                }

            }
        };

        r1.start();
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea AnswerText;
    private javax.swing.JComboBox<String> ThemeBox;
    private javax.swing.JTextField address;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JLabel addressLabel2;
    private javax.swing.JLabel addressLabel3;
    private javax.swing.JLabel answer;
    private javax.swing.JLabel answer1;
    private javax.swing.JCheckBox enable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JComboBox<String> modelBox;
    private javax.swing.JTextField port;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextArea request;
    private javax.swing.JButton send;
    private javax.swing.JTextArea systemPrompt;
    // End of variables declaration//GEN-END:variables

    
}
