/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */

package vib.auxiliary.activemq.semaine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vib.auxiliary.activemq.TextSender;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.signals.BMLTranslator;
import vib.core.signals.Signal;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.xml.XMLTree;

/**
 *
 * @author Angelo Cafaro
 */
public class SSISender extends TextSender {
    
    private HashMap<String, Object> semaineMap;
    public static String DEFAULT_ACTIVEMQ_TOPIC = "SSI";

    public SSISender() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
            WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
            DEFAULT_ACTIVEMQ_TOPIC);
    }

    public SSISender(String host, String port, String topic) {
        super(host, port, topic);
        semaineMap = new HashMap<String, Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("datatype", "XML");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
        semaineMap.put("xml", true);
    }

    @Override
    protected void onSend(Map<String, Object> properties) {
        properties.put("usertime", System.currentTimeMillis());
        properties.put("content-creation-time", System.currentTimeMillis());
        properties.putAll(semaineMap);
        // Must be overrided to complete the map
    }
}
