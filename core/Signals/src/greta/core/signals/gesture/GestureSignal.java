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
package greta.core.signals.gesture;

import greta.core.ideationalunits.IdeationalUnit;
import greta.core.signals.MultiStrokeSignal;
import greta.core.signals.ParametricSignal;
import greta.core.util.enums.Side;
import greta.core.util.math.Functions;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 * This class contains informations about gesture Signals.
 *
 * @author Andre-Marie Pez
 * @author Quoc Anh Le
 * @author Jing Huang
 */
public class GestureSignal extends ParametricSignal implements MultiStrokeSignal {

    public static GesturePose defaultRestPose;

    static {
        UniformPosition p = new UniformPosition(0.25, -0.5, 0);
        p.setXFixed(true);
        p.setYFixed(true);
        p.setZFixed(true);
        defaultRestPose = new GesturePose(
                new Hand(Side.LEFT, "relax", p.getCopy(), new Quaternion(), null),
                new Hand(Side.RIGHT, "relax", p.getCopy(), new Quaternion(), null));
        defaultRestPose.getRightHand().setWristOrientationGlobal(false);
        defaultRestPose.getLeftHand().setWristOrientationGlobal(false);
    }

    private static double preparationTMPScale = 0.5;
    private static double preparationSPCScale = 0.4;
    private static double preparationFLDScale = 0.8;
    private static double preparationPWRScale = 0.6;

    private static double retractionTMPScale = 0.5;
    private static double retractionSPCScale = 0.4;
    private static double retractionFLDScale = 0.8;
    private static double retractionPWRScale = 0.6;
    FittsLaw fl = new FittsLaw();

    private String id;
    private TimeMarker start;
    private TimeMarker ready;
    private TimeMarker strokeStart;
    private TimeMarker stroke;
    private List<TimeMarker> strokes;
//    private TimeMarker strokeEnd;
    private TimeMarker relax;
    private TimeMarker end;
    private List<TimeMarker> timeMarkers;
    private ArrayList<GesturePose> phases; // stroke-start to stroke-end

    private GesturePose startRestPose;
    private GesturePose endRestPose;
    private GesturePose relaxPose;

    private IdeationalUnit ideationalUnit;

    public GestureSignal(String id) {
        this.id = id;
        timeMarkers = new ArrayList<TimeMarker>(7);
        start = new TimeMarker("start");
        timeMarkers.add(start);
        ready = new TimeMarker("ready");
        timeMarkers.add(ready);
        strokeStart = new TimeMarker("stroke-start");
        timeMarkers.add(strokeStart);
        stroke = new TimeMarker("stroke");
        strokes = new ArrayList<TimeMarker>();
        strokes.add(stroke);
        timeMarkers.add(stroke);
//        strokeEnd = new TimeMarker("stroke-end");
//        timeMarkers.add(strokeEnd);
        relax = new TimeMarker("relax");
        timeMarkers.add(relax);
        end = new TimeMarker("end");
        timeMarkers.add(end);
        phases = new ArrayList<GesturePose>();

        startRestPose = new GesturePose(defaultRestPose);
        endRestPose = new GesturePose(defaultRestPose);
        relaxPose = new GesturePose(defaultRestPose);

        ideationalUnit = null;
    }

    public GestureSignal(GestureSignal ref) {
        this(ref.getId());
        this.setCategory(ref.getCategory());
        for (GesturePose phase : ref.getPhases()) {
            phases.add(new GesturePose(phase));
        }

        this.setReference(ref.getReference());
    }

    @Override
    public String getModality() {
        return "gesture";
    }

    @Override
    public List<TimeMarker> getTimeMarkers() {
        return timeMarkers;
    }

