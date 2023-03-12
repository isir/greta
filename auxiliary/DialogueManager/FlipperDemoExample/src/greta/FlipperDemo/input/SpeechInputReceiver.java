package greta.FlipperDemo.input;


import greta.auxiliary.activemq.Receiver;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonString;
import javax.json.JsonValue;

public class SpeechInputReceiver extends Receiver<String> {
	
	private BlockingQueue<String> messages = new LinkedBlockingQueue();
        //public static double positivity =0.0;
        //public Client client = ASRInputManager.getClient();

	
    public SpeechInputReceiver(String host, String port, String topic){
        super(host, port, topic);
        
    }
    public SpeechInputReceiver(){
        
      //  super("localhost", "61616", "greta.asr.response");
        
    }


    /**
     * Initialize the SpeechInputReceiver ASR with default EN-us and start the speech recognizer
     * @return true if successfully started
     */
    public boolean init(){
      
       return true;

    }



    /**
     * Stop the SpeechInputReceiver ASR
     * @return
     */
    public boolean stop(){
       
        return true;
    }

    /**
     * Start the SpeechInputReceiver ASR (do not call after initialization)
     * @return if succeeded
     */
    public boolean start(){
       
        return true;
    }

    /**
     * General method for if it has transcribed speech
     * @return true if a result is final
     */
    public boolean hasMessage(){
        return messages.size() != 0;
    }

    /**
     * General method for retrieving transcribed speech
     * @return the hypothesis speech of SpeechInputReceiver
     */
    public String getMessage(){
        try {
            return messages.take();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return "";
    }
    
     /*public boolean hasSmile(){
        // if chunk or np frame = chunk size
        //return true 
        System.out.println("VERIFICATION DE SOURIR DETECTION");
        return positivity != 0.0;
    }
     
    public String getPositivity(){
        
        System.out.println("LA VALEUR DE SOURIR DETECTEE EST : "+ String.valueOf(this.positivity));
        return String.valueOf(this.positivity);
    }*/
    

                    //System.out.println("Received AU06: " + au6 + "; AU12: " + au12);
                    //SpeechInputReceiver.positivity = (au6 + au12 )/2;
                



    @Override
    protected void onMessage(String message, Map<String, Object> map) {
            System.out.println(" flipper received **: "+ message);
            
            JsonReader jr = Json.createReader(new StringReader(message));
            JsonObject jo = jr.readObject();
            JsonString transcript = jo.getJsonString("TRANSCRIPT");
          
            /*try {
                SpeechInputReceiver.positivity =client.receivePositivity();
            } catch (IOException ex) {
                Logger.getLogger(SpeechInputReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }*/
          
            //System.out.println(" flipper received OPENFACE **: "+ String.valueOf(positivity));
            System.out.println(" flipper received **: "+ transcript.toString());
            String cleanTranscript = transcript.toString().replaceAll("\"", "");
           cleanTranscript = cleanTranscript.toLowerCase().trim();
            messages.add(cleanTranscript);
    }

    @Override
    protected String getContent(Message message)  {
		
		String msg =null;
            try {
                msg = ((TextMessage) message).getText();
            } catch (JMSException ex) {
               // Logger.getLogger(MessageReceiver.class.getName()).log(Level.SEVERE, null, ex);
               System.out.println("cought exception in getContent : "+ ex.toString());
            }
		return msg;        
    }
    
    @Override
    protected void onConnectionStarted() {
    	super.onConnectionStarted();
    }


}
