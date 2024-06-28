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
import greta.core.keyframes.HeadKeyframe;
import greta.core.keyframes.Keyframe;
import greta.core.signals.HeadSignal;
import greta.core.signals.Signal;
import greta.core.signals.SpineDirection;
import greta.core.signals.SpinePhase;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 * This {@code KeyframeGenerator} gererates {@code HeadKeyframes} from {@code HeadSignals}
 * @author Andre-Marie Pez
 */
public class HeadKeyframeGenerator extends KeyframeGenerator {

    /** The current rest position */
    private HeadKeyframe defaultPosition;

    /**
     * Default constructor
     */
    public HeadKeyframeGenerator() {
        super(HeadSignal.class);
        defaultPosition = new HeadKeyframe("rest", new SpinePhase("rest", 0, 0), "rest");
    }

    @Override
    protected void generateKeyframes(List<Signal> inputSignals, List<Keyframe> outputKeyframe) {

        //I) dispatch Head in different category
        LinkedList<HeadSignal> shifts = new LinkedList<HeadSignal>();
        LinkedList<HeadSignal> nonShift = new LinkedList<HeadSignal>();

        for (Signal signal : inputSignals) {
            HeadSignal head = (HeadSignal) signal;
            head.schedulePhases();
            if(head.isDirectionShift()){
                shifts.add(head);
            }
            else{
                nonShift.add(head);
            }
        }

        LinkedList<HeadKeyframe> keyframes = new LinkedList<HeadKeyframe>();

        //II) get the shifts
        for (HeadSignal head : shifts) {
            HeadKeyframe startKeyframe = null;
            if(keyframes.isEmpty()) {
                startKeyframe = new HeadKeyframe(getDefaultPosition());
            }
            else if(keyframes.peekLast().getOffset() <= head.getPhases().get(0).getStartTime()){
                startKeyframe = new HeadKeyframe(keyframes.peekLast());
            }else{
                startKeyframe = new HeadKeyframe(keyframes.peekLast());
            }

            if(startKeyframe != null){
                setTimeOn(startKeyframe, head.getStartValue());
                keyframes.add(startKeyframe);
            }

            HeadKeyframe kf = null;
            kf = createKeyFrame(head, head.getPhases().get(head.getPhases().size()-1));

            keyframes.add(kf);
        }


        //add one keyframe at the end of the who    le animation
        if(keyframes.isEmpty() || (!nonShift.isEmpty() && nonShift.peekLast().getEndValue() > keyframes.peekLast().getOffset())){
            HeadKeyframe endKeyframe = new HeadKeyframe(
                    keyframes.isEmpty() ? getDefaultPosition() : keyframes.peekLast());
            setTimeOn(endKeyframe, nonShift.peekLast().getEndValue());
            keyframes.addLast(endKeyframe);
        }


        Collections.sort(nonShift, startComparator);

        //add one keyframe at the begining of the whole animation
        if(keyframes.isEmpty() || (!nonShift.isEmpty() && nonShift.peekFirst().getStartValue() < keyframes.peekFirst().getOffset())){
            HeadKeyframe startKeyframe = new HeadKeyframe(getDefaultPosition());
            setTimeOn(startKeyframe, nonShift.peekFirst().getStartValue());
            keyframes.addFirst(startKeyframe);
        }


        //III) add the others head movements
        for(HeadSignal head : nonShift){
            List<HeadKeyframe> currentKeyframes = getCurrent(keyframes, head.getStartValue(), head.getEndValue());
            addMovement(head, currentKeyframes);
        }

        //IV) limit the head amplitude of the keyframes to a reasonable value
        for(HeadKeyframe keyframe : keyframes){
            limit(keyframe);
        }

        //V) add the HeadKeyframe into the output list
        outputKeyframe.addAll(keyframes);

        //VI) save the last position
        setRestPosition(keyframes.peekLast()); // put in defaultPosition the last
    }

    @Override
    protected Comparator<Signal> getComparator() {
        return endComparator;
    }

    /**
     * Creates a {@code HeadKeyframe} corresponding to a {@code SpinePhase}.
     * @param head the {@code HeadSignal} of the {@code SpinePhase}.
     * @param phase the {@code SpinePhase}.
     * @return the {@code HeadKeyframe} corresponding to the {@code SpinePhase}.
     */
    private HeadKeyframe createKeyFrame(HeadSignal head, SpinePhase phase) {
        HeadKeyframe keyframe = new HeadKeyframe(head.getId(), phase, head.getCategory());
        ExpressivityParameters e = new ExpressivityParameters();

        e.fld = head.getFLD();
        e.pwr = head.getPWR();
        e.spc = head.getSPC();
        e.tmp = head.getTMP();
        e.tension = head.getTension();
        keyframe.setParameters(e);
        return keyframe;
    }

    /**
     * Store an {@code HeadKeyframe} as a rest position.
     * @param keyframe the {@code HeadKeyframe}.
     */
    private void setRestPosition(HeadKeyframe phase){
        defaultPosition.lateralRoll = new SpineDirection(phase.lateralRoll);
        defaultPosition.sagittalTilt = new SpineDirection(phase.sagittalTilt);
        defaultPosition.verticalTorsion = new SpineDirection(phase.verticalTorsion);
    }

    /**
     * Assigns a time to a {@code HeadKeyframe}.
     * @param kf the {@code HeadKeyframe}.
     * @param time the time to set in second.
     */
    private void setTimeOn(HeadKeyframe kf, double time){
        kf.setOffset(time);
        kf.setOnset(time);
    }

