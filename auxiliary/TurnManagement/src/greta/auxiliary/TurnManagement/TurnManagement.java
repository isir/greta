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

    private Thread server_shutdownHook;
    
    private Server feedback_server;
    private String response;

    private Server turnManagement_server;
    private InputStream inputStream;
    private String result;
    
    private static final Logger LOG = Logger.getLogger(TurnManagement.class.getName());

    private CharacterManager cm;

    // Head nod FML paths (same as TurnManagementModel)
    private String nodShortPath     = ".\\Examples\\DemoEN\\backchannel\\woz_nod_short.xml";
    private String nodLongPath      = ".\\Examples\\DemoEN\\backchannel\\woz_nod_long.xml";
    private String nodLongTwicePath = ".\\Examples\\DemoEN\\backchannel\\woz_nod_long_twice.xml";

    // Legacy random nod folder (kept for backward compat with reactiveBackchannel/responsiveBackchannel)
    private String[] backChannelFMLFileList;
    private String backChannelFMLFileDir_path = ".\\Examples\\DemoEN\\backchannel";

    // Verbal backchannel FML folders
    private static final String CONTINUER_DIR = ".\\Examples\\DemoEN\\backchannel\\verbal_backchannel\\continuer";
    private static final String ASSESSMENT_DIR = ".\\Examples\\DemoEN\\backchannel\\verbal_backchannel\\assessment";

    private String[] continuerFMLFileList;
    private String[] assessmentFMLFileList;
    private double[] continuerWeights;
    private double[] assessmentWeights;
    private Random rand = new Random();

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
        this.cm = cm;
        batch_main_path = main_path;

        ArrayList<String> arrayList = new ArrayList<String>();
        Files.list(Paths.get(backChannelFMLFileDir_path)).forEach(s -> arrayList.add(s.toString()));        
        backChannelFMLFileList = arrayList.stream().toArray(String[]::new);

        // Load verbal backchannel FML file lists with uniform probabilities
        loadVerbalBackchannelLists();

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
                    
                    server_shutdownHook = new Thread(() -> {
                            try {
                                System.out.println("greta.auxiliary.TurnManagement.TurnManagement(): server shutdown hook start");
                                while (server_process.isAlive()) {
                                    try {
                                        turnManagement_server.sendMessage("kill");
                                    } catch (IOException ex) {
                                        Logger.getLogger(TurnManagement.class.getName()).log(Level.SEVERE, null, ex);
                                    }
                                }
                                server_process.waitFor();
                                System.out.println("greta.auxiliary.TurnManagement.TurnManagement(): server shutdown hook end");
                            } catch (InterruptedException ex) {
                                Logger.getLogger(TurnManagement.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    );
                    Runtime.getRuntime().addShutdownHook(server_shutdownHook);
                    
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

                        result = turnManagement_server.receiveMessage();
                        if (result == null || result.isEmpty()) continue;

                        turnManagement_server.sendMessage("ok");

                        if (!prevTurnState.equals(result)) {

                            System.out.println("greta.auxiliary.TurnManagement.TurnManagement() [main]: check - " + result);

                            prevTurnState = result;

                            // Dispatch action on a NEW THREAD so this command-receive loop
                            // is never blocked by TTS/performIntentions (same fix as TurnManagementModel).
                            final String actionResult = result;
                            new Thread(() -> {
                                try {
                                    dispatchAction(actionResult);
                                } catch (Exception ex) {
                                    System.out.println("[TurnManagement] DISPATCH ERROR: " + ex.getMessage());
                                    ex.printStackTrace();
                                }
                            }).start();
                        }

                    }
                    catch (Exception e) {
                        System.out.println("[TurnManagement] DISPATCH LOOP EXCEPTION: " + e.getMessage());
                        e.printStackTrace();
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

    // ===== Action dispatch (runs on a NEW thread per action to avoid blocking the command loop) =====

    private static final int TURN_SHIFT_TIMEOUT_MS = 15000; // max wait for ASR transcript before giving up

    private void dispatchAction(String result) {
        // --- Head nods / Verbal BCs ---
        if (result.contains("shortNod")) {
            sendShortNod();
        }
        else if (result.contains("longNodTwice")) {
            sendLongNodTwice();
        }
        else if (result.contains("longNod")) {
            sendLongNod();
        }
        else if (result.contains("continuerBC")) {
            sendContinuerBackchannel();
        }
        else if (result.contains("assessmentBC")) {
            sendAssessmentBackchannel();
        }
        else if (result.contains("reactiveBackchannel") || result.contains("responsiveBackchannel")) {
            System.out.println("[TurnManagement] -> Legacy Backchannel (random nod)");
            String backChannelFML = pickBackChannelFML();
            System.out.format("Backchannel %s is emitted%n", backChannelFML);
            try { load(backChannelFML); } catch (Exception ex) { LOG.log(Level.SEVERE, "Legacy BC error", ex); }
        }

        // --- Turn shift (wait for ASR with timeout to prevent infinite hang) ---
        if (result.contains("turnShiftUserToAgent")) {
            System.out.println("greta.auxiliary.TurnManagement.TurnManagement() [dispatch]: behavior - " + result);

            int prevLength = transcriptList.size();
            long deadline = System.currentTimeMillis() + TURN_SHIFT_TIMEOUT_MS;
            boolean received = false;

            while (System.currentTimeMillis() < deadline) {
                if (prevLength != transcriptList.size()) {
                    received = true;
                    break;
                }
                try { Thread.sleep(50); } catch (InterruptedException ie) { break; }
            }

            if (received) {
                String transcript = transcriptList.get(transcriptList.size() - 1);
                System.out.println("greta.auxiliary.TurnManagement.TurnManagement() [dispatch]: sent to LLM - " + transcript);
                for (LLMFrame llm : llms) {
                    llm.setRequestTextandSend(transcript);
                }
            } else {
                System.out.println("[TurnManagement] WARNING: turnShift timed out after " + TURN_SHIFT_TIMEOUT_MS + "ms waiting for ASR transcript. Skipping LLM request.");
            }
        }
    }

    // ===== Verbal BC file loading with uniform probabilities =====

    private void loadVerbalBackchannelLists() {
        try {
            ArrayList<String> continuerList = new ArrayList<>();
            Files.list(Paths.get(CONTINUER_DIR))
                .filter(p -> p.toString().endsWith(".xml"))
                .forEach(s -> continuerList.add(s.toString()));
            continuerFMLFileList = continuerList.stream().toArray(String[]::new);

            ArrayList<String> assessmentList = new ArrayList<>();
            Files.list(Paths.get(ASSESSMENT_DIR))
                .filter(p -> p.toString().endsWith(".xml"))
                .forEach(s -> assessmentList.add(s.toString()));
            assessmentFMLFileList = assessmentList.stream().toArray(String[]::new);

            continuerWeights  = buildUniformWeights(continuerFMLFileList.length);
            assessmentWeights = buildUniformWeights(assessmentFMLFileList.length);

            System.out.println("[TurnManagement] Loaded " + continuerFMLFileList.length + " continuer FML files (uniform)");
            for (int i = 0; i < continuerFMLFileList.length; i++) {
                System.out.println("  " + continuerFMLFileList[i] + " -> p=" + String.format("%.3f", continuerWeights[i]));
            }
            System.out.println("[TurnManagement] Loaded " + assessmentFMLFileList.length + " assessment FML files (uniform)");
            for (int i = 0; i < assessmentFMLFileList.length; i++) {
                System.out.println("  " + assessmentFMLFileList[i] + " -> p=" + String.format("%.3f", assessmentWeights[i]));
            }
        } catch (Exception e) {
            System.out.println("[TurnManagement] Warning - could not load verbal backchannel FML files: " + e.getMessage());
            continuerFMLFileList = new String[0];
            assessmentFMLFileList = new String[0];
            continuerWeights = new double[0];
            assessmentWeights = new double[0];
        }
    }

    private double[] buildUniformWeights(int count) {
        double[] weights = new double[count];
        double uniform = 1.0 / Math.max(count, 1);
        java.util.Arrays.fill(weights, uniform);
        return weights;
    }

    private int weightedRandomIndex(double[] weights) {
        double r = rand.nextDouble();
        double cumulative = 0;
        for (int i = 0; i < weights.length; i++) {
            cumulative += weights[i];
            if (r <= cumulative) return i;
        }
        return weights.length - 1;
    }

    // ===== Legacy random nod pick =====

    public String pickBackChannelFML() {
        return backChannelFMLFileList[rand.nextInt(backChannelFMLFileList.length)];
    }

    // ===== Head Nod dispatch (same as TurnManagementModel) =====

    public void sendShortNod() {
        try {
            System.out.println("[TurnManagement] -> Short Nod");
            load(nodShortPath);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error sending short nod", ex);
        }
    }

    public void sendLongNod() {
        try {
            System.out.println("[TurnManagement] -> Long Nod");
            load(nodLongPath);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error sending long nod", ex);
        }
    }

    public void sendLongNodTwice() {
        try {
            System.out.println("[TurnManagement] -> Long Nod Twice");
            load(nodLongTwicePath);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error sending long nod twice", ex);
        }
    }

    // ===== Verbal Backchannel dispatch (same as TurnManagementModel) =====

    public void sendContinuerBackchannel() {
        if (continuerFMLFileList == null || continuerFMLFileList.length == 0) return;
        int idx = weightedRandomIndex(continuerWeights);
        String fml = continuerFMLFileList[idx];
        try {
            System.out.println("[TurnManagement] -> Continuer BC: " + fml + " (p=" + String.format("%.3f", continuerWeights[idx]) + ")");
            load(fml);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error sending continuer BC", ex);
        }
    }

    public void sendAssessmentBackchannel() {
        if (assessmentFMLFileList == null || assessmentFMLFileList.length == 0) return;
        int idx = weightedRandomIndex(assessmentWeights);
        String fml = assessmentFMLFileList[idx];
        try {
            System.out.println("[TurnManagement] -> Assessment BC: " + fml + " (p=" + String.format("%.3f", assessmentWeights[idx]) + ")");
            load(fml);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error sending assessment BC", ex);
        }
    }

    // ===== FML Loading (exact same as TurnManagementModel) =====

    public ID load(String fmlFileName) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
        return load(fmlFileName, CompositionType.blend);
    }

    public ID load(String fmlFileName, CompositionType compositionType) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
        String base = (new File(fmlFileName)).getName().replaceAll("\\.xml$", "");
        String bml_file = FMLToBML(fmlFileName);

        String fml_id = "";
        boolean text_brut = false;

        fmlparser.setValidating(true);
        bmlparser.setValidating(true);
        String text = "";

        boolean flag = false;
        try {
            File myObj = new File(fmlFileName);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                if (!data.contains("<?xml")) {
                    if (!flag) text_brut = true;
                    text += data;
                } else {
                    flag = true;
                    text += data;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (text_brut) {
            fmlFileName = TextToFML(text);
            bml_file = FMLToBML(fmlFileName);
        }

        XMLTree fml = fmlparser.parseFile(fmlFileName);
        if (!text_brut) {
            bml_file = FMLToBML(fmlFileName);
        }
        XMLTree bml = bmlparser.parseFile(bml_file);
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml, cm);
        List<Signal> signals = BMLTranslator.BMLToSignals(bml, cm);

        Mode mode = FMLTranslator.getDefaultFMLMode();
        Mode mode_bml = BMLTranslator.getDefaultBMLMode();
        for (XMLTree fmlchild : fml.getChildrenElement()) {
            if (fmlchild.isNamed("bml")) {
                if (fmlchild.hasAttribute("id")) {
                    mode.setBml_id(fmlchild.getAttribute("id"));
                }
            }
        }
        if (fml.hasAttribute("id")) {
            fml_id = fml.getAttribute("id");
        } else {
            fml_id = "fml_1";
        }

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

        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode, signals);
        }

        return id;
    }

    public String TextToFML(String text) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
        return TextToFML(text, true);
    }

    public String TextToFML(String text, boolean renew) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
        String file_path = "";
        if (text.length() > 1) {
            String construction = "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                    "<fml-apml>\n<bml>" +
                    "\n<speech id=\"s1\" language=\"english\" start=\"0.0\" text=\"\" type=\"SAPI4\" voice=\"marytts\" xmlns=\"\">" +
                    "\n<description level=\"1\" type=\"gretabml\"><reference>tmp/from-fml-apml.pho</reference></description>";
            String[] sp = text.split(" ");
            int i = 1;
            for (int j = 0; j < sp.length; j++) {
                construction = construction + "\n<tm id=\"tm" + i + "\"/>" + sp[j];
                i++;
            }
            construction = construction + "\n<tm id=\"tm" + i + "\"/>";
            construction = construction + "\n<boundary id=\"b1\" type=\"LL\" start=\"s1:tm1\" end=\"s1:tm" + i + "\"/>";
            construction = construction + "\n</speech>\n</bml>\n<fml>\n</fml>\n</fml-apml>";
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(construction)));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            if (renew) {
                file_path = System.getProperty("user.dir") + "\\fml_turnmgmt_renew.xml";
            } else {
                file_path = System.getProperty("user.dir") + "\\fml_turnmgmt.xml";
            }
            OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(new File(file_path)), "ISO-8859-1");
            StreamResult result = new StreamResult(writer);
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.transform(source, result);
        }
        return file_path;
    }

    public String FMLToBML(String filename) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
        BufferedReader br = null;
        PrintWriter pw = null;
        try {
            br = new BufferedReader(new FileReader(filename));
            pw = new PrintWriter(new FileWriter(System.getProperty("user.dir") + "\\fml_to_bml_turnmgmt.xml"));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.contains("fml") && !line.contains("<?xml") && !line.contains("<rest")
                        && !line.contains("<certainty") && !line.contains("<emotion")
                        && !line.contains("<performative") && !line.contains("<iconic")
                        && !line.contains("<deictic") && !line.contains("<beat")) {
                    if (line.contains("<speech")) {
                        pw.println(line);
                        pw.println("<description level=\"1\" type=\"gretabml\"><reference>tmp/from-fml-apml.pho</reference></description>");
                    } else {
                        pw.println(line);
                    }
                }
            }
            br.close();
            pw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return System.getProperty("user.dir") + "\\fml_to_bml_turnmgmt.xml";
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
