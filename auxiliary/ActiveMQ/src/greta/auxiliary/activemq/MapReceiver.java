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
