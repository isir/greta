/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.mistral;

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


/**
 *
 * @author miche
 */
public class MistralFrame extends javax.swing.JFrame implements IntentionEmitter{

    /**
     * Creates new form MistralFrame
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

    private boolean MM_parse_server_activated = false;
    private String MM_python_env_checker_path = "Common\\Data\\MeaningMiner\\python\\check_env.py";
    private String MM_python_env_installer_path = "Common\\Data\\MeaningMiner\\python\\init_env.bat";
    private String MM_parse_server_path         = "Common\\Data\\MeaningMiner\\python\\activate_server.bat";
    private String MM_parse_server_killer_path  = "Common\\Data\\MeaningMiner\\python\\kill_server.bat";
    private String Mistral_python_env_checker_path = "Common\\Data\\Mistral\\check_env.py";
    private String Mistral_python_env_installer_path = "Common\\Data\\Mistral\\init_env.bat";
    private Process server_process;
    private Thread server_shutdownHook;

    public String getAnswer() {
        return answ;
    }

    public void setAnswer(String answer) {
        this.answ = answer;
    }
    
    
    
    
    public CharacterManager cm;
    
    public MistralFrame(CharacterManager cm) throws InterruptedException {
        initComponents();
        server = new Server();
        this.cm=cm;
        systemPrompt.setLineWrap(true);
        systemPrompt.setWrapStyleWord(true);
        request.setLineWrap(true);
        request.setWrapStyleWord(true);
        AnswerText.setLineWrap(true);
        AnswerText.setWrapStyleWord(true);
        
        init_MeaningMiner_server("greta.auxiliary.mistral.MistralFrame");
        
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
         System.out.println("greta.auxiliary.mistral.MistralFrame.TextToFML() "+sp.length);
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
        
        OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(System.getProperty("user.dir")+"\\fml_mistral.xml")),"ISO-8859-1");
        StreamResult result = new StreamResult(writer);
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.transform(source, result);
        }
        return System.getProperty("user.dir")+"\\fml_mistral.xml";
    }
    
     public String FMLToBML(String filename) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException{
        
         System.out.println("greta.core.intentions.FMLFileReader.FMLToBML()");
          BufferedReader br = null;
          PrintWriter pw = null; 
    try {
         br = new BufferedReader(new FileReader(filename));
         pw =  new PrintWriter(new FileWriter(System.getProperty("user.dir")+"\\fml_to_bml.xml"));
         String line;
         while ((line = br.readLine()) != null) {
                
                if(!line.contains("fml") && !line.contains("<?xml") && !line.contains("<rest") && !line.contains("<certainty") && !line.contains("<emotion")&& !line.contains("<performative") && !line.contains("<iconic") && !line.contains("<deictic") && !line.contains("<beat")){
                    if(line.contains("<speech")){
                         pw.println(line);
                        pw.println("<description level=\"1\" type=\"gretabml\"><reference>tmp/from-fml-apml.pho</reference></description>");
                    }else{
                    pw.println(line);
                    }
                }
         }
         br.close();
         pw.close();
    }catch (Exception e) {
         e.printStackTrace();
    }
        return System.getProperty("user.dir")+"\\fml_to_bml.xml";
    }
    public ID load(String fmlFileName) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
   
        String base = (new File(fmlFileName)).getName().replaceAll("\\.xml$", "");
        String bml_file=FMLToBML(fmlFileName);
        String base_bml= (new File(bml_file)).getName().replaceAll("\\.xml$", "");

        String fml_id = "";
        boolean text_brut=false;
        //get the intentions of the FML file
        fmlparser.setValidating(true);
        bmlparser.setValidating(true);
        BufferedReader reader;
        String text="";
        
        
        
        boolean flag=false;
                        try {
                                File myObj = new File(fmlFileName);
                                Scanner myReader = new Scanner(myObj);
                                while (myReader.hasNextLine()) {
                                  String data = myReader.nextLine();
                                  //System.out.println(data);
                                  if(!data.contains("<?xml")){
                                    System.out.println(data);
                                    if(!flag)
                                        text_brut=true;
                                    text+=data;
                                    
                                }
                                else{
                                    flag=true;
                                    text+=data;
                                }
                                }
                                myReader.close();
                              } catch (FileNotFoundException e) {
                                System.out.println("An error occurred.");
                                e.printStackTrace();
                }
                        
			
                
	if(text_brut){
            System.out.println(text);
            fmlFileName=TextToFML(text);
            bml_file=FMLToBML(fmlFileName);
            System.out.println("Nome nuovo file "+fmlFileName);
       }
        
        XMLTree fml = fmlparser.parseFile(fmlFileName);
        if(!text_brut){
             bml_file=FMLToBML(fmlFileName);
             System.out.println("Nome nuovo file "+bml_file);
        }
        XMLTree bml = bmlparser.parseFile(bml_file);
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml,cm);
        List<Signal> signals = BMLTranslator.BMLToSignals(bml,cm);
        System.out.println("greta.core.intentions.FMLFileReader.load()");
        for (int i =0;i>signals.size();i++){
                   System.out.println("greta.core.intentions.FMLFileReader.load()"+signals.get(i).toString());
        }
        Mode mode = FMLTranslator.getDefaultFMLMode();
        Mode mode_bml = BMLTranslator.getDefaultBMLMode();
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

        ID id = IDProvider.createID(base);
        id.setFmlID(fml_id);
        
        if(this.cm.use_MM()){
            ImageSchemaExtractor im = new ImageSchemaExtractor(this.cm);
             //MEANING MINER TREATMENT START
            List<Intention> intention_list;
            System.out.println("File Name "+fml.toString());
            intention_list = im.processText_2(fml.toString());
            intentions.addAll(intention_list);
            //MEANING MINER TREATMENT END
        }
        
        for(int i=0; i<intentions.size();i++){
            System.out.println("[INFO]: Intention_type:"+intentions.get(i).getType()+"   "+intentions.get(i).getName());
            if(this.cm.getGesture_map().containsKey(intentions.get(i).getType())){
                this.cm.setTouch_computed(true);
                this.cm.setTouch_gesture_computed(this.cm.getGesture_map().get(intentions.get(i).getType()));
                //REMOVE TOUCH GESTURE AND DO IT AFTER (IF NEAR OTHER CHARACTER/HUMAN
                intentions.remove(i);
            }
        }
        
       
        //send to all SignalPerformer added
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode);
        }
        for (SignalPerformer performer : signal_performers) {
            performer.performSignals(signals, id, mode);
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
        systemPrompt = new javax.swing.JTextArea();
        answer = new javax.swing.JLabel();
        addressLabel1 = new javax.swing.JLabel();
        languageBox = new javax.swing.JComboBox<>();
        answer1 = new javax.swing.JLabel();
        jScrollPane3 = new javax.swing.JScrollPane();
        AnswerText = new javax.swing.JTextArea();
        addressLabel2 = new javax.swing.JLabel();
        modelBox = new javax.swing.JComboBox<>();
        enable = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

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

        send.setFont(new java.awt.Font("Tahoma", 1, 13)); // NOI18N
        send.setText("Send");
        send.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                sendActionPerformed(evt);
            }
        });

        systemPrompt.setColumns(20);
        systemPrompt.setRows(5);
        jScrollPane2.setViewportView(systemPrompt);

        answer.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        answer.setText("Answer");

        addressLabel1.setText("Language");

        languageBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "English", "French" }));

        answer1.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        answer1.setText("System Prompt");

        AnswerText.setColumns(20);
        AnswerText.setRows(5);
        jScrollPane3.setViewportView(AnswerText);

        addressLabel2.setText("Model");

        modelBox.setModel(new javax.swing.DefaultComboBoxModel<>(new String[] { "Local", "Online" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(send)
                .addGap(25, 25, 25))
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel1)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(addressLabel)
                                    .addComponent(portLabel))
                                .addGap(32, 32, 32)
                                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                    .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                    .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(addressLabel1)
                                .addGap(18, 18, 18)
                                .addComponent(languageBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                            .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 329, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addGroup(jPanel2Layout.createSequentialGroup()
                                .addComponent(addressLabel2)
                                .addGap(18, 18, 18)
                                .addComponent(modelBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))))
                .addGap(57, 57, 57)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(answer1)
                    .addComponent(answer)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(6, 6, 6)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 325, javax.swing.GroupLayout.PREFERRED_SIZE))))
                .addGap(0, 138, Short.MAX_VALUE))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addContainerGap()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(portLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addressLabel)
                            .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addressLabel1)
                            .addComponent(languageBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(addressLabel2)
                            .addComponent(modelBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(63, 63, 63)
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(answer)))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(answer1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 97, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(9, 9, 9)
                        .addComponent(send)
                        .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, jPanel2Layout.createSequentialGroup()
                        .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 180, Short.MAX_VALUE)
                        .addGap(20, 20, 20))))
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
                .addGap(20, 20, 20)
                .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jPanel1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents
    public void setRequestText(String content){
        request.setText(content);
    }
    public void setRequestTextandSend(String content){
        request.setText(content);
        
        // TODO add your handling code here:
        Thread r1 = new Thread() {
            @Override
            public void run() {
                String text=request.getText();
                String language= (String) languageBox.getSelectedItem();
                String model= (String) modelBox.getSelectedItem();
                String systemPromptText = systemPrompt.getText();
                System.out.println("Language selected : "+language);
                if(text.length()>0)
                try {

                    server.sendMessage(model+"#SEP#"+language+"#SEP#"+text+"#SEP#"+systemPromptText);
                    System.out.println("Sent message:"+text);
                } catch (IOException ex) {
                    Logger.getLogger(MistralFrame.class.getName()).log(Level.SEVERE, null, ex);
                }

            }
        };

        r1.start();
    }
    private void enableActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_enableActionPerformed
        
        if(enable.isSelected()){
            
            System.out.println("Mistral port:"+server.getPort());
            server.setAddress(address.getText());
            server.setPort(port.getText());
            boolean python=true;
            
            try{
            server_process = new ProcessBuilder("python", Mistral_python_env_checker_path).redirectErrorStream(true).start();
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
        System.out.println(".init_Mistral_server(): Mistral, python env exist: " + result);
        
        if(result.equals("0")){
            System.out.println(".init_Mistral_server(): Mistral, installing python environment...");
            try{
                server_process = new ProcessBuilder(Mistral_python_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                server_process.waitFor();
            } catch (Exception e){
                e.printStackTrace();
            }
            
        }        
          
            
            
            if(python==false){
                System.out.println(ANSI_YELLOW+"[INFO]This warning appears because it seems that you enabled the Mistral module which is optional. "
                        + "Python and/or openai seem to be not installed. You need to install them in order to use this module!"+ANSI_RESET);
                
  
                enable.setSelected(false);
                enable.setEnabled(false);
                
            }
            
            if(python){
                try{ 
                    System.out.println("Opening python Mistral script");
                    
                    server.startConnection();
                    
                    Thread r1 = new Thread() {
                        @Override
                        public void run() {
                                try {
                                    System.out.println("greta.auxiliary.mistral.MistralFrame.enableActionPerformed(): waiting for client connection (Mistral.py -> Mistral module)");
                                    server.accept_new_connection();
                                } catch (IOException ex) {
                                    Logger.getLogger(MistralFrame.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }                        
                    };
                    
                    Thread r2 = new Thread() {
                        @Override
                        public void run() {

                            try {
                                String[] cmd = {
                                    "cmd.exe","/C","conda","activate","greta_mistral","&&","python","-u",
                                    System.getProperty("user.dir")+"\\Common\\Data\\Mistral\\Mistral.py ",server.getPort(),
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
                                    answ=s;
                                    if(answ!=null && answ.length()>1){
                                        System.out.println("CLIENT:"+answ);
                                        AnswerText.setText(answ.replace("AI Assistant:", "").replace("AI:", "").replace("AI assistant:","").replace("User:", ""));
                                    }
                                    if(AnswerText.getText().length()>1){
                                        answ=null;
                                        System.out.println("TEXTE:"+AnswerText.getText());
                                        String file=TextToFML(AnswerText.getText());
                                        load(file);
                                    }
                                }

                                // Read any errors from the attempted command
                                System.out.println("Here is the standard error of the command (if any):\n");
                                while ((s = stdError.readLine()) != null) {
                                    System.out.println(s);
                                }

                            } catch (IOException ex) {
                                Logger.getLogger(MistralFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (ParserConfigurationException ex) {
                                Logger.getLogger(MistralFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (SAXException ex) {
                                Logger.getLogger(MistralFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (TransformerException ex) {
                                Logger.getLogger(MistralFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (JMSException ex) {
                                Logger.getLogger(MistralFrame.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        } 
                        
                    };

                    r1.start();
                    r2.start();
                    System.out.println("greta.auxiliary.mistral.Mistral:" + server.port + "   " + server.address);
                    
                }
                catch(Exception e)
                {
                e.printStackTrace(); 
                }
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

    private void sendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_sendActionPerformed
        // TODO add your handling code here:
        Thread r1 = new Thread() {
            @Override
            public void run() {
                String text=request.getText();
                String language= (String) languageBox.getSelectedItem();
                String model= (String) modelBox.getSelectedItem();
                String systemPromptText = systemPrompt.getText();
                System.out.println("Language selected : "+language);
                if(text.length()>0)
                try {

                    server.sendMessage(model+"#SEP#"+language+"#SEP#"+text+"#SEP#"+systemPromptText);
                    System.out.println("Sent message:"+text);
                } catch (IOException ex) {
                    Logger.getLogger(MistralFrame.class.getName()).log(Level.SEVERE, null, ex);
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


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTextArea AnswerText;
    private javax.swing.JTextField address;
    private javax.swing.JLabel addressLabel;
    private javax.swing.JLabel addressLabel1;
    private javax.swing.JLabel addressLabel2;
    private javax.swing.JLabel answer;
    private javax.swing.JLabel answer1;
    private javax.swing.JCheckBox enable;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JComboBox<String> languageBox;
    private javax.swing.JComboBox<String> modelBox;
    private javax.swing.JTextField port;
    private javax.swing.JLabel portLabel;
    private javax.swing.JTextArea request;
    private javax.swing.JButton send;
    private javax.swing.JTextArea systemPrompt;
    // End of variables declaration//GEN-END:variables

    public void init_MeaningMiner_server(String this_file_path) throws InterruptedException
    {
        System.out.println(this_file_path + ".init_MeaningMiner_server(): MeaningMiner, checking python environment...");
        try{
            server_process = new ProcessBuilder("python", MM_python_env_checker_path).redirectErrorStream(true).start();
        } catch (IOException ex2){
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex2);
        }
        server_process.waitFor();

        InputStream inputStream = server_process.getInputStream();
        String result = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n")
                );
        System.out.println(this_file_path + ".init_MeaningMiner_server(): MeaningMiner, python env exist: " + result);
        
        if(result.equals("0")){
            System.out.println(this_file_path + ".init_MeaningMiner_server(): MeaningMiner, installing python environment...");
            try{
                server_process = new ProcessBuilder(MM_python_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            } catch (IOException ex2){
                Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex2);
            }
            server_process.waitFor();
        }        

        System.out.println(this_file_path + ".init_MeaningMiner_server(): initializing MeaningMiner python env");
        try {
            server_process = new ProcessBuilder(MM_parse_server_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
            //client_process = new ProcessBuilder("python", "-c", "print('hello')").redirectErrorStream(true).start();
            server_shutdownHook = new shutdownHook(server_process, MM_parse_server_killer_path);
            Runtime.getRuntime().addShutdownHook(server_shutdownHook);
        } catch (IOException ex) {
            Logger.getLogger(ImageSchemaExtractor.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println(this_file_path + ".init_MeaningMiner_server(): MeaningMiner python env initialization signal sent");
        
    }
}