    private TimeMarker getStrokeEnd(){
        return strokes.get(strokes.size()-1);
    }
    @Override
    public TimeMarker getTimeMarker(String name) {
        if (name.equalsIgnoreCase("start")) {
            return start;
        }
        if (name.equalsIgnoreCase("end")) {
            return end;
        }
        if (name.equalsIgnoreCase("ready")) {
            return ready;
        }
        if (name.equalsIgnoreCase("relax")) {
            return relax;
        }
        if (name.equalsIgnoreCase("stroke")) {
            return stroke;
        }
        if (name.equalsIgnoreCase("stroke-start")) {
            return strokeStart;
        }
        if (name.equalsIgnoreCase("stroke-end")) {
//            return strokeEnd;
            return getStrokeEnd();
        }
        return null;
    }

    @Override
    public String getId() {
        return id;
    }

    public void addPhase(GesturePose phase) {
        this.phases.add(phase);
    }

    public List<GesturePose> getPhases() {
        return phases;
    }

    ////////////////////////////////////////////////////////////////////////////
    private void propagateHand(Hand prev, Hand next){
        if(next.getHandShape() == null){
            next.setHandShape(prev.getHandShape());
        }
        if(next.getPosition() == null && prev.getPosition()!=null){
            next.setPosition(prev.getPosition().getCopy());
        }
        if(next.getWristOrientation() == null && prev.getWristOrientation()!=null){
            next.setWristOrientation(new Quaternion(prev.getWristOrientation()));
            next.setWristOrientationGlobal(prev.isWristOrientationGlobal());
        }
        //trajectory ?... difficult
    }

    private void propagatePose(GesturePose prev, GesturePose next){
        if(prev.getLeftHand() != null) {
            if (next.getLeftHand() == null) {
                next.setLeftHand(new Hand(prev.getLeftHand()));
            } else {
                propagateHand(prev.getLeftHand(), next.getLeftHand());
            }
        }
        if(prev.getRightHand() != null) {
            if (next.getRightHand() == null) {
                next.setRightHand(new Hand(prev.getRightHand()));
            } else {
                propagateHand(prev.getRightHand(), next.getRightHand());
            }
        }
    }

    private void propagateStrokePoses(){
        if(phases.isEmpty()){
            return;
        }
        GesturePose prev = phases.get(0);
        for(int i=1; i<phases.size(); ++i){
            propagatePose(prev, phases.get(i));
            prev = phases.get(i);
        }
    }

    public void propagatePoses(){
        //fill rest poses if they are not complete
        propagatePose(defaultRestPose, startRestPose);
        propagatePose(defaultRestPose, endRestPose);

        //fill all other poses
        if(phases.isEmpty()){
            return;
        }
        propagatePose(startRestPose, phases.get(0));
        propagateStrokePoses();
    }
    ////////////////////////////////////////////////////////////////////////////
    private boolean spcApplied = false;

    private void applySPC(Hand hand, boolean withNoise){
        if(hand!=null && hand.getPosition()!=null){
            double noiseValue = withNoise ? (Math.random() * 2 - 1) * 0.2 : 0.0;
            hand.getPosition().applySpacial(getSPC() + noiseValue);
        }
    }

    private void applySPC(boolean withNoise){
        if(!spcApplied && isFilled()){
            for(GesturePose pose : phases){
                applySPC(pose.getLeftHand(), withNoise);
                applySPC(pose.getRightHand(), withNoise);
            }
            spcApplied = true;
        }
    }
    ////////////////////////////////////////////////////////////////////////////
    public double getStrokeFittsLaw(Hand first, Hand last) {
        return fl.getMovementTime(first, last, getTMP(), getPWR(), getSPC(), getFLD());
    }

    private double getStrokeDuration() {
        double d = 0.0;
        if(!phases.isEmpty()){
            getPhases().get(0).setRelativeTime(0);
        }
        for (int i = 0; i < getPhases().size()-1; ++i) {
            d += Math.max(
                    getStrokeFittsLaw(getPhases().get(i).getRightHand(), getPhases().get(i+1).getRightHand()),
                    getStrokeFittsLaw(getPhases().get(i).getLeftHand(),  getPhases().get(i+1).getLeftHand())
                    );
            getPhases().get(i+1).setRelativeTime(d);
        }
        return d;
    }

