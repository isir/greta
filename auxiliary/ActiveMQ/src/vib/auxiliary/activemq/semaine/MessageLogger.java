/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
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