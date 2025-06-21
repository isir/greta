/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.chatgpt;

// import greta.auxiliary.MeaningMiner.ImageSchemaExtractor; // MeaningMiner not in Maven build
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
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
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


/**
 *
 * @author miche
 */
public class ChatGPTFrame extends javax.swing.JFrame implements IntentionEmitter{

    /**
     * Creates new form ChatGPTFrame
     */
    
    private Server server;
    public Socket soc;
    public String answ;
    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_BLACK = "\u001B[30m";
    public static final String ANSI_RED = "\u001B[31m";
    
    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private ArrayList<SignalPerformer> signal_performers = new ArrayList<SignalPerformer>();
    private XMLParser fmlparser = XML.createParser();
    private XMLParser bmlparser = XML.createParser();
    private static String markup = "fml-apml";
    public String getAnswer() {
        return answ;
    }

    public void setAnswer(String answer) {
        this.answ = answer;
    }
    
    
    
    
    public CharacterManager cm;
    
    public ChatGPTFrame(CharacterManager cm) {
        initComponents();
        server = new Server();
        this.cm=cm;
        jTextArea1.setLineWrap(true);
        jTextArea1.setWrapStyleWord(true);
        request.setLineWrap(true);
        request.setWrapStyleWord(true);
    }
    
     public String TextToFML(String text) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException{
        
         System.out.println("TEXT TO TRANSFORM:"+text); 
        if(text.length()>1){
        String construction="<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                            "<fml-apml>\n<bml>"+
                            "\n<speech id=\"s1\" language=\"english\" start=\"0.0\" text=\"\" type=\"SAPI4\" voice=\"marytts\" xmlns=\"\">"+
                            "\n<description level=\"1\" type=\"gretabml\"><reference>tmp/from-fml-apml.pho</reference></description>";
        System.out.println("greta.core.intentions.FMLFileReader.TextToFML()");
        String[] sp=text.split(" ");
        int i=1;
         System.out.println("greta.auxiliary.gpt3.ChatGPTFrame.TextToFML() "+sp.length);
        for(int j=0;j<sp.length;j++){
            construction=construction+"\n<tm id=\"tm"+i+"\"/>"+sp[j];
                        i++;
        }
        i=i-1;
        construction=construction+"\n</speech>\n</bml>\n<fml>\n";
        construction=construction+ "</fml>\n</fml-apml>";
        DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
        Document document = docBuilder.parse(new InputSource(new StringReader(construction)));
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(document);
        FileWriter writer = new FileWriter(new File(System.getProperty("user.dir")+"\\fml_gpt3.xml"));
        StreamResult result = new StreamResult(writer);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
        }
        return System.getProperty("user.dir")+"\\fml_gpt3.xml";
    }
    
    
    public ID load(String fmlFileName) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {

        //get the intentions of the FML file
        fmlparser.setValidating(true);
        bmlparser.setValidating(true);
        String fml_id = "";
        BufferedReader reader;
        String text="";
        fmlFileName=TextToFML(text);
        System.out.println("Name new fml file "+fmlFileName);
       
        
        XMLTree fml = fmlparser.parseFile(fmlFileName);
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml,cm);
        System.out.println("greta.core.intentions.FMLFileReader.load()");
        Mode mode = FMLTranslator.getDefaultFMLMode();
        for (XMLTree fmlchild : fml.getChildrenElement()) {
            // store the bml id in the mode class in order
            if (fmlchild.isNamed("bml")) {
                //System.out.println(fmlchild.getName());
                if(fmlchild.hasAttribute("id")){
                    mode.setBml_id(fmlchild.getAttribute("id"));
                }
            }
        }
        if(fml.hasAttribute("id")){
            fml_id = fml.getAttribute("id");
        }else{
            fml_id = "fml_1";
        }
        if (fml.hasAttribute("composition")) {
            mode.setCompositionType(fml.getAttribute("composition"));
        }
        if (fml.hasAttribute("reaction_type")) {
            mode.setReactionType(fml.getAttribute("reaction_type"));
        }
        if (fml.hasAttribute("reaction_duration")) {
            mode.setReactionDuration(fml.getAttribute("reaction_duration"));
        }
        if (fml.hasAttribute("social_attitude")) {
            mode.setSocialAttitude(fml.getAttribute("social_attitude"));
        }

        ID id = IDProvider.createID(fmlFileName);
        id.setFmlID(fml_id);
        // Meaning Miner functionality disabled - MeaningMiner not in Maven build
        // if(this.cm.use_MM()){
        //     ImageSchemaExtractor im = new ImageSchemaExtractor(this.cm);
        //     //MEANING MINER TREATMENT START
        //     List<Intention> intention_list;
        //     //System.out.println("File Name "+fml.toString());
        //     intention_list = im.processText_2(fml.toString());
        //     intentions.addAll(intention_list);
        //     //MEANING MINER TREATMENT END
        // }
        
        
       