    /**
     * Returns the {@code HeadKeyframes} in a time interval.<br/>
     * The list of {@code HeadKeyframes} in parameter must be sorted by time.
     * @param keyframes a list of {@code HeadKeyframe}.
     * @param start the lower bound of the time interval.
     * @param end the higher bound of the time interval.
     * @return the sublist of {@code HeadKeyframes} in a time interval.
     */
    private List<HeadKeyframe> getCurrent(List<HeadKeyframe> keyframes, double start, double end) {
        ListIterator<HeadKeyframe> begining = keyframes.listIterator(keyframes.size());
        while(begining.hasPrevious() && begining.previous().getOffset()>start){}

        ListIterator<HeadKeyframe> ending = keyframes.listIterator(keyframes.size());
        while(ending.hasPrevious() && ending.previous().getOffset()>end){}
        while(ending.hasNext() && ending.next().getOffset()<end){}

        return keyframes.subList(begining.previousIndex()+1, ending.nextIndex());
    }

    /**
     * Adds in the given list of {@code HeadKeyframes} those corresponding to a {@code HeadSignal}.
     * @param headMovement the {@code HeadSignal} to add.
     * @param destination the list of {@code HeadKeyframes}.
     */
    private void addMovement(HeadSignal headMovement, List<HeadKeyframe> destination){

        List<HeadKeyframe> headKeyframes = new LinkedList<HeadKeyframe>();

        //create the new keframes
        List<HeadKeyframe> newKeyframes = new LinkedList<HeadKeyframe>();
        for (SpinePhase phase : headMovement.getPhases()) {
            HeadKeyframe keyframe = createKeyFrame(headMovement, phase);

            headKeyframes.add(keyframe);

            List<HeadKeyframe> segment = getCurrent(destination, keyframe.getOffset(), keyframe.getOffset());

            //here the size of "segment" is 1 or 2, no less no more
            HeadKeyframe newKf = new HeadKeyframe(keyframe);
            if(segment.size()==1){
                //only one in the list, so they are sames
                blend(newKf, segment.get(0));
            }
            else{
                blend(newKf, interpolate(segment.get(0), segment.get(1), keyframe.getOffset()));
            }
            newKeyframes.add(newKf);
        }

        //update existing frames
        for(HeadKeyframe toUpdate: destination){
            List<HeadKeyframe> segment = getCurrent(headKeyframes, toUpdate.getOffset(), toUpdate.getOffset());
            if(segment.size()>1){
                HeadKeyframe newKf = new HeadKeyframe(toUpdate);
                blend(newKf, interpolate(segment.get(0), segment.get(1), toUpdate.getOffset()));
                newKeyframes.add(newKf);
            }
        }

        //then add the new keyframes in the destination list
        for(HeadKeyframe kf : newKeyframes){
            List<HeadKeyframe> segment = getCurrent(destination, kf.getOffset(), kf.getOffset());
            if(segment.size()==1){
                segment.set(0, kf);
            }
            else{
                segment.add(1, kf);
            }
        }
    }

    /**
     * Bends, in a {@code HeadKeyframe}, an other {@code HeadKeyframe}.
     * @param first the {@code HeadKeyframe} that will be modified.
     * @param second the {@code HeadKeyframe} to add in the first {@code HeadKeyframe}.
     */
    protected void blend(HeadKeyframe first, HeadKeyframe second) {
        first.lateralRoll.add(second.lateralRoll);
        first.sagittalTilt.add(second.sagittalTilt);
        first.verticalTorsion.add(second.verticalTorsion);
    }

    /**
     * Interpolate two {@code HeadKeyframes}.<br/>
     * The intepolation is linear.
     * @param first the first {@code HeadKeyframe}.
     * @param second the last {@code HeadKeyframe}.
     * @param time the time of the intepolation.
     * @return the interpolated {@code HeadKeyframe}.
     */
    protected HeadKeyframe interpolate(HeadKeyframe first, HeadKeyframe second, double time){
        double t = (time - first.getOffset()) / (second.getOffset()-first.getOffset());

        HeadKeyframe result = new HeadKeyframe(first);
        //result = first

        result.lateralRoll.inverse();
        result.sagittalTilt.inverse();
        result.verticalTorsion.inverse();
        //result = -first

        blend(result, second);
        //result = second-first


        result.lateralRoll.multiply(t);
        result.sagittalTilt.multiply(t);
        result.verticalTorsion.multiply(t);
        //result = t*(second-first)

        blend(result, first);
        //result = t*(second-first) + first

        setTimeOn(result, time);
        return result;
    }

    /**
     * Limits the value of all {@code SpineDirection} of a {@code HeadKeyframe}.
     * @param keyframe the {@code HeadKeyframe} to limit.
     */
    private void limit(HeadKeyframe keyframe) {
        limit(keyframe.lateralRoll);
        limit(keyframe.sagittalTilt);
        limit(keyframe.verticalTorsion);
    }

    /**
     * Limits the value of a {@code SpineDirection}.
     * @param headDirection the {@code SpineDirection} to limit
     */
    private void limit(SpineDirection headDirection) {
        if(headDirection.value > 1) {
            headDirection.value = 1 + (headDirection.value/10);
        }
    }

    /**
     * @return the defaultPosition
     */
    public HeadKeyframe getDefaultPosition() {
        return defaultPosition;
    }

    /**
     * @param defaultPosition the defaultPosition to set
     */
    public void setDefaultPosition(HeadKeyframe defaultPosition) {
        this.defaultPosition = defaultPosition;
    }
}
