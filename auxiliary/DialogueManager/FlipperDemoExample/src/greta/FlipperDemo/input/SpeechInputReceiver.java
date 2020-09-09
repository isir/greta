package greta.FlipperDemo.input;


import greta.auxiliary.activemq.Receiver;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

public class SpeechInputReceiver extends Receiver<String> {
	
	private BlockingQueue<String> messages = new LinkedBlockingQueue();

	
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
       // messages.add("Jai gayatri mata");
       return true;

    }

    private void initSphinx(){
       while(true){
           
       }
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

  /*  public static void main(String[] args) throws IOException {
        SpeechInputReceiver sphinx = new SpeechInputReceiver();
        sphinx.init();

    }
*/

    @Override
    protected void onMessage(String message, Map<String, Object> map) {
            System.out.println(" flipper received **: "+ message);
            messages.add("BONJOUR");
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

}
