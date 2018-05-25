/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextSender;
import vib.auxiliary.activemq.WhiteBoard;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import vib.auxiliary.emotionml.EmotionMLTranslator;
import vib.auxiliary.socialparameters.SocialParameterFrame;
import vib.auxiliary.socialparameters.SocialParameterPerformer;
import vib.core.util.id.ID;
import vib.core.util.xml.XMLTree;

/**
 *
 * @author Mathieu Chollet
 */


public class EmotionMLSender extends TextSender implements SocialParameterPerformer{

    private HashMap<String,Object> semaineMap;

    public EmotionMLSender(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "EmotionML");
    }
    public EmotionMLSender(String host, String port, String topic){
        super(host, port, topic);
        semaineMap = new HashMap<String,Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("datatype", "EmotionML");
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

    @Override
    public void performSocialParameter(List<SocialParameterFrame> frames, ID requestId) {
        XMLTree emotionML = EmotionMLTranslator.SocialParametersToEmotionML(frames);
        semaineMap.put("content-id", requestId.toString());
        this.send(emotionML.toString(), semaineMap);
    }
}
