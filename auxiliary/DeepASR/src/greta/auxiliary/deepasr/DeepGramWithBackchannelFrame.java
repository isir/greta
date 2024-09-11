/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.deepasr;


import greta.auxiliary.llm.LLMFrame;
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
import greta.auxiliary.llm.MistralFrame;
import greta.core.feedbacks.Callback;
import java.io.InputStream;
import java.util.stream.Collectors;
import greta.core.feedbacks.FeedbackPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import greta.core.intentions.FMLFileReader;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.util.enums.CompositionType;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Random;
import java.util.stream.Stream;
import javafx.scene.shape.Path;
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
public class DeepGramWithBackchannelFrame extends DeepASRFrame implements IntentionEmitter {

    /**
     * Creates new form DeepGramFrame
     */
    
    
    private String DeepASR_python_env_checker_path = "Common\\Data\\DeepASR\\DeepGram\\check_env.py";
    private String DeepASR_python_env_installer_path = "Common\\Data\\DeepASR\\DeepGram\\init_env.bat";
    private String python_asr_path = "\\Common\\Data\\DeepASR\\DeepGram\\DeepGram.py";
    private Process server_process;
    private Thread server_shutdownHook;
    private boolean automaticListenBool = false;
    private ArrayList<MistralFrame> mistrals = new ArrayList<MistralFrame>();
    private Server server;


    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private ArrayList<SignalPerformer> signal_performers = new ArrayList<SignalPerformer>();
    private XMLParser fmlparser = XML.createParser();
    private XMLParser bmlparser = XML.createParser();
    private static String markup = "fml-apml";
    
    public CharacterManager cm;
    
    public String[] backChannelFMLFileList;
    public String backChannelFMLFileDir_path = ".\\Examples\\DemoEN\\backchannel";

    public DeepGramWithBackchannelFrame(CharacterManager cm)throws InterruptedException, IOException {
        super(cm);
       initComponents();
       server = new greta.auxiliary.deepasr.Server();
        this.cm=cm;
        
        
//        Stream<java.nio.file.Path> fmlFileStream = Files.list(Paths.get(backChannelFMLFileDir_path));
//        backChannelFMLFileList = fmlFileStream.toArray(String[]::new);
        
//        StringBuffer strList = new StringBuffer();
        ArrayList<String> arrayList = new ArrayList<String>();
        Files.list(Paths.get(backChannelFMLFileDir_path)).forEach(s -> arrayList.add(s.toString()));
        
        backChannelFMLFileList = arrayList.stream().toArray(String[]::new);
        
    }
    
    @Override
    public void performFeedback(String type){
       Boolean IsStreaming = Boolean.FALSE;
       for (LLMFrame llm : llms){
                      IsStreaming = llm.IsStreaming | IsStreaming;                  
                                  }
      
       if (type == "end" & !IsStreaming){
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

                    } catch (Exception ex) {
                        Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
           };
           r3.start();
                
                
                }
        }
        if (type == "start"){
      
           Thread r3 = new Thread() {
                @Override
                public void run() {
                    try{
                        Thread.sleep(100);
                    }catch (Exception ex) {
                        Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    if (IsListenning){
                         try{

                            server.sendMessage("STOP");
                            System.out.println("Stopping");
                            IsListenning = Boolean.FALSE;
                            listen.setText("Listen");
                         }catch (Exception ex) {
                             Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                         }
                    }
                }
            };
            r3.start();
   
        }
    }
    
    public String pickBackChannelFML() {
        Random rand = new Random();
        
        return backChannelFMLFileList[rand.nextInt(backChannelFMLFileList.length)];
    }
    
    public String TextToFML(String text) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException{
        String fml_name = TextToFML(text, true);
        return fml_name;
    }
    
    public String TextToFML(String text, boolean renew) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException{
        
        String file_path = "";        
        
        System.out.println("TEXT TO TRANSFORM:"+text); 
        if(text.length()>1){
            String construction="<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                                "<fml-apml>\n<bml>"+
                                "\n<speech id=\"s1\" language=\"english\" start=\"0.0\" text=\"\" type=\"SAPI4\" voice=\"marytts\" xmlns=\"\">"+
                                "\n<description level=\"1\" type=\"gretabml\"><reference>tmp/from-fml-apml.pho</reference></description>";
            System.out.println("greta.core.intentions.FMLFileReader.TextToFML()");
            String[] sp=text.split(" ");
            int i=1;
            System.out.println("greta.auxiliary.llm.LLMFrame.TextToFML() "+sp.length);
            for(int j=0;j<sp.length;j++){
                construction=construction+"\n<tm id=\"tm"+i+"\"/>"+sp[j];
                i++;
            }
            construction=construction+"\n<tm id=\"tm"+i+"\"/>";

            construction=construction+"\n<boundary id=\"b1\" type=\"LL\" start=\"s1:tm1\" end=\"s1:tm"+i+"\"/>";

            construction=construction+"\n</speech>\n</bml>\n<fml>\n";
            construction=construction+ "</fml>\n</fml-apml>";
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(construction)));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            if(renew){
                file_path = System.getProperty("user.dir")+"\\fml_mistral_renew.xml";
            }else{
                file_path = System.getProperty("user.dir")+"\\fml_mistral.xml";
            }
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(file_path)),"ISO-8859-1");                        
            StreamResult result = new StreamResult(writer);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        }
        
        return file_path;
        
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
        
        ID id = load(fmlFileName, CompositionType.replace);
        return id;
    }
    
    public ID load(String fmlFileName, CompositionType compositionType) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
   
        double load_start = greta.core.util.time.Timer.getTime();
        System.out.format("[PROCESS TIME] greta.auxiliary.llm.LLMFrame.load(): START: %.3f ##############################%n", load_start);

        System.out.println("CompositionType: " + compositionType);
        
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
        
