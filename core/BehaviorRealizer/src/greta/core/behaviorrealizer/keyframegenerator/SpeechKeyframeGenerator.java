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
