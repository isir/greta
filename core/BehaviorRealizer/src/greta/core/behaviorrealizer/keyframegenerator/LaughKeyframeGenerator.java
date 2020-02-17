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
