/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
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
