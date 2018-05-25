/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq;

import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import org.apache.activemq.command.ActiveMQMapMessage;

/**
 *
 * @author Florian Pecune
 */
public class MapReceiver extends Receiver<Map<String, Object>>{

    @Override
    protected void onMessage(Map<String, Object> content, Map<String, Object> properties) {
        
    }

    @Override
    protected Map<String, Object> getContent(Message message) throws JMSException {
        if(message instanceof ActiveMQMapMessage){
            return ((ActiveMQMapMessage)message).getContentMap();}
                    
        return null;
    }
    
}
