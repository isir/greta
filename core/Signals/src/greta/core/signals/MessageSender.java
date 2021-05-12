package greta.core.signals;

import greta.core.signals.Test_NVBG_output;
import greta.core.signals.VHMSG;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
 
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.camel.util.Time;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.Scanner;
 
public class MessageSender{
     
    //URL of the JMS server. DEFAULT_BROKER_URL will just mean that JMS server is on localhost
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    
     
    // default broker URL is : tcp://localhost:61616"
    private static String subject = "DEFAULT_SCOPE"; // Queue Name.You can create any/many queue names as per your requirement. 
    
    private BufferedWriter p_stdin;

    public List<String> traitement_NVBG(String input,boolean nvbg) throws JMSException, FileNotFoundException, InterruptedException, IOException{  
        System.out.println("NVBG.MessageSender.main()");
        System.out.println(url);
        if(nvbg==false){
        // init shell
        ProcessBuilder builder = new ProcessBuilder("C:/Windows/System32/cmd.exe");
        Process p = null;
        try {
            p = builder.start();
        } catch (IOException e) {
            System.out.println(e);
        }
        // get stdin of shell
        p_stdin = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

        // execute commands
        executeCommand("cd apache-activemq-5.15.14\\bin");
        executeCommand("activemq start");
        //executeCommand("exit");

        // write stdout of shell (=output of all commands)



    
        }
        // Getting JMS connection from the server and starting it
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        //Creating a non transactional session to send/receive JMS message.
        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);  
        //Destination represents here our queue 'DEFAULT_SCOPE' on the JMS server. 
        //The queue will be created automatically on the server.
        Destination destination = session.createTopic(subject);
        System.out.println(session.createTopic(subject).getTopicName() +" "+ destination);
         
        // MessageProducer is used for sending messages to the queue.
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        String op= "vrExpress";
        String gstring ="Brad ranger harmony221 <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><act><participant id=\"harmony\" role=\"actor\"/><fml></fml><bml><speech id=\"sp1\" type=\"application/ssml+xml\">"+input+"</speech></bml></act>";
        
        System.out.println("Ding: "+input);
        String g="Brad ranger harmony221 <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>\r\n" +
               "   <act>\r\n" +
               "       <participant id=\"harmony\" role=\"actor\"/>\r\n" +
               "        <fml>\r\n" +
               "        </fml>\r\n" +
               "        <bml>\r\n" +
               "            <speech id=\"sp1\" type=\"application/ssml+xml\">"+input+"</speech>\r\n" +
               "        </bml>\r\n" +
               "    </act> ";
        
        
        String string4 ="Brad ranger harmony221 <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?>     <act>         <participant id=\"harmony\" role=\"actor\"/>         <fml>         <object name=\"A316\">             <attribute name=\"addressee\">ranger</attribute>             <attribute name=\"speech-act\">             <object name=\"A317\">             <attribute name=\"content\">             <object name=\"V28\">             <attribute name=\"modality\">             <object name=\"V29\">             <attribute name=\"conditional\">should</attribute>             </object>             </attribute>             <attribute name=\"polarity\">negative</attribute>             <attribute name=\"attribute\">jobAttribute</attribute>             <attribute name=\"value\">bartender-job</attribute>             <attribute name=\"object-id\">utah</attribute>             <attribute name=\"type\">state</attribute>             <attribute name=\"time\">present</attribute>             </object>             </attribute>             <attribute name=\"motivation\">             <object name=\"V27\">             <attribute name=\"reason\">become-sheriff-harmony</attribute>             <attribute name=\"goal\">address-problem</attribute>             </object>             </attribute>             <attribute name=\"addressee\">ranger</attribute>             <attribute name=\"action\">assert</attribute>             <attribute name=\"actor\">harmony</attribute>             </object>             </attribute>             </object>             </intention>         </fml>         <bml>             <speech id=\"sp1\" type=\"application/ssml+xml\">These micropiles are anchored in the limestone that one finds under the museum; in order to prevent the boat from rising with the rise of flood</speech>         </bml>     </act>";
        
        TextMessage message = session.createTextMessage(op+" "+g);
        
        //Open NVBG and wait three second before send the message -> avoid lost of data if message is not sent entirely
        System.out.println("greta.core.signals.MessageSender.traitement_NVBG()"+"  "+nvbg);
        try {
                        if(nvbg==false){
			run_NVBG();
                        TimeUnit.SECONDS.sleep(3);
                        }
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        message.setObjectProperty("ELVISH SCOPE", subject);
        message.setObjectProperty("MESSAGE_PREFIX", "vrExpress");
        message.setObjectProperty("VHMSG_VERSION","1.0.0.0" );
        message.setObjectProperty("VHMSG", "VHMSG");
        message.setObjectProperty("MESSAGE_TYPE_VHMSG", "VHMSG");
        // Here we are sending our message!
        producer.send(message);
        
        VHMSG vsg= new VHMSG("localhost", "61616", "DEFAULT_SCOPE" );
        vsg.openConnection();
        vsg.sendMessage(op, g);
      
        System.out.println("JCG printing@@ " + message.getText() + "");
        
        MessageConsumer consumer = session.createConsumer(destination);
        Message message_vrSpeak = null;
        TextMessage textMessage=null;
        boolean received_vrSpeak=false;
        // Here we receive the message.
        while(true) {
        Message message1 = consumer.receive();
        if (message1 instanceof TextMessage) {
            textMessage = (TextMessage) message1;
            //System.out.println(textMessage.getText().split(" ")[0]);
            
            if(textMessage.getText().split(" ")[0].contentEquals("vrSpeak")) {
            	System.out.println("FOUND");
            	message_vrSpeak=message1;
            	received_vrSpeak=true;
            }
        }
        if (received_vrSpeak) {
        	break;
        }
        }
        String animation=vsg.VHMSGonMessage(message_vrSpeak);
        //System.out.println("Animtion:" +animation);
        Test_NVBG_output test=new  Test_NVBG_output();
        List<String> gesture=test.traitement(animation);
        System.out.println("GESTURE:"+gesture);
        System.out.println("WHAT HAPPened");
        return gesture;
        
        
    }
 
        // We will be using TestMessage in our example. MessageProducer sent us a TextMessage
        // so we must cast to it to get access to its .getText() method.

    
    public static void run_NVBG() throws IOException {
        System.out.println("NVBG.MessageSender.run_NVBG()");
        String path=System.getProperty("user.dir");
    	path+="\\run-toolkit-NVBG-C#-all.bat";
    	Runtime rn=Runtime.getRuntime();
    	Process pr=rn.exec(path);
        
    }
    
    private void executeCommand(String command) {
        try {
            // single execution
            p_stdin.write(command);
            p_stdin.newLine();
            p_stdin.flush();
        } catch (IOException e) {
            System.out.println(e);
        }
    }

    
}