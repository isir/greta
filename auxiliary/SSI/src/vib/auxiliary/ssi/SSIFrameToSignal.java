/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*/package vib.auxiliary.ssi;

import java.util.ArrayList;
import java.util.List;
import vib.core.signals.FaceSignal;
import vib.core.signals.HeadSignal;
import vib.core.signals.Signal;
import vib.core.signals.SignalEmitter;
import vib.core.signals.SignalPerformer;
import vib.core.signals.SpeechSignal;
import vib.core.util.CharacterManager;
import vib.core.util.Mode;
import vib.core.util.enums.CompositionType;
import vib.core.util.id.ID;

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
    private CharacterManager cm;
    
    public SSIFrameToSignal(CharacterManager cm){
        this.cm = cm;
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

        headNod = ssi_frame.getIntValue(SSITypes.SSIFeatureNames.head_nod_cat);
        if (previousHeadNod == 1 && headNod == 0) {
            HeadSignal hs = new HeadSignal("nod");

            //dummy time values
            hs.getTimeMarker("start").setValue(0.0);
            hs.getTimeMarker("end").setValue(0.0);

            hs.setReference("nod");
            toSend.add(hs);
        }
        previousHeadNod = headNod;

        headShake = ssi_frame.getIntValue(SSITypes.SSIFeatureNames.head_shake_cat);
        if (previousHeadShake == 1 && headShake == 0) {
            HeadSignal hs = new HeadSignal("shake");

            //dummy time values
            hs.getTimeMarker("start").setValue(0.0);
            hs.getTimeMarker("end").setValue(0.0);

            hs.setReference("shake");
            toSend.add(hs);
        }
        previousHeadShake = headShake;

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
        signalPerformers.remove(sp);
    }
}
