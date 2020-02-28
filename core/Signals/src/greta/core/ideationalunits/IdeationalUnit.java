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
package greta.core.ideationalunits;

import greta.core.repositories.Gestuary;
import greta.core.repositories.SignalEntry;
import greta.core.signals.FaceSignal;
import greta.core.signals.GazeSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.Signal;
import greta.core.signals.TorsoSignal;
import greta.core.signals.gesture.GesturePose;
import greta.core.signals.gesture.GestureSignal;
import greta.core.signals.gesture.Hand;
import greta.core.signals.gesture.Position;
import greta.core.signals.gesture.SymbolicPosition;
import greta.core.signals.gesture.UniformPosition;
import greta.core.util.enums.Side;
import greta.core.util.math.Quaternion;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Brice Donval
 */
public class IdeationalUnit extends Unit {

    private final IdeationalUnitFactory parent;

    private final String mainSignalsPrefix;
    private final Map<Class, List<Signal>> signals;

    private GestureSignal mainGesture;
    private Side mainGestureSide;

    /* ---------------------------------------------------------------------- */
    protected IdeationalUnit(IdeationalUnitFactory parent, String id, String mainIntentionId) {
        super(id);
        this.parent = parent;
        this.mainSignalsPrefix = mainIntentionId + "_";
        this.signals = new HashMap<Class, List<Signal>>();
    }

    /* ---------------------------------------------------------------------- */
    public IdeationalUnitFactory getParentFactory() {
        return parent;
    }

    public void addSignal(Signal signal) {

        Class SignalClass = signal.getClass();

        if (signals.get(SignalClass) == null) {
            signals.put(SignalClass, new ArrayList<Signal>());
        }
        signals.get(SignalClass).add(signal);

        if (signal.getId().startsWith(mainSignalsPrefix)) {
            if (signal instanceof GestureSignal) {
                mainGesture = (GestureSignal) signal;
                Pattern p = Pattern.compile("^(.+)=(.+)_(L|R|L(_.+)|R(_.+))$");
                Matcher m = p.matcher(mainGesture.getReference());
                if (m.matches()) {
                    boolean gestureSideIsLeft = m.group(3).startsWith("L");
                    mainGestureSide = gestureSideIsLeft ? Side.LEFT : Side.RIGHT;
                } else {
                    mainGestureSide = Side.BOTH;
                }
            }
        }
    }

    public void removeSignal(Signal signal) {

        Class SignalClass = signal.getClass();

        if (signals.get(SignalClass) != null) {
            signals.get(SignalClass).remove(signal);
            if (signals.get(SignalClass).isEmpty()) {
                signals.remove(SignalClass);
            }
        }

        if (mainGesture == signal) {
            mainGesture = null;
        }
    }

    /* ---------------------------------------------------------------------- */
    protected List<FaceSignal> getFaceSignals() {
        return (List<FaceSignal>) (List<?>) signals.get(FaceSignal.class);
    }

    protected List<GazeSignal> getGazeSignals() {
        return (List<GazeSignal>) (List<?>) signals.get(GazeSignal.class);
    }

    protected List<GestureSignal> getGestureSignals() {
        return (List<GestureSignal>) (List<?>) signals.get(GestureSignal.class);
    }

    protected List<HeadSignal> getHeadSignals() {
        return (List<HeadSignal>) (List<?>) signals.get(HeadSignal.class);
    }

    protected List<TorsoSignal> getTorsoSignals() {
        return (List<TorsoSignal>) (List<?>) signals.get(TorsoSignal.class);
    }

    /* ---------------------------------------------------------------------- */
    protected void preprocessSignals() {
        preprocessFaceSignals();
        preprocessGazeSignals();
        preprocessGestureSignals();
        preprocessHeadSignals();
        preprocessTorsoSignals();
    }

    protected void processSignals(List<GestureSignal> restPoses) {
        processFaceSignals(restPoses);
        processGazeSignals(restPoses);
        processGestureSignals(restPoses);
        processHeadSignals(restPoses);
        processTorsoSignals(restPoses);
    }

    /* ---------------------------------------------------------------------- */
    private void preprocessFaceSignals() {
        List<FaceSignal> faceSignals = getFaceSignals();
        if (faceSignals != null) {
            // ...
        }
    }

    /* -------------------------------------------------- */
    private void preprocessGazeSignals() {
        List<GazeSignal> gazeSignals = getGazeSignals();
        if (gazeSignals != null) {
            // ...
        }
    }

    /* -------------------------------------------------- */
    private void preprocessGestureSignals() {

        List<GestureSignal> gestureSignals = getGestureSignals();

        if (gestureSignals != null) {

            if (mainGestureSide == Side.LEFT || mainGestureSide == Side.RIGHT) {
                for (GestureSignal gesture : gestureSignals) {
                    Pattern p = Pattern.compile("^(.+)=(.+)_(L|R|L(_.+)|R(_.+))$");
                    Matcher m = p.matcher(gesture.getReference());
                    if (m.matches()) {
                        String firstPartOfGestureId = m.group(2);
                        String lastPartOfGestureId = (m.group(4) != null) ? m.group(4) : ((m.group(5) != null) ? m.group(5) : "");

                        String overridenGestureCategory = m.group(1);
                        String overridenGestureId = firstPartOfGestureId + (mainGestureSide == Side.LEFT ? "_L" : "_R") + lastPartOfGestureId;
                        String overridenGestureReference = overridenGestureCategory + "=" + overridenGestureId;

                        boolean overridenGestureExists = (Gestuary.global_gestuary.getSignal(overridenGestureReference) != null);
                        if (!overridenGestureExists) {
                            GestureSignal originalGesture = Gestuary.global_gestuary.getSignal(gesture.getReference());
                            GestureSignal mirrorGesture = originalGesture.generateMirrorGesture(overridenGestureId);
                            mirrorGesture.setReference(overridenGestureReference);
                            Gestuary.global_gestuary.getCurrentDefinition().addParameter(new SignalEntry<GestureSignal>(overridenGestureReference, mirrorGesture));
                        }
                        gesture.setReference(overridenGestureReference);
                    }
                }
            } else if (mainGestureSide == Side.BOTH) {
                for (GestureSignal gesture : gestureSignals) {
                    Pattern p = Pattern.compile("^(.+)=(.+)_(L|R|L(_.+)|R(_.+))$");
                    Matcher m = p.matcher(gesture.getReference());
                    if (m.matches()) {
                        String firstPartOfGestureId = m.group(2);
                        String lastPartOfGestureId = (m.group(4) != null) ? m.group(4) : ((m.group(5) != null) ? m.group(5) : "");

                        String overridenGestureCategory = m.group(1);
                        String overridenGestureId = firstPartOfGestureId + "_B" + lastPartOfGestureId;
                        String overridenGestureReference = overridenGestureCategory + "=" + overridenGestureId;

                        boolean overridenGestureExists = (Gestuary.global_gestuary.getSignal(overridenGestureReference) != null);
                        if (!overridenGestureExists) {

                            GestureSignal originalGesture = Gestuary.global_gestuary.getSignal(gesture.getReference());
                            GestureSignal mirrorGesture = originalGesture.generateMirrorGesture(gesture.getReference() + "_Mirror");

                            GestureSignal bothHandsGesture = new GestureSignal(overridenGestureId);
                            bothHandsGesture.setCategory(overridenGestureCategory);

                            for (int i = 0; i < originalGesture.getPhases().size(); ++i) {

                                GesturePose originalGestureCurrentPose = originalGesture.getPhases().get(i);
                                GesturePose mirrorGestureCurrentPose = mirrorGesture.getPhases().get(i);

                                Hand originalGestureCurrentPoseLeftHand = originalGestureCurrentPose.getLeftHand();
                                Hand originalGestureCurrentPoseRightHand = originalGestureCurrentPose.getRightHand();

                                Hand mirrorGestureCurrentPoseLeftHand = mirrorGestureCurrentPose.getLeftHand();
                                Hand mirrorGestureCurrentPoseRightHand = mirrorGestureCurrentPose.getRightHand();

                                Hand bothHandsGestureCurrentPoseLeftHand = null;
                                Hand bothHandsGestureCurrentPoseRightHand = null;

                                if ((originalGestureCurrentPoseLeftHand != null) && (originalGestureCurrentPoseRightHand != null)) {
                                    bothHandsGestureCurrentPoseLeftHand = new Hand(originalGestureCurrentPoseLeftHand);
                                    bothHandsGestureCurrentPoseRightHand = new Hand(originalGestureCurrentPoseRightHand);
                                } else if (originalGestureCurrentPoseLeftHand != null) {
                                    bothHandsGestureCurrentPoseLeftHand = new Hand(originalGestureCurrentPoseLeftHand);
                                    bothHandsGestureCurrentPoseRightHand = new Hand(mirrorGestureCurrentPoseRightHand);
                                } else if (originalGestureCurrentPoseRightHand != null) {
                                    bothHandsGestureCurrentPoseRightHand = new Hand(originalGestureCurrentPoseRightHand);
                                    bothHandsGestureCurrentPoseLeftHand = new Hand(mirrorGestureCurrentPoseLeftHand);
                                }

                                bothHandsGesture.addPhase(new GesturePose(bothHandsGestureCurrentPoseLeftHand, bothHandsGestureCurrentPoseRightHand));
                            }

                            bothHandsGesture.setReference(overridenGestureReference);
                            Gestuary.global_gestuary.getCurrentDefinition().addParameter(new SignalEntry<GestureSignal>(overridenGestureReference, bothHandsGesture));
                        }
                        gesture.setReference(overridenGestureReference);
                    }
                }
            }
        }
    }

