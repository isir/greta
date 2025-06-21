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
package greta.furhat.activemq;

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
