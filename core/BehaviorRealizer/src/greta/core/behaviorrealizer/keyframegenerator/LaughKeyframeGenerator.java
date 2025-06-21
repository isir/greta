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
import greta.core.keyframes.LaughPhonemSequence;
import greta.core.signals.LaughSignal;
import greta.core.signals.Signal;
import java.util.Comparator;
import java.util.List;

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
