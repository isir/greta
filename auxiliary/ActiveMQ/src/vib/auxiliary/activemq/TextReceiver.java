/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq;

import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

/**
 *
 * @author Andre-Marie Pez
 */
public class TextReceiver extends Receiver<String>{

    public TextReceiver(){
        super();
    }
    public TextReceiver(String host, String port, String topic){
        super(host, port, topic);
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        // Must be overrided to use the content received and/or the map
    }

    @Override
    protected String getContent(Message message) throws JMSException {
        if(message instanceof TextMessage)
            return ((TextMessage)message).getText();
        return null;
    }

}
