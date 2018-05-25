/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorrealizer.keyframegenerator;

import vib.core.keyframes.AudioKeyFrame;
import vib.core.keyframes.Keyframe;
import vib.core.signals.LaughSignal;
import vib.core.signals.Signal;
import java.util.Comparator;
import java.util.List;
import vib.core.keyframes.LaughPhonemSequence;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class LaughKeyframeGenerator extends KeyframeGenerator{

    public LaughKeyframeGenerator(){
        super(LaughSignal.class);
    }

    @Override
    protected void generateKeyframes(List<Signal> inputSignals, List<Keyframe> outputKeyframe) {
        for(Signal signal : inputSignals){
            LaughSignal laugh = (LaughSignal) signal;

            //the laugh phonems
            LaughPhonemSequence phonemes = new LaughPhonemSequence(laugh);
            outputKeyframe.add(phonemes);
            //the audio
            AudioKeyFrame audio = new AudioKeyFrame(laugh);
            outputKeyframe.add(audio);
        }
    }

    @Override
    protected Comparator<Signal> getComparator() {
        return emptyComparator;
    }

}
