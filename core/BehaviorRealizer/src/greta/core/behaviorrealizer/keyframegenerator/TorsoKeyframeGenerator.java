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

import greta.core.keyframes.ExpressivityParameters;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.TorsoKeyframe;
import greta.core.signals.Signal;
import greta.core.signals.SpineDirection;
import greta.core.signals.SpinePhase;
import greta.core.signals.TorsoSignal;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Quoc Anh Le
 */
public class TorsoKeyframeGenerator extends KeyframeGenerator {

    private TorsoKeyframe defaultPosition;

    public TorsoKeyframeGenerator() {
        super(TorsoSignal.class);
        defaultPosition = new TorsoKeyframe("rest", new SpinePhase("rest", 0, 0), "rest");
    }

    @Override
    protected void generateKeyframes(List<Signal> inputSignals, List<Keyframe> outputKeyframes) {

        for (Signal signal : inputSignals) {
            TorsoSignal torso = (TorsoSignal) signal;
            torso.schedulePhases();

            LinkedList<TorsoKeyframe> keyframes = new LinkedList<TorsoKeyframe>();
            TorsoKeyframe startKeyframe = null;

            if(keyframes.isEmpty()) {
                startKeyframe = new TorsoKeyframe(getDefaultPosition());
            }
            else if(keyframes.peekLast().getOffset()<=torso.getPhases().get(0).getStartTime()){
                    startKeyframe = new TorsoKeyframe(keyframes.peekLast());
            }

            if(startKeyframe != null){
                setTimeOn(startKeyframe, torso.getStartValue());
                ExpressivityParameters e = new ExpressivityParameters();
                e.fld = torso.getFLD();
                e.pwr = torso.getPWR();
                e.spc = torso.getSPC();
                e.tmp = torso.getTMP();
                e.tension = torso.getTension();
                startKeyframe.setParameters(e);

                if (torso.shoulder){
                    startKeyframe.setOnlytheShoulder();
                }

                keyframes.add(startKeyframe);
            }

            TorsoKeyframe kf = null;
            kf = createKeyFrame(torso, torso.getPhases().get(torso.getPhases().size()-1));
            keyframes.add(kf);
            //keyframes.add(createKeyFrame(torso, torso.getPhases().get(torso.getPhases().size()-1)));

            //V) add the ShoulderKeyframe into the output list
            outputKeyframes.addAll(keyframes);

            //VI) save the last position
            setRestPosition(keyframes.peekLast());
        }
    }

    @Override
    protected Comparator<Signal> getComparator() {
        return emptyComparator;
    }

    private void setTimeOn(TorsoKeyframe kf, double time){
        kf.setOffset(time);
        kf.setOnset(time);
    }

    private void setRestPosition(TorsoKeyframe phase){
        defaultPosition.verticalTorsion = new SpineDirection(phase.verticalTorsion); //phase.verticalTorsion;
    }

    private TorsoKeyframe createKeyFrame(TorsoSignal sh, SpinePhase phase) {
        TorsoKeyframe keyframe = new TorsoKeyframe(sh.getId(), phase, sh.getCategory());
        if (sh.shoulder){
            keyframe.setOnlytheShoulder();}

        ExpressivityParameters e = new ExpressivityParameters();

        e.fld = sh.getFLD();
        e.pwr = sh.getPWR();
        e.spc = sh.getSPC();
        e.tmp = sh.getTMP();
        e.tension = sh.getTension();
        keyframe.setParameters(e);
        return keyframe;
    }

    protected TorsoKeyframe interpolate(TorsoKeyframe first, TorsoKeyframe second, double time){
        double t = (time - first.getOffset()) / (second.getOffset()-first.getOffset());

        TorsoKeyframe result = new TorsoKeyframe(); // first
        //result = first

        SpineDirection vert = new SpineDirection(first.verticalTorsion);
        vert.inverse();

        result.verticalTorsion= vert;
        result.verticalTorsion.add(second.verticalTorsion);

        //result.lateralRoll.inverse();
        //result.sagittalTilt.inverse();
        //result.verticalTorsion.inverse();
        //result = -first

        //blend(result, second);
        //result = second+(-first)


        //result.lateralRoll.multiply(t);
        //result.sagittalTilt.multiply(t);
        result.verticalTorsion.multiply(t);
        //result = t*(second-first)

        result.verticalTorsion.add(first.verticalTorsion);
        //blend(result, first);
        //result = t*(second-first) + first

        setTimeOn(result, time);
        return result;
    }

    protected void blend(TorsoKeyframe first, TorsoKeyframe second) {
        //first.lateralRoll.add(second.lateralRoll);
        //first.sagittalTilt.add(second.sagittalTilt);
        first.verticalTorsion.add(second.verticalTorsion);
    }

    /**
     * @return the defaultPosition
     */
    public TorsoKeyframe getDefaultPosition() {
        return defaultPosition;
    }

    /**
     * @param defaultPosition the defaultPosition to set
     */
    public void setDefaultPosition(TorsoKeyframe defaultPosition) {
        this.defaultPosition = defaultPosition;
    }
}
