/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
