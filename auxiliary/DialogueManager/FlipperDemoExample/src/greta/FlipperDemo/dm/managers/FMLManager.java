/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.dm.managers;

import eu.aria.util.activemq.SimpleProducerWrapper;
import eu.aria.util.activemq.SimpleReceiverWrapper;
import eu.aria.util.activemq.util.UrlBuilder;
import eu.aria.util.translator.Translator;
import eu.aria.util.translator.api.ActiveMQConnector;
import eu.aria.util.translator.api.AgentFeedback;
import eu.aria.util.translator.api.FileCache;
import eu.aria.util.translator.api.ReplacerGroup;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.*;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import static java.lang.Thread.sleep;
import static java.nio.file.StandardOpenOption.APPEND;
import static java.nio.file.StandardOpenOption.CREATE;

/**
 * Created by WaterschootJB on 30-5-2017.
 */
public class FMLManager extends SimpleManager{

    private static org.slf4j.Logger logger = LoggerFactory.getLogger(FMLManager.class.getName());
    public boolean stoppedTalking;
    private String logfile;
    private boolean isPlanning;
   private SimpleProducerWrapper sendAgentData;
    private SimpleProducerWrapper sendDialogTurn;//sends agent's start here and user stopped in NLUManager
    public FMLManager(){
        super();
        this.receivedBMLQueue = new LinkedBlockingQueue<>();
        this.receiverBML = new SimpleReceiverWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort),amqOutputBML,true);
        this.isPlanning = false;
        setup();
        
       System.out.println("FMLManager started");
        this.sendDialogTurn = new SimpleProducerWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort),"DialogTurn",true);
        this.sendDialogTurn.init();
        this.sendAgentData = new SimpleProducerWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort), amqDialog,true);
       this.sendAgentData.init();
    }
    public FMLManager(String host, String port, String senderFmlTopic){
        super();
        setAmqHostname(host);
        setAmqPort(port);
        setAmqSendTopicName(senderFmlTopic);
        this.receivedBMLQueue = new LinkedBlockingQueue<>();
        this.receiverBML = new SimpleReceiverWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort),amqOutputBML,true);
        this.isPlanning = false;
        setup();
        
       System.out.println("FMLManager started");
        this.sendDialogTurn = new SimpleProducerWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort),"DialogTurn",true);
        this.sendDialogTurn.init();
        this.sendAgentData = new SimpleProducerWrapper(UrlBuilder.getUrlTcp(amqHostname,amqPort), amqDialog,true);
       this.sendAgentData.init();
    }
    private boolean agentTalking = false;
    private String outputBML = "";
    private String amqSendTopicName = "greta.input.FML";
    private String amqFeedbackTopicName = "greta.output.feedback.BML";
    private String amqOutputBML = "greta.output.BML";
    private String amqPort = "61616";
    private String amqHostname = "localhost"; // 192.168.0.1
    private String amqDialog = "dialog";
    private String replacerConfigPath = "Common\\Data\\FlipperResources\\data\\a-config.json";
    private String templateFolderPath = "Common/Data/FlipperResources/fmltemplates";
    private boolean showFMLGUI = true;
    private boolean showFallbackGUI = true;
    private boolean disableAMQ = false;
    LinkedList<QueueableBehaviour> behaviourQueue = new LinkedList<>();
    ActiveMQConnector activeMQConnector;
    private boolean isConnected;
    private SimpleReceiverWrapper receiverBML;
    private BlockingQueue<String> receivedBMLQueue = null;
    
    public void setAmqHostname(String host){
        amqHostname = host;
    }
    public void setAmqSendTopicName(String senderFmlTopic){
        amqSendTopicName = senderFmlTopic;
    }
    public void setAmqPort(String port){
        amqPort = port;
    }
    @Override
    public void process(){
        processNext();
        super.process();
    }
    Map<String, Translator> translators = new HashMap<>();

    /**
     * DEPRACATED: OLD FLIPPER
     * @param params
     * @param paramArrays
     */
    public void setParams(Map<String, String> params, Map<String, String[]> paramArrays){
        if(!params.containsKey("fml_template_folder")){
            System.err.println("Manager "+ name + "("+id+") must provide a FML template folder containing only FML templates: 'fml_template_folder'.");
        }else{
            templateFolderPath = params.get("fml_template_folder");
        }
        if(!params.containsKey("replacer_config_path")){
            System.err.println("Manager "+ name + "("+id+") "
                    + "must provide a path for the replacer config file: 'replacer_config_path'.");
        }else{
            replacerConfigPath = params.get("replacer_config_path");
        }
        if(params.containsKey("amq_hostname")){
            amqHostname = params.get("amq_hostname");
        }
        if(params.containsKey("amq_port")){
            amqPort = params.get("amq_port");
        }
        if(params.containsKey("amq_feedback_topic_name")){
            amqFeedbackTopicName = params.get("amq_feedback_topic_name");
        }
        if(params.containsKey("amq_send_topic_name")){
            amqSendTopicName = params.get("amq_send_topic_name");
        }
        if(params.containsKey("amq_full_bml_feedback")){
            amqOutputBML = params.get("amq_full_bml_feedback");
        }
        if(params.containsKey("show_fml_gui")){
            showFMLGUI = Boolean.parseBoolean(params.get("show_fml_gui"));
        }
        if(params.containsKey("show_fallback_gui")){
            showFallbackGUI = Boolean.parseBoolean(params.get("show_fallback_gui"));
        }
        if(params.containsKey("disable_amq")){
            disableAMQ = Boolean.parseBoolean(params.get("disable_amq"));
        }
        setup();
    }

    ReplacerGroup replacerGroup;
    static long lastTimeStamp = System.currentTimeMillis();
    static int fmlId = 0;

    public static Say agentSay;

    public void setLogfile(String s){
        this.logfile = s;
    }

    HashMap<String, File> templates = new HashMap<>();


    /**
     * Sets up the ActiveMQConnectors
     */
    public void setup(){
        if(!disableAMQ){
            replacerGroup = new ReplacerGroup(replacerConfigPath);

            activeMQConnector = new ActiveMQConnector();
            activeMQConnector.setReplacerGroup(replacerGroup);

            activeMQConnector.initialiseSender(amqHostname, amqPort, amqSendTopicName);
            activeMQConnector.initialiseFeedback(amqHostname, amqPort, amqFeedbackTopicName);
            activeMQConnector.initialiseFeedback(amqHostname, amqPort, amqOutputBML);


           System.out.println("BML receiver started");
            receiverBML.start((Message message) ->{
                if(message instanceof TextMessage){
                    try{
                        outputBML = (((TextMessage) message).getText());
                        receiveData(outputBML);
                    }
                    catch(JMSException e){
                        e.printStackTrace();
                    }
                }
            });


            if(showFMLGUI){
                activeMQConnector.showSenderGui(200, 200);
                activeMQConnector.showFeedbackGui(200, 400);
            }

            File templateFolder = new File(templateFolderPath);

            File[] filesInFolder = templateFolder.listFiles();
            for (File cur : filesInFolder) {
                if(cur.getName().endsWith(".xml")){
                    templates.put(cur.getName(), cur);
                }
            }

            FileCache.getInstance().preloadFilesInFolder(templateFolder);
            this.isConnected = true;

            activeMQConnector.addFeedbackListener((AgentFeedback feedback) -> {
                switch (feedback.getType()) {
                    case Start:
                        if(feedback.getId().startsWith("ID")){
                            Message m = this.sendDialogTurn.createTextMessage("agent_start");
                           this.sendDialogTurn.sendMessage(m);

                            this.isPlanning = false;
                           System.out.println("stopped planning!");
                           System.out.println("Agent started playback. ID: " + feedback.getId()+" type: "+feedback.toString());
                            setIsTalking(true);
                            if(agentSay == null)
                                this.agentSay = new Say();
                            this.agentSay.setActorName("Agent");
                            this.agentSay.setTimestamp(System.currentTimeMillis());
                            this.agentSay.setTalking(true);
                            this.agentSay.setLanguage("en");
                        }
                        break;
                    case End:
                        if(feedback.getId().startsWith("ID")) {

                                Message m = this.sendDialogTurn.createTextMessage("agent_end");
                               this.sendDialogTurn.sendMessage(m);
                           System.out.println("Agent finished. ID: " + feedback.getId() + " type: " + feedback.toString());
                            setIsTalking(false);
                            this.agentSay.setLength(System.currentTimeMillis()-this.agentSay.getTimestamp());
                            this.agentSay.setTalking(false);

                            stoppedTalking = true;
                            try {
                                sleep(500);
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                            if(this.agentSay.getText() != null) {
                                addAgentSay();
                            }
                            stoppedTalking = false;
                        }
                        break;
                    case Other:
                        try{
                        if(feedback.getId().startsWith("ID")){
                            
                           System.out.println("Agent didnt perform. ID: " + feedback.getId()+" type: "+feedback.toString());
                            if(feedback.getTypeString().equals("dead")){
                                setIsTalking(false);

                            }
                            if(feedback.getTypeString().equals("stopped"+" type: "+feedback.toString())){
                                setIsTalking(false);
                            }
                        }
                        }
                        catch (Exception e){
                            //System.out.println(feedback.getType()..toString());
                        }
                    default:
                        try{
                        if(feedback.getTypeString().equals("dead") || feedback.getTypeString().equals("stopped")){
                            logger.debug("Feedback not of DM: " + feedback.getTypeString());
                        }
                        else{
                           System.out.println("Unknown feedback type from GRETA! '" + feedback.getTypeString() + "'"+" type: "+feedback.toString());
                        }
                        }
                        catch(Exception e){
                            //O no, some weird feedback

                        }
                        break;
                }
            });
        }

    }

    /**
     * Receives a BML string and prints what the agent has actually said.
     * @param outputBML, the BML String
     */
    private void receiveData(String outputBML) {
        //outputBML = outputBML.replaceAll("xmlns","xmlns:xsi");
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = null;
        try {
            builder = factory.newDocumentBuilder();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        InputSource bml = new InputSource();
        bml.setCharacterStream(new StringReader(outputBML));
        try {
            Document doc = builder.parse(bml);
            logger.debug("Agent data received: " + outputBML);
            this.receivedBMLQueue.add(outputBML);

            XPathFactory xPathfactory = XPathFactory.newInstance();
            XPath xpath = xPathfactory.newXPath();
            String speech = "";
            try {
                String expression = "/bml/speech/voice";
                XPathExpression expr = xpath.compile(expression);
                speech = (String) expr.evaluate(doc, XPathConstants.STRING);
                speech = speech.replace("\n"," ");
                speech = speech.trim();
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
            logger.debug("Agent says: " + speech);
            String language = "";
            try {
                String expression = "string(/bml/speech/@language)";
                XPathExpression expr = xpath.compile(expression);
                language = (String) expr.evaluate(doc, XPathConstants.STRING);
            } catch (XPathExpressionException e) {
                e.printStackTrace();
            }
            if(agentSay == null)
                this.agentSay = new Say();
            this.agentSay.setText(speech);
            this.agentSay.setLanguage(language);

        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isBMLReceiverConnected(){
        return this.receiverBML.isReceiver();
    }

    public boolean hasReceivedBML(){
        return !this.receivedBMLQueue.isEmpty();
    }

    /**
     * Needs to be updated to a robust check. The XMLTranslator needs an update in order to retrieve the status
     * of the AMQ wrappers.
     * @return if the AMQ for FML sending is still active.
     */
    public boolean isConnected(){
        return isConnected;
    }

    /**
     * Checks if there is still a behaviour in the queue
     * @return true if so
     */
    public boolean hasBehaviour(){
        return !this.behaviourQueue.isEmpty();
    }

    public boolean setIsTalking(boolean isTalking){
        return this.agentTalking = isTalking;
    }

    public boolean getAgentIsTalking(){return this.agentTalking;}

    public boolean showFallbackGUI(){
        return showFallbackGUI;
    }

    private void processNext(ArrayList<String> argNames, ArrayList<String> argValues){
        File f = templates.get(argValues.get(argNames.indexOf("template"))+".xml");
        if(f == null){
           System.out.println("Template named: "+argValues.get(argNames.indexOf("template")) + " not found!" );
        }
        String content = FileCache.getInstance().getFileContent(f);
        // read the template
        replacerGroup.readString(content);

        // generate internal decision
        replacerGroup.generateComponents();

        //Replace attributes and variables
        for(String argName :  argNames){
            String value = argValues.get(argNames.indexOf(argName));
            if(argName.startsWith("var.")){
                argName = argName.substring(4);
                replacerGroup.replaceVar(argName, value);
            }
            else if(argName.startsWith("alt.")){
                argName = argName.substring(4);
                replacerGroup.replaceVar(argName, value);
            }
            else if(argName.startsWith("certainty.")){
                argName = argName.substring(10);
                replacerGroup.replaceAttribute("certainty", argName, value);
            }
            else if(argName.startsWith("emotion.")){
                argName = argName.substring(8); // "em2.type"
                int secondPoint = argName.indexOf(".");
                String eid = argName.substring(0,secondPoint); //"em2"
                argName = argName.substring(secondPoint+1);
                replacerGroup.replaceAttribute(eid, argName, value);
            }
            else if(argName.startsWith("importance.")){
                argName = argName.substring(11);
                replacerGroup.replaceAttribute("importance", argName, value);
            }
            else if(argName.startsWith("voice.")){
                argName = argName.substring(6);
                replacerGroup.replaceAttribute("", argName, value);
            }
            else if(argName.startsWith("fml-apml.")){
                argName = argName.substring(9);
                replacerGroup.replaceAttribute("",argName,value);
            }
        }
        // this will perform the replacements and sends them to the ActiveMQ topic
        if(!disableAMQ){
            replacerGroup.performReplacements("ID_" + fmlId++);
        }
        //setIsTalking(false);
        //lastTimeStamp = System.currentTimeMillis();
    }

    private boolean processNext(){
        if(behaviourQueue.peek() != null){
            QueueableBehaviour next = behaviourQueue.poll();
            if(!disableAMQ){
                processNext(next.argNames, next.argValues);
            }
            return true;
        }
        return false;
    }

    public void queue( ArrayList<String> argNames, ArrayList<String> argValues){
        QueueableBehaviour next = new QueueableBehaviour();
        next.argNames = argNames;
        next.argValues = argValues;
        behaviourQueue.add(next);
    }

    public void setPlanning(Boolean planning) {
       System.out.println("Planning...");
        this.isPlanning = planning;
    }

    private class QueueableBehaviour{
        public ArrayList<String> argNames;
        public ArrayList<String> argValues;
    }

    public void addAgentSay() {
        if(logfile != null) {
            long endTime = agentSay.getTimestamp() + agentSay.getLength();
            String turn = agentSay.getId() + ";" + agentSay.getTimestamp() + ";" + endTime + ";" + agentSay.getActorName() + ";" + agentSay.getText() +  ";";
            turn +=  agentSay.strategy; //agentSay.restpose + ";" + agentSay.gesture + ";" + agentSay.smile + ";" + agentSay._text;
            /*+ ";"+ agentSay.random + ";"+ agentSay.reward+ ";";
            for(int i =0; i<agentSay.qTable.length;i++)
            {
                turn += agentSay.qTable[i]+ ";";
            }*/
            turn += "\n";
            Charset charset = Charset.forName("UTF-8");
            byte data[] = turn.getBytes(charset);
            Path file = Paths.get(logfile);
            try (OutputStream out = new BufferedOutputStream(
                    Files.newOutputStream(file, CREATE, APPEND))) {
                out.write(data, 0, data.length);
            //    Message m = this.sendAgentData.createTextMessage(turn);
              //  this.sendAgentData.sendMessage(m);
            } catch (IOException e) {
               System.out.println("IOException: %s%n"+ e);
            }
            //this.agentSay = null;

        }
    }

    public String getLastText(){
        if(agentSay == null || agentSay.getText() == null){
            return "";
        }
        return this.agentSay.getText();
    }

    public boolean isPlanning(){
        return this.isPlanning;
    }
}
