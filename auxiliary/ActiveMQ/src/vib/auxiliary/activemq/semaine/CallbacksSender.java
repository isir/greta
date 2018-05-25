/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextSender;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.feedbacks.Callback;
import vib.core.feedbacks.CallbackPerformer;
import vib.core.util.time.Timer;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLTree;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class CallbacksSender extends TextSender implements CallbackPerformer{

    private HashMap<String,Object> semaineMap;


    public CallbacksSender() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "semaine.callback.output.Animation");
    }

    public CallbacksSender(String host, String port, String topic) {
        super(host, port, topic);
        semaineMap = new HashMap<String,Object>();
        semaineMap.put("datatype", "callback");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
        semaineMap.put("xml", true);
    }

    @Override
    public void performCallback(Callback clbck) {

        XMLTree callback = XML.createTree("callback", "http://www.semaine-project.eu/semaineml");
        XMLTree event = callback.createChild("event", "http://www.semaine-project.eu/semaineml");
        event.setAttribute("type", clbck.type());
        event.setAttribute("data", "Animation");
        event.setAttribute("id", clbck.animId().toString());
        event.setAttribute("contentType", clbck.type());
        event.setAttribute("time", ""+((long)(clbck.time()*1000.0)));
        semaineMap.put("content-type", clbck.type());
        semaineMap.put("content-id", clbck.animId());
        this.send(callback.toString());
    }


    @Override
    protected void onSend(Map<String, Object> properties) {
        properties.put("usertime", Timer.getTimeMillis());
        properties.put("content-creation-time", Timer.getTimeMillis());
        properties.putAll(semaineMap);
        // Must be overrided to complete the map
    }

}
