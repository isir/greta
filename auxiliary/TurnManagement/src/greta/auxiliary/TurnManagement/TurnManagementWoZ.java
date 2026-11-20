/*
 * Wizard of Oz Turn Management backend for Greta.
 *
 * Simplified version of TurnManagement.java that receives VAD state from
 * turnManagerWoz.py and exposes methods for the WoZ operator to trigger
 * specific head nod types through the UI.
 *
 * Instead of automatic backchannel decisions (done by the original TurnManagement),
 * this class only tracks:
 *   - Whether the user is speaking (from Python VAD)
 *   - Whether the agent is speaking (from the feedback loop)
 * and lets the human operator decide when and which head nod to send.
 */
package greta.auxiliary.TurnManagement;

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
 * Wizard of Oz Turn Management backend.
 * 
 * Receives VAD state from turnManagerWoz.py and provides methods to
 * send specific head nod FML files when triggered by the WoZ operator.
 * 
 * @author Based on TurnManagement by Takeshi Saga
 */
public class TurnManagementWoZ {

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
    
    private CharacterManager cm;

    // WoZ-specific FML file paths for the three nod types
    private String wozNodShortPath = ".\\Examples\\DemoEN\\backchannel\\woz_nod_short.xml";
    private String wozNodLongPath = ".\\Examples\\DemoEN\\backchannel\\woz_nod_long.xml";
    private String wozNodLongTwicePath = ".\\Examples\\DemoEN\\backchannel\\woz_nod_long_twice.xml";

    // Verbal backchannel folder paths
    private String continuerFMLDir = ".\\Examples\\DemoEN\\backchannel\\verbal_backchannel\\continuer";
    private String assessmentFMLDir = ".\\Examples\\DemoEN\\backchannel\\verbal_backchannel\\assessment";
    private String[] continuerFMLFileList;
    private String[] assessmentFMLFileList;
    private Random rand = new Random();

    private ArrayList<IntentionPerformer> performers = new ArrayList<IntentionPerformer>();
    private ArrayList<SignalPerformer> signal_performers = new ArrayList<SignalPerformer>();
    private XMLParser fmlparser = XML.createParser();
    private XMLParser bmlparser = XML.createParser();
    private static String markup = "fml-apml";

    // VAD state tracking (volatile for thread-safe reads from the EDT)
    private volatile boolean userSpeaking = false;
    private volatile boolean agentSpeaking = false;

    // Listener for VAD state changes (used by the Frame to update button states)
    private VadStateListener vadStateListener;

    /**
     * Listener interface for VAD state changes.
     * The Frame implements this to enable/disable buttons based on state.
     */
    public interface VadStateListener {
        void onVadStateChanged(boolean userSpeaking, boolean agentSpeaking);
    }

