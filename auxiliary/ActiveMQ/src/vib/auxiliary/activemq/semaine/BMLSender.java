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
import vib.core.signals.SignalPerformer;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.xml.XMLTree;

/**
 *
 * @author Andre-Marie Pez
 */
public class BMLSender extends TextSender implements SignalPerformer {

    private HashMap<String, Object> semaineMap;

    public BMLSender() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "vib.BML");
    }

    public BMLSender(String host, String port, String topic) {
        super(host, port, topic);
        semaineMap = new HashMap<String, Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("datatype", "BML");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
        semaineMap.put("xml", true);
    }

    @Override
    public void performSignals(List<Signal> list, ID requestId, Mode mode) {
        XMLTree bml = BMLTranslator.SignalsToBML(list, mode);
        semaineMap.put("content-id", requestId.getSource());
        this.send(bml.toString(), semaineMap);
    }

    @Override
    protected void onSend(Map<String, Object> properties) {
        properties.put("usertime", System.currentTimeMillis());
        properties.put("content-creation-time", System.currentTimeMillis());
        properties.putAll(semaineMap);
        // Must be overrided to complete the map
    }
}
