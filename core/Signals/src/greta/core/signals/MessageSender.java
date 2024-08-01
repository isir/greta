package greta.core.signals;

import greta.core.signals.Test_NVBG_output;
import greta.core.signals.VHMSG;
import greta.core.util.log.Logs;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;

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
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

//import org.apache.tika.detect.EncodingDetector;
//import org.apache.tika.detect.AutoDetectReader;
//import org.apache.tika.parser.txt.UniversalEncodingDetector;
//import org.apache.tika.metadata.Metadata;
//import java.io.ByteArrayInputStream;
//import java.nio.charset.Charset;

//import com.ibm.icu.text.CharsetDetector;
//import com.ibm.icu.text.CharsetMatch;

public class MessageSender {
     
    //URL of the JMS server. DEFAULT_BROKER_URL will just mean that JMS server is on localhost
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    
     
    // default broker URL is : tcp://localhost:61616"
    private static String subject = "DEFAULT_SCOPE"; // Queue Name.You can create any/many queue names as per your requirement. 
    
    private BufferedWriter p_stdin;
    
    private CharacterManager charactermanager;

    public static final String STRING_ENCODING = StandardCharsets.UTF_8.toString();
//    public static final String STRING_ENCODING = StandardCharsets.UTF_16.toString();
//    public static final String STRING_ENCODING = StandardCharsets.US_ASCII.toString();
//    public static final String STRING_ENCODING = StandardCharsets.UTF_16BE.toString();
//    public static final String STRING_ENCODING = StandardCharsets.UTF_16LE.toString();
//    public static final String STRING_ENCODING = StandardCharsets.ISO_8859_1.toString();
    
    public MessageSender(CharacterManager cm){
        charactermanager = cm;
    }

    public List<String> traitement_NVBG(String input,boolean nvbg) throws JMSException, FileNotFoundException, InterruptedException, IOException{ 
        
        System.out.println("Encoding " + System.getProperty("file.encoding"));
        
        List<String> gesture=new ArrayList<String>();
        
        System.out.println("[NVBG INFO]:MessageSender.main()");
        System.out.println(url);
        
        // Getting JMS connection from the server and starting it
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
        //Creating a non transactional session to send/receive JMS message.
        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);  
        //Destination represents here our queue 'DEFAULT_SCOPE' on the JMS server. 
        //The queue will be created automatically on the server.
        Destination destination = session.createTopic(subject);
        System.out.println("[NVBG INFO]: " + session.createTopic(subject).getTopicName() +" "+ destination);
         
        // MessageProducer is used for sending messages to the queue.
        MessageProducer producer = session.createProducer(destination);
        producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
        String op= "vrExpress";
        String gstring ="Brad ranger harmony221 <?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"no\" ?><act><participant id=\"harmony\" role=\"actor\"/><fml></fml><bml><speech id=\"sp1\" type=\"application/ssml+xml\">"+input+"</speech></bml></act>";
        
        System.out.println("[NVBG INFO]:Ding: "+input);
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
        
        //Open NVBG and wait one second before send the message -> avoid lost of data if message is not sent entirely
        System.out.println("[NVBG INFO]:greta.core.signals.MessageSender.traitement_NVBG()"+"  "+nvbg);
        if (nvbg==false) {
            MyThreadNVBG thread_nvbg = new MyThreadNVBG();
            thread_nvbg.run();
            TimeUnit.SECONDS.sleep(1);
        } // TODO Auto-generated catch block
        
//        TextMessage message = session.createTextMessage(op+" "+g);
//        message.setObjectProperty("ELVISH SCOPE", subject);
//        message.setObjectProperty("MESSAGE_PREFIX", "vrExpress");
//        message.setObjectProperty("VHMSG_VERSION","1.0.0.0" );
//        message.setObjectProperty("VHMSG", "VHMSG");
//        message.setObjectProperty("MESSAGE_TYPE_VHMSG", "VHMSG");
//        // Here we are sending our message!
//        producer.send(message);
        
        VHMSG vsg= new VHMSG("localhost", "61616", "DEFAULT_SCOPE", STRING_ENCODING);
        vsg.openConnection();
        if(!vsg.isConnected()){
            System.out.println("Couldn't connect to NVBG server at localhost:61616");
            return gesture;
        }
        vsg.sendMessage(op, g);
      
