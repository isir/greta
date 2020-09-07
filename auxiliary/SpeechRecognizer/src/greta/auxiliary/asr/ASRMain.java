
package greta.auxiliary.asr;

/**
 *
 * @author admin
 */


public class ASRMain {
    
    	private SpeechRecognizer sr = null;
    	private ActiveMQConnector activeMQConnection = null;
        	
	private String host = null;
	private String port = null;
	private String requestTopic = null;
	private String responseTopic = null;
	
	
	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return this.port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getRequestTopic() {
		return this.requestTopic;
	}
	
	public String getResponseTopic() {
		return this.responseTopic;
	}

	public void setRequestTopic(String requestTopic) {
		this.requestTopic = requestTopic;
	}
	
	public void setResponseTopic(String responseTopic) {
		this.responseTopic = responseTopic;
	}
        public ASRMain(){
            activeMQConnection = new ActiveMQConnector();
            sr = new SpeechRecognizer();
            
        }
        public void initialiseSpeechRecognition(){
            sr.setTranscriptSender(activeMQConnection.getSender());
            sr.start();
            
        }
        
        
        public ActiveMQConnector getActiveMQConnector(){
            return activeMQConnection;
        }
	public void establishActiveMqConnection(String host, String port, String requestTopic, String responseTopic) {
               
                this.host = host;
                this.port = port;
                this.requestTopic = requestTopic;
                this.responseTopic = responseTopic;
		
		this.activeMQConnection.setHost(host);
		this.activeMQConnection.setPort(port);
		this.activeMQConnection.setRequestTopic(requestTopic);
		this.activeMQConnection.setResponseTopic(responseTopic);		
		this.activeMQConnection.initializeSenderAndReceiver();
		
	} 
}
