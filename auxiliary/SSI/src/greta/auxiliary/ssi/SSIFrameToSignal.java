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
package greta.auxiliary.ssi;

import greta.core.signals.FaceSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author Brian Ravenet
 */
public class SSIFrameToSignal implements SSIFramePerfomer, SignalEmitter {

    private List<SignalPerformer> signalPerformers = new ArrayList<SignalPerformer>();
    private int previousSpeaking = 0;
    private int speaking = 0;
    private int previousHeadShake = 0;
    private int headShake= 0;
    private int previousHeadNod = 0;
    private int headNod= 0;
    private Double previousSmile = 0.0;
    private Double smile= 0.0;
    private int pitchDirection = 0;

    // head orientation
    private double head_yaw = 0; // shake
    private double head_pitch = 0; // nod
    private double head_roll = 0; // tilt

    //previous value yaw and pitch
    private double previous_head_yaw = 0; // shake
    private double previous_head_pitch = 0; // nod

    boolean isShake = false;
    boolean isNod = false;

    // vettor of frame
    public List<Double> frames_yaw;
    public List<Double> frames_pitch;

    private CharacterManager cm;

    public SSIFrameToSignal(CharacterManager cm){
        this.cm = cm;
        this.frames_yaw = new ArrayList<Double>();
        this.frames_pitch = new ArrayList<Double>();

    }


    @Override
    public void performSSIFrames(List<SSIFrame> ssi_frames_list, ID requestId) {
        for (SSIFrame ssf : ssi_frames_list) {
            performSSIFrame(ssf, requestId);
        }
    }

    @Override
    public void performSSIFrame(SSIFrame ssi_frame, ID requestId) {
        ArrayList<Signal> toSend = new ArrayList<Signal>();

        speaking = ssi_frame.getIntValue(SSITypes.SSIFeatureNames.prosody_voice_activity);
        if (previousSpeaking == 1 && speaking == 0) {
            SpeechSignal ss = new SpeechSignal(cm);

            //dummy time values
            ss.getTimeMarker("start").setValue(0.0);
            ss.getTimeMarker("end").setValue(0.0);

            ss.setReference("silence");
            toSend.add(ss);
        }
        previousSpeaking = speaking;

        // shake
        head_yaw = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_orientation_yaw);
        if(frames_yaw.size() <= 15){
            frames_yaw.add(frames_yaw.size(), head_yaw);
        }else{
            frames_yaw.remove(0);
            frames_yaw.add(frames_yaw.size() ,head_yaw);
        }

        //check max and min of the vectors
        if (frames_yaw.size() == 16){

            double max_yaw = Collections.max(frames_yaw);
            double min_yaw = Collections.min(frames_yaw);

            // if the difference between max and min overcome a threeshold we trigger a nod or shake signal
            if ((max_yaw - min_yaw) > 5){
                HeadSignal hs = new HeadSignal("nod");

                //dummy time values
                hs.getTimeMarker("start").setValue(0.0);
                hs.getTimeMarker("end").setValue(0.0);

                hs.setReference("nod");
                toSend.add(hs);

                // clear the vector frame to do not have the same signal until the vectori is update in each position
                frames_yaw.clear();
            }
        }

        // nod
        head_pitch = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_orientation_pitch);

        // update the frames' vectors
        if(frames_pitch.size() <= 15){
            frames_pitch.add(frames_pitch.size(), head_pitch);
        }else{
            frames_pitch.remove(0);
            frames_pitch.add(frames_pitch.size(), head_pitch);
        }

        //check max and min of the vectors
        if (frames_pitch.size() == 16){
            double max_pitch = Collections.max(frames_pitch);
            double min_pitch = Collections.min(frames_pitch);

            if ((max_pitch - min_pitch) > 10){
                HeadSignal hs = new HeadSignal("shake");

                //dummy time values
                hs.getTimeMarker("start").setValue(0.0);
                hs.getTimeMarker("end").setValue(0.0);

                hs.setReference("shake");
                toSend.add(hs);

                frames_pitch.clear();
            }
        }

        //head_roll = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_orientation_roll);
        //previous_head_yaw = head_yaw;
        //previous_head_pitch = head_pitch;

        // ******* To use with SSI **********
        /*headNod = ssi_frame.getIntValue(SSITypes.SSIFeatureNames.head_nod_cat);
        if (previousHeadNod == 1 && headNod == 0) {
            HeadSignal hs = new HeadSignal("nod");

            //dummy time values
            hs.getTimeMarker("start").setValue(0.0);
            hs.getTimeMarker("end").setValue(0.0);

            hs.setReference("nod");
            toSend.add(hs);
        }
        previousHeadNod = headNod;*/

        /*headShake = ssi_frame.getIntValue(SSITypes.SSIFeatureNames.head_shake_cat);
        if (previousHeadShake == 1 && headShake == 0) {
            HeadSignal hs = new HeadSignal("shake");

            //dummy time values
            hs.getTimeMarker("start").setValue(0.0);
            hs.getTimeMarker("end").setValue(0.0);

            hs.setReference("shake");
            toSend.add(hs);
        }
        previousHeadShake = headShake;*/

        // nod and shake


        smile = ssi_frame.getDoubleValue(SSITypes.SSIFeatureNames.head_smile);
        if (previousSmile > 70 && smile < 70) {
            FaceSignal fs = new FaceSignal("smile");

            //dummy time values
            fs.getTimeMarker("start").setValue(0.0);
            fs.getTimeMarker("end").setValue(0.0);

            fs.setReference("smile");
            toSend.add(fs);
        }
        previousSmile = smile;


        pitchDirection = ssi_frame.getIntValue(SSITypes.SSIFeatureNames.prosody_opensmile_pitch_direction_cat);
        if(pitchDirection!=0){
            SpeechSignal ss = new SpeechSignal(cm);
            ss.getTimeMarker("start").setValue(0.0);
            ss.getTimeMarker("end").setValue(0.0);
            ss.setReference(SSITypes.SSIPitchDirectionValues.getPitchDirectionValueName(pitchDirection).name());
            toSend.add(ss);
        }

        for(SignalPerformer sp : signalPerformers)
        {
            sp.performSignals(toSend, requestId, new Mode(CompositionType.blend));
        }

    }

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        signalPerformers.add(sp);
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        signalPerformers.add(sp);
    }
}