    /* -------------------------------------------------- */
    private void preprocessHeadSignals() {
        List<HeadSignal> headSignals = getHeadSignals();
        if (headSignals != null) {
            // ...
        }
    }

    /* -------------------------------------------------- */
    private void preprocessTorsoSignals() {
        List<TorsoSignal> torsoSignal = getTorsoSignals();
        if (torsoSignal != null) {
            // ...
        }
    }

    /* ---------------------------------------------------------------------- */
    private void processFaceSignals(List<GestureSignal> restPoses) {
        List<FaceSignal> faceSignals = getFaceSignals();
        if (faceSignals != null) {
            // ...
        }
    }

    /* -------------------------------------------------- */
    private void processGazeSignals(List<GestureSignal> restPoses) {
        List<GazeSignal> gazeSignals = getGazeSignals();
        if (gazeSignals != null) {
            // ...
        }
    }

    /* -------------------------------------------------- */
    private void processGestureSignals(List<GestureSignal> restPoses) {

        List<GestureSignal> gestureSignals = getGestureSignals();

        if (gestureSignals != null) {

            cleanupGestureSignals(gestureSignals);

            initRelaxPoses(gestureSignals);

            for (GestureSignal currentGesture : gestureSignals) {
                initPropagationOfImportantParameters(currentGesture, mainGesture);
                propagateImportantParameters(currentGesture);
            }

            for (GestureSignal currentGesture : gestureSignals) {

                int currentGestureIndex = gestureSignals.indexOf(currentGesture);
                if (currentGestureIndex > 0) {

                    GestureSignal previousGesture = gestureSignals.get(currentGestureIndex - 1);

                    expandOrContractPreviousGestureRelaxPose(currentGesture, previousGesture);
                    setupRelaxPoseBetweenGestures(currentGesture, previousGesture);

                    propagateCurrentModificationsIfRepeatedGestures(currentGesture, previousGesture);

                    increaseSpeedIfRepeatedGestures(currentGesture, previousGesture);
                    increaseAmplitudeIfRepeatedGestures(currentGesture, previousGesture);
                    retractALittleIfRepeatedGestures(currentGesture, previousGesture);

                    propagateImportantParameters(currentGesture, previousGesture);

                    fixStartRestPose(currentGesture, previousGesture, restPoses);

                }
            }
        }
    }

    /* ------------------------------ */
    private void cleanupGestureSignals(List<GestureSignal> gestureSignals) {

        List<GestureSignal> gestureSignalsCopy = new ArrayList<GestureSignal>(gestureSignals);
        for (GestureSignal currentGesture : gestureSignalsCopy) {
            if (currentGesture.getPhases().isEmpty()) {
                currentGesture.setIdeationalUnit(null);
                removeSignal(currentGesture);
            }
        }


        //BRIAN : JE NE COMPRENDS PAS L4INTERET DU CODE CI-DESSOUS ALORS JE l'AI MIS EN COMMENTAIRE
        //LE CHANGEMENT DANS LE TIMEMARKER END DU DERNIER GESTE M4EMPECHE DE LE PLACER CORRECTEMENT
        //A DISCUTER AVEC BRICE
        /*if (!gestureSignals.isEmpty()) {
            GestureSignal firstGesture = gestureSignals.get(0);
            double start = firstGesture.getStart().getValue();
            double ready = firstGesture.getTimeMarker("ready").getValue();
            double strokeStart = firstGesture.getTimeMarker("stroke-start").getValue();
            firstGesture.getTimeMarker("ready").setValue(strokeStart);
            firstGesture.getStart().setValue(strokeStart - (ready - start));

            GestureSignal lastGesture = gestureSignals.get(gestureSignals.size() - 1);
            double end = lastGesture.getEnd().getValue();
            double relax = lastGesture.getTimeMarker("relax").getValue();
            double strokeEnd = lastGesture.getTimeMarker("stroke-end").getValue();
            lastGesture.getTimeMarker("relax").setValue(strokeEnd);
            lastGesture.getEnd().setValue(strokeEnd + (end - relax));
        }*/
    }

    private void initRelaxPoses(List<GestureSignal> gestureSignals) {

        for (GestureSignal currentGesture : gestureSignals) {

            List<GesturePose> currentGesturePoses = currentGesture.getPhases();
            GesturePose currentGestureLastPose = currentGesturePoses.get(currentGesturePoses.size() - 1);

            Hand currentGestureLastPoseLeftHand = currentGestureLastPose.getLeftHand();
            Hand currentGestureLastPoseRightHand = currentGestureLastPose.getRightHand();

            GesturePose currentGestureRelaxPose = new GesturePose(currentGesture.getEndRestPose());
            if (currentGestureLastPoseLeftHand != null) {
                currentGestureRelaxPose.setLeftHand(new Hand(currentGestureLastPoseLeftHand));
                currentGestureRelaxPose.getLeftHand().setHandShape("relax");
            }
            if (currentGestureLastPoseRightHand != null) {
                currentGestureRelaxPose.setRightHand(new Hand(currentGestureLastPoseRightHand));
                currentGestureRelaxPose.getRightHand().setHandShape("relax");
            }
            currentGesture.setRelaxPose(currentGestureRelaxPose);
        }
    }