//        if (fml.hasAttribute("composition")) {
//            mode.setCompositionType(fml.getAttribute("composition"));
//        }
        mode.setCompositionType(compositionType);
        
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
        
        double preprocess_end = greta.core.util.time.Timer.getTime();
        System.out.format("[PROCESS TIME] greta.auxiliary.llm.LLMFrame.load(): preprocess - %.3f%n", preprocess_end - load_start);        

        double MM_end = greta.core.util.time.Timer.getTime();
        System.out.format("[PROCESS TIME] greta.auxiliary.llm.LLMFrame.load(): MeaningMiner - %.3f%n", MM_end - preprocess_end);        
        
        for(int i=0; i<intentions.size();i++){
            System.out.println("[INFO]: Intention_type:"+intentions.get(i).getType()+"   "+intentions.get(i).getName());
            if(this.cm.getGesture_map().containsKey(intentions.get(i).getType())){
                this.cm.setTouch_computed(true);
                this.cm.setTouch_gesture_computed(this.cm.getGesture_map().get(intentions.get(i).getType()));
                //REMOVE TOUCH GESTURE AND DO IT AFTER (IF NEAR OTHER CHARACTER/HUMAN
                intentions.remove(i);
            }
        }

        //Option2: send intentions and signals together
        //You don't need to add connector from FMLFileReader to BehaviorRealizer
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode, signals);
        }

        double load_end = greta.core.util.time.Timer.getTime();
        System.out.format("[PROCESS TIME] greta.auxiliary.llm.LLMFrame.load(): total - %.3f%n", load_end - load_start);          
        System.out.format("[PROCESS TIME] greta.auxiliary.llm.LLMFrame.load(): END: %.3f ##############################%n", load_end);                
        
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
                                        
                                        //
                                        // Excute if speech end is detected
                                        //
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
                                        else if (answ.contains("Interim Results:")) {
                                            String backChannelFML = pickBackChannelFML();
                                            System.out.format("Backchannel %s is emitted%n", backChannelFML);
                                            load(backChannelFML);
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
                                }   
                            } catch (IOException ex) {
                                Logger.getLogger(DeepASRFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (TransformerException ex) {
                                Logger.getLogger(DeepGramWithBackchannelFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (SAXException ex) {
                                Logger.getLogger(DeepGramWithBackchannelFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (ParserConfigurationException ex) {
                                Logger.getLogger(DeepGramWithBackchannelFrame.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (JMSException ex) {
                                Logger.getLogger(DeepGramWithBackchannelFrame.class.getName()).log(Level.SEVERE, null, ex);
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

 
    private void portActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portActionPerformed

    private void automaticListenActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_automaticListenActionPerformed
        // TODO add your handling code here:
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
