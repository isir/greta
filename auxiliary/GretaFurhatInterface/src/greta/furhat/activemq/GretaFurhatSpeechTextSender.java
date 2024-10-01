package greta.furhat.activemq;



import java.io.IOException;
import java.util.List;
import java.util.Map;


import javax.jms.TextMessage;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.*;
import javax.jms.MessageProducer;
import org.apache.activemq.command.ActiveMQMessage;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.ExecutionException;


import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import java.io.StringReader;
import javax.xml.parsers.ParserConfigurationException;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import java.io.IOException;



/**
 *
 * @author Fousseyni Sangaré 04/2024-09/2024
 */


public class GretaFurhatSpeechTextSender extends WhiteBoard{

    protected MessageProducer producer;
    private ScheduledExecutorService executorService;

    public GretaFurhatSpeechTextSender(){
        super();
    }
    public GretaFurhatSpeechTextSender(String host, String port, String topic){
        super(host, port, topic);
        executorService = Executors.newScheduledThreadPool(1);
        startConnectionAttempt();
    }

    @Override
    protected void onConnectionStarted() {
        createProducer();
        super.onConnectionStarted();
    }

    private void createProducer(){
        try {
            producer = session.createProducer(destination);
            producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        } catch (Exception ex) {
            //Logs.error("producer not created");
        }
    } 

    @Override
    protected void onReboot() {
        super.onReboot();
        closeProducer();
    }

    private void closeProducer(){
        try { producer.close(); } catch (Exception ex) {//Logs.error("at producer closing : "+ex.getLocalizedMessage());

        }
        producer = null;
    }

    @Override
    protected void onDestinationChanged() {
        super.onDestinationChanged();
        closeProducer();
        createProducer();
    }
 

    public void send(String speechText){
        
        try {
                    
            // Phoneme data and speech text
            //phonemeJson = "{ \"class\": \"furhatos.records.Transcription\", \"phones\": [ { \"name\": \"_s\", \"prominent\": false, \"start\": 0.0, \"end\": 0.2 }, { \"name\": \"AI\", \"prominent\": true, \"start\": 0.2, \"word\": \"kids\", \"end\": 1.2 }, { \"name\": \"_s\", \"prominent\": false, \"start\": 1.2, \"end\": 1.7 }, { \"name\": \"AE\", \"prominent\": true, \"start\": 1.7, \"word\": \"are\", \"end\": 2.2 }, { \"name\": \"M\", \"prominent\": false, \"start\": 2.22, \"end\": 2.33 }, { \"name\": \"AH\", \"prominent\": false, \"start\": 2.33, \"word\": \"talking\", \"end\": 2.67 }, { \"name\": \"_s\", \"prominent\": false, \"start\": 2.67, \"end\": 2.7 }, { \"name\": \"R\", \"prominent\": false, \"start\": 2.7, \"word\": \"by\", \"end\": 2.81 }, { \"name\": \"OWX\", \"prominent\": false, \"start\": 2.81, \"end\": 2.86 }, { \"name\": \"B\", \"prominent\": false, \"start\": 2.86, \"end\": 2.92 }, { \"name\": \"AH\", \"prominent\": false, \"start\": 2.92, \"word\": \"the\", \"end\": 3.02 }, { \"name\": \"T\", \"prominent\": false, \"start\": 3.02, \"end\": 3.2 }, { \"name\": \"_s\", \"prominent\": false, \"start\": 3.2, \"word\": \"door\", \"end\": 3.8 }, { \"name\": \"\", \"prominent\": false, \"start\": 3.8, \"end\": 4.0 } ] }";

            // Create a TextMessage for the speech text
            TextMessage speechMessage = session.createTextMessage(speechText);

            // Set properties for the speech message
            speechMessage.setStringProperty("Type", "SpeechText");

            // Send the speech message
            producer.send(speechMessage);
            
        } catch (Exception ex) {
            //Logs.error("could not send message");
            System.out.println("greta.furhat.activemq.GretaFurhatSpeechTextSender: could not send message: "+ex.toString());
        }
    }
    /*
    public String processSpeechElement(List<Object> speechElements) {
        StringBuilder result = new StringBuilder();
        
        System.out.println("vvvvvvvvvvvvvvvvvvvvvv"+speechElements.toString());

        speechElements.stream()
            .filter(element -> element instanceof String)
            .forEach(element -> result.append((String) element));

        return result.toString();
    }*/
   

    private void startConnectionAttempt() {
        executorService.scheduleWithFixedDelay(this::attemptConnection, 0, 1, TimeUnit.SECONDS);
    }

    private void attemptConnection() {
        
        
        if (!this.isConnected()){
            
            try{
                //System.out.println("Attempting to connect audioserver to broker at: "+this.getURL());
                startConnection();
            } catch(Exception e){
            System.err.println("greta.furhat.activemq.GretaFurhatSpeechTextSender: speechText server connection attempt failed: " + e.getMessage());
            }
        }
        else{
            System.out.println("greta.furhat.activemq.GretaFurhatSpeechTextSender: speechText server connected to broker at: "+this.getURL() );
            executorService.shutdown(); // Stop retrying after a successful connection
         }   
    }
    
    public static String extractSpeechText(String xmlContent) throws ParserConfigurationException, SAXException, IOException {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document document = builder.parse(new InputSource(new StringReader(xmlContent)));

        return getTextFromNode(document.getDocumentElement());
    }

    private static String getTextFromNode(Node node) {
        StringBuilder textContent = new StringBuilder();
        NodeList childNodes = node.getChildNodes();
        
        for (int i = 0; i < childNodes.getLength(); i++) {
            Node childNode = childNodes.item(i);

            if (childNode.getNodeType() == Node.TEXT_NODE && isRelevantTextNode(childNode)) {
                textContent.append(childNode.getTextContent().trim()).append(" ");
            } else {
                textContent.append(getTextFromNode(childNode));
            }
        }
        
        return textContent.toString().trim();
    }

    private static boolean isRelevantTextNode(Node node) {
        Node parent = node.getParentNode();
        if (parent != null && (parent.getNodeName().equals("description") || parent.getNodeName().equals("reference"))) {
            return false;
        }
        return true;
    }
}