        //send to all SignalPerformer added
        for (IntentionPerformer performer : performers) {
            if(intentions.size()>0)
                performer.performIntentions(intentions, id, mode);
        }
        return id;
    }
    
         @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        performers.add(performer);
    }
    
        @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
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
        send = new javax.swing.JButton();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTextArea1 = new javax.swing.JTextArea();
        answer = new javax.swing.JLabel();
        enable = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "ChatGPT", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Socket Parametes", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 13))); // NOI18N

        port.setText("4444");
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

        send.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        send.setText("Send");
        send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendActionPerformed(evt);
            }
        });

        jTextArea1.setColumns(20);
        jTextArea1.setRows(5);
        jScrollPane2.setViewportView(jTextArea1);

        answer.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        answer.setText("Answer");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(addressLabel)
                            .addComponent(portLabel))
                        .addGap(32, 32, 32)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                            .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(63, 63, 63)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(answer))))
                .addGap(0, 164, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(send)
                .addGap(25, 25, 25))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(portLabel))
                .addGap(23, 23, 23)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(addressLabel)
                    .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(answer))
                .addGap(18, 18, 18)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 157, Short.MAX_VALUE)
                    .addComponent(jScrollPane1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 25, Short.MAX_VALUE)
                .addComponent(send)
                .addContainerGap())
        );

        enable.setText("Enable");
        enable.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                enableActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(enable)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(enable)
                .addGap(18, 18, 18)
                .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void portActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portActionPerformed

    private void enableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableActionPerformed
        
        if(enable.isSelected()){
            
            System.out.println("ChatGPT port:"+server.getPort());
            server.setAddress(address.getText());
            server.setPort(port.getText());
            boolean python=true;
            String result="";
            try{ 
                String[] cmd = {
                        "python ", "-c", "import openai"
                    };
                    Runtime rt = Runtime.getRuntime();
                try {
                    Process proc = rt.exec(cmd);
                    BufferedReader reader = new BufferedReader(new InputStreamReader(proc.getInputStream()));
                        String line = "";
                        while ((line = reader.readLine()) != null) {
                            System.out.println(line + "\n");
                        }
                    if(result.contains("not available") || result.contains("introuvable") || result.contains("no module"))
                        python=false;
                    
                } catch (IOException ex) {
                    System.out.println("greta.auxiliary.chatgpt.ChatGPTFrame.enableActionPerformed()");
                    Logger.getLogger(ChatGPTFrame.class.getName()).log(Level.SEVERE, null, ex);
                    python=false;
                }
                }
                catch(Exception e)
                {
                e.printStackTrace(); 
            }
            
            
            if(python==false){
                System.out.println(ANSI_YELLOW+"[INFO]This warning appears because it seems that you enabled the ChatGPT module which is optional. "
                        + "Python and/or openai seem to be not installed. You need to install them in order to use this module!"+ANSI_RESET);
                
  
                enable.setSelected(false);
                enable.setEnabled(false);
                
            }
            
            if(python){
                try{ 
                    System.out.println("Opening python ChatGPT script");
                    
                    server.startConnection();
                    Thread r1 = new Thread() {
                    @Override
                    public void run() {
                        
                            try {
                                System.out.println("Checking new connections");
                                server.accept_new_connection();
                            } catch (IOException ex) {
                                Logger.getLogger(ChatGPTFrame.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                        
                    };
                    
                    Thread r2 = new Thread() {
                    @Override
                    public void run() {
                        
                        try {
                            String[] cmd = {
                                "python","-u",
                                System.getProperty("user.dir")+"\\Common\\Data\\ChatGPT\\ChatGPT.py ",server.getPort(),
                            };
                            Runtime rt = Runtime.getRuntime();
                            System.out.println("command:"+cmd[0]+" "+cmd[1]+" "+cmd[2]);
                            
                            Process proc = rt.exec(cmd);
                            
                            BufferedReader stdInput = new BufferedReader(new
                        InputStreamReader(proc.getInputStream()));
                            BufferedReader stdError = new BufferedReader(new
                        InputStreamReader(proc.getErrorStream()));
                            
                            // Read the output from the command
                            System.out.println("Here is the standard output of the command:\n");
                            String s = null;
                           
                            while ((s = stdInput.readLine()) != null) {
                                System.out.println("READ INPUT PYTHON:"+s);
                                answ=s;
                                if(answ!=null && answ.length()>1){
                                    System.out.println("CLIENT:"+answ);
                                    jTextArea1.setText(answ.replace("AI Assistant:", "").replace("AI:", "").replace("AI assistant:","").replace("User:", ""));
                                }
                                if(jTextArea1.getText().length()>1){
                                    answ=null;
                                    System.out.println("TEXTE:"+jTextArea1.getText());
                                    String file=TextToFML(jTextArea1.getText());
                                    load(file);
                                    
                                    
                        }
                            }
                            
                            // Read any errors from the attempted command
                            System.out.println("Here is the standard error of the command (if any):\n");
                            while ((s = stdError.readLine()) != null) {
                                System.out.println(s);
                            }   } catch (IOException ex) {
                            Logger.getLogger(ChatGPTFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (ParserConfigurationException ex) {
                            Logger.getLogger(ChatGPTFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (SAXException ex) {
                            Logger.getLogger(ChatGPTFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (TransformerException ex) {
                            Logger.getLogger(ChatGPTFrame.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (JMSException ex) {
                            Logger.getLogger(ChatGPTFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
 } 
                        
                    };
                
                

                     
                        
                    r1.start();
                    r2.start();
                    System.out.println("greta.auxiliary.chatgpt.ChatGPT:" + server.port + "   " + server.address);
                    
                    }
                    catch(Exception e)
                    {
                    e.printStackTrace(); 
                }
            }
        }
    }//GEN-LAST:event_enableActionPerformed

    private void sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendActionPerformed
        // TODO add your handling code here:
        Thread r1 = new Thread() {
            @Override
            public void run() {
                String text=request.getText();
                if(text.length()>0)
                    try {
                       
                        server.sendMessage(text);
                        System.out.println("Sent message:"+text);
                } catch (IOException ex) {
                    Logger.getLogger(ChatGPTFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        };
        
        r1.start();
        
        
    }//GEN-LAST:event_sendActionPerformed

    /**
     * @param args the command line arguments
     */


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextField address;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JLabel answer;
    private javax.swing.JCheckBox enable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTextArea jTextArea1;
    private javax.swing.JTextField port;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextArea request;
    private javax.swing.JButton send;
    // End of variables declaration//GEN-END:variables

}