    private void expandOrContractPreviousGestureRelaxPose(GestureSignal currentGesture, GestureSignal previousGesture) {

        if (!currentGesture.getReference().equals(previousGesture.getReference())) {

            GesturePose previousGestureRelaxPose = previousGesture.getRelaxPose();

            Hand previousGestureRelaxPoseLeftHand = previousGestureRelaxPose.getLeftHand();
            Hand previousGestureRelaxPoseRightHand = previousGestureRelaxPose.getRightHand();

            if ((previousGestureRelaxPoseLeftHand != null) && (previousGestureRelaxPoseRightHand != null)) {

                Position previousGestureRelaxPoseLeftHandPosition = previousGestureRelaxPoseLeftHand.getPosition();
                Position previousGestureRelaxPoseRightHandPosition = previousGestureRelaxPoseRightHand.getPosition();

                if ((previousGestureRelaxPoseLeftHandPosition != null) && (previousGestureRelaxPoseRightHandPosition != null)) {

                    double dx = previousGestureRelaxPoseLeftHandPosition.getX() + previousGestureRelaxPoseRightHandPosition.getX();
                    double dy = previousGestureRelaxPoseLeftHandPosition.getY() - previousGestureRelaxPoseRightHandPosition.getY();
                    double dz = previousGestureRelaxPoseLeftHandPosition.getZ() - previousGestureRelaxPoseRightHandPosition.getZ();

                    double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

                    // If the two hands are too close, the hand will separate one from another during the relaxPose
                    // TODO : calculs ci-dessous ??
                    if (distance <= 0.4
                            && previousGestureRelaxPoseLeftHandPosition.getX() <= 0.2
                            && previousGestureRelaxPoseRightHandPosition.getX() <= 0.2) {

                        previousGestureRelaxPoseLeftHandPosition.setX(previousGestureRelaxPoseLeftHandPosition.getX() * 1.15);
                        previousGestureRelaxPoseLeftHandPosition.setY(previousGestureRelaxPoseLeftHandPosition.getY() * 1.15);
                        previousGestureRelaxPoseLeftHandPosition.setZ(previousGestureRelaxPoseLeftHandPosition.getZ() * 1.15);

                        previousGestureRelaxPoseRightHandPosition.setX(previousGestureRelaxPoseRightHandPosition.getX() * 1.15);
                        previousGestureRelaxPoseRightHandPosition.setY(previousGestureRelaxPoseRightHandPosition.getY() * 1.15);
                        previousGestureRelaxPoseRightHandPosition.setZ(previousGestureRelaxPoseRightHandPosition.getZ() * 1.15);

                    } else {

                        // RelaxPose will be calculate from the position of the last pose of the stroke.
                        // Different thresholds are defined. Each threshold has its coefficient <1 to reduce the hand's amplitude
                        double XEP = SymbolicPosition.horizontalPositions.get("XEP");
                        double XP = SymbolicPosition.horizontalPositions.get("XP");
                        double XC = SymbolicPosition.horizontalPositions.get("XC");
                        double XCC = SymbolicPosition.horizontalPositions.get("XCC");
                        double XOppC = SymbolicPosition.horizontalPositions.get("XOppC");

                        if (previousGestureRelaxPoseLeftHandPosition.getX() >= XEP) { // X >= XEP
                            previousGestureRelaxPoseLeftHandPosition.setX(previousGestureRelaxPoseLeftHandPosition.getX() * 0.5);
                        } else if (previousGestureRelaxPoseLeftHandPosition.getX() >= XP) { // X >= XP
                            previousGestureRelaxPoseLeftHandPosition.setX(previousGestureRelaxPoseLeftHandPosition.getX() * 0.7);
                        } else if (previousGestureRelaxPoseLeftHandPosition.getX() >= XC) { // X >= XC
                            previousGestureRelaxPoseLeftHandPosition.setX(previousGestureRelaxPoseLeftHandPosition.getX() * 0.9);
                        } else if (previousGestureRelaxPoseLeftHandPosition.getX() <= XOppC) { // X <= XOppC
                            previousGestureRelaxPoseLeftHandPosition.setX(previousGestureRelaxPoseLeftHandPosition.getX() * 0.8);
                        }

                        if (previousGestureRelaxPoseRightHandPosition.getX() >= XEP) { // X >= XEP
                            previousGestureRelaxPoseRightHandPosition.setX(previousGestureRelaxPoseRightHandPosition.getX() * 0.5);
                        } else if (previousGestureRelaxPoseRightHandPosition.getX() >= XP) { // X >= XP
                            previousGestureRelaxPoseRightHandPosition.setX(previousGestureRelaxPoseRightHandPosition.getX() * 0.7);
                        } else if (previousGestureRelaxPoseRightHandPosition.getX() >= XC) { // X >= XC
                            previousGestureRelaxPoseRightHandPosition.setX(previousGestureRelaxPoseRightHandPosition.getX() * 0.9);
                        } else if (previousGestureRelaxPoseRightHandPosition.getX() <= XOppC) { // X <= XOppC
                            previousGestureRelaxPoseRightHandPosition.setX(previousGestureRelaxPoseRightHandPosition.getX() * 0.8);
                        }

                        double YUpperEP = SymbolicPosition.verticalPositions.get("YUpperEP");
                        double YUpperP = SymbolicPosition.verticalPositions.get("YUpperP");
                        double YUpperC = SymbolicPosition.verticalPositions.get("YUpperC");
                        double YCC = SymbolicPosition.verticalPositions.get("YCC");
                        double YLowerC = SymbolicPosition.verticalPositions.get("YLowerC");
                        double YLowerP = SymbolicPosition.verticalPositions.get("YLowerP");
                        double YLowerEP = SymbolicPosition.verticalPositions.get("YLowerEP");

                        if (previousGestureRelaxPoseLeftHandPosition.getY() >= YUpperP) { // Y >= YUpperP && YUpperEP
                            previousGestureRelaxPoseLeftHandPosition.setY(previousGestureRelaxPoseLeftHandPosition.getY() * 0.3);
                        } else if (previousGestureRelaxPoseLeftHandPosition.getY() >= YUpperC) { // Y >= YUpperC
                            previousGestureRelaxPoseLeftHandPosition.setY(previousGestureRelaxPoseLeftHandPosition.getY() * 0.5);
                        } else if (previousGestureRelaxPoseLeftHandPosition.getY() >= YCC) { // Y >= YCC
                            previousGestureRelaxPoseLeftHandPosition.setY(previousGestureRelaxPoseLeftHandPosition.getY() * 0.7);
                        } else if (previousGestureRelaxPoseLeftHandPosition.getY() <= YLowerP) { // Y <= YLowerP
                            previousGestureRelaxPoseLeftHandPosition.setY(previousGestureRelaxPoseLeftHandPosition.getY() * 0.8);
                        } else if (previousGestureRelaxPoseLeftHandPosition.getY() <= YLowerEP) { // Y <= YLowerEP
                            previousGestureRelaxPoseLeftHandPosition.setY(previousGestureRelaxPoseLeftHandPosition.getY() * 0.9);
                        }

                        if (previousGestureRelaxPoseRightHandPosition.getY() >= YUpperP) { // Y >= YUpperP && YUpperEP
                            previousGestureRelaxPoseRightHandPosition.setY(previousGestureRelaxPoseRightHandPosition.getY() * 0.3);
                        } else if (previousGestureRelaxPoseRightHandPosition.getY() >= YUpperC) { // Y >= YUpperC
                            previousGestureRelaxPoseRightHandPosition.setY(previousGestureRelaxPoseRightHandPosition.getY() * 0.5);
                        } else if (previousGestureRelaxPoseRightHandPosition.getY() >= YCC) { // Y >= YCC
                            previousGestureRelaxPoseRightHandPosition.setY(previousGestureRelaxPoseRightHandPosition.getY() * 0.7);
                        } else if (previousGestureRelaxPoseRightHandPosition.getY() <= YLowerP) { // Y <= YLowerP
                            previousGestureRelaxPoseRightHandPosition.setY(previousGestureRelaxPoseRightHandPosition.getY() * 0.8);
                        } else if (previousGestureRelaxPoseRightHandPosition.getY() <= YLowerEP) { // Y <= YLowerEP
                            previousGestureRelaxPoseRightHandPosition.setY(previousGestureRelaxPoseRightHandPosition.getY() * 0.9);
                        }

                        double ZFar = SymbolicPosition.frontalPositions.get("ZFar");
                        double ZMiddle = SymbolicPosition.frontalPositions.get("ZMiddle");
                        double ZNear = SymbolicPosition.frontalPositions.get("ZNear");

                        if (Math.abs(previousGestureRelaxPoseLeftHandPosition.getZ()) >= ZFar) {// Z >= ZFar
                            previousGestureRelaxPoseLeftHandPosition.setZ(previousGestureRelaxPoseLeftHandPosition.getZ() * 0.5);
                        } else if (Math.abs(previousGestureRelaxPoseLeftHandPosition.getZ()) >= ZMiddle) {// Z >= ZMiddle
                            previousGestureRelaxPoseLeftHandPosition.setZ(previousGestureRelaxPoseLeftHandPosition.getZ() * 0.75);
                        } else if (Math.abs(previousGestureRelaxPoseLeftHandPosition.getZ()) >= ZNear) {// Z >= ZNear
                            previousGestureRelaxPoseLeftHandPosition.setZ(previousGestureRelaxPoseLeftHandPosition.getZ() * 0.9);
                        }

                        if (Math.abs(previousGestureRelaxPoseRightHandPosition.getZ()) >= ZFar) { // Z >= ZFar
                            previousGestureRelaxPoseRightHandPosition.setZ(previousGestureRelaxPoseRightHandPosition.getZ() * 0.5);
                        } else if (Math.abs(previousGestureRelaxPoseRightHandPosition.getZ()) >= ZMiddle) { // Z >= ZMiddle
                            previousGestureRelaxPoseRightHandPosition.setZ(previousGestureRelaxPoseRightHandPosition.getZ() * 0.75);
                        } else if (Math.abs(previousGestureRelaxPoseRightHandPosition.getZ()) >= ZNear) { // Z >= ZNear
                            previousGestureRelaxPoseRightHandPosition.setZ(previousGestureRelaxPoseRightHandPosition.getZ() * 0.9);
                        }
                    }
                }
            }
        }
    }

