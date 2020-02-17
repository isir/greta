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
package greta.core.signals;

import greta.core.repositories.HeadLibrary;
import greta.core.util.math.Quaternion;
import greta.core.util.time.SynchPoint;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains informations about spine Signals.
 *
 * @author Andre-Marie Pez
 * @author Brice Donval
 */
public abstract class SpineSignal extends ParametricSignal {

    /**
     * Identifier of this Signal
     */
    private String id;

    /**
     * the name or type of the movement or direction
     */
    private String lexeme;

    /**
     * the number of repetition to do
     */
    private int repetitions;

    /**
     * if true the target position will be also the final position, if false the
     * final position will be 0 (rest)
     */
    private boolean shift = false;

    /**
     * time to start
     */
    private TimeMarker start;

    /**
     * time when the head reaches the target position
     */
    private TimeMarker attack;

    /**
     * time when the head quits the target position
     */
    private TimeMarker sustain;

    /**
     * time when the head reaches the final position (rest position or shift)
     */
    private TimeMarker end;

    /**
     * list containing the previous TimeMarkers
     */
    private List<TimeMarker> timeMarkers;

    /**
     * start, attack, sustain, end
     */
    private List<SpinePhase> phases;

    /**
     * Construct a new {@code SpineSignal}.
     *
     * @param id The identifier of this {@code SpineSignal}.
     */
    public SpineSignal(String id) {
        this.id = id;

        repetitions = 0;

        timeMarkers = new ArrayList<>(4);
        start = new TimeMarker("start");
        timeMarkers.add(start);
        attack = new TimeMarker("attack");
        timeMarkers.add(attack);
        sustain = new TimeMarker("sustain");
        timeMarkers.add(sustain);
        end = new TimeMarker("end");
        timeMarkers.add(end);

        phases = new ArrayList<>();
    }

    /**
     * Returns the list of the phases of this {@code SpinePhase}.<br/>
     * This list will be empty until {@code fillFromHeadLibrary(HeadLibrary)}
     * will be called. And complete after calling {@code schedulePhases()}.
     *
     * @return the list of the phases of this {@code HeadSignal}.
     */
    public List<SpinePhase> getPhases() {
        return phases;
    }

    @Override
    public List<TimeMarker> getTimeMarkers() {
        return timeMarkers;
    }

