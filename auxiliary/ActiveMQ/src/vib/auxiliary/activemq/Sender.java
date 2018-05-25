/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq;

import java.util.HashMap;
import java.util.Map;
import javax.jms.DeliveryMode;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageProducer;
import org.apache.activemq.command.ActiveMQMessage;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class Sender<O> extends WhiteBoard{

    protected MessageProducer producer;

    public Sender(){
        super();
    }
    public Sender(String host, String port, String topic){
        super(host, port, topic);
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

    public void send(O content){
        send(content, new HashMap<String,Object>());
    }

    public void send(O content, Map<String,Object> properties){
        onSend(properties);
        Message message = null;
        try {
            message = createMessage(content);
            if(properties != null){
                if(message instanceof ActiveMQMessage){
                    ((ActiveMQMessage)message).setProperties(properties);
                }
                else{
                    for(String key : properties.keySet()){//Logs.debug(key);
                        message.setObjectProperty(key, properties.get(key));
                    }
                }
            }
            producer.send(message);
        } catch (Exception ex) {
            //Logs.error("could not send message");
        }
    }

    /**
     * Callback
     * @param properties properties to fill
     */
    protected abstract void onSend(Map<String, Object> properties);

    protected abstract Message createMessage(O content) throws JMSException;
}
