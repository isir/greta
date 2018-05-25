/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextReceiver;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.feedbacks.Callback;
import vib.core.feedbacks.CallbackEmitter;
import vib.core.feedbacks.CallbackPerformer;
import vib.core.util.id.IDProvider;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Angelo Cafaro
 */
public class BMLCallbacksReceiver extends TextReceiver implements CallbackEmitter {

    private ArrayList<CallbackPerformer> callbackPerformersfList;

    public BMLCallbacksReceiver() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "vib.input.feedback.BML");
    }

    public BMLCallbacksReceiver(String host, String port, String topic) {
        super(host, port, topic);
        callbackPerformersfList = new ArrayList<CallbackPerformer>();
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        String callbackType = properties.get("feedback-type").toString();
        double callbackTime = Double.parseDouble(properties.get("feedback-time").toString()) / 1000;
        String requestId = properties.get("feedback-id").toString();
        
        Callback callback = new Callback(callbackType, callbackTime, IDProvider.createID(requestId));
        for (CallbackPerformer performer : callbackPerformersfList) {
            performer.performCallback(callback);
        }
    }

    @Override
    public void addCallbackPerformer(CallbackPerformer performer) {
        callbackPerformersfList.add(performer);
    }

    @Override
    public void removeCallbackPerformer(CallbackPerformer performer) {
        callbackPerformersfList.remove(performer);
    }
}
