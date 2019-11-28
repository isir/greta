/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextReceiver;
import vib.auxiliary.activemq.WhiteBoard;
import java.util.Map;
import java.util.Map.Entry;
import vib.core.util.log.Logs;

/**
 *
 * @author Angelo Cafaro
 */
public class MessageLogger extends TextReceiver {

    public MessageLogger() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "");
    }

    public MessageLogger(String host, String port, String topic) {
        super(host, port, topic);
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        
        Logs.info("[ActiveMQ Message Logger] Incoming message\n");
        Logs.info("---------------------------------------- CONTENT BEGIN ----------------------------------------\n");
        Logs.info(content + "\n");
        Logs.info("---------------------------------------- CONTENT END   ----------------------------------------\n\n");
        Logs.info("---------------------------------------- PROPERTIES BEGIN ----------------------------------------\n");
        for (Entry<String, Object> entry : properties.entrySet())
        {
            Logs.info(entry.getKey() + " = " + entry.getValue() + "\n");
        }
        Logs.info("---------------------------------------- PROPERTIES END ----------------------------------------\n");
    }

}