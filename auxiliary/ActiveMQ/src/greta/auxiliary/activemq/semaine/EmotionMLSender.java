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

import greta.auxiliary.activemq.TextSender;
import greta.auxiliary.activemq.WhiteBoard;
import greta.auxiliary.emotionml.EmotionMLTranslator;
import greta.auxiliary.socialparameters.SocialParameterFrame;
import greta.auxiliary.socialparameters.SocialParameterPerformer;
import greta.core.util.id.ID;
import greta.core.util.xml.XMLTree;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
