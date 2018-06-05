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
*//*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorrealizer.keyframegenerator;

import java.util.Comparator;
import java.util.List;
import vib.core.keyframes.ExpressivityParameters;
import vib.core.keyframes.Keyframe;
import vib.core.keyframes.TorsoKeyframe;
import vib.core.signals.Signal;
import vib.core.signals.SpinePhase;
import vib.core.signals.TorsoSignal;

/**
 *
 * @author Quoc Anh Le
 */
public class TorsoKeyframeGenerator extends KeyframeGenerator {

    public TorsoKeyframeGenerator() {
        super(TorsoSignal.class);
    }

    @Override
    protected void generateKeyframes(List<Signal> inputSignals, List<Keyframe> outputKeyframes) {

        for (Signal signal : inputSignals) {

            TorsoSignal torso = (TorsoSignal) signal;
            torso.schedulePhases();

            for (SpinePhase phase : torso.getPhases()) {
                TorsoKeyframe keyframe = new TorsoKeyframe(torso.getId(), phase, torso.getCategory());
                ExpressivityParameters e = new ExpressivityParameters();
                e.fld = torso.getFLD();
                e.pwr = torso.getPWR();
                e.spc = torso.getSPC();
                e.tmp = torso.getTMP();
                e.tension = torso.getTension();
                keyframe.setParameters(e);
                outputKeyframes.add(keyframe);
            }
            /*
             * TorsoKeyframe keyframe_start = new
             * TorsoKeyframe(signal.getId(), torso.getCategory(),
             * torso.getStart(), (torso.getEnd()+torso.getStart())/2);
             * TorsoKeyframe keyframe_end = new
             * TorsoKeyframe(signal.getId(), "stand",
             * (torso.getEnd()+torso.getStart())/2, torso.getEnd());
             *
             * keyframes.add(keyframe_start); keyframes.add(keyframe_end);
             *
             */
        }
    }

    @Override
    protected Comparator<Signal> getComparator() {
        return emptyComparator;
    }
}
