/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.TurnManagement;

import greta.auxiliary.llm.LLMFrame;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionPerformer;
import greta.core.intentions.FMLTranslator;
import greta.core.signals.Signal;
import greta.core.signals.SignalPerformer;
import greta.core.signals.BMLTranslator;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.charset.StandardCharsets;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
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

import javax.jms.JMSException;

/**
 *
 * @author Takeshi Saga
 */

public class TurnManagement {

    private String python_env_checker_path = "Common\\Data\\TurnManagement\\check_env.py";
    private String batch_env_installer_path = "Common\\Data\\TurnManagement\\init_env.bat";
    private String batch_main_path;
    private String batch_kill_path = "Common\\Data\\TurnManagement\\kill_server.bat";
    private Process server_process;

    private Server feedback_server;
    private String response;

    private Server turnManagement_server;
    private InputStream inputStream;
    private String result;
    
    private CharacterManager cm;
    private String[] backChannelFMLFileList;
    private String backChannelFMLFileDir_path = ".\\Examples\\DemoEN\\backchannel";    
    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private ArrayList<SignalPerformer> signal_performers = new ArrayList<SignalPerformer>();
    private XMLParser fmlparser = XML.createParser();
    private XMLParser bmlparser = XML.createParser();
    private static String markup = "fml-apml";

    private String prevTurnState = "";

    protected ArrayList<LLMFrame> llms = new ArrayList<LLMFrame>();    
    
    private ArrayList<String> transcriptList = new ArrayList<String>();    
    
    /**
     *
     * @throws IOException
     */
    public TurnManagement (CharacterManager cm, String main_path) throws IOException {

        System.out.println("greta.auxiliary.TurnManagement.TurnManagement()");
        
        batch_main_path = main_path;

        ArrayList<String> arrayList = new ArrayList<String>();
        Files.list(Paths.get(backChannelFMLFileDir_path)).forEach(s -> arrayList.add(s.toString()));        
        backChannelFMLFileList = arrayList.stream().toArray(String[]::new);

        
        feedback_server = new Server();
        feedback_server.setAddress("localhost");
        feedback_server.setPort("5960");
        
        turnManagement_server = new Server();
        turnManagement_server.setAddress("localhost");
        turnManagement_server.setPort("5961");
        
        ///////////////////////
        // Check environment
        ///////////////////////

        try{
            server_process = new ProcessBuilder("python", python_env_checker_path).redirectErrorStream(true).start();
            // server_process.waitFor();
        } catch (Exception e){
           e.printStackTrace();
        }
        inputStream = server_process.getInputStream();
        result = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n")
                );
        System.out.println(".init_TurnManagement_server(): TurnManagement, python env exist: " + result);        
        
        ///////////////////////
        // Create environment if not exit
        ///////////////////////