        System.out.println("[NVBG INFO]:JCG printing");
        //System.out.println(message.getText());
        System.out.println(g);
        
        MessageConsumer consumer = session.createConsumer(destination);
        Message message_vrSpeak = null;
        TextMessage textMessage=null;
        boolean received_vrSpeak=false;
        long start = System.currentTimeMillis();
        long end;
        long elapsedTime;
        
        long loop_timeout = 100;
        long session_timeout = 5000;
        
        // Here we receive the message.
        while(true) {
        
            end = System.currentTimeMillis();
            elapsedTime = end - start;
            // System.out.println("Elapsed: " + Long.toString(elapsedTime));
            Message message1 = consumer.receive(loop_timeout);

            if (message1 instanceof TextMessage) {

                textMessage = (TextMessage) message1;
                String stringMessage = textMessage.getText();
                
                stringMessage = URLDecoder.decode(stringMessage, STRING_ENCODING );
                stringMessage = stringMessage.trim();
                
                // byte[] byteMessage = textMessage.getText().getBytes("ISO-8859-1");
                // String stringMessage = new String(byteMessage,"UTF-8");
                
                System.out.println("Message received");
                System.out.println(stringMessage);
                //System.out.println(textMessage.getText().split(" ")[0]);

                if(stringMessage.split(" ")[0].contentEquals("vrSpeak")) {
                    System.out.println("[NVBG INFO]:FOUND");
                    message_vrSpeak=message1;
                    received_vrSpeak=true;
                }
                
//                if(charactermanager.language.equals("FR") && stringMessage.split(" ")[0].contentEquals("parser_result")) {
//                                        
////                    String src_encoding = "Unicode";
////                    String src_encoding = "UTF-16";
////                    String src_encoding = "ISO-8859-1";
//                    String tgt_encoding = "UTF-8";
//                    
//                    String[] encodingArray = {"US-ASCII", "Unicode", "UTF-16", "UTF-8", "ISO-8859-1", "UTF-32"};
//                    
//                    for (String src_encoding:encodingArray) {
//                                        
//    //                    byte[] byteMessage = textMessage.getText().getBytes("ISO-8859-1");
//                        byte[] byteMessage = textMessage.getText().getBytes(src_encoding);
//                        stringMessage = new String(byteMessage, tgt_encoding);
//
////                        System.out.println("src encoding: " + src_encoding);
////                        System.out.println("tgt encoding: " + tgt_encoding);
////                        System.out.println("Received message: " + stringMessage);
//                        
//                        System.out.println("Test encoding: " + src_encoding + " : " + stringMessage);
//                
//                    }
//
//                }
                
            }
            if (received_vrSpeak || elapsedTime>session_timeout) {

                    if(elapsedTime>5000){
                        System.out.println("5s elapsed , message not received from NVBG");
                    }
                    break;
            }
        }
        
        if(message_vrSpeak!=null){
            String animation=vsg.VHMSGonMessage(message_vrSpeak);
            //System.out.println("Animtion:" +animation);
            Test_NVBG_output test=new  Test_NVBG_output();
            gesture=test.traitement(animation);
        }
        
        System.out.println("GESTURE:"+gesture);
        return gesture;
        
        
    }
 
    // We will be using TestMessage in our example. MessageProducer sent us a TextMessage
    // so we must cast to it to get access to its .getText() method.
    
    public class MyThreadNVBG extends Thread{
         
         public MyThreadNVBG(){
             
         }
         
         public  void run(){
            
            System.out.println("[NVBG INFO]:NVBG.MessageSender.run_NVBG()");
            String path=System.getProperty("user.dir");
            path+="\\run-toolkit-NVBG-C#-all.bat " + charactermanager.getLanguage();
            Runtime rn=Runtime.getRuntime();
             try {
                final Process pr=rn.exec(path);
             } catch (IOException ex) {
                 Logger.getLogger(MessageSender.class.getName()).log(Level.SEVERE, null, ex);
             }

             
             
            
            
            }
             }
         
         }
         
     
