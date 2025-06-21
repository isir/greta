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
import greta.core.keyframes.GestureKeyframe;
import greta.core.keyframes.HeadKeyframe;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.KeyframePerformer;
import greta.core.keyframes.SpeechKeyframe;
import greta.core.keyframes.TorsoKeyframe;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class KeyFrameSender extends TextSender implements KeyframePerformer{
    private HashMap<String,Object> semaineMap;

    public KeyFrameSender(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "Keyframes");
        semaineMap = new HashMap<String,Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("datatype", "KF");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
    }
    public KeyFrameSender(String host, String port, String topic){
        super(host, port, topic);
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId) {
        try {
            //TODO translates the keyframe list into a String
            String keyframeString = "";

            keyframeString = keyframeString.concat("<?xml version="+'"'+"1.0"+'"'+" encoding="+'"'+"UTF-8"+'"'+"?>\n");
            keyframeString = keyframeString.concat("<keyframes>\n");
            for(Keyframe key : keyframes){

            // speech keyframe
            if(key instanceof SpeechKeyframe){
              keyframeString = keyframeString.concat("   <keyframe onset="+'"'+((SpeechKeyframe)key).getOnset()+'"'
                     + " modality="+'"'+((SpeechKeyframe)key).getModality()+'"'
                     + " fileName="+'"'+((SpeechKeyframe)key).getFileName()+'"'
                     + ">\n");
              keyframeString = keyframeString.concat("   </keyframe>\n");
            }

            // gesture keyframe
            if(key instanceof GestureKeyframe) {
                    if (((GestureKeyframe)key).getPhaseType().compareToIgnoreCase("START") != 0){
                 keyframeString = keyframeString.concat("   <keyframe onset="+'"'+((GestureKeyframe)key).getOnset()+'"'
                         +" offset="+'"'+((GestureKeyframe)key).getOffset()+'"'
                         + " modality="+'"'+((GestureKeyframe)key).getModality()+'"'
                         + " side="+'"'+((GestureKeyframe)key).getSide()+'"'
                         + " id="+'"'+((GestureKeyframe)key).getId()+'"'
                         + " phase="+'"'+((GestureKeyframe)key).getPhaseType()+'"'
                         + ">\n");
                 keyframeString = keyframeString.concat(((GestureKeyframe)key).getHand().getStringPosition());
                 keyframeString = keyframeString.concat("       <handShape>"+((GestureKeyframe)key).getHand().getHandShape()+"</handShape>\n");
//                 keyframeString = keyframeString.concat("       <palmOrientation>"+((GestureKeyframe)key).getHand().getStringPalmOrientation()+"</palmOrientation>\n");
//                 keyframeString = keyframeString.concat("       <fingersOrientation>"+((GestureKeyframe)key).getHand().getStringFingersOrientation()+"</fingersOrientation>\n");

                 keyframeString = keyframeString.concat("       <SPC>"+((GestureKeyframe)key).getParameters().spc+"</SPC>\n");
                 keyframeString = keyframeString.concat("       <TMP>"+((GestureKeyframe)key).getParameters().tmp+"</TMP>\n");
                 keyframeString = keyframeString.concat("       <PWR>"+((GestureKeyframe)key).getParameters().pwr+"</PWR>\n");
                 keyframeString = keyframeString.concat("       <FLD>"+((GestureKeyframe)key).getParameters().fld+"</FLD>\n");
                 keyframeString = keyframeString.concat("       <Tension>"+((GestureKeyframe)key).getParameters().tension+"</Tension>\n");
                 keyframeString = keyframeString.concat("   </keyframe>\n");
                }
                }

            // head keyframe
            if(key instanceof HeadKeyframe){
             keyframeString = keyframeString.concat("   <keyframe onset="+'"'+((HeadKeyframe)key).getOnset()+'"'
                     + " offset="+'"'+((HeadKeyframe)key).getOffset()+'"'
                     + " modality="+'"'+((HeadKeyframe)key).getModality()+'"'
                     + " category="+'"'+((HeadKeyframe)key).getCategory()+'"'
                     + "/>\n");
            }

            // torso keyframe
            if(key instanceof TorsoKeyframe){
             keyframeString = keyframeString.concat("   <keyframe onset="+'"'+((TorsoKeyframe)key).getOnset()+'"'
                     + " offset="+'"'+((TorsoKeyframe)key).getOffset()+'"'
                     + " modality="+'"'+((TorsoKeyframe)key).getModality()+'"'
                     + " category="+'"'+((TorsoKeyframe)key).getCategory()+'"'
                     + "/>\n");
                }
            }

            keyframeString = keyframeString.concat("</keyframes>\n");

            semaineMap.put("content-id", requestId.toString());
            semaineMap.put("usertime", System.currentTimeMillis());
            semaineMap.put("content-creation-time", System.currentTimeMillis());
            this.send(keyframeString,semaineMap);

        } catch (Exception ex) {
            Logs.error("Can not send Keyframes");
        }
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId, Mode mode) {
        // TODO : Mode management in progress
        performKeyframes(keyframes, requestId);
    }
}