    private void setupRelaxPoseBetweenGestures(GestureSignal currentGesture, GestureSignal previousGesture) {

        // Add relaxPose to coarticulation, if there is enough time
        // TODO : calcul ci-dessous ??
        if (currentGesture.getEnd().getValue() - previousGesture.getStart().getValue() < 10) {
            GesturePose previousGestureRelaxPose = previousGesture.getRelaxPose();
            previousGesture.setEndRestPose(previousGestureRelaxPose);
            currentGesture.setStartRestPose(previousGestureRelaxPose);
        }
    }

    private void propagateCurrentModificationsIfRepeatedGestures(GestureSignal currentGesture, GestureSignal previousGesture) {

        if (currentGesture.getReference().equals(previousGesture.getReference())) {

            // Impose previous modifications from ideational Unit to the repeated gesture
            for (GesturePose currentGestureCurrentPose : currentGesture.getPhases()) {

                int currentGestureCurrentPoseIndex = currentGesture.getPhases().indexOf(currentGestureCurrentPose);

                GesturePose previousGestureCurrentPose = previousGesture.getPhases().get(currentGestureCurrentPoseIndex);

                Hand currentGestureCurrentPoseLeftHand = currentGestureCurrentPose.getLeftHand();
                Hand previousGestureCurrentPoseLeftHand = previousGestureCurrentPose.getLeftHand();

                if ((currentGestureCurrentPoseLeftHand != null) && (previousGestureCurrentPoseLeftHand != null)) {

                    Position currentGestureCurrentPoseLeftHandPosition = currentGestureCurrentPoseLeftHand.getPosition();
                    Position previousGestureCurrentPoseLeftHandPosition = previousGestureCurrentPoseLeftHand.getPosition();

                    if ((currentGestureCurrentPoseLeftHandPosition != null) && (previousGestureCurrentPoseLeftHandPosition != null)) {
                        currentGestureCurrentPoseLeftHandPosition.setX(previousGestureCurrentPoseLeftHandPosition.getX());
                        currentGestureCurrentPoseLeftHandPosition.setY(previousGestureCurrentPoseLeftHandPosition.getY());
                        currentGestureCurrentPoseLeftHandPosition.setZ(previousGestureCurrentPoseLeftHandPosition.getZ());
                    }

                    Quaternion previousGestureCurrentPoseLeftHandWristOrientation = previousGestureCurrentPoseLeftHand.getWristOrientation();
                    if (previousGestureCurrentPoseLeftHandWristOrientation != null) {
                        currentGestureCurrentPoseLeftHand.setWristOrientation(new Quaternion(previousGestureCurrentPoseLeftHandWristOrientation));
                    }

                    currentGestureCurrentPoseLeftHand.setHandShape(previousGestureCurrentPoseLeftHand.getHandShape());
                    currentGestureCurrentPoseLeftHand.setOpenness(previousGestureCurrentPoseLeftHand.getOpenness());
                }

                Hand currentGestureCurrentPoseRightHand = currentGestureCurrentPose.getRightHand();
                Hand previousGestureCurrentPoseRightHand = previousGestureCurrentPose.getRightHand();

                if ((currentGestureCurrentPoseRightHand != null) && (previousGestureCurrentPoseRightHand != null)) {

                    Position currentGestureCurrentPoseRightHandPosition = currentGestureCurrentPoseRightHand.getPosition();
                    Position previousGestureCurrentPoseRightHandPosition = previousGestureCurrentPoseRightHand.getPosition();

                    if ((currentGestureCurrentPoseRightHandPosition != null) && (previousGestureCurrentPoseRightHandPosition != null)) {
                        currentGestureCurrentPoseRightHandPosition.setX(previousGestureCurrentPoseRightHandPosition.getX());
                        currentGestureCurrentPoseRightHandPosition.setY(previousGestureCurrentPoseRightHandPosition.getY());
                        currentGestureCurrentPoseRightHandPosition.setZ(previousGestureCurrentPoseRightHandPosition.getZ());
                    }

                    Quaternion previousGestureCurrentPoseRightHandWristOrientation = previousGestureCurrentPoseRightHand.getWristOrientation();
                    if (previousGestureCurrentPoseRightHandWristOrientation != null) {
                        currentGestureCurrentPoseRightHand.setWristOrientation(new Quaternion(previousGestureCurrentPoseRightHandWristOrientation));
                    }

                    currentGestureCurrentPoseRightHand.setHandShape(previousGestureCurrentPoseRightHand.getHandShape());
                    currentGestureCurrentPoseRightHand.setOpenness(previousGestureCurrentPoseRightHand.getOpenness());
                }
            }
        }
    }

