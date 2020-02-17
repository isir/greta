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
package greta.core.behaviorrealizer.keyframegenerator;

import greta.core.keyframes.AudioKeyFrame;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.PhonemSequence;
import greta.core.keyframes.SpeechKeyframe;
import greta.core.signals.Signal;
import greta.core.signals.SpeechSignal;
import greta.core.util.IniManager;
import greta.core.util.log.Logs;
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
            SpeechKeyframe keyframe = new SpeechKeyframe(speechStartTime, IniManager.getProgramPath() + "./acapelaTemp.wav");
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
