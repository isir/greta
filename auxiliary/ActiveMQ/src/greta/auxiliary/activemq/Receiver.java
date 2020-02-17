/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.activemq;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import org.apache.activemq.command.ActiveMQMessage;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class Receiver<O> extends WhiteBoard implements MessageListener{
    protected MessageConsumer consumer;

    public Receiver(){
        super();
    }
    public Receiver(String host, String port, String topic){
        super(host, port, topic);
    }

    @Override
    protected void onConnectionStarted() {
        super.onConnectionStarted();
        createConsumer();
    }

    private void createConsumer(){
        try {
            consumer = session.createConsumer(destination);

            consumer.setMessageListener(this);
        } catch (Exception ex) {
            //Logs.error("consumer not created");
        }
    }

    @Override
    protected void onReboot() {
        super.onReboot();
        closeConsumer();
    }

    private void closeConsumer(){
        try {
            consumer.setMessageListener(null);
            consumer.close();
        }
        catch (Exception ex) {
            //Logs.error("at consumer closing : "+ex.getLocalizedMessage());
        }
        consumer = null;
    }


    @Override
    protected void onDestinationChanged() {
        super.onDestinationChanged();
        closeConsumer();
        createConsumer();
    }

    @Override
    public void onMessage(Message message) {
        O content;
        Map<String, Object> properties;
        try {
            content = getContent(message);
            if(message instanceof ActiveMQMessage)
                properties = ((ActiveMQMessage)message).getProperties();
            else{
                properties = new HashMap<String, Object>();
                Enumeration keys= message.getPropertyNames();
                while(keys.hasMoreElements()){
                    String key = (String)keys.nextElement();
                    properties.put(key, message.getObjectProperty(key));
                }
            }
            onMessage(content, properties);
        } catch (Exception ex) {
            //Logs.error("could not read the message");
            ex.printStackTrace();
        }
    }

    protected abstract void onMessage(O content, Map<String, Object> properties);
    protected abstract O getContent(Message message) throws JMSException;
}