    ////////////////////////////////////////////////////////////////////////////
    public double getPreparationFittsLaw(Hand first, Hand last) {
        return fl.getMovementTime(
                first,
                last,
                Functions.changeInterval(Functions.changeInterval(getTMP(), 0, 1, -1, 1) * preparationTMPScale , -1, 1, 0, 1),
                getPWR() * preparationPWRScale,
                getSPC() * preparationSPCScale,
                getFLD() * preparationFLDScale);
    }

    private double getLeftPreparationDuration() {
        for (int i = 0; i < this.getPhases().size(); i++) {
            Hand pos = this.getPhases().get(i).getLeftHand();
            if (pos != null) {
                return getPreparationFittsLaw(pos, startRestPose.getLeftHand());
            }
        }
        return 0;
    }

    private double getRightPreparationDuration() {
        for (int i = 0; i < this.getPhases().size(); i++) {
            Hand pos = this.getPhases().get(i).getRightHand();
            if (pos != null) {
                return getPreparationFittsLaw(pos, startRestPose.getRightHand());
            }
        }
        return 0;
    }

    public double getPreparationDuration() {
        double lpd = getLeftPreparationDuration();
        double rpd = getRightPreparationDuration();
        return lpd < rpd ? rpd : lpd;
    }

    ////////////////////////////////////////////////////////////////////////////
    public double getRetractionFittsLaw(Hand first, Hand last) {
        return fl.getMovementTime(
                first,
                last,
                Functions.changeInterval(Functions.changeInterval(getTMP(), 0, 1, -1, 1) * retractionTMPScale , -1, 1, 0, 1),
                getPWR() * retractionPWRScale,
                getSPC() * retractionSPCScale,
                getFLD() * retractionFLDScale);
    }

    private double getLeftRetractionDuration() {
        for (int i = this.getPhases().size() - 1; i > 0; i--) {
            Hand pos = this.getPhases().get(i).getLeftHand();
            if (pos != null) {
                return getRetractionFittsLaw(pos, endRestPose.getLeftHand());
            }
        }
        return getRetractionFittsLaw(startRestPose.getLeftHand(), endRestPose.getLeftHand());
    }

    private double getRightRetractionDuration() {
        for (int i = this.getPhases().size() - 1; i > 0; i--) {
            Hand pos = this.getPhases().get(i).getRightHand();
            if (pos != null) {
                return getRetractionFittsLaw(pos, endRestPose.getRightHand());
            }
        }
        return getRetractionFittsLaw(startRestPose.getRightHand(), endRestPose.getRightHand());
    }

    public double getRetractionDuration() {
        double lrd = getLeftRetractionDuration();
        double rrd = getRightRetractionDuration();
        return lrd < rrd ? rrd : lrd;
    }


