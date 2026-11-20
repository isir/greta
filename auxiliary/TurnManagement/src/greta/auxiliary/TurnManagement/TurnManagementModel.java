/*
 * Model-driven Turn Management backend for Greta.
 *
 * Receives action commands from RealTimeGreta.py (the VAP model) via TCP socket
 * and dispatches head nods, verbal backchannels, and HALT interruptions.
 *
 * Action protocol (received as newline-terminated strings from Python):
 *   "shortNod"      -> load woz_nod_short.xml
 *   "longNod"       -> load woz_nod_long.xml
 *   "longNodTwice"  -> load woz_nod_long_twice.xml
 *   "continuerBC"   -> load random continuer verbal backchannel FML
 *   "assessmentBC"  -> load random assessment verbal backchannel FML
 *   "turnShift"     -> trigger HALT interruption (stop agent speech)
 *   "nothing"       -> heartbeat (no action)
 *
 * Architecture:
 *   [RealTimeGreta.py] --TCP 5961--> [TurnManagementModel] --> FML load --> Planner
 *                                                            --> HALT     --> InterruptionManager
 *
 * Based on: TurnManagementWoZ.java (socket pattern, FML loading, nod/BC dispatch)
 *           InterruptionClicker.java (HALT interruption pattern)
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
import greta.core.util.speech.Speech;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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

public class TurnManagementModel {

    private static final Logger LOG = Logger.getLogger(TurnManagementModel.class.getName());

    // Paths
    private String batch_main_path;
    private String batch_kill_path = "Common\\Data\\TurnManagement\\kill_server.bat";

    // Socket servers (same ports as WoZ, reused protocol)
    private Server feedback_server;
    private Server command_server;
    private String response;
    private Process server_process;
    private Thread server_shutdownHook;

    private CharacterManager cm;

    // FML paths for head nods
    private String nodShortPath   = ".\\Examples\\DemoEN\\backchannel\\woz_nod_short.xml";
    private String nodLongPath    = ".\\Examples\\DemoEN\\backchannel\\woz_nod_long.xml";
    private String nodLongTwicePath = ".\\Examples\\DemoEN\\backchannel\\woz_nod_long_twice.xml";

    // Verbal backchannel FML folders (per language)
    private static final String CONTINUER_DIR_EN  = ".\\Examples\\DemoFR\\backchannel\\verbal_backchannel\\continuer";
    private static final String ASSESSMENT_DIR_EN = ".\\Examples\\DemoFR\\backchannel\\verbal_backchannel\\assessment";
    private static final String CONTINUER_DIR_FR  = ".\\Examples\\DemoFR\\backchannel\\verbal_backchannel\\continuer";
    private static final String ASSESSMENT_DIR_FR = ".\\Examples\\DemoFR\\backchannel\\verbal_backchannel\\assessment";

    private String continuerFMLDir;
    private String assessmentFMLDir;
    private String[] continuerFMLFileList;
    private String[] assessmentFMLFileList;
    private double[] continuerWeights;
    private double[] assessmentWeights;
    private Random rand = new Random();

    // FML parsing and intention dispatch
    private ArrayList<IntentionPerformer> performers = new ArrayList<>();
    private XMLParser fmlparser = XML.createParser();
    private XMLParser bmlparser = XML.createParser();

    // Action counters for status display
    private volatile int totalActionsReceived = 0;
    private volatile int nodsTriggered = 0;
    private volatile int bcTriggered = 0;
    private volatile int haltsTriggered = 0;
    private volatile String lastAction = "none";
    private volatile boolean modelConnected = false;

    // Listener for status updates (used by the Frame)
    private StatusListener statusListener;

    public interface StatusListener {
        void onStatusChanged(String lastAction, int totalActions, int nods, int bcs, int halts, boolean connected);
    }

    public TurnManagementModel(CharacterManager cm, String main_path) throws IOException {
        System.out.println("[TurnManagementModel] Initializing...");
        this.cm = cm;
        this.batch_main_path = main_path;

        // Select language-appropriate backchannel directories
        selectLanguagePaths();

        // Load verbal backchannel FML file lists
        loadVerbalBackchannelLists();

        // Setup socket servers (same ports as WoZ: feedback=5960, commands=5961)
        feedback_server = new Server();
        feedback_server.setAddress("localhost");
        feedback_server.setPort("5960");

        command_server = new Server();
        command_server.setAddress("localhost");
        command_server.setPort("5961");

        // Start feedback server
        feedback_server.startConnection();
        Thread feedbackThread = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("[TurnManagementModel] Waiting for feedback connection...");
                    feedback_server.accept_new_connection("Model feedback");
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        };
        feedbackThread.start();

        // Start command server
        command_server.startConnection();
        Thread commandThread = new Thread() {
            @Override
            public void run() {
                try {
                    System.out.println("[TurnManagementModel] Waiting for Python model connection on port 5961...");
                    command_server.accept_new_connection("TurnManagementModel");
                    command_server.sendMessage("ok");

                    Thread shutdownHook = new Thread(() -> {
                        try {
                            System.out.println("[TurnManagementModel] Shutdown hook start");
                            if (server_process != null) {
                                while (server_process.isAlive()) {
                                    try {
                                        command_server.sendMessage("kill");
                                    } catch (IOException ex) {
                                        break;
                                    }
                                }
                                server_process.waitFor();
                            }
                            System.out.println("[TurnManagementModel] Shutdown hook end");
                        } catch (InterruptedException ex) {
                            LOG.log(Level.SEVERE, null, ex);
                        }
                    });
                    Runtime.getRuntime().addShutdownHook(shutdownHook);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        };
        commandThread.start();

        // Start Python RealTimeGreta.py process
        Thread mainThread = new Thread() {
            @Override
            public void run() {
                System.out.println("[TurnManagementModel] Starting RealTimeGreta.py...");

                try {
                    server_process = new ProcessBuilder(batch_main_path)
                            .redirectErrorStream(true)
                            .redirectOutput(ProcessBuilder.Redirect.INHERIT)
                            .start();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }

                // Wait for "Generator started" message from Python
                while (true) {
                    try {
                        String msg = command_server.receiveMessage();
                        System.out.println("[TurnManagementModel] Preparation: " + msg);
                        if (msg != null && msg.contains("Generator started")) {
                            System.out.println("[TurnManagementModel] Python model connected and ready");
                            modelConnected = true;
                            notifyStatus();
                            break;
                        }
                        Thread.sleep(100);
                    } catch (Exception e) {
                        // Keep waiting
                    }
                }

                // Main command loop: receive actions from Python and dispatch
                while (true) {
                    try {
                        String action = command_server.receiveMessage();
                        if (action == null || action.isEmpty()) continue;

                        action = action.trim();
                        command_server.sendMessage("ok");

                        totalActionsReceived++;
                        lastAction = action;

                        final String actionToDispatch = action;
                        new Thread(() -> {
                            try {
                                dispatchAction(actionToDispatch);
                            } catch (Exception e) {
                                System.out.println("[TurnManagementModel] DISPATCH ERROR: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }).start();

                        if (totalActionsReceived % 50 == 0) {
                            notifyStatus();
                        }

                    } catch (IOException ex) {
                        System.out.println("[TurnManagementModel] Connection error: " + ex.getMessage());
                        modelConnected = false;
                        notifyStatus();
                        break;
                    } catch (Exception ex) {
                        System.out.println("[TurnManagementModel] Command loop error: " + ex.getMessage());
                    }
                }
            }
        };
        mainThread.start();

        System.out.println("[TurnManagementModel] Initialization complete");
    }

    private void selectLanguagePaths() {
        String lang = cm.getLanguage();
        System.out.println("[TurnManagementModel] Character language: " + lang);

        if ("FR".equalsIgnoreCase(lang) || "french".equalsIgnoreCase(lang)) {
            continuerFMLDir  = CONTINUER_DIR_FR;
            assessmentFMLDir = ASSESSMENT_DIR_FR;
            System.out.println("[TurnManagementModel] Using FRENCH verbal backchannels");
        } else {
            continuerFMLDir  = CONTINUER_DIR_EN;
            assessmentFMLDir = ASSESSMENT_DIR_EN;
            System.out.println("[TurnManagementModel] Using ENGLISH verbal backchannels");
        }
    }

    // French BC frequency weights from training data (occurrence counts).
    // Keys are the distinctive part of the filename (between last separator and .xml).
    private static final java.util.Map<String, Integer> FR_CONTINUER_FREQ = new java.util.HashMap<>();
    private static final java.util.Map<String, Integer> FR_ASSESSMENT_FREQ = new java.util.HashMap<>();
    static {
        FR_CONTINUER_FREQ.put("continuer_ouais",    931);  // ouais (571) + Ouais (360)
        FR_CONTINUER_FREQ.put("continuer_oui",      953);  // oui (405) + Oui (548)
        FR_CONTINUER_FREQ.put("continuer_ok",        516);  // Ok (377) + OK (139)
        FR_CONTINUER_FREQ.put("continuer_tresbien",  258);
        FR_CONTINUER_FREQ.put("continuer_hmm",       191);
        FR_CONTINUER_FREQ.put("continuer_daccord",   182);

        FR_ASSESSMENT_FREQ.put("assessment_ah",          225);  // Ah (176) + ah (49)
        FR_ASSESSMENT_FREQ.put("assessment_super",        18);
        FR_ASSESSMENT_FREQ.put("assessment_genial",       10);
        FR_ASSESSMENT_FREQ.put("assessment_oh",           13);  // Oh (9) + oh (4)
        FR_ASSESSMENT_FREQ.put("assessment_incroyable",    4);
        FR_ASSESSMENT_FREQ.put("assessment_cestfou",       4);
    }

    private void loadVerbalBackchannelLists() {
        try {
            ArrayList<String> continuerList = new ArrayList<>();
            Files.list(Paths.get(continuerFMLDir))
                .filter(p -> p.toString().endsWith(".xml"))
                .forEach(s -> continuerList.add(s.toString()));
            continuerFMLFileList = continuerList.stream().toArray(String[]::new);

            ArrayList<String> assessmentList = new ArrayList<>();
            Files.list(Paths.get(assessmentFMLDir))
                .filter(p -> p.toString().endsWith(".xml"))
                .forEach(s -> assessmentList.add(s.toString()));
            assessmentFMLFileList = assessmentList.stream().toArray(String[]::new);

            String lang = cm.getLanguage();
            boolean isFrench = "FR".equalsIgnoreCase(lang) || "french".equalsIgnoreCase(lang);

            continuerWeights  = buildWeights(continuerFMLFileList, isFrench ? FR_CONTINUER_FREQ : null);
            assessmentWeights = buildWeights(assessmentFMLFileList, isFrench ? FR_ASSESSMENT_FREQ : null);

            System.out.println("[TurnManagementModel] Loaded " + continuerFMLFileList.length + " continuer FML files"
                    + (isFrench ? " (weighted)" : " (uniform)"));
            for (int i = 0; i < continuerFMLFileList.length; i++) {
                System.out.println("  " + continuerFMLFileList[i] + " -> p=" + String.format("%.3f", continuerWeights[i]));
            }
            System.out.println("[TurnManagementModel] Loaded " + assessmentFMLFileList.length + " assessment FML files"
                    + (isFrench ? " (weighted)" : " (uniform)"));
            for (int i = 0; i < assessmentFMLFileList.length; i++) {
                System.out.println("  " + assessmentFMLFileList[i] + " -> p=" + String.format("%.3f", assessmentWeights[i]));
            }
        } catch (Exception e) {
            System.out.println("[TurnManagementModel] Warning - could not load verbal backchannel FML files: " + e.getMessage());
            continuerFMLFileList = new String[0];
            assessmentFMLFileList = new String[0];
            continuerWeights = new double[0];
            assessmentWeights = new double[0];
        }
    }

    private double[] buildWeights(String[] filePaths, java.util.Map<String, Integer> freqMap) {
        double[] weights = new double[filePaths.length];
        if (freqMap == null || filePaths.length == 0) {
            double uniform = 1.0 / Math.max(filePaths.length, 1);
            java.util.Arrays.fill(weights, uniform);
            return weights;
        }

        double total = 0;
        for (int i = 0; i < filePaths.length; i++) {
            String name = Paths.get(filePaths[i]).getFileName().toString().replaceAll("\\.xml$", "");
            Integer freq = freqMap.get(name);
            weights[i] = (freq != null) ? freq : 1.0;
            total += weights[i];
        }
        if (total > 0) {
            for (int i = 0; i < weights.length; i++) {
                weights[i] /= total;
            }
        }
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

    /**
     * Dispatch an action command received from the Python model.
     */
    private void dispatchAction(String action) {
        switch (action) {
            case "shortNod":
                sendShortNod();
                nodsTriggered++;
                break;
            case "longNod":
                sendLongNod();
                nodsTriggered++;
                break;
            case "longNodTwice":
                sendLongNodTwice();
                nodsTriggered++;
                break;
            case "continuerBC":
                sendContinuerBackchannel();
                bcTriggered++;
                break;
            case "assessmentBC":
                sendAssessmentBackchannel();
                bcTriggered++;
                break;
            case "turnShift":
                triggerHaltInterruption();
                haltsTriggered++;
                break;
            case "nothing":
                // Heartbeat - no action
                break;
            default:
                System.out.println("[TurnManagementModel] Unknown action: " + action);
                break;
        }
    }

    // ===== Head Nod dispatch =====

    public void sendShortNod() {
        try {
            System.out.println("[TurnManagementModel] -> Short Nod");
            load(nodShortPath);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error sending short nod", ex);
        }
    }

    public void sendLongNod() {
        try {
            System.out.println("[TurnManagementModel] -> Long Nod");
            load(nodLongPath);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error sending long nod", ex);
        }
    }

    public void sendLongNodTwice() {
        try {
            System.out.println("[TurnManagementModel] -> Long Nod Twice");
            load(nodLongTwicePath);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error sending long nod twice", ex);
        }
    }

    // ===== Verbal Backchannel dispatch =====

    public void sendContinuerBackchannel() {
        if (continuerFMLFileList == null || continuerFMLFileList.length == 0) return;
        int idx = weightedRandomIndex(continuerWeights);
        String fml = continuerFMLFileList[idx];
        try {
            System.out.println("[TurnManagementModel] -> Continuer BC: " + fml + " (p=" + String.format("%.3f", continuerWeights[idx]) + ")");
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
            System.out.println("[TurnManagementModel] -> Assessment BC: " + fml + " (p=" + String.format("%.3f", assessmentWeights[idx]) + ")");
            load(fml);
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error sending assessment BC", ex);
        }
    }

    // ===== HALT Interruption (from InterruptionClicker pattern) =====

    public void triggerHaltInterruption() {
        Speech currentSpeech = Speech.getCurrentPlayingScheduledSpeech(false);
        if (currentSpeech == null) {
            System.out.println("[TurnManagementModel] -> Turn Shift (no speech playing, skipping HALT)");
            return;
        }

        System.out.println("[TurnManagementModel] -> HALT Interruption (turn shift)");

        try {
            String interruptionFML =
                "<?xml version=\"1.0\" encoding=\"ISO-8859-1\" ?>\n" +
                "<fml-apml>\n" +
                "\t<bml>\n" +
                "\t\t<speech id=\"s1\" start=\"0.0\" language=\"english\" text=\"\" voice=\"cereproc\">\n" +
                "\t\t</speech>\n" +
                "\t</bml>\n" +
                "\t<fml>\n" +
                "\t</fml>\n" +
                "</fml-apml>\n";

            File tempFile = new File("TurnManagementModel_halt_temp.xml");
            FileWriter writer = new FileWriter(tempFile);
            writer.write(interruptionFML);
            writer.close();

            XMLParser parser = XML.createParser();
            parser.setValidating(true);
            XMLTree fml = parser.parseFile(tempFile.getPath());
            List<Intention> intentions = FMLTranslator.FMLToIntentions(fml, cm);

            Mode mode = FMLTranslator.getDefaultFMLMode();
            mode.setCompositionType("replace");
            mode.setReactionType("HALT");
            mode.setReactionDuration("SHORT");
            mode.setSocialAttitude("neutral");

            ID id = IDProvider.createID("TurnManagementModel_HALT");

            for (IntentionPerformer performer : performers) {
                performer.performIntentions(intentions, id, mode);
            }

            tempFile.delete();

        } catch (Exception ex) {
            LOG.log(Level.SEVERE, "Error triggering HALT interruption", ex);
        }
    }

    // ===== FML Loading (from TurnManagementWoZ pattern) =====

    public ID load(String fmlFileName) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
        return load(fmlFileName, CompositionType.blend);
    }

    public ID load(String fmlFileName, CompositionType compositionType) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
    //public synchronized ID load(String fmlFileName, CompositionType compositionType) throws IOException, TransformerException, SAXException, ParserConfigurationException, JMSException {
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
                file_path = System.getProperty("user.dir") + "\\fml_model_renew.xml";
            } else {
                file_path = System.getProperty("user.dir") + "\\fml_model.xml";
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
            pw = new PrintWriter(new FileWriter(System.getProperty("user.dir") + "\\fml_to_bml_model.xml"));
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
        return System.getProperty("user.dir") + "\\fml_to_bml_model.xml";
    }

    // ===== Feedback (agent speaking state) =====

    public void performFeedback(String type) {
        Thread t = new Thread(() -> {
            while (true) {
                try {
                    if (feedback_server != null && feedback_server.getOut() != null) {
                        feedback_server.sendMessage(type);
                        response = feedback_server.receiveMessage();
                    } else {
                        Thread.sleep(100);
                    }
                } catch (Exception ex) {
                    System.out.println("[TurnManagementModel] Feedback error: " + ex.getMessage());
                }
            }
        });
        t.setDaemon(true);
        t.start();
    }

    // ===== Status and accessors =====

    public void setStatusListener(StatusListener listener) {
        this.statusListener = listener;
    }

    private void notifyStatus() {
        if (statusListener != null) {
            statusListener.onStatusChanged(lastAction, totalActionsReceived, nodsTriggered, bcTriggered, haltsTriggered, modelConnected);
        }
    }

    public boolean isModelConnected() { return modelConnected; }
    public int getTotalActionsReceived() { return totalActionsReceived; }
    public int getNodsTriggered() { return nodsTriggered; }
    public int getBcTriggered() { return bcTriggered; }
    public int getHaltsTriggered() { return haltsTriggered; }
    public String getLastAction() { return lastAction; }

    public ArrayList<IntentionPerformer> getPerformers() {
        return performers;
    }

    public void resetMicServer(String port) {
        try {
            if (command_server != null && command_server.getOut() != null) {
                command_server.sendMessage("updateMicPort " + port);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }
}