    private void increaseSpeedIfRepeatedGestures(GestureSignal currentGesture, GestureSignal previousGesture) {

        if (currentGesture.getReference().equals(previousGesture.getReference())) {

            double previousGesturePWR = previousGesture.getPWR();

            if (previousGesturePWR == 0) {
                currentGesture.setPWR(0.3);
            } else {
                currentGesture.setPWR(previousGesturePWR * 2);
                if (currentGesture.getPWR() > 1) {
                    currentGesture.setPWR(1);
                }
            }
        }
    }

    private void retractALittleIfRepeatedGestures(GestureSignal currentGesture, GestureSignal previousGesture) {

        // Small retraction : the position of the first pose of the stroke of the repeated gesture will be equal to the mean of the positions of the previous gesture
        if (currentGesture.getReference().equals(previousGesture.getReference())) {

            GesturePose currentGestureFirstPose = currentGesture.getPhases().get(0);
            GesturePose currentGestureSecondPose = currentGesture.getPhases().get(1);

            if ((currentGestureFirstPose != null) && (currentGestureSecondPose != null)) {

                Hand currentGestureFirstPoseLeftHand = currentGestureFirstPose.getLeftHand();
                Hand currentGestureSecondPoseLeftHand = currentGestureSecondPose.getLeftHand();

                if ((currentGestureFirstPoseLeftHand != null) && (currentGestureSecondPoseLeftHand != null)) {

                    Position currentGestureFirstPoseLeftHandPosition = currentGestureFirstPoseLeftHand.getPosition();
                    Position currentGestureSecondPoseLeftHandPosition = currentGestureSecondPoseLeftHand.getPosition();

                    if ((currentGestureFirstPoseLeftHandPosition != null) && (currentGestureSecondPoseLeftHandPosition != null)) {
                        currentGestureFirstPoseLeftHandPosition.setX((currentGestureFirstPoseLeftHandPosition.getX() + currentGestureSecondPoseLeftHandPosition.getX()) / 2);
                        currentGestureFirstPoseLeftHandPosition.setY((currentGestureFirstPoseLeftHandPosition.getY() + currentGestureSecondPoseLeftHandPosition.getY()) / 2);
                        currentGestureFirstPoseLeftHandPosition.setZ((currentGestureFirstPoseLeftHandPosition.getZ() + currentGestureSecondPoseLeftHandPosition.getZ()) / 2);
                    }
                }

                Hand currentGestureFirstPoseRightHand = currentGestureFirstPose.getRightHand();
                Hand currentGestureSecondPoseRightHand = currentGestureSecondPose.getRightHand();

                if ((currentGestureFirstPoseRightHand != null) && (currentGestureSecondPoseRightHand != null)) {

                    Position currentGestureFirstPoseRightHandPosition = currentGestureFirstPoseRightHand.getPosition();
                    Position currentGestureSecondPoseRightHandPosition = currentGestureSecondPoseRightHand.getPosition();

                    if ((currentGestureFirstPoseRightHandPosition != null) && (currentGestureSecondPoseRightHandPosition != null)) {
                        currentGestureFirstPoseRightHandPosition.setX((currentGestureFirstPoseRightHandPosition.getX() + currentGestureSecondPoseRightHandPosition.getX()) / 2);
                        currentGestureFirstPoseRightHandPosition.setY((currentGestureFirstPoseRightHandPosition.getY() + currentGestureSecondPoseRightHandPosition.getY()) / 2);
                        currentGestureFirstPoseRightHandPosition.setZ((currentGestureFirstPoseRightHandPosition.getZ() + currentGestureSecondPoseRightHandPosition.getZ()) / 2);
                    }
                }
            }
        }
    }

    private void increaseAmplitudeIfRepeatedGestures(GestureSignal currentGesture, GestureSignal previousGesture) {

        if (currentGesture.getReference().equals(previousGesture.getReference())) {

            GesturePose currentGestureLastPose = currentGesture.getPhases().get(currentGesture.getPhases().size() - 1);
            GesturePose previousGestureLastPose = previousGesture.getPhases().get(previousGesture.getPhases().size() - 1);

            Hand currentGestureLastPoseLeftHand = currentGestureLastPose.getLeftHand();
            Hand previousGestureLastPoseLeftHand = previousGestureLastPose.getLeftHand();

            if ((currentGestureLastPoseLeftHand != null) && (previousGestureLastPoseLeftHand != null)) {

                Position currentGestureLastPoseLeftHandPosition = currentGestureLastPoseLeftHand.getPosition();
                Position previousGestureLastPoseLeftHandPosition = previousGestureLastPoseLeftHand.getPosition();

                if ((currentGestureLastPoseLeftHandPosition != null) && (previousGestureLastPoseLeftHandPosition != null)) {

                    currentGestureLastPoseLeftHandPosition.setX(previousGestureLastPoseLeftHandPosition.getX() * 1.17);
                    if (currentGestureLastPoseLeftHandPosition.getX() > 1) {
                        currentGestureLastPoseLeftHandPosition.setX(1);
                    }
                    if (currentGestureLastPoseLeftHandPosition.getX() < -1) {
                        currentGestureLastPoseLeftHandPosition.setX(-1);
                    }
                    // If X<0, adapt openness to the modified position
                    if (currentGestureLastPoseLeftHandPosition.getX() < 0) {
                        currentGestureLastPoseLeftHand.setOpenness(currentGestureLastPoseLeftHand.getOpenness() * 0.7);
                    }

                    currentGestureLastPoseLeftHandPosition.setY(previousGestureLastPoseLeftHandPosition.getY() * 1.17);
                    if (currentGestureLastPoseLeftHandPosition.getY() > 1) {
                        currentGestureLastPoseLeftHandPosition.setY(1);
                    }
                    if (currentGestureLastPoseLeftHandPosition.getY() < -1) {
                        currentGestureLastPoseLeftHandPosition.setY(-1);
                    }

                    currentGestureLastPoseLeftHandPosition.setZ(previousGestureLastPoseLeftHandPosition.getZ() * 1.17);
                    if (currentGestureLastPoseLeftHandPosition.getZ() > 1) {
                        currentGestureLastPoseLeftHandPosition.setZ(1);
                    }
                    if (currentGestureLastPoseLeftHandPosition.getZ() < -1) {
                        currentGestureLastPoseLeftHandPosition.setZ(-1);
                    }
                }
            }

            Hand currentGestureLastPoseRightHand = currentGestureLastPose.getRightHand();
            Hand previousGestureLastPoseRightHand = previousGestureLastPose.getRightHand();

            if ((currentGestureLastPoseRightHand != null) && (previousGestureLastPoseRightHand != null)) {

                Position currentGestureLastPoseRightHandPosition = currentGestureLastPoseRightHand.getPosition();
                Position previousGestureLastPoseRightHandPosition = previousGestureLastPoseRightHand.getPosition();

                if ((currentGestureLastPoseRightHandPosition != null) && (previousGestureLastPoseRightHandPosition != null)) {

                    currentGestureLastPoseRightHandPosition.setX(previousGestureLastPoseRightHandPosition.getX() * 1.17);
                    if (currentGestureLastPoseRightHandPosition.getX() > 1) {
                        currentGestureLastPoseRightHandPosition.setX(1);
                    }
                    if (currentGestureLastPoseRightHandPosition.getX() < -1) {
                        currentGestureLastPoseRightHandPosition.setX(-1);
                    }
                    // If X<0, adapt openness to the modified position
                    if (currentGestureLastPoseRightHandPosition.getX() < 0) {
                        currentGestureLastPoseRightHand.setOpenness(currentGestureLastPoseRightHand.getOpenness() * 0.7);
                    }

                    currentGestureLastPoseRightHandPosition.setY(previousGestureLastPoseRightHandPosition.getY() * 1.17);
                    if (currentGestureLastPoseRightHandPosition.getY() > 1) {
                        currentGestureLastPoseRightHandPosition.setY(1);
                    }
                    if (currentGestureLastPoseRightHandPosition.getY() < -1) {
                        currentGestureLastPoseRightHandPosition.setY(-1);
                    }

                    currentGestureLastPoseRightHandPosition.setZ(previousGestureLastPoseRightHandPosition.getZ() * 1.17);
                    if (currentGestureLastPoseRightHandPosition.getZ() > 1) {
                        currentGestureLastPoseRightHandPosition.setZ(1);
                    }
                    if (currentGestureLastPoseRightHandPosition.getZ() < -1) {
                        currentGestureLastPoseRightHandPosition.setZ(-1);
                    }
                }
            }
        }
    }

