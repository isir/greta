package greta.core.signals;
import greta.core.util.log.Logs;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.Session;
import javax.jms.TextMessage;
 
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
 
public class MessageReceiver{
 
    // URL of the JMS server
    private static String url = ActiveMQConnection.DEFAULT_BROKER_URL;
    // default broker URL is : tcp://localhost:61616"
 
    // Name of the queue we will receive messages from
    private static String subject = "DEFAULT_SCOPE";
 
    public static void main(String[] args) throws JMSException {
        // Getting JMS connection from the server
        ConnectionFactory connectionFactory = new ActiveMQConnectionFactory(url);
        Connection connection = connectionFactory.createConnection();
        connection.start();
 
        // Creating session for sending messages
        Session session = connection.createSession(false,Session.AUTO_ACKNOWLEDGE);
 
        // Getting the queue 'DEFAULT_SCOPE'
        Destination destination = session.createTopic(subject);
 
        // MessageConsumer is used for receiving (consuming) messages
        MessageConsumer consumer = session.createConsumer(destination);
 
        // Here we receive the message.
        while(true) {
        	Message message = consumer.receive();
 
        // We will be using TestMessage in our example. MessageProducer sent us a TextMessage
        // so we must cast to it to get access to its .getText() method.
        if (message instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) message;
            Logs.debug("[NVBG INFO]:Received message " + textMessage.getText() + "");
        }
        }
        
    }

}