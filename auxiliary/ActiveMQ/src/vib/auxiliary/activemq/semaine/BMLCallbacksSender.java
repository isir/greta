/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextSender;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.feedbacks.Callback;
import vib.core.feedbacks.CallbackPerformer;
import java.util.HashMap;

/**
 *
 * @author Angelo Cafaro
 */
public class BMLCallbacksSender extends TextSender implements CallbackPerformer {

    private HashMap<String,Object> propertiesMap;

    public BMLCallbacksSender() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "vib.output.feedback.BML");
    }

    public BMLCallbacksSender(String host, String port, String topic) {
        super(host, port, topic);
        propertiesMap = new HashMap<String,Object>();
    }

    @Override
    public void performCallback(Callback clbck) {
        propertiesMap.put("feedback-type", clbck.type());
        propertiesMap.put("feedback-id", clbck.animId().getSource());
        propertiesMap.put("feedback-time", ((long)(clbck.time()*1000.0)));
        this.send("", propertiesMap);
    }
}