    @Override
    public void schedule() {

        propagateStrokePoses();
        applySPC(false);

        // start              ready     strk-start    strk-end     relax            end
        //   <---preparation---><---hold---><---stroke---><---hold---><--retraction-->


        double prep = durationBetween(start, ready);
        double hold1 = durationBetween(ready, strokeStart);
        double strk = durationBetween(strokeStart, getStrokeEnd());
        double hold2 = durationBetween(getStrokeEnd(), relax);
        double retr = durationBetween(relax, end);

        //we want ton real values for prep, hold1, strk hold2 and retr
        double strokeNeededDuration = getStrokeDuration();
        double prepNeededDuration = getPreparationDuration();
        double retrNeededDuration = getRetractionDuration();

        double hold1DefaultValue = 0;//strokeNeededDuration*0.7;//TODO, find a better value
        double hold2DefaultValue = strokeNeededDuration*0.5;//1.5;//TODO, find a better value
        double ratioHold1Hold2 = 0;//1.0/3.0;

        if(this.getCategory()!=null && this.getCategory().equalsIgnoreCase("rest")){
            hold1DefaultValue = 0;
            hold2DefaultValue = 0;
            ratioHold1Hold2 = 0;
        }

        if(start.isConcretized()){
            if(ready.isConcretized()){
                if(strokeStart.isConcretized()){
                    if(getStrokeEnd().isConcretized()){
                        if(relax.isConcretized()){
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double hold2ToRetr = durationBetween(getStrokeEnd(), end);
                                retr = Math.min(hold2ToRetr, retrNeededDuration);
                                hold2 = hold2ToRetr - retr;
                            }
                            else{
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                    else{
                        if(relax.isConcretized()){
                            double strkToHold2 = durationBetween(strokeStart, relax);
                            strk = Math.min(strkToHold2, strokeNeededDuration);
                            hold2 = strkToHold2 - strk;
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double strkToRetr = durationBetween(strokeStart, end);
                                strk = Math.min(strkToRetr, strokeNeededDuration);
                                retr = Math.min(strkToRetr-strk, retrNeededDuration);
                                hold2 = strkToRetr-strk-retr;
                            }
                            else{
                                strk = strokeNeededDuration;
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                }
                else{
                    if(getStrokeEnd().isConcretized()){
                        double hold1ToStrk = durationBetween(ready, getStrokeEnd());
                        strk = Math.min(hold1ToStrk, strokeNeededDuration);
                        hold1 = hold1ToStrk - strk;
                        if(relax.isConcretized()){
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double hold2ToRetr = durationBetween(getStrokeEnd(), end);
                                retr = Math.min(hold2ToRetr, retrNeededDuration);
                                hold2 = hold2ToRetr - retr;
                            }
                            else{
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                    else{
                        if(relax.isConcretized()){
                            double hold1ToHold2 = durationBetween(ready, relax);
                            strk = Math.min(hold1ToHold2, strokeNeededDuration);
                            hold1 = (hold1ToHold2-strk)*ratioHold1Hold2;
                            hold2 = hold1ToHold2-strk-hold1;
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double hold1ToRetr = durationBetween(ready, end);
                                strk = Math.min(hold1ToRetr, strokeNeededDuration);
                                retr = Math.min(hold1ToRetr - strk, retrNeededDuration);
                                hold1 = (hold1ToRetr-strk-retr)*ratioHold1Hold2;
                                hold2 = hold1ToRetr-strk-retr-hold1;
                            }
                            else{
                                hold1 = hold1DefaultValue;
                                strk = strokeNeededDuration;
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                }
            }
            else{
                if(strokeStart.isConcretized()){
                    double prepToHold1 = durationBetween(start, strokeStart);
                    prep = Math.min(prepToHold1, prepNeededDuration);
                    hold1 = prepToHold1-prep;
                    if(getStrokeEnd().isConcretized()){
                        if(relax.isConcretized()){
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double hold2ToRetr = durationBetween(getStrokeEnd(), end);
                                retr = Math.min(hold2ToRetr, retrNeededDuration);
                                hold2 = hold2ToRetr - retr;
                            }
                            else{
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                    else{
                        if(relax.isConcretized()){
                            double strkToHold2 = durationBetween(strokeStart, relax);
                            strk = Math.min(strkToHold2, strokeNeededDuration);
                            hold2 = strkToHold2 - strk;
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double strkToRetr = durationBetween(strokeStart, end);
                                strk = Math.min(strkToRetr, strokeNeededDuration);
                                retr = Math.min(strkToRetr-strk, retrNeededDuration);
                                hold2 = strkToRetr-strk-retr;
                            }
                            else{
                                strk = strokeNeededDuration;
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                }
                else{
                    if(getStrokeEnd().isConcretized()){
                        double prepToStrk = durationBetween(start, getStrokeEnd());
                        strk = Math.min(prepToStrk, strk);
                        prep = Math.min(prepToStrk - strk, prepNeededDuration);
                        hold1 = prepToStrk - strk - prep;
                        if(relax.isConcretized()){
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double hold2ToRetr = durationBetween(getStrokeEnd(), end);
                                retr = Math.min(hold2ToRetr, retrNeededDuration);
                                hold2 = hold2ToRetr - retr;
                            }
                            else{
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                    else{
                        if(relax.isConcretized()){
                            double prepToHold2 = durationBetween(start, relax);
                            strk = Math.min(prepToHold2, strokeNeededDuration);
                            prep = Math.min(prepToHold2 - strk, prepNeededDuration);
                            hold1 = (prepToHold2 - strk - prep) * ratioHold1Hold2;
                            hold2 = prepToHold2 - strk - prep - hold1;
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double total = durationBetween(start, end);
                                strk = Math.min(total, strokeNeededDuration);
                                prep = Math.min(total - strk, prepNeededDuration);
                                retr = Math.min(total - strk - prep, retrNeededDuration);
                                hold1 = (total - strk - prep - retr) * ratioHold1Hold2;
                                hold2 = total - strk - prep - retr - hold1;
                            }
                            else{
                                prep = prepNeededDuration;
                                hold1 = hold1DefaultValue;
                                strk = strokeNeededDuration;
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                }
            }
        }
        else{
            prep = prepNeededDuration;
            if(ready.isConcretized()){
                if(strokeStart.isConcretized()){
                    if(getStrokeEnd().isConcretized()){
                        if(relax.isConcretized()){
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                 double hold2ToRetr = durationBetween(getStrokeEnd(), end);
                                 retr = Math.min(hold2ToRetr, retrNeededDuration);
                                 hold2 = hold2ToRetr - retr;
                            }
                            else{
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                    else{
                        if(relax.isConcretized()){
                            double strkToHold2 = durationBetween(strokeStart, relax);
                            strk = Math.min(strkToHold2, strokeNeededDuration);
                            hold2 = strkToHold2 - strk;
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double strkToRetr = durationBetween(strokeStart, end);
                                strk = Math.min(strkToRetr, strokeNeededDuration);
                                retr = strkToRetr - strk;
                                hold2 = strkToRetr - strk - retr;
                            }
                            else{
                                strk = strokeNeededDuration;
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                }
                else{
                    if(getStrokeEnd().isConcretized()){
                        double hold1ToStrk = durationBetween(ready, getStrokeEnd());
                        strk = Math.min(hold1ToStrk, strokeNeededDuration);
                        hold1 = hold1ToStrk - strk;
                        if(relax.isConcretized()){
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double hold2ToRetr = durationBetween(getStrokeEnd(), end);
                                retr = Math.min(hold2ToRetr, retrNeededDuration);
                                hold2 = hold2ToRetr - retr;
                            }
                            else{
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                    else{
                        if(relax.isConcretized()){
                            double hold1ToHold2 = durationBetween(ready, relax);
                            strk = Math.min(hold1ToHold2, strokeNeededDuration);
                            hold1 = (hold1ToHold2 - strk) * ratioHold1Hold2;
                            hold2 = hold1ToHold2 - strk - hold1;
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double hold1ToRetr = durationBetween(ready, end);
                                strk = Math.min(hold1ToRetr, strokeNeededDuration);
                                retr = Math.min(hold1ToRetr-strk, retrNeededDuration);
                                hold1 = (hold1ToRetr-strk-retr) * ratioHold1Hold2;
                                hold2 = hold1ToRetr-strk-retr - hold1;
                            }
                            else{
                                hold1 = hold1DefaultValue;
                                strk = strokeNeededDuration;
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                }
            }
            else{
                hold1 = hold1DefaultValue;
                if(strokeStart.isConcretized()){
                    if(getStrokeEnd().isConcretized()){
                        if(relax.isConcretized()){
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double hold2ToRetr = durationBetween(getStrokeEnd(), end);
                                retr = Math.min(hold2ToRetr, retrNeededDuration);
                                hold2 = hold2ToRetr - retr;
                            }
                            else{
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                    else{
                        if(relax.isConcretized()){
                            double strkToHold2 = durationBetween(strokeStart, relax);
                            strk = Math.min(strkToHold2, strokeNeededDuration);
                            hold2 = strkToHold2 - strk;
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double strkToRetr = durationBetween(strokeStart, end);
                                strk = Math.min(strkToRetr, strokeNeededDuration);
                                retr = strkToRetr - strk;
                                hold2 = strkToRetr - strk - retr;
                            }
                            else{
                                strk = strokeNeededDuration;
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                }
                else{
                    strk = strokeNeededDuration;
                    if(getStrokeEnd().isConcretized()){
                        if(relax.isConcretized()){
                            if(!end.isConcretized()){
                                retr = retrNeededDuration;
                            }
                        }
                        else{
                            if(end.isConcretized()){
                                double hold2ToRetr = durationBetween(getStrokeEnd(), end);
                                retr = Math.min(hold2ToRetr, retrNeededDuration);
                                hold2 = hold2ToRetr - retr;
                            }
                            else{
                                hold2 = hold2DefaultValue;
                                retr = retrNeededDuration;
                            }
                        }
                    }
                    else{
                        hold2 = hold2DefaultValue;
                        if(retr<0){
                            retr = retrNeededDuration;
                        }
                    }
                }
            }
        }

        if(strk<strokeNeededDuration){
            double scaleFactor = strk/strokeNeededDuration;
            for(GesturePose pose : phases){
                pose.setRelativeTime(pose.getRelativeTime() * scaleFactor);
                //TODO rescale positions to match to strk
            }
        }

        //now we can affect TimeMarkers
        start.addReference(ready, -prep);
        start.addReference(strokeStart, -prep-hold1);
        start.addReference(getStrokeEnd(), -prep-hold1-strk);
        start.addReference(relax, -prep-hold1-strk-hold2);
        start.addReference(end, -prep-hold1-strk-hold2-retr);
        if( ! start.concretizeByReferences()) {
            //no TimeMarkers are concrete:
            //we put a default start value
            start.setValue(0);
        }

        ready.addReference(start, prep);
        ready.concretizeByReferences();

        strokeStart.addReference(start, prep+hold1);
        strokeStart.concretizeByReferences();

        getStrokeEnd().addReference(start, prep+hold1+strk);
        getStrokeEnd().concretizeByReferences();

        relax.addReference(start, prep+hold1+strk+hold2);
        relax.concretizeByReferences();

        end.addReference(start, prep+hold1+strk+hold2+retr);
        end.concretizeByReferences();
    }

    public double durationBetween(TimeMarker tm1, TimeMarker tm2){
        return tm1.isConcretized() && tm2.isConcretized() ? tm2.getValue()-tm1.getValue() : -1;
    }

    @Override
    public TimeMarker getStroke(int index) {
        if (index >= 0 && index < strokes.size()) {
            return strokes.get(index);
        }
        return null;
    }

    @Override
    public void setStroke(int index, String synchPoint) {
        if (index >= 0 && index < strokes.size()) {
            strokes.get(index).addReference(synchPoint);
        } else {
            TimeMarker newStroke = new TimeMarker("stroke" + strokes.size());
            newStroke.addReference(synchPoint);
            strokes.add(newStroke);
            timeMarkers.add(newStroke);
        }
    }

    public void setStartRestPose(GesturePose startRestPose) {
        this.startRestPose = startRestPose;
    }

    public void setEndRestPose(GesturePose endRestPose) {
        this.endRestPose = endRestPose;
    }

    public void setRelaxPose(GesturePose relaxPose) {
        this.relaxPose = relaxPose;
    }

    public GesturePose getStartRestPose() {
        return startRestPose;
    }

    public GesturePose getEndRestPose() {
        return endRestPose;
    }

     public GesturePose getRelaxPose() {
        return relaxPose;
    }

    @Override
    public TimeMarker getStart() {
        return start;
    }

    @Override
    public TimeMarker getEnd() {
        return end;
    }

    public GestureSignal generateMirrorGesture(String mirrorGestureId) {

        GestureSignal mirrorGesture = new GestureSignal(mirrorGestureId);
        mirrorGesture.setCategory(getCategory());

        for (GesturePose originalPhase : getPhases()) {

            Hand originaLeftHand = originalPhase.getLeftHand();
            Hand originalRightHand = originalPhase.getRightHand();

            Hand mirrorLeftHand = null;
            Hand mirrorRightHand = null;

            if (originaLeftHand != null) {

                mirrorRightHand = new Hand(originaLeftHand);
                mirrorRightHand.setSide(Side.RIGHT);

                Quaternion originaLeftHandWristOrientation = originaLeftHand.getWristOrientation();
                if (originaLeftHandWristOrientation != null) {
                    Vec3d mirrorRightHandWristOrientationEulerXYZ = originaLeftHandWristOrientation.getEulerAngleXYZ();
                    mirrorRightHandWristOrientationEulerXYZ.setY(-mirrorRightHandWristOrientationEulerXYZ.y());
                    mirrorRightHandWristOrientationEulerXYZ.setZ(-mirrorRightHandWristOrientationEulerXYZ.z());
                    Quaternion mirrorRightHandWristOrientation = new Quaternion();
                    mirrorRightHandWristOrientation.fromEulerXYZ(
                            mirrorRightHandWristOrientationEulerXYZ.x(),
                            mirrorRightHandWristOrientationEulerXYZ.y(),
                            mirrorRightHandWristOrientationEulerXYZ.z()
                    );
                    mirrorRightHand.setWristOrientation(mirrorRightHandWristOrientation);
                }
            }
            if (originalRightHand != null) {

                mirrorLeftHand = new Hand(originalRightHand);
                mirrorLeftHand.setSide(Side.LEFT);

                Quaternion originaRightHandWristOrientation = originalRightHand.getWristOrientation();
                if (originaRightHandWristOrientation != null) {
                    Vec3d mirrorLeftHandWristOrientationEulerXYZ = originaRightHandWristOrientation.getEulerAngleXYZ();
                    mirrorLeftHandWristOrientationEulerXYZ.setY(-mirrorLeftHandWristOrientationEulerXYZ.y());
                    mirrorLeftHandWristOrientationEulerXYZ.setZ(-mirrorLeftHandWristOrientationEulerXYZ.z());
                    Quaternion mirrorLeftHandWristOrientation = new Quaternion();
                    mirrorLeftHandWristOrientation.fromEulerXYZ(
                            mirrorLeftHandWristOrientationEulerXYZ.x(),
                            mirrorLeftHandWristOrientationEulerXYZ.y(),
                            mirrorLeftHandWristOrientationEulerXYZ.z()
                    );
                    mirrorLeftHand.setWristOrientation(mirrorLeftHandWristOrientation);
                }
            }

            GesturePose mirrorPhase = new GesturePose(mirrorLeftHand, mirrorRightHand);
            mirrorGesture.addPhase(mirrorPhase);
        }

        return mirrorGesture;
    }

    public IdeationalUnit getIdeationalUnit() {
        return ideationalUnit;
    }

    public void setIdeationalUnit(IdeationalUnit ideationalUnit) {
        this.ideationalUnit = ideationalUnit;
    }

    @Override
    public String toString() {
        return "gesture: " + id + " " + getCategory() + " " + getReference();
    }
}