        if(result.equals("0")){
            System.out.println(".init_TurnManagement_server(): TurnManagement, installing python environment...");
            try{
                server_process = new ProcessBuilder(batch_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                server_process.waitFor();
            } catch (Exception e){
                e.printStackTrace();
            }            
        }
        
        ///////////////////////
        // Prepare feedback receiver server
        ///////////////////////

        feedback_server.startConnection();
        Thread r1 = new Thread() {
            @Override
            synchronized public void run() {

                try {
                    System.out.println("greta.auxiliary.TurnManagement.TurnManagement(): checking new connections for feedback receive server");
                    feedback_server.accept_new_connection("feedback");
                } catch (IOException ex) {
                    Logger.getLogger(TurnManagementFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        };
        r1.start();

        ///////////////////////
        // Prepare turn management server
        ///////////////////////

        turnManagement_server.startConnection();
        Thread r2 = new Thread() {
        @Override
        synchronized public void run() {

            try {
                System.out.println("greta.auxiliary.TurnManagement.TurnManagement(): checking new connections for turnManagement server");
                turnManagement_server.accept_new_connection("turnManagment");
                turnManagement_server.sendMessage("ok");
                Thread server_shutdownHook = new Thread() {
                    public void run() {
                        Process process;
                        try {
                            process = new ProcessBuilder(batch_kill_path, "5961").redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                            process.waitFor();
                        } catch (IOException ex) {
                            Logger.getLogger(TurnManagement.class.getName()).log(Level.SEVERE, null, ex);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(TurnManagement.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }

                };
                Runtime.getRuntime().addShutdownHook(server_shutdownHook);
            } catch (IOException ex) {
                Logger.getLogger(TurnManagementFrame.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

        };
        r2.start();
                
        Thread r3 = new Thread() {
            @Override
            synchronized public void run() {
                

                System.out.println(".init_TurnManagement_server(): TurnManagement, starting TurnManagement server...");

                try {
                    server_process = new ProcessBuilder(batch_main_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                } catch (IOException ex) {
                    Logger.getLogger(TurnManagement.class.getName()).log(Level.SEVERE, null, ex);
                }

                while (true) {

                    try {

                        result = turnManagement_server.receiveMessage();

                        System.out.println("greta.auxiliary.TurnManagement.TurnManagement() [preparation]: " + result);
                        if (result.contains("Generator started")) {
                            System.out.println("Turn management system started");
                            break;
                        }
                        else {
                            System.out.println("Waiting for turn management system start");
                            Thread.sleep(100);
                        }

                    }
                    catch (Exception e) {

                    }

                }

                while (true) {

                    try {

                        double s_time = greta.core.util.time.Timer.getTime();

                        result = turnManagement_server.receiveMessage();

                        turnManagement_server.sendMessage("ok");
                        
//                        System.out.format("prevTurnState(%s), result(%s)%n", prevTurnState, result);

                        if (!prevTurnState.equals(result)) {

//                            System.out.println("greta.auxiliary.TurnManagement.TurnManagement() [main]: check - " + result);

                            prevTurnState = result;

                            if (result.contains("reactiveBackchannel")) {
                                System.out.println("greta.auxiliary.TurnManagement.TurnManagement() [main]: behavior - " + result);
                                String backChannelFML = pickBackChannelFML();
                                System.out.format("Backchannel %s is emitted%n", backChannelFML);
                                load(backChannelFML);
                            }
                            if (result.contains("responsiveBackchannel")) {
                                System.out.println("greta.auxiliary.TurnManagement.TurnManagement() [main]: behavior - " + result);
                                String backChannelFML = pickBackChannelFML();
                                System.out.format("Backchannel %s is emitted%n", backChannelFML);
                                load(backChannelFML);
                            }
                            if (result.contains("turnShiftUserToAgent")) {
                                System.out.println("greta.auxiliary.TurnManagement.TurnManagement() [main]: behavior - " + result);
                                int prevLength = transcriptList.size();
                                while(true) {
                                    //System.out.println("greta.auxiliary.TurnManagement.TurnManagement() [main]: " + prevLength + " " + transcriptList.size());
                                    if (prevLength != transcriptList.size()){
                                        break;
                                    }
                                    Thread.sleep(50);
                               }
                                System.out.println("greta.auxiliary.TurnManagement.TurnManagement() [main]: sent to LLM - " + transcriptList.get(transcriptList.size() - 1));                                
                                for (LLMFrame llm : llms) {
                                    llm.setRequestTextandSend(transcriptList.get(transcriptList.size() - 1));
                                }
                            }
                        }
                        else {
                            // System.out.println("greta.auxiliary.TurnManagement.TurnManagement() [main]: behavior - " + result);                            
                        }

                        double e_time = greta.core.util.time.Timer.getTime();

//                        System.out.format("greta.auxiliary.TurnManagement.TurnManagement(): loop time: %.2f%n", e_time - s_time);
                    }
                    catch (Exception e) {

                    }


                }

            }
        };
        r3.start();
        
    }
    
    public String readLine(InputStream inputStream) throws IOException {
        
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, "UTF-8"), 20);
        
        result = reader.readLine();
                
        return result;
    }

    public void performFeedback(String type){
        
        Thread r3 = new Thread() {
            @Override
            public void run() {
                
                while (true) {

                    try {
                        feedback_server.sendMessage(type);
                        // System.out.println("greta.auxiliary.TurnManagement.performFeedback(): Feedback - " + type);
                        response = feedback_server.receiveMessage();
                        // System.out.println("greta.auxiliary.TurnManagement.performFeedback(): response - " + response);

                    } catch (Exception ex) {
                        Logger.getLogger(TurnManagementFrame.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("greta.auxiliary.TurnManagement.performFeedback(): " + ex);                 
                        System.out.println("greta.auxiliary.TurnManagement.performFeedback(): " + response);
                    }
                }
            }
        };
        r3.start();
        
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

        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode, signals);
        }

        double load_end = greta.core.util.time.Timer.getTime();
        System.out.format("[PROCESS TIME] greta.auxiliary.llm.LLMFrame.load(): total - %.3f%n", load_end - load_start);          
        System.out.format("[PROCESS TIME] greta.auxiliary.llm.LLMFrame.load(): END: %.3f ##############################%n", load_end);                
        
        return id;
        
    }
    
    public ArrayList<IntentionPerformer> getPerformers() {
        return performers;
    }
    
    public void resetMicServer(String port) {

        try {
            turnManagement_server.sendMessage("updateMicPort " + port);
            System.out.println("greta.auxiliary.TurnManagement.TurnManagement.resetMicServer(): mic server update signal sent: " + port);
        }
        catch (IOException ex) {
            Logger.getLogger(TurnManagement.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public void addTranscript(String transcript) {
        transcriptList.add(transcript);
    }
    
}
