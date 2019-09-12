/*
 * This file is part of the auxiliaries of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
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
