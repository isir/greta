/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq;

import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;

/**
 *
 * @author Andre-Marie Pez
 */
public class TextSender extends Sender<String>{


    public TextSender(){
        super();
    }
    public TextSender(String host, String port, String topic){
        super(host, port, topic);
    }

    @Override
    protected void onSend(Map<String, Object> properties) {
        // Must be overrided to complete the map
    }

    @Override
    protected Message createMessage(String content) throws JMSException{
        return session.createTextMessage(content.toString());
    }
}
