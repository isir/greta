/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextSender;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.feedbacks.Callback;
import vib.core.feedbacks.FeedbackPerformer;
import vib.core.util.id.ID;
import vib.core.util.time.Temporizable;
import vib.core.util.time.Timer;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */

public class FeedbacksSender extends TextSender implements FeedbackPerformer{

    private HashMap<String,Object> semaineMap;
    private boolean detailedFeedbacks;
    private boolean detailsOnFace;
    private boolean detailsOnGestures;

    public FeedbacksSender(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "semaine.callback.output.feedback");
    }
    public FeedbacksSender(String host, String port, String topic){
        super(host, port, topic);
        semaineMap = new HashMap<String,Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("datatype", "feedback");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
        semaineMap.put("xml", true);
        detailedFeedbacks = false; // TODO implement the possibility to use detailedFeedbacks
    }

    @Override
    public void performFeedback(ID AnimId, String Type, List<Temporizable> listTmp) {
        String content = Type + "\n";
        for(Temporizable tmp : listTmp){
            content += tmp.getId() + "\n" + tmp.toString() + "\n";
        }
        semaineMap.put("AnimId", AnimId.toString());
        semaineMap.put("type", "ongoing");
        semaineMap.put("current-time", Timer.getTimeMillis());
        semaineMap.put("content-id", AnimId.toString());
        semaineMap.put("usertime", Timer.getTimeMillis());
        semaineMap.put("content-creation-time", Timer.getTimeMillis());
        this.send(content, semaineMap);
     }

    @Override
    public void performFeedback(Callback callback) {
       String content=callback.animId()+" "+callback.type()+" "+String.valueOf(callback.time());
         semaineMap.put("AnimId", callback.animId().toString());
        semaineMap.put("type", callback.type());
         semaineMap.put("current-time", Timer.getTimeMillis());
        semaineMap.put("content-id", callback.animId().toString());
        semaineMap.put("usertime", Timer.getTimeMillis());
        semaineMap.put("content-creation-time", Timer.getTimeMillis());
        this.send(content, semaineMap);
    }

    @Override
    public void setDetailsOption(boolean detailed){
        detailedFeedbacks = detailed;
    }
    @Override
    public boolean areDetailedFeedbacks(){
        return detailedFeedbacks;
    }

    @Override
    public void setDetailsOnFace(boolean detailsOnFace) {
        this.detailsOnFace = detailsOnFace;
    }

    @Override
    public boolean areDetailsOnFace() {
        return this.detailsOnFace;
    }

    @Override
    public void setDetailsOnGestures(boolean detailsOnGestures) {
        this.detailsOnGestures = detailsOnGestures;
    }

    @Override
    public boolean areDetailsOnGestures() {
        return this.detailsOnGestures;
    }
 }
