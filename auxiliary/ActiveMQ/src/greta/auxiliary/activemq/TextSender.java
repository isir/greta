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
