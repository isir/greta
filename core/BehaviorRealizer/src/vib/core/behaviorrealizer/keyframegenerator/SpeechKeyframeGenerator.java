/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorrealizer.keyframegenerator;

import vib.core.keyframes.AudioKeyFrame;
import vib.core.keyframes.Keyframe;
import vib.core.keyframes.PhonemSequence;
import vib.core.keyframes.SpeechKeyframe;
import vib.core.signals.Signal;
import vib.core.signals.SpeechSignal;
import vib.core.util.IniManager;
import vib.core.util.log.Logs;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class SpeechKeyframeGenerator extends KeyframeGenerator {

    public SpeechKeyframeGenerator() {
        super(SpeechSignal.class);
    }

    @Override
    protected void generateKeyframes(List<Signal> inputSignals, List<Keyframe> outputKeyframes) {

        for (Signal signal : inputSignals) {
            SpeechSignal speech = (SpeechSignal) signal;
            double speechStartTime = speech.getStart().getValue();

            //only for NAO
            SpeechKeyframe keyframe = new SpeechKeyframe(speechStartTime, IniManager.getProgramPath() + "/acapelaTemp.wav");
            outputKeyframes.add(keyframe);
            //end "only for NAO"


            //get the phonem sequence from sppech
            if (speech.getPhonems() != null && // the phonem list is not null
                !speech.getPhonems().isEmpty()) { // the phonem list is not empty
                outputKeyframes.add(new PhonemSequence(speech));
            }
            else {
                Logs.warning(this.getClass().getName() + ": the speech " + speech.getId() + " has no phonem.");
            }

            //get the audio from sppech
            if (speech.getAudio() != null && // the audio is not null
                speech.getAudio().getDurationMillis() > 0) { // the audio is not empty
                outputKeyframes.add(new AudioKeyFrame(speech));
            }
            else {
                Logs.warning(this.getClass().getName() + ": the speech " + speech.getId() + " has no audio.");
            }
        }
    }

    @Override
    protected Comparator<Signal> getComparator() {
        return emptyComparator;
    }
}
