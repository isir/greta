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
