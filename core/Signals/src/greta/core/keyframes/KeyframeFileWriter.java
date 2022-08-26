/*
 * This file is part of Greta.
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
package greta.core.keyframes;

import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.List;

/**
 * This class is an implementation of {@code KeyframePerformer} interface.<br/>
 * When {@code KeyframePerformer} function is called, the {@code Keyframes} received
 * are saved in a file in Keyframe format.
 *
 * @author Quoc Anh Le
 */
public class KeyframeFileWriter implements KeyframePerformer{

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
              keyframeString = keyframeString.concat("   <keyframe start="+'"'+((SpeechKeyframe)key).getOnset()+'"'
                     + " modality="+'"'+((SpeechKeyframe)key).getModality()+'"'
                     + " fileName="+'"'+((SpeechKeyframe)key).getFileName()+'"'
                     + ">\n");
              keyframeString = keyframeString.concat("   </keyframe>\n");
            }

            // gesture keyframe
            if(key instanceof GestureKeyframe)                {
             keyframeString = keyframeString.concat("   <keyframe "
                     +" time="+'"'+((GestureKeyframe)key).getOffset()+'"'
                     + " modality="+'"'+((GestureKeyframe)key).getModality()+'"'
                     + " side="+'"'+((GestureKeyframe)key).getSide()+'"'
                     + " id="+'"'+((GestureKeyframe)key).getId()+'"'
                     + " phase="+'"'+((GestureKeyframe)key).getPhaseType()+'"'
                     + " trajectory="+'"'+((GestureKeyframe)key).getTrajectoryType()+'"'
                     + ">\n");
             keyframeString = keyframeString.concat(((GestureKeyframe)key).getHand().getStringPosition());
             keyframeString = keyframeString.concat("       <handShape>"+((GestureKeyframe)key).getHand().getHandShape()+"</handShape>\n");
             Quaternion q = ((GestureKeyframe)key).getHand().getWristOrientation();
             Vec3d r = q.getEulerAngleXYZByAngle();
             keyframeString = keyframeString.concat("       <wristOrientationX>"+r.x()+"</wristOrientationX>\n");
             keyframeString = keyframeString.concat("       <wristOrientationY>"+r.y()+"</wristOrientationY>\n");
             keyframeString = keyframeString.concat("       <wristOrientationZ>"+r.z()+"</wristOrientationZ>\n");

             keyframeString = keyframeString.concat("       <SPC>"+((GestureKeyframe)key).getParameters().spc+"</SPC>\n");
             keyframeString = keyframeString.concat("       <TMP>"+((GestureKeyframe)key).getParameters().tmp+"</TMP>\n");
             keyframeString = keyframeString.concat("       <PWR>"+((GestureKeyframe)key).getParameters().pwr+"</PWR>\n");
             keyframeString = keyframeString.concat("       <FLD>"+((GestureKeyframe)key).getParameters().fld+"</FLD>\n");
             keyframeString = keyframeString.concat("       <Tension>"+((GestureKeyframe)key).getParameters().tension+"</Tension>\n");
             keyframeString = keyframeString.concat("   </keyframe>\n");
            }

            // head keyframe
            if(key instanceof HeadKeyframe){
             keyframeString = keyframeString.concat("   <keyframe "
                     + " time="+'"'+((HeadKeyframe)key).getOffset()+'"'
                     + " modality="+'"'+((HeadKeyframe)key).getModality()+'"'
                     + " category="+'"'+((HeadKeyframe)key).getCategory()+'"'
                     + " id="+'"'+((HeadKeyframe)key).getId()+'"'
                     + "/>\n");
            }

            /* torso keyframe */
            if(key instanceof TorsoKeyframe){
             keyframeString = keyframeString.concat("   <keyframe "
                     + " time="+'"'+((TorsoKeyframe)key).getOffset()+'"'
                     + " modality="+'"'+((TorsoKeyframe)key).getModality()+'"'
                     + " category="+'"'+((TorsoKeyframe)key).getCategory()+'"'
                     + "/>\n");
                }

            /*
            // face keyframe
            if(key instanceof FaceKeyframe){
             keyframeString = keyframeString.concat("   <keyframe onset="+'"'+((FaceKeyframe)key).getOnset()+'"'
                     + " offset="+'"'+((FaceKeyframe)key).getOffset()+'"'
                     + " modality="+'"'+((FaceKeyframe)key).getModality()+'"'
                     + " category="+'"'+((FaceKeyframe)key).getCategory()+'"'
                     + " id="+'"'+((FaceKeyframe)key).getId()+'"'
                     + "/>\n");
            }
            *
            */
            }

            keyframeString = keyframeString.concat("</keyframes>\n");


            PrintStream out = null;
            try {
                out = new PrintStream(new FileOutputStream("keyframes-"+requestId+".xml"));
                out.print(keyframeString);
                //System.out.println(keyframeString);
            }
            finally {
                if (out != null) out.close();
            }


        } catch (Exception ex) {
            Logs.error("Can not save Keyframes");
        }

    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId, Mode mode) {
        // TODO : Mode management in progress
        performKeyframes(keyframes, requestId);
    }

}