    private void initPropagationOfImportantParameters(GestureSignal currentGesture, GestureSignal referenceGesture) {
        //Brian: I had to add a test for non null because of exceptions. I have no idea how this code works and what it does.
        if (currentGesture != null && referenceGesture != null && !currentGesture.getReference().equals(referenceGesture.getReference())) {

            GesturePose currentGestureFirstPose = currentGesture.getPhases().get(0);
            GesturePose referenceGestureRelaxPose = referenceGesture.getRelaxPose();

            Hand currentGestureFirstPoseLeftHand = currentGestureFirstPose.getLeftHand();
            Hand referenceGestureRelaxPoseLeftHand = referenceGestureRelaxPose.getLeftHand();

            if ((currentGestureFirstPoseLeftHand != null) && (referenceGestureRelaxPoseLeftHand != null) && (mainGestureSide == Side.LEFT ||mainGestureSide == Side.BOTH)) {

                Position currentGestureFirstPoseLeftHandPosition = currentGestureFirstPoseLeftHand.getPosition();
                Position referenceGestureRelaxPoseLeftHandPosition = referenceGestureRelaxPoseLeftHand.getPosition();

                if ((currentGestureFirstPoseLeftHandPosition != null) && (referenceGestureRelaxPoseLeftHandPosition != null)) {

                    if ((currentGestureFirstPoseLeftHandPosition instanceof UniformPosition) && (referenceGestureRelaxPoseLeftHandPosition instanceof UniformPosition)) {

                        UniformPosition currentGestureFirstPoseLeftHandUniformPosition = (UniformPosition) currentGestureFirstPoseLeftHandPosition;
                        if (currentGestureFirstPoseLeftHandUniformPosition.isXOverridable()) {
                            currentGestureFirstPoseLeftHandUniformPosition.setX(referenceGestureRelaxPoseLeftHandPosition.getX());
                            currentGestureFirstPoseLeftHand.setOpenness(referenceGestureRelaxPoseLeftHand.getOpenness());
                        }
                        if (currentGestureFirstPoseLeftHandUniformPosition.isYOverridable()) {
                            currentGestureFirstPoseLeftHand.getPosition().setY(referenceGestureRelaxPoseLeftHandPosition.getY());
                        }
                        if (currentGestureFirstPoseLeftHandUniformPosition.isZOverridable()) {
                            currentGestureFirstPoseLeftHand.getPosition().setZ(referenceGestureRelaxPoseLeftHandPosition.getZ());
                        }
                    }
                }

                Quaternion referenceGestureRelaxPoseLeftHandWristOrientation = referenceGestureRelaxPoseLeftHand.getWristOrientation();
                if (referenceGestureRelaxPoseLeftHandWristOrientation != null) {
                    if (currentGestureFirstPoseLeftHand.isWristOrientationOverridable()) {
                        currentGestureFirstPoseLeftHand.setWristOrientation(new Quaternion(referenceGestureRelaxPoseLeftHandWristOrientation));
                    }
                }

                GesturePose referenceGestureLastPose = referenceGesture.getPhases().get(referenceGesture.getPhases().size() - 1);
                Hand referenceGestureLastPoseLeftHand = referenceGestureLastPose.getLeftHand();
                if (referenceGestureLastPoseLeftHand != null) {
                    if (currentGestureFirstPoseLeftHand.isHandShapeOverridable()) {
                        currentGestureFirstPoseLeftHand.setHandShape(referenceGestureLastPoseLeftHand.getHandShape());
                    }
                }
            }

            Hand currentGestureFirstPoseRightHand = currentGestureFirstPose.getRightHand();
            Hand referenceGestureRelaxPoseRightHand = referenceGestureRelaxPose.getRightHand();

            if ((currentGestureFirstPoseRightHand != null) && (referenceGestureRelaxPoseRightHand != null) && (mainGestureSide == Side.RIGHT ||mainGestureSide == Side.BOTH)) {

                Position currentGestureFirstPoseRightHandPosition = currentGestureFirstPoseRightHand.getPosition();
                Position referenceGestureRelaxPoseRightHandPosition = referenceGestureRelaxPoseRightHand.getPosition();

                if ((currentGestureFirstPoseRightHandPosition != null) && (referenceGestureRelaxPoseRightHandPosition != null)) {

                    if ((currentGestureFirstPoseRightHandPosition instanceof UniformPosition) && (referenceGestureRelaxPoseRightHandPosition instanceof UniformPosition)) {

                        UniformPosition currentGestureFirstPoseRightHandUniformPosition = (UniformPosition) currentGestureFirstPoseRightHandPosition;
                        if (currentGestureFirstPoseRightHandUniformPosition.isXOverridable()) {
                            currentGestureFirstPoseRightHandUniformPosition.setX(referenceGestureRelaxPoseRightHandPosition.getX());
                            currentGestureFirstPoseRightHand.setOpenness(referenceGestureRelaxPoseRightHand.getOpenness());
                        }
                        if (currentGestureFirstPoseRightHandUniformPosition.isYOverridable()) {
                            currentGestureFirstPoseRightHand.getPosition().setY(referenceGestureRelaxPoseRightHandPosition.getY());
                        }
                        if (currentGestureFirstPoseRightHandUniformPosition.isZOverridable()) {
                            currentGestureFirstPoseRightHand.getPosition().setZ(referenceGestureRelaxPoseRightHandPosition.getZ());
                        }
                    }
                }

                Quaternion referenceGestureRelaxPoseRightHandWristOrientation = referenceGestureRelaxPoseRightHand.getWristOrientation();
                if (referenceGestureRelaxPoseRightHandWristOrientation != null) {
                    if (currentGestureFirstPoseRightHand.isWristOrientationOverridable()) {
                        currentGestureFirstPoseRightHand.setWristOrientation(new Quaternion(referenceGestureRelaxPoseRightHandWristOrientation));
                    }
                }

                GesturePose referenceGestureLastPose = referenceGesture.getPhases().get(referenceGesture.getPhases().size() - 1);
                Hand referenceGestureLastPoseRightHand = referenceGestureLastPose.getRightHand();
                if (referenceGestureLastPoseRightHand != null) {
                    if (currentGestureFirstPoseRightHand.isHandShapeOverridable()) {
                        currentGestureFirstPoseRightHand.setHandShape(referenceGestureLastPoseRightHand.getHandShape());
                    }
                }
            }
        }
    }

