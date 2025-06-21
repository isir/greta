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
package greta.auxiliary.activemq.semaine;

import greta.auxiliary.activemq.TextReceiver;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.util.log.Logs;
import java.util.Map;
import java.util.Map.Entry;

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