    /**
     * Constructor.
     *
     * @param cm CharacterManager
     * @param main_path Path to the batch file that starts the WoZ Python script
     * @throws IOException
     */
    public TurnManagementWoZ(CharacterManager cm, String main_path) throws IOException {

        System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ()");
        
        this.cm = cm;
        batch_main_path = main_path;

        // Scan verbal backchannel folders for available FML files
        try {
            ArrayList<String> continuerList = new ArrayList<String>();
            Files.list(Paths.get(continuerFMLDir))
                .filter(p -> p.toString().endsWith(".xml"))
                .forEach(s -> continuerList.add(s.toString()));
            continuerFMLFileList = continuerList.stream().toArray(String[]::new);
            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ(): Loaded " + continuerFMLFileList.length + " continuer FML files");

            ArrayList<String> assessmentList = new ArrayList<String>();
            Files.list(Paths.get(assessmentFMLDir))
                .filter(p -> p.toString().endsWith(".xml"))
                .forEach(s -> assessmentList.add(s.toString()));
            assessmentFMLFileList = assessmentList.stream().toArray(String[]::new);
            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ(): Loaded " + assessmentFMLFileList.length + " assessment FML files");
        } catch (Exception e) {
            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ(): Warning - could not load verbal backchannel FML files: " + e.getMessage());
            continuerFMLFileList = new String[0];
            assessmentFMLFileList = new String[0];
        }

        feedback_server = new Server();
        feedback_server.setAddress("localhost");
        feedback_server.setPort("5960");
        
        turnManagement_server = new Server();
        turnManagement_server.setAddress("localhost");
        turnManagement_server.setPort("5961");
        
        ///////////////////////
        // Check environment
        ///////////////////////

        try {
            server_process = new ProcessBuilder("python", python_env_checker_path).redirectErrorStream(true).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
        inputStream = server_process.getInputStream();
        result = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                .lines()
                .collect(Collectors.joining("\n")
                );
        System.out.println(".init_TurnManagementWoZ_server(): python env exist: " + result);        
        
        ///////////////////////
        // Create environment if not exist
        ///////////////////////

        if (result.equals("0")) {
            System.out.println(".init_TurnManagementWoZ_server(): installing python environment...");
            try {
                server_process = new ProcessBuilder(batch_env_installer_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                server_process.waitFor();
            } catch (Exception e) {
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
                    System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ(): checking new connections for feedback receive server");
                    feedback_server.accept_new_connection("WoZ feedback");
                } catch (IOException ex) {
                    Logger.getLogger(TurnManagementFrameWoZ.class.getName()).log(Level.SEVERE, null, ex);
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
                    System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ(): checking new connections for turnManagement WoZ server");
                    turnManagement_server.accept_new_connection("turnManagementWoZ");
                    turnManagement_server.sendMessage("ok");
                    Thread server_shutdownHook = new Thread() {
                        public void run() {
                            Process process;
                            try {
                                process = new ProcessBuilder(batch_kill_path, "5961").redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                                process.waitFor();
                            } catch (IOException ex) {
                                Logger.getLogger(TurnManagementWoZ.class.getName()).log(Level.SEVERE, null, ex);
                            } catch (InterruptedException ex) {
                                Logger.getLogger(TurnManagementWoZ.class.getName()).log(Level.SEVERE, null, ex);
                            }
                        }
                    };
                    Runtime.getRuntime().addShutdownHook(server_shutdownHook);
                } catch (IOException ex) {
                    Logger.getLogger(TurnManagementFrameWoZ.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        };
        r2.start();
                
        // Main thread: starts Python process and receives VAD state messages
        Thread r3 = new Thread() {
            @Override
            synchronized public void run() {

                System.out.println(".init_TurnManagementWoZ_server(): starting TurnManagementWoZ server...");

                try {
                    server_process = new ProcessBuilder(batch_main_path).redirectErrorStream(true).redirectOutput(ProcessBuilder.Redirect.INHERIT).start();
                    
                    server_shutdownHook = new Thread(() -> {
                        try {
                            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ(): server shutdown hook start");
                            while (server_process.isAlive()) {
                                try {
                                    turnManagement_server.sendMessage("kill");
                                } catch (IOException ex) {
                                    Logger.getLogger(TurnManagementWoZ.class.getName()).log(Level.SEVERE, null, ex);
                                }
                            }
                            server_process.waitFor();
                            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ(): server shutdown hook end");
                        } catch (InterruptedException ex) {
                            Logger.getLogger(TurnManagementWoZ.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    });
                    Runtime.getRuntime().addShutdownHook(server_shutdownHook);
                    
                } catch (IOException ex) {
                    Logger.getLogger(TurnManagementWoZ.class.getName()).log(Level.SEVERE, null, ex);
                }

                // Wait for "Generator started" message from Python
                while (true) {
                    try {
                        result = turnManagement_server.receiveMessage();
                        System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ() [preparation]: " + result);
                        if (result.contains("Generator started")) {
                            System.out.println("WoZ Turn management system started");
                            break;
                        } else {
                            System.out.println("Waiting for WoZ turn management system start");
                            Thread.sleep(100);
                        }
                    } catch (Exception e) {
                        // Keep waiting
                    }
                }

                // Main loop: receive VAD state from Python and update internal flags
                while (true) {
                    try {
                        result = turnManagement_server.receiveMessage();
                        turnManagement_server.sendMessage("ok");

                        // Update VAD state flags based on Python message
                        if (result != null) {
                            boolean prevUserSpeaking = userSpeaking;

                            if (result.contains("userSpeaking")) {
                                userSpeaking = true;
                            } else if (result.contains("userSilent")) {
                                userSpeaking = false;
                            }

                            // Log and notify listener if state changed
                            if (prevUserSpeaking != userSpeaking) {
                                System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ [VAD state changed]: userSpeaking=" + userSpeaking);
                                if (vadStateListener != null) {
                                    vadStateListener.onVadStateChanged(userSpeaking, agentSpeaking);
                                }
                            }
                        }
                    } catch (Exception e) {
                        // Continue listening
                    }
                }
            }
        };
        r3.start();
    }

    /**
     * Returns true when the WoZ operator can send a nod:
     * user must be speaking AND agent must NOT be speaking.
     */
    public boolean canSendNod() {
        return userSpeaking && !agentSpeaking;
    }

    /**
     * Returns whether the user is currently speaking (VAD detected).
     */
    public boolean isUserSpeaking() {
        return userSpeaking;
    }

    /**
     * Returns whether the agent is currently speaking.
     */
    public boolean isAgentSpeaking() {
        return agentSpeaking;
    }

    /**
     * Sets the VAD state listener (typically the Frame).
     */
    public void setVadStateListener(VadStateListener listener) {
        this.vadStateListener = listener;
    }

    /**
     * Send a short nod FML.
     * The Frame should check canSendNod() before calling this.
     */
    public void sendShortNod() {
        try {
            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ.sendShortNod(): Sending short nod");
            load(wozNodShortPath);
        } catch (Exception ex) {
            Logger.getLogger(TurnManagementWoZ.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Send a long nod FML.
     * The Frame should check canSendNod() before calling this.
     */
    public void sendLongNod() {
        try {
            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ.sendLongNod(): Sending long nod");
            load(wozNodLongPath);
        } catch (Exception ex) {
            Logger.getLogger(TurnManagementWoZ.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Send a long nod twice FML.
     * The Frame should check canSendNod() before calling this.
     */
    public void sendLongNodTwice() {
        try {
            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ.sendLongNodTwice(): Sending long nod twice");
            load(wozNodLongTwicePath);
        } catch (Exception ex) {
            Logger.getLogger(TurnManagementWoZ.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Pick a random continuer FML file from the continuer folder.
     */
    public String pickContinuerFML() {
        if (continuerFMLFileList == null || continuerFMLFileList.length == 0) {
            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ.pickContinuerFML(): No continuer FML files available");
            return null;
        }
        return continuerFMLFileList[rand.nextInt(continuerFMLFileList.length)];
    }

    /**
     * Pick a random assessment FML file from the assessment folder.
     */
    public String pickAssessmentFML() {
        if (assessmentFMLFileList == null || assessmentFMLFileList.length == 0) {
            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ.pickAssessmentFML(): No assessment FML files available");
            return null;
        }
        return assessmentFMLFileList[rand.nextInt(assessmentFMLFileList.length)];
    }

    /**
     * Send a random continuer verbal backchannel.
     * The Frame should check canSendNod() before calling this.
     */
    public void sendContinuerBackchannel() {
        String fmlFile = pickContinuerFML();
        if (fmlFile == null) return;
        try {
            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ.sendContinuerBackchannel(): Sending " + fmlFile);
            load(fmlFile);
        } catch (Exception ex) {
            Logger.getLogger(TurnManagementWoZ.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Send a random assessment verbal backchannel.
     * The Frame should check canSendNod() before calling this.
     */
    public void sendAssessmentBackchannel() {
        String fmlFile = pickAssessmentFML();
        if (fmlFile == null) return;
        try {
            System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ.sendAssessmentBackchannel(): Sending " + fmlFile);
            load(fmlFile);
        } catch (Exception ex) {
            Logger.getLogger(TurnManagementWoZ.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Process feedback from the Greta system (agent speaking state).
     * Called by the Frame when it receives feedback callbacks.
     * 
     * Note: agentSpeaking is NOT tracked here because the feedback mechanism
     * in the original TurnManagement is not reliably connected. The Java side
     * does not block nods based on agentSpeaking for now (same as original behavior).
     * The operator can see/hear the agent speaking and should not click during that time.
     */
    public void performFeedback(String type) {

        Thread r3 = new Thread() {
            @Override
            public void run() {
                while (true) {
                    try {
                        if (feedback_server != null && feedback_server.getOut() != null) {
                            feedback_server.sendMessage(type);
                            response = feedback_server.receiveMessage();
                        } else {
                            Thread.sleep(100);
                        }
                    } catch (Exception ex) {
                        Logger.getLogger(TurnManagementFrameWoZ.class.getName()).log(Level.SEVERE, null, ex);
                        System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ.performFeedback(): " + ex);
                    }
                }
            }
        };
        r3.start();
    }

    /**
     * Load an FML file and send intentions to performers.
     */
    public ID load(String fmlFileName) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
        ID id = load(fmlFileName, CompositionType.blend);
        return id;
    }
    
    /**
     * Load an FML file with a specified composition type and send intentions to performers.
     */
    public ID load(String fmlFileName, CompositionType compositionType) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
   
        System.out.format("greta.auxiliary.TurnManagement.TurnManagementWoZ.load(): loading %s%n", fmlFileName);

        String base = (new File(fmlFileName)).getName().replaceAll("\\.xml$", "");
        String bml_file = FMLToBML(fmlFileName);
        String base_bml = (new File(bml_file)).getName().replaceAll("\\.xml$", "");

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
                    if (!flag)
                        text_brut = true;
                    text += data;
                } else {
                    flag = true;
                    text += data;
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
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

        System.out.format("greta.auxiliary.TurnManagement.TurnManagementWoZ.load(): %s loaded successfully%n", fmlFileName);
        
        return id;
    }

    public String TextToFML(String text) throws ParserConfigurationException, SAXException, IOException, TransformerConfigurationException, TransformerException {
        String fml_name = TextToFML(text, true);
        return fml_name;
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
            construction = construction + "\n</speech>\n</bml>\n<fml>\n";
            construction = construction + "</fml>\n</fml-apml>";
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document document = docBuilder.parse(new InputSource(new StringReader(construction)));
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            DOMSource source = new DOMSource(document);

            if (renew) {
                file_path = System.getProperty("user.dir") + "\\fml_woz_renew.xml";
            } else {
                file_path = System.getProperty("user.dir") + "\\fml_woz.xml";
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
            pw = new PrintWriter(new FileWriter(System.getProperty("user.dir") + "\\fml_to_bml_woz.xml"));
            String line;
            while ((line = br.readLine()) != null) {
                if (!line.contains("fml") && !line.contains("<?xml") && !line.contains("<rest") && !line.contains("<certainty") && !line.contains("<emotion") && !line.contains("<performative") && !line.contains("<iconic") && !line.contains("<deictic") && !line.contains("<beat")) {
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
        return System.getProperty("user.dir") + "\\fml_to_bml_woz.xml";
    }

    public ArrayList<IntentionPerformer> getPerformers() {
        return performers;
    }
    
    public void resetMicServer(String port) {
        try {
            if (turnManagement_server != null && turnManagement_server.getOut() != null) {
                turnManagement_server.sendMessage("updateMicPort " + port);
                System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ.resetMicServer(): mic server update signal sent: " + port);
            } else {
                System.out.println("greta.auxiliary.TurnManagement.TurnManagementWoZ.resetMicServer(): server not connected yet, cannot update mic port");
            }
        } catch (IOException ex) {
            Logger.getLogger(TurnManagementWoZ.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
}