    @Override
    public TimeMarker getTimeMarker(String name) {

        if (name.equalsIgnoreCase("start")) {
            return start;
        }
        if (name.equalsIgnoreCase("stroke")
                || name.equalsIgnoreCase("stroke-start")
                || name.equalsIgnoreCase("strokestart")
                || name.equalsIgnoreCase("ready")
                || name.equalsIgnoreCase("attack")) {
            return attack;
        }
        if (name.equalsIgnoreCase("stroke-end")
                || name.equalsIgnoreCase("strokeend")
                || name.equalsIgnoreCase("relax")) {
            return sustain;
        }
        if (name.equalsIgnoreCase("end")) {
            return end;
        }

        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    /**
     * Returns the time of the "start" {@code TimeMarker}.
     *
     * @return the time of the "start" {@code TimeMarker}.
     */
    public double getStartValue() {
        return this.start.getValue();
    }

    /**
     * Returns the time of the "end" {@code TimeMarker}.
     *
     * @return the time of the "end" {@code TimeMarker}.
     */
    public double getEndValue() {
        return this.end.getValue();
    }

    /**
     * Resets this {@code HeadSignal}.
     */
    public void reset() {

        start = resetTM(start);
        attack = resetTM(attack);
        sustain = resetTM(sustain);
        end = resetTM(end);

        timeMarkers.clear();
        timeMarkers.add(start);
        timeMarkers.add(attack);
        timeMarkers.add(sustain);
        timeMarkers.add(end);

        phases.clear();
        setFilled(false);

        spcComputed = false;
    }

    /**
     * Resets a {@code TimeMarker}.<br/>
     * Due to the {@code TimeMarker} implementation, it is not possible to fully
     * reset a {@code TimeMarker}. So this method creates a new one and recovers
     * some properties of the original {@code TimeMarker}.
     *
     * @param tm the {@code TimeMarker} to reset
     * @return the reseted {@code TimeMarker}.
     */
    private TimeMarker resetTM(TimeMarker tm) {
        TimeMarker newTM = new TimeMarker(tm.getName());
        for (SynchPoint s : tm.getReferences()) {
            if (s.hasTargetTimeMarker()) {
                if (!timeMarkers.contains(s.getTarget())) {
                    tm.addReference(s.getTarget(), s.getOffset());
                }
            } else {
                tm.addReference(s.toString());
            }
        }
        return newTM;
    }

    /**
     * Check if this {@code HeadSignal} is a movement.<br/>
     * Movements are nod, shake and tilt.
     *
     * @return {@code true} if this {@code HeadSignal} is a movement,
     * {@code false} if it is just a direction to reach.
     */
    public boolean isMovement() {
        return ("nod".equalsIgnoreCase(lexeme) || "shake".equalsIgnoreCase(lexeme) || "tilt".equalsIgnoreCase(lexeme));
    }

    /**
     * Sets the timming of each phases.<br/>
     * It must be call after the {@code schedule} function and the
     * {@code fillFromHeadLibrary} function.
     */
    public void schedulePhases() {
        if (phases.size() < 1) {
            return;
        }

        //affect the spc to phases
        computeSPC();

        // the first one is the start phase so its time is the start time
        phases.get(0).setStartTime(start.getValue());
        phases.get(0).setEndTime(start.getValue());

        // the last one is at the end
        phases.get(phases.size() - 1).setStartTime(end.getValue());
        phases.get(phases.size() - 1).setEndTime(end.getValue());

        if (phases.size() > 2) {
            // all the others are beetwen attack and sustain
            double attackTime = attack.getValue();
            double wantedHoldDuration = sustain.getValue() - attackTime;
            double targetHoldDuration = computeHoldDuration();

            if (targetHoldDuration == 0) {
                if (phases.size() == 3) {
                    //create a hold
                    phases.add(2, new SpinePhase(phases.get(1)));
                }
                //else all positions are the sames...
                double timeStep = wantedHoldDuration / (phases.size() - 3);
                for (int i = 1; i < phases.size() - 1; ++i) {
                    phases.get(i).setStartTime(attackTime + (timeStep * (i - 1)));
                    phases.get(i).setEndTime(attackTime + (timeStep * (i - 1)));
                }
            } else {
                if (wantedHoldDuration <= targetHoldDuration) {
                    double startTime = attackTime;
                    double ratio = wantedHoldDuration / targetHoldDuration;
                    for (int i = 1; i < phases.size() - 1; ++i) {
                        phases.get(i).setStartTime(startTime);
                        phases.get(i).setEndTime(startTime);
                        startTime = startTime + ratio * timeBeetween(phases.get(i), phases.get(i + 1));
                    }
                } else {
                    //check if we can add repetitions. only nod shake tilt ?
                    if (repetitions == 0 //no imposed repetitions
                            && isMovement()) {
                        double prepareToRepDuration = timeBeetween(phases.get(1), phases.get(phases.size() - 2));
                        int possibleRep = 0;
                        while (wantedHoldDuration > (possibleRep + 1) * (targetHoldDuration) + possibleRep * prepareToRepDuration) {
                            possibleRep++;
                        }
                        //possibleRep equals one or more because wantedDuration>=targetDuration
                        //if more than one, we can repete
                        if (possibleRep > 1) {
                            int numPhasesToCopy = phases.size() - 2; //not start and end
                            for (int i = 1; i < possibleRep; ++i) {
                                for (int j = 0; j < numPhasesToCopy; j++) {
                                    phases.add(
                                            phases.size() - 1,// before the end
                                            new SpinePhase(phases.get(j + 1))
                                    );
                                }
                            }
                        }
                        //new target duration:
                        targetHoldDuration = possibleRep * targetHoldDuration + (possibleRep - 1) * prepareToRepDuration;
                    }

                    double startTime = attackTime;
                    int startIndex = 1;

                    //TODO ...
                    //it may have a little hold
                    //where it must be added ???? let's try at the begining... after testing, it is not good
//                    if(wantedHoldDuration-targetHoldDuration > 0.01){
//                        phases.add(2, new SpinePhase(phases.get(1)));
//                        phases.get(1).setStartTime(startTime);
//                        phases.get(1).setEndTime(startTime);
//                        startTime += wantedHoldDuration-targetHoldDuration;
//                        startIndex = 2;
//                    }

                    for (int i = startIndex; i < phases.size() - 1; ++i) {
                        phases.get(i).setStartTime(startTime);
                        phases.get(i).setEndTime(startTime);
                        startTime += timeBeetween(phases.get(i), phases.get(i + 1));
                    }
                }
            }
            //if the time of each phases is not enougth, we need to rescale the movment
            double wantedTotalDuration = end.getValue() - start.getValue();
            double targetStart = timeBeetween(phases.get(0), phases.get(1));
            double targetEnd = timeBeetween(phases.get(phases.size() - 1), phases.get(phases.size() - 2));
            double targetTotalDuration = targetStart + targetHoldDuration + targetEnd;

            double scale = Math.max(Math.min(1.0, (targetTotalDuration == 0 ? 1.0 : wantedTotalDuration / targetTotalDuration)), 0.1 * (1 + warp(getSPC())));
            for (int i = 1; i < phases.size() - 1; ++i) {
                SpinePhase current = phases.get(i);
                current.lateralRoll.value *= scale;
                current.sagittalTilt.value *= scale;
                current.verticalTorsion.value *= scale;
            }
        }
    }


    /**
     * variable for the computeSPC function. It must be modified only by this
     * function.
     */
    private boolean spcComputed = false;

    /**
     * Modifies the values of the positions of each phases according to the SPC
     * parameter.<br/>
     * this fuction as no effect if it is called before the fill function or if
     * values are already modified.
     */
    private void computeSPC() {
        if (!spcComputed && isFilled()) {
            computeSPC(phases.get(0), 1);
            for (int i = 1; i < phases.size() - 1; ++i) {
                SpinePhase phase = phases.get(i);
                computeSPC(phase, getSPC());
            }
            computeSPC(phases.get(phases.size() - 1), 1);
            spcComputed = true;
        }
    }

    /**
     * Modifies the values of the positions of one phases according to an SPC
     * parameter.
     *
     * @param phase the {@code SpinePhase} to modify
     * @param spc the SPC value to use.
     */
    private void computeSPC(SpinePhase phase, double spc) {
        computeDrirectionWithSPC(phase.lateralRoll, spc);
        computeDrirectionWithSPC(phase.sagittalTilt, spc);
        computeDrirectionWithSPC(phase.verticalTorsion, spc);
    }

    /**
     * Modifies the values of a direction according to an SPC parameter.
     *
     * @param headDirection the {@code SpineDirection} to modify
     * @param spc the SPC value to use.
     */
    private void computeDrirectionWithSPC(SpineDirection headDirection, double spc) {
        headDirection.value = greta.core.util.math.Functions.changeInterval(warp(spc), 0, 1, headDirection.valueMin, headDirection.valueMax);
    }

    /**
     * Ensure that an expressivity parameter is in the interval [0, 1].
     *
     * @param param a parameter value.
     * @return The parameter value warped between 0 and 1.
     */
    private double warp(double param) {
        return Math.max(0, Math.min(param, 1));
    }

    /**
     * Returns the max angular speed in degree per seconds
     *
     * @return the max angular speed in degree per seconds
     */
    private double getVMax() {
        return 90; //arbitrary value (degree per seconds)
    }

    /**
     * Returns the min angular speed in degree per seconds
     *
     * @return the min angular speed in degree per seconds
     */
    private double getVMin() {
        return 27; //arbitrary value (degree per seconds)
    }

    /**
     * Computes the radian value of a vertical direction.
     *
     * @param headDirection the vertical {@code SpineDirection}.
     * @return the radian value.
     */
    private double realValueOfVertical(SpineDirection headDirection) {
        return headDirection.value * Math.toRadians((headDirection.direction == SpineDirection.Direction.RIGHTWARD
                ? -HeadLibrary.getGlobalLibrary().getHeadIntervals().verticalRightMax
                : HeadLibrary.getGlobalLibrary().getHeadIntervals().verticalLeftMax));
    }

    /**
     * Computes the radian value of a lateral direction.
     *
     * @param headDirection the lateral {@code SpineDirection}.
     * @return the radian value.
     */
    private double realValueOfLateral(SpineDirection headDirection) {
        return headDirection.value * Math.toRadians((headDirection.direction == SpineDirection.Direction.RIGHTWARD
                ? -HeadLibrary.getGlobalLibrary().getHeadIntervals().lateralRightMax
                : HeadLibrary.getGlobalLibrary().getHeadIntervals().lateralLeftMax));
    }


    /**
     * Computes the radian value of a sagital direction.
     *
     * @param headDirection the sagital {@code SpineDirection}.
     * @return the radian value.
     */
    private double realValueOfSagital(SpineDirection headDirection) {
        return headDirection.value * Math.toRadians((headDirection.direction == SpineDirection.Direction.FORWARD
                ? -HeadLibrary.getGlobalLibrary().getHeadIntervals().sagittalDownMax
                : HeadLibrary.getGlobalLibrary().getHeadIntervals().sagittalUpMax));
    }

    private Quaternion getOrientationOf(SpinePhase phase) {

        Quaternion orientation = new Quaternion();

        if (phase._rotations.isEmpty()) {
            orientation.fromEulerXYZ(
                    (float) realValueOfVertical(phase.verticalTorsion),
                    (float) realValueOfLateral(phase.lateralRoll),
                    (float) realValueOfSagital(phase.sagittalTilt));
        } else {
            for (Quaternion q : phase._rotations.values()) {
                orientation = Quaternion.multiplication(orientation, q);
            }
        }

        return orientation;
    }

    /**
     * Computes the angle distance between two {@code SpinePhases}.
     *
     * @param first a {@code SpinePhase}.
     * @param second an other {@code SpinePhase}.
     * @return their distance in degree.
     */
    private double distanceBetween(SpinePhase first, SpinePhase second) {

        Quaternion q1 = getOrientationOf(first);
        Quaternion q2 = getOrientationOf(second);

        Quaternion delta = Quaternion.multiplication(q2, q1.inverse());
        delta.normalize();

        return Math.abs(Math.toDegrees(delta.angle()));
    }

    /**
     * Computes the time to go from a {@code SpinePhase} to an other
     * {@code SpinePhase}
     *
     * @param first the {@code SpinePhase} to start
     * @param second the {@code SpinePhase} to reach
     * @return the time in second
     */
    private double timeBeetween(SpinePhase first, SpinePhase second) {
        return timeBeetween(first, second, getTMP());
    }

    /**
     * Computes the time to go from a {@code SpinePhase} to an other
     * {@code SpinePhase} according to a TMP value.
     *
     * @param first the {@code SpinePhase} to start
     * @param second the {@code SpinePhase} to reach
     * @param tmp the TMP value.
     * @return the time in second
     */
    private double timeBeetween(SpinePhase first, SpinePhase second, double tmp) {
        double distance = 10; //default value (in degree)
        if (first != null || second != null) {
            distance = distanceBetween(first, second);
        }
        double v = getVMin() + warp(tmp) * (getVMax() - getVMin());
        return (distance
                + (distance > 0.001 ? 10 : 0) //add an incompressible time
                ) / v;
    }

    /**
     * Computes the "hold" duration.<br/>
     * i.e. the time between attack and sustain.
     *
     * @return the duration of the "hold" in second
     */
    private double computeHoldDuration() {
        double holdTemp = 0;
        if (isFilled() && phases.size() > 2) {
            for (int i = 2; i < phases.size() - 1; ++i) {
                holdTemp += timeBeetween(phases.get(i - 1), phases.get(i));
            }
        }
        return holdTemp;
    }

    /**
     * Computes the "return" duration.<br/>
     * i.e. the time between sustain and end.
     *
     * @return the duration of the "return" in second
     */
    private double computeReturnDuration() {
        double returnTemp = 0;
        if (!isFilled() || phases.size() > 2) {
            returnTemp = timeBeetween(
                    isFilled() ? phases.get(phases.size() - 1) : null,
                    isFilled() && phases.size() > 1 ? phases.get(phases.size() - 2) : null
            );
        } // in the "else" case, this is filled and we know that there's no return
        return returnTemp;
    }

    @Override
    public void schedule() {
        //first, if it is filled, SPC must modify all non-bounds phases
        computeSPC();

        // their is 3 real phases : Go to position, maintain position and return to the default position

        // start    attack      sustain      end
        //   <---go---><---hold---><--return-->
        //   <-------go+hold------>
        //             <------hold+return----->
        //   <--------------total------------->
        double goDuration = -1;
        double holdDuration = -1;
        double returnDuration = -1;
        double goPlusHoldDuration = -1;
        double holdPlusReturnDuration = -1;
        double totalDuration = -1;

        //try to re-use specified timings
        if (start.isConcretized() && attack.isConcretized()) {
            goDuration = attack.getValue() - start.getValue();
        }

        if (sustain.isConcretized() && end.isConcretized()) {
            returnDuration = end.getValue() - sustain.getValue();
        }

        if (start.isConcretized() && end.isConcretized()) {
            totalDuration = end.getValue() - start.getValue();
        }

        if (attack.isConcretized() && sustain.isConcretized()) {
            holdDuration = sustain.getValue() - attack.getValue();
        }

        if (start.isConcretized() && sustain.isConcretized()) {
            goPlusHoldDuration = sustain.getValue() - start.getValue();
        }

        if (attack.isConcretized() && end.isConcretized()) {
            holdPlusReturnDuration = end.getValue() - attack.getValue();
        }


        //check go
        if (goDuration < 0) {//not set so we compute it
            double goTemp = timeBeetween(
                    isFilled() && phases.size() > 0 ? phases.get(0) : null,
                    isFilled() && phases.size() > 1 ? phases.get(1) : null
            );

            if (!start.isConcretized()) {
                //the start is not set, we have all the time we want
                goDuration = goTemp;
            } else {
                //here we know that start is set and attack is not set.
                //totalDuration and goPlusHoldDuration may be already set.
                if (totalDuration < 0 && goPlusHoldDuration < 0) {
                    //no one is set, we have all the time we want
                    goDuration = goTemp;
                } else {
                    //one of totalDuration or goPlusHoldDuration is set.

                    double holdTemp = computeHoldDuration();

                    if (goPlusHoldDuration < 0) {
                        //here only totalDuration is set
                        //and returnDuration can not be set:
                        //  if returnDuration is set, sustain and end are set.
                        //  here start is set and if start and sustain are set,
                        //  goPlusHoldDuration must be set. Or it is not the case.
                        double returnTemp = computeReturnDuration();
                        if (goTemp + holdTemp + returnTemp > totalDuration) {
                            double scaleFactor = totalDuration / (goTemp + holdTemp + returnTemp);
                            goTemp = goTemp * scaleFactor;
                            returnTemp = returnTemp * scaleFactor;
                        }
                        goDuration = goTemp;
                        returnDuration = returnTemp;
                        holdDuration = totalDuration - goDuration - returnDuration;
                        //goPlusHoldDuration = goDuration+holdDuration;
                        holdPlusReturnDuration = holdDuration + returnDuration;
                    } else {
                        //here goPlusHoldDuration is set (perhaps totalDuration to but we don't care)
                        if (goTemp + holdTemp > goPlusHoldDuration) {
                            //we need to addapt timmings
                            goTemp = goTemp * goPlusHoldDuration / (goTemp + holdTemp);
                        }
                        goDuration = goTemp;
                        //here, we know the hold duration, so we set it
                        holdDuration = goPlusHoldDuration - goDuration;
                        if (returnDuration >= 0) {
                            holdPlusReturnDuration = holdDuration + returnDuration;
                        }
                    }
                }
            }
        }

        //check return
        if (returnDuration < 0) {//not set so we compute it
            double returnTemp = computeReturnDuration();

            if (!end.isConcretized()) {
                //the end is not set, we have all the time we want
                returnDuration = returnTemp;
            } else {
                //at this point, only three case are possible :
                // - go is known
                // - go and hold+return are known (total is also known but not computed)
                // - go, hold+return and total are known.

                //the first case means that we have all the time we want
                if (holdPlusReturnDuration < 0) {
                    returnDuration = returnTemp;
                } else {
                    double holdTemp = computeHoldDuration();
                    if (holdTemp + returnTemp > holdPlusReturnDuration) {
                        //we need to addapt timmings
                        returnTemp = returnTemp * holdPlusReturnDuration / (returnTemp + holdTemp);
                    }
                    returnDuration = returnTemp;
                    //here, we know the hold duration, so we set it
                    holdDuration = holdPlusReturnDuration - returnDuration;
                }
            }
        }

        //this is the turn of hold
        if (holdDuration < 0) {
            //at this point if holdDuration is not computed, the only case possible is
            //that no duration containing the hold are computed
            //so we have all the time we want
            holdDuration = computeHoldDuration();
        }

        //total
        if (totalDuration < 0) {
            totalDuration = goDuration + holdDuration + returnDuration;
        }

        //now we can affect TimeMarkers

        start.addReference(attack, -goDuration);
        start.addReference(sustain, -goDuration - holdDuration);
        start.addReference(end, -totalDuration);
        if (!start.concretizeByReferences()) {
            //no TimeMarkers are concrete:
            //we put a default start value
            start.setValue(0);
        }

        attack.addReference(start, goDuration);
        attack.concretizeByReferences();

        sustain.addReference(start, goDuration + holdDuration);
        sustain.concretizeByReferences();

        end.addReference(start, totalDuration);
        end.concretizeByReferences();
    }

    @Override
    public void setReference(String reference) {
        super.setReference(reference);
        if (lexeme == null) {
            setLexeme(reference);
        }
    }


    /**
     * Returns the lexeme of this {@code HeadSignal}.<br/>
     * i.e. the name or type of the movement or direction.
     *
     * @return the lexeme of this {@code HeadSignal}.
     */
    public String getLexeme() {
        return lexeme;
    }

    /**
     * Set the lexeme of this {@code HeadSignal}.<br/>
     * i.e. the name or type of the movement or direction.
     *
     * @param lexeme the lexeme
     */
    public void setLexeme(String lexeme) {
        this.lexeme = lexeme;
    }

    /**
     * Retunrs the number of repetitions to do.
     *
     * @return the number of repetitions to do.
     */
    public int getRepetitions() {
        return repetitions;
    }

    /**
     * Set the number of repetitions to do.
     *
     * @param repetitions the number of repetitions to do.
     */
    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    /**
     *
     * @return {@code true} the target position will be also the final position,
     * {@code false} the final position will be the rest position.
     */
    public boolean isDirectionShift() {
        return shift;
    }

    /**
     *
     * @param isShift {@code true} the target position will be also the final
     * position, {@code false} the final position will be the rest position.
     */
    public void setDirectionShift(boolean isShift) {
        this.shift = isShift;
    }

    @Override
    public TimeMarker getStart() {
        return start;
    }

    @Override
    public TimeMarker getEnd() {
        return end;
    }

}