    private void propagateImportantParameters(GestureSignal currentGesture, GestureSignal referenceGesture) {
        if (!currentGesture.getReference().equals(referenceGesture.getReference())) {
            propagateImportantParameters(currentGesture);
        }
    }

    private void propagateImportantParameters(GestureSignal currentGesture) {

        // Replace gesture parameters for all the poses, if they are overridable
        for (GesturePose currentGestureCurrentPose : currentGesture.getPhases()) {

            int currentGestureCurrentPoseIndex = currentGesture.getPhases().indexOf(currentGestureCurrentPose);
            if (currentGestureCurrentPoseIndex > 0) {

                GesturePose currentGesturePreviousPose = currentGesture.getPhases().get(currentGestureCurrentPoseIndex - 1);

                Hand currentGestureCurrentPoseLeftHand = currentGestureCurrentPose.getLeftHand();
                Hand currentGesturePreviousPoseLeftHand = currentGesturePreviousPose.getLeftHand();

                if ((currentGestureCurrentPoseLeftHand != null) && (currentGesturePreviousPoseLeftHand != null) && (mainGestureSide == Side.LEFT ||mainGestureSide == Side.BOTH)) {

                    Position currentGestureCurrentPoseLeftHandPosition = currentGestureCurrentPoseLeftHand.getPosition();
                    Position currentGesturePreviousPoseLeftHandPosition = currentGesturePreviousPoseLeftHand.getPosition();

                    if ((currentGestureCurrentPoseLeftHandPosition != null) && (currentGesturePreviousPoseLeftHandPosition != null)) {

                        if ((currentGestureCurrentPoseLeftHandPosition instanceof UniformPosition) && (currentGesturePreviousPoseLeftHandPosition instanceof UniformPosition)) {

                            UniformPosition currentGestureCurrentPoseLeftHandUniformPosition = (UniformPosition) currentGestureCurrentPoseLeftHandPosition;

                            // If all the gesture parameters are Overridable, gestures can not be distinguible within ideational units.
                            // In order to distinguish gestures, only handshape and wristOrientation will be alterate
                            if (currentGestureCurrentPoseLeftHand.isHandShapeOverridable()
                                    && currentGestureCurrentPoseLeftHand.isWristOrientationOverridable()
                                    && currentGestureCurrentPoseLeftHandUniformPosition.isXOverridable()
                                    && currentGestureCurrentPoseLeftHandUniformPosition.isYOverridable()
                                    && currentGestureCurrentPoseLeftHandUniformPosition.isZOverridable()) {

                                Quaternion currentGesturePreviousPoseLeftHandWristOrientation = currentGesturePreviousPoseLeftHand.getWristOrientation();
                                if (currentGesturePreviousPoseLeftHandWristOrientation != null) {
                                    currentGestureCurrentPoseLeftHand.setWristOrientation(new Quaternion(currentGesturePreviousPoseLeftHandWristOrientation));
                                }

                                currentGestureCurrentPoseLeftHand.setHandShape(currentGesturePreviousPoseLeftHand.getHandShape());

                            } else if (currentGestureCurrentPoseLeftHandUniformPosition.isXOverridable()
                                    && currentGestureCurrentPoseLeftHandUniformPosition.isYOverridable()
                                    && currentGestureCurrentPoseLeftHandUniformPosition.isZOverridable()) {

                                currentGestureCurrentPoseLeftHand.setHandShape(currentGesturePreviousPoseLeftHand.getHandShape());

                            } else {

                                if (currentGestureCurrentPoseLeftHandUniformPosition.isXOverridable()) {
                                    currentGestureCurrentPoseLeftHandUniformPosition.setX(currentGesturePreviousPoseLeftHandPosition.getX());
                                    currentGestureCurrentPoseLeftHand.setOpenness(currentGesturePreviousPoseLeftHand.getOpenness());
                                }
                                if (currentGestureCurrentPoseLeftHandUniformPosition.isYOverridable()) {
                                    currentGestureCurrentPoseLeftHandUniformPosition.setY(currentGesturePreviousPoseLeftHandPosition.getY());
                                }
                                if (currentGestureCurrentPoseLeftHandUniformPosition.isZOverridable()) {
                                    currentGestureCurrentPoseLeftHandUniformPosition.setZ(currentGesturePreviousPoseLeftHandPosition.getZ());
                                }

                                Quaternion currentGesturePreviousPoseLeftHandWristOrientation = currentGesturePreviousPoseLeftHand.getWristOrientation();
                                if (currentGesturePreviousPoseLeftHandWristOrientation != null) {
                                    if (currentGestureCurrentPoseLeftHand.isWristOrientationOverridable()) {
                                        currentGestureCurrentPoseLeftHand.setWristOrientation(new Quaternion(currentGesturePreviousPoseLeftHandWristOrientation));
                                    }
                                }

                                if (currentGestureCurrentPoseLeftHand.isHandShapeOverridable()) {
                                    currentGestureCurrentPoseLeftHand.setHandShape(currentGesturePreviousPose.getLeftHand().getHandShape());
                                }
                            }
                        }
                    }
                }

                Hand currentGestureCurrentPoseRightHand = currentGestureCurrentPose.getRightHand();
                Hand currentGesturePreviousPoseRightHand = currentGesturePreviousPose.getRightHand();

                if ((currentGestureCurrentPoseRightHand != null) && (currentGesturePreviousPoseRightHand != null)&& (mainGestureSide == Side.RIGHT ||mainGestureSide == Side.BOTH)) {

                    Position currentGestureCurrentPoseRightHandPosition = currentGestureCurrentPoseRightHand.getPosition();
                    Position currentGesturePreviousPoseRightHandPosition = currentGesturePreviousPoseRightHand.getPosition();

                    if ((currentGestureCurrentPoseRightHandPosition != null) && (currentGesturePreviousPoseRightHandPosition != null)) {

                        if ((currentGestureCurrentPoseRightHandPosition instanceof UniformPosition) && (currentGesturePreviousPoseRightHandPosition instanceof UniformPosition)) {

                            UniformPosition currentGestureCurrentPoseRightHandUniformPosition = (UniformPosition) currentGestureCurrentPoseRightHandPosition;

                            // If all the gesture parameters are Overridable, gestures can not be distinguible within ideational units.
                            // In order to distinguish gestures, only handshape and wristOrientation will be alterate
                            if (currentGestureCurrentPoseRightHand.isHandShapeOverridable()
                                    && currentGestureCurrentPoseRightHand.isWristOrientationOverridable()
                                    && currentGestureCurrentPoseRightHandUniformPosition.isXOverridable()
                                    && currentGestureCurrentPoseRightHandUniformPosition.isYOverridable()
                                    && currentGestureCurrentPoseRightHandUniformPosition.isZOverridable()) {

                                Quaternion currentGesturePreviousPoseRightHandWristOrientation = currentGesturePreviousPoseRightHand.getWristOrientation();
                                if (currentGesturePreviousPoseRightHandWristOrientation != null) {
                                    currentGestureCurrentPoseRightHand.setWristOrientation(new Quaternion(currentGesturePreviousPoseRightHandWristOrientation));
                                }

                                currentGestureCurrentPoseRightHand.setHandShape(currentGesturePreviousPoseRightHand.getHandShape());

                            } else if (currentGestureCurrentPoseRightHandUniformPosition.isXOverridable()
                                    && currentGestureCurrentPoseRightHandUniformPosition.isYOverridable()
                                    && currentGestureCurrentPoseRightHandUniformPosition.isZOverridable()) {

                                currentGestureCurrentPoseRightHand.setHandShape(currentGesturePreviousPoseRightHand.getHandShape());

                            } else {

                                if (currentGestureCurrentPoseRightHandUniformPosition.isXOverridable()) {
                                    currentGestureCurrentPoseRightHandUniformPosition.setX(currentGesturePreviousPoseRightHandPosition.getX());
                                    currentGestureCurrentPoseRightHand.setOpenness(currentGesturePreviousPoseRightHand.getOpenness());
                                }
                                if (currentGestureCurrentPoseRightHandUniformPosition.isYOverridable()) {
                                    currentGestureCurrentPoseRightHandUniformPosition.setY(currentGesturePreviousPoseRightHandPosition.getY());
                                }
                                if (currentGestureCurrentPoseRightHandUniformPosition.isZOverridable()) {
                                    currentGestureCurrentPoseRightHandUniformPosition.setZ(currentGesturePreviousPoseRightHandPosition.getZ());
                                }

                                Quaternion currentGesturePreviousPoseRightHandWristOrientation = currentGesturePreviousPoseRightHand.getWristOrientation();
                                if (currentGesturePreviousPoseRightHandWristOrientation != null) {
                                    if (currentGestureCurrentPoseRightHand.isWristOrientationOverridable()) {
                                        currentGestureCurrentPoseRightHand.setWristOrientation(new Quaternion(currentGesturePreviousPoseRightHandWristOrientation));
                                    }
                                }

                                if (currentGestureCurrentPoseRightHand.isHandShapeOverridable()) {
                                    currentGestureCurrentPoseRightHand.setHandShape(currentGesturePreviousPose.getRightHand().getHandShape());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    private void fixStartRestPose(GestureSignal currentGesture, GestureSignal previousGesture, List<GestureSignal> restPoses) {

        GesturePose previousGestureLastPose = previousGesture.getPhases().get(previousGesture.getPhases().size() - 1);
        GesturePose currentGestureFirstPose = currentGesture.getPhases().get(0);

        Hand previousGestureLastPoseLeftHand = previousGestureLastPose.getLeftHand();
        Hand currentGestureFirstPoseLeftHand = currentGestureFirstPose.getLeftHand();

        if ((previousGestureLastPoseLeftHand == null) && (currentGestureFirstPoseLeftHand == null)) {
            currentGesture.getStartRestPose().setLeftHand(getRestPoseAt(restPoses, currentGesture.getStart().getValue()).getLeftHand());
        }

        Hand previousGestureLastPoseRightHand = previousGestureLastPose.getRightHand();
        Hand currentGestureFirstPoseRightHand = currentGestureFirstPose.getRightHand();

        if ((previousGestureLastPoseRightHand == null) && (currentGestureFirstPoseRightHand == null)) {
            currentGesture.getStartRestPose().setRightHand(getRestPoseAt(restPoses, currentGesture.getStart().getValue()).getRightHand());
        }
    }

    private GesturePose getRestPoseAt(List<GestureSignal> restPoses, double time) {
        GestureSignal restPostAtTime = restPoses.get(0);
        for (GestureSignal gs : restPoses) {
            if (gs.getStart().getValue() < time) {
                restPostAtTime = gs;
            } else {
                break; //the list is sorted so we can break whenever we are after "time"
            }
        }
        return restPostAtTime.getEnd().getValue() < time ? restPostAtTime.getEndRestPose() : restPostAtTime.getEndRestPose(); //a restpose has a reference like "rest=xxxxx"
    }

    /* -------------------------------------------------- */
    private void processHeadSignals(List<GestureSignal> restPoses) {
        List<HeadSignal> headSignals = getHeadSignals();
        if (headSignals != null) {
            // ...
        }
    }

    /* -------------------------------------------------- */
    private void processTorsoSignals(List<GestureSignal> restPoses) {
        List<TorsoSignal> torsoSignal = getTorsoSignals();
        if (torsoSignal != null) {
            // ...
        }
    }

    /* ---------------------------------------------------------------------- */
    private void processAlgo(GestureSignal currentGesture, GestureSignal previousGesture) {

        // If a gesture1 is done with two hands and gesture2 is done with 1 hand.
        GesturePose currentGestureFirstPose = currentGesture.getPhases().get(0);
        GesturePose previousGestureRelaxPose = previousGesture.getRelaxPose();

        Hand currentGestureFirstPoseLeftHand = currentGestureFirstPose.getLeftHand();
        Hand currentGestureFirstPoseRightHand = currentGestureFirstPose.getRightHand();

        Hand previousGestureRelaxPoseLeftHand = previousGestureRelaxPose.getLeftHand();
        Hand previousGestureRelaxPoseRightHand = previousGestureRelaxPose.getRightHand();

        if ((previousGestureRelaxPoseLeftHand != null) && (previousGestureRelaxPoseRightHand != null)) {

            if ((currentGestureFirstPoseLeftHand == null) && (currentGestureFirstPoseRightHand != null)) {

                currentGestureFirstPose.setLeftHand(new Hand(previousGestureRelaxPoseLeftHand));
                currentGestureFirstPoseLeftHand = currentGestureFirstPose.getLeftHand();

                Position currentGestureFirstPoseLeftHandPosition = currentGestureFirstPoseLeftHand.getPosition();
                Position currentGestureFirstPoseRightHandPosition = currentGestureFirstPoseRightHand.getPosition();

                double horizontalDistance = currentGestureFirstPoseLeftHandPosition.getX() + currentGestureFirstPoseRightHandPosition.getX();
                if (horizontalDistance <= 0) {
                    currentGestureFirstPoseLeftHandPosition.setX(currentGestureFirstPoseLeftHandPosition.getX() - horizontalDistance * 2.5);
                    if (currentGestureFirstPoseLeftHandPosition.getX() > 1) {
                        currentGestureFirstPoseLeftHandPosition.setX(1);
                    }
                }
                for (GesturePose currentGesturePose : currentGesture.getPhases()) {
                    currentGesturePose.setLeftHand(new Hand(currentGestureFirstPoseLeftHand));
                }
            }

            if ((currentGestureFirstPoseRightHand == null) && (currentGestureFirstPoseLeftHand != null)) {

                currentGestureFirstPose.setRightHand(new Hand(previousGestureRelaxPoseRightHand));
                currentGestureFirstPoseRightHand = currentGestureFirstPose.getRightHand();

                Position currentGestureFirstPoseRightHandPosition = currentGestureFirstPoseRightHand.getPosition();
                Position currentGestureFirstPoseLeftHandPosition = currentGestureFirstPoseLeftHand.getPosition();

                double horizontalDistance = currentGestureFirstPoseRightHandPosition.getX() + currentGestureFirstPoseLeftHandPosition.getX();
                if (horizontalDistance <= 0) {
                    currentGestureFirstPoseRightHandPosition.setX(currentGestureFirstPoseRightHandPosition.getX() - horizontalDistance * 2.5);
                    if (currentGestureFirstPoseRightHandPosition.getX() > 1) {
                        currentGestureFirstPoseRightHandPosition.setX(1);
                    }
                }
                for (GesturePose currentGesturePose : currentGesture.getPhases()) {
                    currentGesturePose.setRightHand(new Hand(currentGestureFirstPoseRightHand));
                }
            }
        }
    }

}
