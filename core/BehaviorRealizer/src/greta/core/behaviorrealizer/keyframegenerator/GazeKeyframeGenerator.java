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

import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.keyframes.HeadKeyframe;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.ShoulderKeyframe;
import greta.core.keyframes.TorsoKeyframe;
import greta.core.keyframes.face.AUAPFrameInterpolator;
import greta.core.keyframes.face.AUKeyFrame;
import greta.core.repositories.AUAPFrame;
import greta.core.repositories.AUExpression;
import greta.core.repositories.AUItem;
import greta.core.repositories.FaceLibrary;
import greta.core.repositories.HeadLibrary;
import greta.core.repositories.TorsoLibrary;
import greta.core.signals.GazeSignal;
import greta.core.signals.HeadSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.signals.SpineDirection;
import greta.core.signals.SpinePhase;
import greta.core.signals.SpineSignal;
import greta.core.signals.TorsoSignal;
import greta.core.util.CharacterManager;
import greta.core.util.Constants;
import greta.core.util.Mode;
import greta.core.util.audio.Mixer;
import greta.core.util.enums.CompositionType;
import greta.core.util.enums.GazeDirection;
import greta.core.util.enums.Influence;
import greta.core.util.enums.Side;
import greta.core.util.environment.Animatable;
import greta.core.util.environment.Environment;
import greta.core.util.environment.EnvironmentEventListener;
import greta.core.util.environment.Leaf;
import greta.core.util.environment.LeafEvent;
import greta.core.util.environment.Node;
import greta.core.util.environment.NodeEvent;
import greta.core.util.environment.TreeEvent;
import greta.core.util.environment.TreeNode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.time.Timer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author Mathieu Chollet
 * @author André-Marie Pez
 * @author Donatella Simonetti
 * @author Nawhal Sayarh
 * @author Fajrian Yunus
 */
public class GazeKeyframeGenerator extends KeyframeGenerator implements EnvironmentEventListener, SignalEmitter{

    /* eyes, head, shoulder/torso angular speed
     * this are the default value as found in the paper:
     * "Gaze and Attention Management for Embodied Conversational Agents"
     * authors: TOMISLAV PEJSA, SEAN ANDRIST, MICHAEL GLEICHER, and BILGE MUTLU */
    // default velocities for eyes, head and shoulder
    private static final double EYES_ANGULAR_SPEED = 2.61799;   // 150 degs/s, 2.62 rad/s
    private static final double HEAD_ANGULAR_SPEED = 0.872665;  //  50 degs/s, 5.2  rad/s
    private static final double TORSO_ANGULAR_SPEED = 0.261799; //  15 degs/s, 5.2  rad/s

    // constraint on horizontal(yaw) an vertical(pitch) eye movement
    private static final double EYES_YAW_LIMIT = Math.toRadians(50); //Math.PI / 3; //0.6;
    private static final double EYES_PITCH_LIMIT = Math.PI / 3;//S Math.PI / 3;

    // constraint on horizontal(yaw) an vertical(pitch)  head movement
    private static final double HEAD_YAW_LIMIT = Math.toRadians(HeadLibrary.getGlobalLibrary().getHeadIntervals().verticalLeftMax);//(2 * Math.PI / 360) * HeadLibrary.getGlobalLibrary().getHeadIntervals().verticalLeftMax;
    private static final double HEAD_PITCH_LIMIT_UP = Math.toRadians(HeadLibrary.getGlobalLibrary().getHeadIntervals().sagittalUpMax);//(2 * Math.PI / 360) * HeadLibrary.getGlobalLibrary().getHeadIntervals().sagittalUpMax;
    private static final double HEAD_PITCH_LIMIT_DOWN = Math.toRadians(HeadLibrary.getGlobalLibrary().getHeadIntervals().sagittalDownMax);//(2 * Math.PI / 360) * HeadLibrary.getGlobalLibrary().getHeadIntervals().sagittalDownMax;

    // constraint on horizontal torso/shoulder movement
    private static final double TORSO_YAW_LIMIT = Math.toRadians(TorsoLibrary.getGlobalLibrary().getTorsoIntervals().verticalL); //Math.toRadians(35); // 30°-40°

    private Environment env;
    private List<KeyframeGenerator> otherModalitiesKFGenerators;

    private Map<GazeSignal, Long> currentGazes;
    /** The current position */
    private AUKeyFrame defaultGazeLeft;
    private AUKeyFrame defaultGazeRight;

    private List<SignalPerformer> performers;

    // vector for the rest position of eyes, head, shoulder and torso
    private static Vec3d headAnglesHeadOffset = new Vec3d(0, 0.0534, 0); //new Vec3d(0, 0.0534, 0.0617);
    private static Vec3d headAnglesLeftEyeOffset = new Vec3d(headAnglesHeadOffset.x() + 0.0304, headAnglesHeadOffset.y(), 0.0617);
    private static Vec3d headAnglesRightEyeOffset = new Vec3d(headAnglesHeadOffset.x() - 0.0304, headAnglesHeadOffset.y(), 0.0617);
    private static Vec3d shoulderAnglesHeadOffset = new Vec3d(headAnglesHeadOffset.x(), 0.08651898, headAnglesHeadOffset.z());
    private static Vec3d torsoAnglesHeadOffset = new Vec3d(headAnglesHeadOffset.x(), 0.0348305, headAnglesHeadOffset.z());
    // TODO FIXME torsoAnglesHeadOffset is never used, why keep it ?

    // the head move after a latency time in a range (0, 100)ms
    // we take as default latency time: 50 ms
    private double headLatency = 0.05;

    private CharacterManager cm;

    private AUAPFrameInterpolator interpolator = new AUAPFrameInterpolator();

    /**
     * Returns the current list of {@code Signals}.
     */
    public List<Signal> getSignals() {
        return super.signals;
    }

    /**
     * Constructor.
     *
     * @param otherGenerators The other {@code KeyframeGenerators}. They are
     * needed to compute exact eye angles at times between two keyframes of
     * other modalities.
     */
    public GazeKeyframeGenerator(CharacterManager cm, List<KeyframeGenerator> otherGenerators) {
        super(GazeSignal.class);
        this.cm = cm;
        this.env = this.cm.getEnvironment();
        otherModalitiesKFGenerators = otherGenerators;
        currentGazes = new ConcurrentHashMap<>();
        performers = new ArrayList<>();
        defaultGazeLeft = new AUKeyFrame("restLeft", 0, new AUAPFrame());
        defaultGazeRight = new AUKeyFrame("restRight", 0, new AUAPFrame());
    }

    private void setGazeRestPosition(AUKeyFrame gazeLeft, AUKeyFrame gazeRight) {
        defaultGazeLeft = gazeLeft;
        defaultGazeRight = gazeRight;
    }

    private void cleanGazeShifts() {
        GazeSignal currentShift = null;
        long currentShiftTime = 0;
        for (GazeSignal gazeSignal : currentGazes.keySet()) {
            if (gazeSignal.isGazeShift()) {
                long realStartTime = currentGazes.get(gazeSignal) + (long) (gazeSignal.getStartValue() * 1000);
                if (realStartTime <= Timer.getTimeMillis()) {
                    if (currentShift == null || realStartTime > currentShiftTime) {
                        if (currentShift != null) {
                            // A more recent shift is found : currentShift is outdated
                            currentGazes.remove(currentShift);
                        }
                        currentShift = gazeSignal;
                        currentShiftTime = realStartTime;
                    } else if (realStartTime < currentShiftTime) {
                        // gs is outdated
                        currentGazes.remove(gazeSignal);
                    } else {
                        // realStartTime == currentShiftTime
                        // get the one from the latest signal packet:
                        if (currentGazes.get(gazeSignal) < currentGazes.get(currentShift)) {
                            currentGazes.remove(gazeSignal);
                        } else {
                            currentGazes.remove(currentShift);
                            currentShift = gazeSignal;
                            currentShiftTime = realStartTime;
                        }
                    }
                }
            }
        }
    }

    /**
     * Computes {@code Keyframes} for the body modalities of the current
     * {@code GazeSignals}.
     *
     * @param outputKeyframe List of {@code Keyframes} where we add the computed
     * body {@code Keyframes}.
     * @return The list of {@code Keyframes} with the computed body
     * {@code Keyframes} added to it.
     */
    public List<Keyframe> generateBodyKeyframes(List<Keyframe> outputKeyframe) {
        if (!signals.isEmpty()) {
            signals.sort(getComparator());
        }
        // SpinePhase for each body part involved in the gaze in order to store the information about the last position after a gazeShift
        SpinePhase lastShiftHead = new SpinePhase("head", 0, 0);
        SpinePhase lastShiftTorso = new SpinePhase("torso", 0, 0);

        // take the MPEG4 for the agent whom is performing the gaze
        MPEG4Animatable currentAgent = null;
        for (Node node : this.env.getTreeNode().getChildren()) {
            if (node instanceof MPEG4Animatable) {
                MPEG4Animatable agent = (MPEG4Animatable) node;
                if (agent.getCharacterManager().getCurrentCharacterName().equals(cm.getCurrentCharacterName())) {
                    currentAgent = agent;
                    break;
                }
            }
        }

        for (Signal signal : signals) {
            GazeSignal gaze = (GazeSignal) signal;
            currentGazes.put(gaze, Timer.getTimeMillis());

            //euler angles to target + offset, for head
            HeadAndEyesAngles headAndEyesAngles = new HeadAndEyesAngles(this.env, gaze);

            // head angles give by the additional rotation of each cervical vertebrae
            double agentHeadPitch = currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc1_tilt) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc2_tilt) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc3_tilt) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc4_tilt) +
                    currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc5_tilt) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc6_tilt) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc7_tilt);
            double agentHeadYaw = currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc1_torsion) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc2_torsion) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc3_torsion) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc4_torsion) +
                    currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc5_torsion) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc6_torsion) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc7_torsion);
            double agentHeadRoll = currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc1_roll) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc2_roll) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc3_roll) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc4_roll) +
                    currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc5_roll) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc6_roll) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc7_roll);


            // trying to correct a little defect in the gazeShift. Each time we have a gazeShift behavior are calculated
            // only the keyframe at the target and not at the starting time. This is because the position
            // of any body part can be different to the rest position if a gazeShift happened before. Therefore instead
            // to calculate the rotation angle's difference between the actual position and the target position
            // it is just calculated the angle between the rest position and the target one, updating the position like
            // we delete the last position and put the new one at the target time.
            // the only problem is that when the rotation angle to reach the target (calculated respect to the
            // rest position) is bigger than the actual angle. In this case the movement of the eyes, that start to
            // move before the head are not correct. A way to overcome this defect is to delete the headLatency in this case.
            Quaternion actualHeadOrientation = new Quaternion( new Vec3d (1,0,0), agentHeadPitch);
            actualHeadOrientation.multiply(new Quaternion( new Vec3d (0,1,0), agentHeadYaw));
            actualHeadOrientation.multiply(new Quaternion( new Vec3d (0,0,1), agentHeadRoll));

            // add the rotation of the root
            actualHeadOrientation.multiply(new Quaternion(currentAgent.getRotationNode().getOrientation().x(),
                    currentAgent.getRotationNode().getOrientation().y(),
                    currentAgent.getRotationNode().getOrientation().z(),
                    currentAgent.getRotationNode().getOrientation().w()));

            Vec3d headActualAngle = actualHeadOrientation.toEulerXYZ(); // radians

            if (headActualAngle.y() < headAndEyesAngles.headYawAngle) {
                headLatency = 0;
            }

            //Compute the influence according the gaze amplitude
            Influence inf = gaze.getInfluence();
            if (inf == null) {
                if (Math.max(Math.abs(headAndEyesAngles.headPitchAngle), Math.abs(headAndEyesAngles.headYawAngle)) > 0.523599){ // 30°
                    inf = Influence.TORSO;
                } else if (Math.max(Math.abs(headAndEyesAngles.headPitchAngle), Math.abs(headAndEyesAngles.headYawAngle)) > 0.349066){ // 20°
                    inf = Influence.SHOULDER;
                } else if (Math.max(Math.abs(headAndEyesAngles.headPitchAngle), Math.abs(headAndEyesAngles.headYawAngle)) > 0.261799){ // 15
                    inf = Influence.HEAD;
                }else{
                    inf = Influence.EYES;
                }
            }
            gaze.setInfluence(inf);

            /*if (inf == null) {
                inf = Influence.TORSO;
                gaze.setInfluence(Influence.TORSO);
            }*/

            //times computation
            //start keyframe : all influences at original position
            double start = gaze.getStart().getValue();
            // ready and relax will be recomputed according to the influence
            double ready = gaze.getTimeMarker("ready").getValue();
            double relax = gaze.getTimeMarker("relax").getValue();
            //end keyframe : all influences at original position
            double end = gaze.getEnd().getValue();

            Influence gazeInfluence = computeGazeInfluence(gaze, headAndEyesAngles);

            if (gazeInfluence.ordinal() >= Influence.SHOULDER.ordinal()) {
                // if the influence involve the torso we create the keyframe just for the torso that already include the movement of
                // vt12 vertebrae (the same we move just for the shoulder). So we don't need to create the keyframe also for the shoulder
                //********************************************************************************//
                ShouldersAngles shouldersAngles = new ShouldersAngles(this.env, gaze, headAndEyesAngles);

                // calculate the shoulder max speed depending on the rotation angle
                double shoulderMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(Math.abs(shouldersAngles.shoulderMinimumAlign*TORSO_YAW_LIMIT))/15 + 2)*Math.toDegrees(TORSO_ANGULAR_SPEED)));
                //double maxVelShoulderPitch = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(Math.abs(shouldersAngles.shoulderLimitedYaw*TORSO_YAW_LIMIT))/15 + 2)*Math.toDegrees(TORSO_ANGULAR_SPEED)));

                double shoulderMoveTime = Math.max(Math.abs(shouldersAngles.shoulderMinimumAlign), Math.abs(shouldersAngles.shoulderLimitedPitch)) / shoulderMaxSpeed;
                double timeShoulderAtTarget = start + shouldersAngles.shoulderLatency + shoulderMoveTime;
                if (end == 0) {
                    ready = timeShoulderAtTarget;
                    relax = ready + 0.2;
                }

                if (timeShoulderAtTarget > ready) {
                    timeShoulderAtTarget = ready;
                }
                double timeBackShoulderAtZero = relax + shoulderMoveTime;

                if (end == 0) {
                    end = timeBackShoulderAtZero;
                }

                if (timeBackShoulderAtZero > end) {
                    timeBackShoulderAtZero = end;
                }
                // torsoSignalTargetPosition torso signal at target position
                TorsoSignal torsoSignalTargetPosition = new TorsoSignal(IDProvider.createID("gazegenerator").toString());
                torsoSignalTargetPosition.setDirectionShift(true);
                SpinePhase spinePhaseTargetPosition = createSpinePhase("end", timeShoulderAtTarget, timeShoulderAtTarget, shouldersAngles.shoulderMinimumAlign, shouldersAngles.shoulderLimitedPitch); // ready
                spinePhaseTargetPosition.setStartTime(timeShoulderAtTarget); // ready
                setupTorsoSignalAtPosition(torsoSignalTargetPosition, spinePhaseTargetPosition, start,
                        timeShoulderAtTarget, shouldersAngles, gazeInfluence);

                if (!gaze.isGazeShift()) {
                    // torsoSignalRestPosition torso signal at rest position
                    TorsoSignal torsoSignalRestPosition = new TorsoSignal(IDProvider.createID("gazegenerator").toString());
                    SpinePhase spinePhaseRestPosition;
                    spinePhaseRestPosition = new SpinePhase(lastShiftTorso);
                    spinePhaseRestPosition.setStartTime(timeBackShoulderAtZero); // end
                    spinePhaseRestPosition.setEndTime(timeBackShoulderAtZero);
                    setupTorsoSignalAtPosition(torsoSignalRestPosition, spinePhaseRestPosition, relax,
                            timeBackShoulderAtZero, shouldersAngles, gazeInfluence);

                    // add both torso signals to TorsoKeyFrameGenerator
                    addTwoSignalsToKeyframeGenerator(torsoSignalTargetPosition, torsoSignalRestPosition);
                }
                else {
                    lastShiftTorso = spinePhaseTargetPosition;
                    addSignalToKeyframeGenerator(torsoSignalTargetPosition);
                }

                /*********************************************************************
                 * HEAD
                 **********************************************************************/

                double headMaxSpeed;
                // calculate the head max speed depending on the rotation angle
                if (Math.abs(shouldersAngles.headAngles.headLimitedYaw) > Math.abs(shouldersAngles.headAngles.headLimitedPitch)) {
                    headMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(shouldersAngles.headAngles.headLimitedYaw
                            * HEAD_YAW_LIMIT)
                            / 50 + 2f/5) * Math.toDegrees(HEAD_ANGULAR_SPEED)));
                } else {
                    headMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(shouldersAngles.headAngles.headLimitedPitch
                            * (shouldersAngles.headAngles.headLimitedPitch < 0 ? HEAD_PITCH_LIMIT_DOWN : HEAD_PITCH_LIMIT_UP))
                            / 50 + 2f/5) * Math.toDegrees(HEAD_ANGULAR_SPEED)));
                }

                // time head reach the target position and come back
                double headMoveTime = Math.abs(shouldersAngles.headAngles.headLimitedYaw * HEAD_YAW_LIMIT)/ headMaxSpeed;
                double timeHeadAtTarget = start + headLatency + headMoveTime; // 0.1 is the latency time
                if (timeHeadAtTarget > ready) {
                    timeHeadAtTarget = ready;
                }

                if (end == 0) {
                    ready = timeHeadAtTarget;
                    relax = ready + 0.2;
                }

                double timeBackHeadAtZero = relax + headMoveTime;
                if (timeBackHeadAtZero > end) {
                    timeBackHeadAtZero = end;
                }

                if (end == 0) {
                    end = timeBackHeadAtZero; // TODO FIXME end is not used after that, why change its value ?
                }

                // headSignalLookToTarget head signal when look to the target
                HeadSignal headSignalLookToTarget = createHeadSignalWithDirectionShift();
                SpinePhase spinePhaseLookToTarget;
                // if the movement involves also the shoulder or all torso, the movement of the head is enhanced by the movement of the shoulder/torso
                spinePhaseLookToTarget = createSpinePhase("end", timeHeadAtTarget, timeHeadAtTarget, shouldersAngles.headAngles.headLimitedYaw, shouldersAngles.headAngles.headLimitedPitch); // read
                spinePhaseLookToTarget.setStartTime(timeHeadAtTarget);  // ready
                // head latency equals to 100 ms
                setupSignal(headSignalLookToTarget, spinePhaseLookToTarget, start + headLatency, timeHeadAtTarget);

                if (!gaze.isGazeShift()) {
                    // headSignalRestPosition head signal at rest position
                    HeadSignal headSignalRestPosition = createHeadSignalWithDirectionShift();
                    SpinePhase spinePhaseRestPosition = new SpinePhase(lastShiftHead);
                    spinePhaseRestPosition.setStartTime(timeBackHeadAtZero); // end
                    setupSignal(headSignalRestPosition, spinePhaseRestPosition, relax, timeBackHeadAtZero);

                    // add both head signals to HeadKeyFrameGenerator
                    addTwoSignalsToKeyframeGenerator(headSignalLookToTarget, headSignalRestPosition);
                } else {
                    lastShiftHead = spinePhaseLookToTarget;
                    addSignalToKeyframeGenerator(headSignalLookToTarget);
                }

                // if the influence involves the shoulder we create the keyframe just for the shoulder
            } else if (gazeInfluence.ordinal() == Influence.HEAD.ordinal()) {

                // Head Signals
                double headMaxSpeed;
                // calculate the head max speed depending on the rotation angle
                if (Math.abs(headAndEyesAngles.headLimitedYaw) > Math.abs(headAndEyesAngles.headLimitedPitch)) {
                    headMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(headAndEyesAngles.headLimitedYaw *HEAD_YAW_LIMIT)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                } else {
                    headMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(headAndEyesAngles.headLimitedPitch
                            * headAndEyesAngles.headLimitedPitch < 0 ? HEAD_PITCH_LIMIT_DOWN : HEAD_PITCH_LIMIT_UP)
                            / 50 + 2f/5) * Math.toDegrees(HEAD_ANGULAR_SPEED)));
                }


                // time head reach the target position and come back
                double shoulderMoveTime = Math.max(Math.abs(headAndEyesAngles.headLimitedYaw *HEAD_YAW_LIMIT), Math.abs(headAndEyesAngles.headLimitedPitch *HEAD_YAW_LIMIT)) / headMaxSpeed;
                double timeHeadAtTarget = start + headLatency + shoulderMoveTime; // 0.1 is the latency time

                if (end == 0) {
                    ready = timeHeadAtTarget;
                    relax = ready + 0.2;
                }

                if (timeHeadAtTarget > ready) {
                    timeHeadAtTarget = ready;
                }
                double timeBackHeadAtZero = relax + shoulderMoveTime;

                if (end == 0) {
                    end = timeBackHeadAtZero;
                }

                if (timeBackHeadAtZero > end) {
                    timeBackHeadAtZero = end;
                }

                // headSignalToTarget head signal when look to the target
                HeadSignal headSignalToTarget = createHeadSignalWithDirectionShift();
                SpinePhase spinePhaseToTarget;
                spinePhaseToTarget = createSpinePhase("end", timeHeadAtTarget, timeHeadAtTarget, headAndEyesAngles.headLimitedYaw, headAndEyesAngles.headLimitedPitch); // ready
                spinePhaseToTarget.setStartTime(timeHeadAtTarget);
                // head latency equal to 50 ms
                setupSignal(headSignalToTarget, spinePhaseToTarget, start + headLatency, timeHeadAtTarget);

                if (!gaze.isGazeShift()) {
                    // headSignalRestPosition head signal at rest position
                    HeadSignal headSignalRestPosition = createHeadSignalWithDirectionShift();
                    SpinePhase spinePhaseRestPosition = new SpinePhase(lastShiftHead);
                    spinePhaseRestPosition.setStartTime(timeBackHeadAtZero); // end
                    spinePhaseRestPosition.setEndTime(timeBackHeadAtZero); // end

                    setupSignal(headSignalRestPosition, spinePhaseRestPosition, relax, timeBackHeadAtZero);

                    // add both head signals to HeadKeyFrameGenerator
                    addTwoSignalsToKeyframeGenerator(headSignalToTarget, headSignalRestPosition);
                }
                else {
                    lastShiftHead = spinePhaseToTarget;
                    addSignalToKeyframeGenerator(headSignalToTarget);
                }

                // in the case there was a gazeShift before and there was a rotation of the torso, this rotation has to be canceled  if  the next gaze
                // involve just the head. So it is create a torsoSignel with rotation equal to 0
                // torsoSignalTargetPosition torso signal at target position
                TorsoSignal torsoSignalTargetPosition = new TorsoSignal(IDProvider.createID("gazegenerator").toString());
                torsoSignalTargetPosition.setDirectionShift(true);
                SpinePhase spinePhaseTargetPosition = createSpinePhase("end", timeHeadAtTarget, timeHeadAtTarget, 0, 0); // ready
                spinePhaseTargetPosition.setStartTime(timeHeadAtTarget); // ready
                setupSignal(torsoSignalTargetPosition, spinePhaseTargetPosition, start + headLatency, timeHeadAtTarget);

                if (!gaze.isGazeShift()) {
                    // torsoSignalRestPosition torso signal at rest position
                    TorsoSignal torsoSignalRestPosition = new TorsoSignal(IDProvider.createID("gazegenerator").toString());
                    SpinePhase spinePhaseRestPosition;
                    spinePhaseRestPosition = new SpinePhase(lastShiftTorso);
                    spinePhaseRestPosition.setStartTime(timeBackHeadAtZero); // end
                    spinePhaseRestPosition.setEndTime(timeBackHeadAtZero);
                    setupSignal(torsoSignalRestPosition, spinePhaseRestPosition, relax, timeBackHeadAtZero);

                    // add both torso signals to TorsoKeyFrameGenerator
                    addTwoSignalsToKeyframeGenerator(torsoSignalTargetPosition, torsoSignalRestPosition);
                }
                else {
                    lastShiftTorso = spinePhaseTargetPosition;
                    addSignalToKeyframeGenerator(torsoSignalTargetPosition);
                }

            } else {
                /**********************************************************************************************
                 * ************************************JUST EYES**********************************************
                 * *******************************************************************************************/

                // there was a gaze shift that involved torso and head and now just the eyes,
                // so we have to delete the rotation of head and torso

                // compute the time to go back in the front position
                TorsoKeyframeGenerator torsoKeyframeGenerator = (TorsoKeyframeGenerator) this.otherModalitiesKFGenerators.get(5);
                TorsoKeyframe torsoKeyframe = torsoKeyframeGenerator.getDefaultPosition();

                double yawTorso = torsoKeyframe.verticalTorsion.value;
                double pitchTorso = torsoKeyframe.sagittalTilt.value;

                double angleTorso = Math.max(yawTorso, pitchTorso);

                double shoulderMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(Math.abs(angleTorso*TORSO_YAW_LIMIT))/15 + 2)*Math.toDegrees(TORSO_ANGULAR_SPEED)));
                double timeShoulderAtTarget = start + Math.abs(angleTorso) / shoulderMaxSpeed;

                if (angleTorso == 0) {
                    timeShoulderAtTarget = end;
                }

                // torso signal at target position
                TorsoSignal torsoSignalTargetPosition = new TorsoSignal(IDProvider.createID("gazegenerator").toString());
                torsoSignalTargetPosition.setDirectionShift(true);
                SpinePhase spinePhaseTargetPosition;
                double spinePhaseTime = gaze.isGazeShift() ? timeShoulderAtTarget : end;
                spinePhaseTargetPosition = createSpinePhase("end", spinePhaseTime, spinePhaseTime, 0, 0);
                torsoSignalTargetPosition.getEnd().setValue(spinePhaseTime);
                torsoSignalTargetPosition.getPhases().add(spinePhaseTargetPosition);
                torsoSignalTargetPosition.getStart().setValue(start + headLatency);
                addSignalToKeyframeGenerator(torsoSignalTargetPosition);

                // compute the time to go back in the front position
                HeadKeyframeGenerator headKeyframeGenerator = (HeadKeyframeGenerator) this.otherModalitiesKFGenerators.get(2);
                HeadKeyframe headKeyframe = headKeyframeGenerator.getDefaultPosition();
                double yawHead = headKeyframe.verticalTorsion.value;
                double pitchHead = headKeyframe.sagittalTilt.value;

                double angleHead = Math.max(yawHead, pitchHead);

                double headMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(angleHead*HEAD_YAW_LIMIT)/50 + 2f/5) * Math.toDegrees(HEAD_ANGULAR_SPEED)));
                double timeHeadAtTarget = angleHead == 0 ? end : (start + (Math.abs(angleHead) / headMaxSpeed));

                // head signal at target position
                HeadSignal headSignalWithDirectionShift = createHeadSignalWithDirectionShift();
                SpinePhase headPhase;
                double headPhaseTime = gaze.isGazeShift() ? timeHeadAtTarget : end;
                headPhase = createSpinePhase("end", headPhaseTime, headPhaseTime, 0, 0);
                headSignalWithDirectionShift.getEnd().setValue(headPhaseTime);
                headSignalWithDirectionShift.getPhases().add(headPhase);
                headSignalWithDirectionShift.getStart().setValue(start + headLatency);
                addSignalToKeyframeGenerator(headSignalWithDirectionShift);
            }
        }
        return outputKeyframe;
    }

    /**
     * This function takes into account the head and torso signals happening in the same time lapse and that are involved and not in the gaze behavior.
     * If we have a gaze signal and in the same moment an external head signal or torso signal, the keyframe for the haed or torso should be a new one
     * that takes into account the two rotations and gives the sum of them.
     * @param outputKeyframe
     */
    private void BodyKeyframeOverlapping (List<Keyframe> outputKeyframe) { // TODO delete this if it is as useless as it looks
    }

    private AUAPFrame generateAUAPFrameFromAUItems(GazeSignal face, String tmName, double scale) {
        return generateAUAPFrameFromAUItems(face.getActionUnits(), face.getTimeMarker(tmName).getValue(), scale * face.getIntensity());
    }

    private AUAPFrame generateAUAPFrameFromAUItems(List<AUItem> aus, double time, double scale) {
        int timeIndex = (int)(time*Constants.FRAME_PER_SECOND);
        AUAPFrame auapFrame = new AUAPFrame(timeIndex);
        for (AUItem au : aus) {
            if (au.getSide() == Side.LEFT || au.getSide() == Side.BOTH) {
                auapFrame.setAUAPleft(au.getAUnum(), au.getIntensity() * scale);
            }
            if (au.getSide() == Side.RIGHT || au.getSide() == Side.BOTH) {
                auapFrame.setAUAPright(au.getAUnum(), au.getIntensity() * scale);
            }
        }
        return auapFrame;
    }

    /**
     * Computes {@code Keyframes} for the eyes, for the current
     * {@code GazeSignals}.
     *
     * @param outputKeyframe List of {@code Keyframes} where we add the computed
     * eyes {@code Keyframes}.
     * @return The list of {@code Keyframes} with the computed eyes
     * {@code Keyframes} added to it.
     */
    public List<Keyframe> generateEyesKeyframes(List<Keyframe> outputKeyframe) {
        this.cleanGazeShifts();

        if (!signals.isEmpty()) {
            signals.sort(getComparator());
        }
        for (Signal signal : signals) {
            GazeSignal gaze = (GazeSignal) signal;
            currentGazes.put(gaze, Timer.getTimeMillis());
            String gazeId = gaze.getId();

            //euler angles to target + offset, for head
            HeadAndEyesAngles headAngles = new HeadAndEyesAngles(this.env, gaze);

            //euler angles to target + offset, for shoulder (same for torso)
            ShouldersAngles shouldersAngles = new ShouldersAngles(this.env, gaze, headAngles);

            // check if the gaze expression is in the facelibrary
            // if we look at a target there is no information in the library
            if ((gaze.getTarget() == null || gaze.getTarget().isEmpty())
                    && gaze.getOffsetDirection() == GazeDirection.FRONT && gaze.getOffsetAngle() == 0) {
                AUExpression faceLibraryExpression = FaceLibrary.global_facelibrary.get(gaze.getReference());
                if (faceLibraryExpression != null) {
                    for (AUItem auItem : faceLibraryExpression.getActionUnits()) {
                        AUItem newAuItem = new AUItem(auItem.getAUnum(), auItem.getIntensity(), auItem.getSide());
                        gaze.add(newAuItem);
                    }
                    gaze.setFilled(true);
                } else {
                    gaze.setFilled(false);
                    Logs.error("There is no entry in the FaceLibrary for " + gaze.getReference());
                }

                if (gaze.isScheduled() && gaze.isFilled()) {
                    AUAPFrame start = generateAUAPFrameFromAUItems(gaze, "start", gaze.getStart().getValue());
                    AUAPFrame ready = generateAUAPFrameFromAUItems(gaze, "ready", gaze.getTimeMarker("ready").getValue());
                    AUAPFrame relax = generateAUAPFrameFromAUItems(gaze, "relax", gaze.getTimeMarker("relax").getValue());
                    AUAPFrame end = generateAUAPFrameFromAUItems(gaze, "end", gaze.getEnd().getValue());

                    interpolator.blendSegment(start, ready, ready, relax, end);

                    for (AUAPFrame frame : interpolator.getAUAPFrameList()) {
                        double time = frame.getFrameNumber() * Constants.FRAME_DURATION_SECONDS;
                        AUKeyFrame auKeyFrame = new AUKeyFrame("AUs_at_"+time, time, frame);
                        outputKeyframe.add(auKeyFrame);
                    }
                } else {
                    boolean left; // TODO FIXME left, up and pitch are assigned but never used, why keep them ?
                    boolean up;

                    double yaw;
                    double pitch;

                    // take the last gaze shift angle for left eye
                    double yawLeft = defaultGazeLeft.getAus().getAUAP(61, Side.LEFT).getNormalizedValue();
                    double yawRight = defaultGazeLeft.getAus().getAUAP(62, Side.RIGHT).getNormalizedValue();
                    double pitchUp = defaultGazeLeft.getAus().getAUAP(63, Side.LEFT).getNormalizedValue();
                    double pitchDown = defaultGazeLeft.getAus().getAUAP(64, Side.LEFT).getNormalizedValue();

                    if (yawLeft != 0){ // gazedirection = left
                        left = true;
                        yaw = yawLeft;
                        if (pitchUp != 0) { // up
                            pitch = pitchUp;
                            up = true;
                        } else { // down
                            pitch = pitchDown;
                        }
                    } else { // gazedirection = right
                        yaw = yawRight;
                        if (pitchUp != 0){ // up
                            up = true;
                            pitch = pitchUp;
                        } else { // down
                            pitch = pitchDown;
                        }
                    }

                    // calculate the max speed of the head depending on the rotation angle
                    double Amin = Math.toDegrees(Math.abs(yaw*EYES_YAW_LIMIT));
                    //Amin_pitch = Math.toDegrees(Math.abs(Math.min(shouldersAngles.ha.l_limitedPitch*EYES_PITCH_LIMIT, shouldersAngles.ha.r_limitedPitch*EYES_PITCH_LIMIT)));
                    // double eyesMaxSpeed = Math.toRadians((2*Amin/75 + 1f/6) * Math.toDegrees(EYES_ANGULAR_SPEED));
                    // TODO FIXME eyesMaxSpeed is assigned but never used, why keep it ?
                    //eyesMaxSpeed = Math.toRadians((2*Amin_pitch/75 + 1f/6)*Math.toDegrees(EYES_ANGULAR_SPEED));
                    //double timeEyesAtTarget = gaze.getStart().getValue() + Math.abs(yaw*EYES_YAW_LIMIT) / eyesMaxSpeed;
                    // TODO FIXME timeEyesAtTarget is assigned but never used, why keep it ?

                    addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", gaze.getEnd().getValue(), Side.LEFT, GazeDirection.FRONT, 0, 0);
                    addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", gaze.getEnd().getValue(), Side.RIGHT, GazeDirection.FRONT, 0, 0);

                    AUAPFrame auFrameLeft = createAUAPFrameForEyeSide(Side.LEFT);
                    AUAPFrame auFrameRight = createAUAPFrameForEyeSide(Side.RIGHT);
                    setGazeRestPosition(new AUKeyFrame(gazeId + "_back", gaze.getEnd().getValue(), auFrameLeft), new AUKeyFrame(gazeId + "_back", gaze.getEnd().getValue(), auFrameRight));
                }
            } else {
                //times computation
                //start keyframe : all influences at original position
                double start = gaze.getStart().getValue();
                //ready and relax will be recomputed according to the influence just from the gaze (gazeshift has start and ready that corresponds to the end of movement)
                double ready = gaze.getTimeMarker("ready").getValue();
                double relax = gaze.getTimeMarker("relax").getValue();
                //end keyframe : all influences at original position
                double end = gaze.getEnd().getValue();

                List<Double> timesWithEyesKeyframes = new ArrayList<>(); // to store the times including the eyes movement
                for (Keyframe keyframe : outputKeyframe) { // for each keyframe check the offset
                    if (keyframe.getOffset() >= start && keyframe.getOffset() <= end
                            && (keyframe instanceof HeadKeyframe || keyframe instanceof TorsoKeyframe)) {
                        // if the offset is between start and end times of the eyes
                        timesWithEyesKeyframes.add(keyframe.getOffset());
                    }
                }

                // check the influence. if null the influence is automatically calculated according to the gaze rotation angle
                // - after 15° the head move
                // - with a gaze rotation more than 20, the shoulder start to move
                // - with a gaze rotation more than 30, the all torso start to move
                Influence gazeInfluence = computeGazeInfluence(gaze, headAngles);

                /*********************************************************************************************************************************************************************************************************************/

                // calculate the SHOULDER max speed depending on the rotation angle
                double maxVelShoulder = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(Math.abs(shouldersAngles.shoulderMinimumAlign*TORSO_YAW_LIMIT))/15 + 2)*Math.toDegrees(TORSO_ANGULAR_SPEED)));
                double timeShoulderAtTarget;

                // HEAD
                double Amin;
                // double AminPitch; // TODO FIXME AminPitch is never used, why keep it ?
                double timeHeadAtTarget;
                double eyesMaxSpeed;
                // double eyesMaxSpeedPitch; // TODO FIXME eyesMaxSpeedPitch is never used, why keep it ?

                // EYES
                double timeEyesAtTarget;
                double timeBackEyesAtZero;
                double shoulderMoveTime = Math.max(Math.abs(shouldersAngles.shoulderMinimumAlign), Math.abs(shouldersAngles.shoulderLimitedPitch)) / maxVelShoulder;

                if (gazeInfluence.ordinal()> Influence.HEAD.ordinal()) { // time head reach the target position and come back
                    timeShoulderAtTarget = start + shouldersAngles.shoulderLatency + shoulderMoveTime;

                    // set ready and relax
                    ready = timeShoulderAtTarget;
                    if (end == 0) {
                        relax = timeShoulderAtTarget + 0.2;// set as ready the time the last body parte reach the target position
                    }

                    double timeBackShoulderAtZero = relax + shoulderMoveTime;

                    // if end is not set, we put the timeback of the last bodypart
                    if (end == 0) {
                        end = timeBackShoulderAtZero;
                    }

                    timesWithEyesKeyframes.add(timeShoulderAtTarget);
                    timesWithEyesKeyframes.add(timeBackShoulderAtZero);

                    // calculate the head max speed depending on the rotation angle
                    double maxVelHead = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(shouldersAngles.headAngles.headLimitedYaw)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    /*if (shouldersAngles.headAngles.headLimitedPitch < 0) {
                        double headMaxSpeedPitch = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(shouldersAngles.headAngles.headLimitedPitch)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    } else {
                        double headMaxSpeedPitch = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(shouldersAngles.headAngles.headLimitedPitch)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    }*/ // TODO FIXME headMaxSpeedPitch is never used, why keep it ?
                    // time head reach the target position and come back
                    double headMoveTime = Math.max(Math.abs(shouldersAngles.headAngles.headLimitedYaw), Math.abs(shouldersAngles.headAngles.headLimitedPitch)) / maxVelHead;
                    timeHeadAtTarget = start + headLatency + headMoveTime; // 0.1 is the latency time
                    double timeBackHeadAtZero = relax + headMoveTime;
                    timesWithEyesKeyframes.add(timeHeadAtTarget);
                    timesWithEyesKeyframes.add(timeBackHeadAtZero);

                    // calculate the max speed of the head depending on the rotation angle
                    Amin = Math.toDegrees(Math.abs(Math.min(shouldersAngles.headAngles.leftEyeLimitedYaw * EYES_YAW_LIMIT, shouldersAngles.headAngles.rightEyeLimitedYaw * EYES_YAW_LIMIT)));
                    //AminPitch = Math.toDegrees(Math.abs(Math.min(shouldersAngles.headAngles.leftLimitedPitch*EYES_PITCH_LIMIT, shouldersAngles.headAngles.rightLimitedPitch*EYES_PITCH_LIMIT)));
                    eyesMaxSpeed = Math.toRadians((2*Amin/75 + 1f/6)*Math.toDegrees(EYES_ANGULAR_SPEED));
                    //eyesMaxSpeedPitch = Math.toRadians((2*AminPitch/75 + 1f/6)*Math.toDegrees(EYES_ANGULAR_SPEED));
                    timeEyesAtTarget = Math.min(start +Math.abs(shouldersAngles.headAngles.leftEyeLimitedYaw * EYES_YAW_LIMIT)/ eyesMaxSpeed, start + Math.abs(shouldersAngles.headAngles.rightEyeLimitedYaw *EYES_YAW_LIMIT)/ eyesMaxSpeed);
                    timeBackEyesAtZero = Math.min(relax + Math.abs(shouldersAngles.headAngles.leftEyeLimitedYaw * EYES_YAW_LIMIT)/ eyesMaxSpeed, relax + Math.abs(shouldersAngles.headAngles.rightEyeLimitedYaw *EYES_YAW_LIMIT)/ eyesMaxSpeed);
                    //timeBackEyesAtZero = Math.min(relax + Math.max(Math.abs(shouldersAngles.headAngles.leftLimitedYaw), Math.abs(shouldersAngles.headAngles.leftLimitedPitch))/ eyesMaxSpeed, relax + Math.max(Math.abs(shouldersAngles.headAngles.rightLimitedYaw), Math.abs(shouldersAngles.headAngles.rightLimitedPitch)) / eyesMaxSpeed);
                    timesWithEyesKeyframes.add(timeEyesAtTarget);
                    timesWithEyesKeyframes.add(timeBackEyesAtZero);
                } else if (gazeInfluence.ordinal()>Influence.EYES.ordinal()) {
                    // calculate the head max speed depending on the rotation angle
                    double maxVelHead = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(headAngles.headLimitedYaw)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    /*if (shouldersAngles.headAngles.headLimitedPitch < 0) {
                        double headMaxSpeedPitch = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(headAngles.headLimitedPitch)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    } else {
                        double headMaxSpeedPitch = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(headAngles.headLimitedPitch)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    }*/ // TODO FIXME headMaxSpeedPitch is never used, why keep it ?
                    // time head reach the target position and come back
                    double headMoveTime = Math.max(Math.abs(headAngles.headLimitedYaw), Math.abs(headAngles.headLimitedPitch)) / maxVelHead;
                    timeHeadAtTarget = start + headLatency + headMoveTime; // 0.1 is the latency time

                    // set ready and relax
                    ready = timeHeadAtTarget;
                    if (end == 0) {
                        relax = ready + 0.4; // 0.4 is indicative
                    }

                    double timeBackHeadAtZero = relax + headMoveTime;

                    // if end is not setted, we put the timeback of the last bodypart
                    if (end == 0) {
                        end = timeBackHeadAtZero;
                    }

                    timesWithEyesKeyframes.add(timeHeadAtTarget);
                    timesWithEyesKeyframes.add(timeBackHeadAtZero);

                    // calculate the max speed of the head depending on the rotation angle
                    Amin = Math.toDegrees(Math.abs(Math.min(Math.abs(headAngles.leftEyeLimitedYaw)*EYES_YAW_LIMIT, Math.abs(headAngles.rightEyeLimitedYaw)*EYES_YAW_LIMIT)));
                    //AminPitch = Math.toDegrees(Math.abs(Math.min(headAngles.leftLimitedPitch*EYES_PITCH_LIMIT, headAngles.rightLimitedPitch*EYES_PITCH_LIMIT)));
                    eyesMaxSpeed = Math.toRadians((2*Amin/75 + (1f/6))*Math.toDegrees(EYES_ANGULAR_SPEED));
                    //eyesMaxSpeedPitch = Math.toRadians((2*AminPitch/75 + 1f/6)*Math.toDegrees(EYES_ANGULAR_SPEED));
                    double leftEyeTime = Math.abs(headAngles.leftEyeLimitedYaw*EYES_YAW_LIMIT)/eyesMaxSpeed;
                    double rightEyeTime = Math.abs(headAngles.rightEyeLimitedYaw*EYES_YAW_LIMIT)/eyesMaxSpeed;
                    timeEyesAtTarget = Math.min(start + leftEyeTime, start + rightEyeTime);
                    timeBackEyesAtZero = Math.min(relax + leftEyeTime, relax + rightEyeTime);
                    //timeEyesAtTarget = Math.min(start + Math.max(Math.abs(headAngles.leftLimitedYaw*EYES_YAW_LIMIT), Math.abs(headAngles.leftLimitedPitch*EYES_PITCH_LIMIT))/ eyesMaxSpeed, start + Math.max(Math.abs(headAngles.rightLimitedYaw*EYES_YAW_LIMIT), Math.abs(headAngles.rightLimitedPitch*EYES_PITCH_LIMIT))/ eyesMaxSpeed);
                    //timeBackEyesAtZero = Math.min(relax + Math.max(Math.abs(headAngles.leftLimitedYaw*EYES_YAW_LIMIT), Math.abs(headAngles.leftLimitedPitch*EYES_PITCH_LIMIT))/ eyesMaxSpeed, relax + Math.max(Math.abs(headAngles.rightLimitedYaw*EYES_YAW_LIMIT), Math.abs(headAngles.rightLimitedPitch*EYES_PITCH_LIMIT)) / eyesMaxSpeed);
                    timesWithEyesKeyframes.add(timeEyesAtTarget);
                    timesWithEyesKeyframes.add(timeBackEyesAtZero);
                } else {
                    // calculate the max speed of the head depending on the rotation angle
                    Amin = Math.toDegrees(Math.abs(Math.min(headAngles.leftEyeLimitedYaw*EYES_YAW_LIMIT, headAngles.rightEyeLimitedYaw*EYES_YAW_LIMIT)));
                    //AminPitch = Math.toDegrees(Math.abs(Math.min(headAngles.leftLimitedPitch*EYES_PITCH_LIMIT, headAngles.rightLimitedPitch*EYES_PITCH_LIMIT)));
                    eyesMaxSpeed = Math.toRadians((2*Amin/75 + 1f/6)*Math.toDegrees(EYES_ANGULAR_SPEED));
                    //eyesMaxSpeedPitch = Math.toRadians((2*AminPitch/75 + 1f/6)*Math.toDegrees(EYES_ANGULAR_SPEED));
                    double leftEyeTime = Math.abs(headAngles.leftEyeLimitedYaw*EYES_YAW_LIMIT)/eyesMaxSpeed;
                    double rightEyeTime = Math.abs(headAngles.rightEyeLimitedYaw*EYES_YAW_LIMIT)/eyesMaxSpeed;
                    timeEyesAtTarget = Math.min(start + leftEyeTime, start + rightEyeTime);
                    timeBackEyesAtZero = Math.min(relax + leftEyeTime, relax + rightEyeTime);
                    timesWithEyesKeyframes.add(timeBackEyesAtZero);
                }

                //else, just use ready and relax of the gaze
                timesWithEyesKeyframes.add(ready);
                timesWithEyesKeyframes.add(relax);

                // sort the times frame
                Collections.sort(timesWithEyesKeyframes);

                // check if the shoulder start move after the head
                if (headLatency > shouldersAngles.shoulderLatency) {
                    headLatency = 0;
                }

                //add to outputKeyframe the eyes keyframe at START time
                if (!gaze.isGazeShift()) {
                    addEyesAUKeyFrame(outputKeyframe, gazeId + "_to", "start", start, Side.LEFT, headAngles.leftEyeGazeDirection, 0, 0);
                    addEyesAUKeyFrame(outputKeyframe, gazeId + "_to", "start", start, Side.RIGHT, headAngles.rightEyeGazeDirection, 0, 0);
                }

                // add Keyframes for the eyes at every moment there is a body keyframe between start and end
                // for instance, when there is a nod, the eyes should keep on the target
                if (!timesWithEyesKeyframes.isEmpty()) {
                    Double latestTime = start;

                    double leftLimitYaw = 0;
                    double rightLimitYaw = 0;
                    double leftLimitPitch = 0;
                    double rightLimitPitch = 0;

                    for (Double time : timesWithEyesKeyframes) {
                        if (time > start && time < end && latestTime.compareTo(time) != 0) {
                            /*************************************************
                             * TO REACH THE TARGET
                             **************************************************/
                            if (time <= relax) {
                                /* -------------------------------------------------
                                 * INFLUENCE INVOLVES TORSO OR SHOULDER
                                 * --------------------------------------------------*/
                                if (gazeInfluence.ordinal() >= Influence.SHOULDER.ordinal()) {
                                    // if the eyes reach already the target position
                                    if (time >= timeEyesAtTarget) {
                                        leftLimitYaw = shouldersAngles.headAngles.leftEyeLimitedYaw;
                                        rightLimitYaw = shouldersAngles.headAngles.rightEyeLimitedYaw;
                                        leftLimitPitch = shouldersAngles.headAngles.leftEyeLimitedPitch;
                                        rightLimitPitch = shouldersAngles.headAngles.rightEyeLimitedPitch;
                                    }
                                }
                                /* -------------------------------------------------
                                 * INFLUENCE INVOLVES THE HEAD
                                 * -------------------------------------------------- */
                                else if (gaze.getInfluence().ordinal() >= Influence.HEAD.ordinal()) {
                                    // if the eyes reach already the target position
                                    if (time >= timeEyesAtTarget) {
                                        leftLimitYaw = headAngles.leftEyeLimitedYaw;
                                        rightLimitYaw = headAngles.rightEyeLimitedYaw;
                                        leftLimitPitch = headAngles.leftEyeLimitedPitch;
                                        rightLimitPitch = headAngles.rightEyeLimitedPitch;
                                    }
                                } else {
                                    /* -------------------------------------------------
                                     * INFLUENCE INVOLVES JUST THE EYES
                                     * -------------------------------------------------- */
                                    // if the eyes reach already the target position
                                    if ( time >= timeEyesAtTarget){
                                        leftLimitYaw = Math.min(Math.abs(headAngles.leftEyeLimitedYaw), EYES_YAW_LIMIT);
                                        rightLimitYaw = Math.min(Math.abs(headAngles.rightEyeLimitedYaw), EYES_YAW_LIMIT);
                                        leftLimitPitch = Math.min(Math.abs(headAngles.leftEyeLimitedPitch), EYES_PITCH_LIMIT);
                                        rightLimitPitch = Math.min(Math.abs(headAngles.rightEyeLimitedPitch), EYES_PITCH_LIMIT);
                                    }
                                }
                                /*************************************************
                                 * BACK TO ZERO
                                 **************************************************/
                            } else if (time > relax && time < end && !gaze.isGazeShift()) {
                                // if the eyes reach already the target position
                                if (time >= timeBackEyesAtZero) {
                                    if (gazeStillInProgress()) {
                                        handleGazeStillInProgress(time, outputKeyframe);

                                        leftLimitYaw = -10;
                                        rightLimitYaw = -10;
                                        leftLimitPitch = -10;
                                        rightLimitPitch = -10;
                                    } else {
                                        leftLimitYaw = 0;
                                        rightLimitYaw = 0;
                                        leftLimitPitch = 0;
                                        rightLimitPitch = 0;
                                    }
                                }
                            }

                            // add eyeskeyframe
                            if (leftLimitYaw != -10 && rightLimitYaw != -10 && leftLimitPitch != -10 && rightLimitPitch != -10) {
                                addEyesAUKeyFrame(outputKeyframe, gazeId + "_to", gazeId + "_to_kf" + time, time, Side.LEFT,
                                        headAngles.leftEyeGazeDirection, leftLimitYaw, leftLimitPitch);
                                addEyesAUKeyFrame(outputKeyframe, gazeId + "_to", gazeId + "_to_kf" + time, time, Side.RIGHT,
                                        headAngles.rightEyeGazeDirection, rightLimitYaw, rightLimitPitch);
                            }

                            latestTime = time;
                        }
                    }

                    // Add gazekeyframe for the END of the gaze
                    if (!gaze.isGazeShift()) {
                        if (gazeStillInProgress()) {
                            handleGazeStillInProgress(end, outputKeyframe);
                        } else {
                            addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", end, Side.LEFT, GazeDirection.FRONT, 0, 0);
                            addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", end, Side.RIGHT, GazeDirection.FRONT, 0, 0);

                            AUAPFrame auFrameLeft = createAUAPFrameForEyeSide(Side.LEFT);
                            AUAPFrame auFrameRight = createAUAPFrameForEyeSide(Side.RIGHT);
                            setGazeRestPosition(new AUKeyFrame(gazeId + "_back", end, auFrameLeft), new AUKeyFrame(gazeId + "_back", end, auFrameRight));
                        }
                    } else {
                        addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", timeBackEyesAtZero, Side.LEFT, headAngles.leftEyeGazeDirection, leftLimitYaw, leftLimitPitch);
                        addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", timeBackEyesAtZero, Side.RIGHT, headAngles.rightEyeGazeDirection, rightLimitYaw, rightLimitPitch);

                        AUAPFrame auFrameLeft = new AUAPFrame();
                        AUAPFrame auFrameRight = new AUAPFrame();
                        //AU61: eyes turn left
                        if (headAngles.leftEyeGazeDirection == GazeDirection.DOWNLEFT || headAngles.leftEyeGazeDirection == GazeDirection.LEFT || headAngles.leftEyeGazeDirection == GazeDirection.UPLEFT) {
                            auFrameLeft.setAUAP(61, leftLimitYaw / EYES_YAW_LIMIT, Side.LEFT);
                            auFrameRight.setAUAP(61, rightLimitYaw / EYES_YAW_LIMIT, Side.RIGHT);
                        }
                        //AU62: eyes turn right
                        if (headAngles.leftEyeGazeDirection == GazeDirection.DOWNRIGHT || headAngles.leftEyeGazeDirection == GazeDirection.RIGHT || headAngles.leftEyeGazeDirection == GazeDirection.UPRIGHT) {
                            auFrameLeft.setAUAP(62, leftLimitYaw / EYES_YAW_LIMIT, Side.LEFT);
                            auFrameRight.setAUAP(62, rightLimitYaw / EYES_YAW_LIMIT, Side.RIGHT);
                        }
                        //AU63: eyes up
                        if (headAngles.leftEyeGazeDirection == GazeDirection.UPRIGHT || headAngles.leftEyeGazeDirection == GazeDirection.UP || headAngles.leftEyeGazeDirection == GazeDirection.UPLEFT) {
                            auFrameLeft.setAUAP(63, leftLimitPitch / EYES_PITCH_LIMIT, Side.LEFT);
                            auFrameRight.setAUAP(63, rightLimitYaw / EYES_YAW_LIMIT, Side.RIGHT);
                        }
                        //AU64: eyes down
                        if (headAngles.leftEyeGazeDirection == GazeDirection.DOWNRIGHT || headAngles.leftEyeGazeDirection == GazeDirection.DOWN || headAngles.leftEyeGazeDirection == GazeDirection.DOWNLEFT) {
                            auFrameLeft.setAUAP(64, leftLimitPitch / EYES_PITCH_LIMIT, Side.LEFT);
                            auFrameRight.setAUAP(64, rightLimitYaw / EYES_YAW_LIMIT, Side.RIGHT);
                        }

                        /*//AU61: eyes turn left
                        if (headAngles.leftGazeDirection == GazeDirection.DOWNLEFT || headAngles.leftGazeDirection == GazeDirection.LEFT || headAngles.leftGazeDirection == GazeDirection.UPLEFT) {
                            auFrameRight.setAUAP(61, rightLimitYaw / EYES_YAW_LIMIT, Side.RIGHT);
                        }
                        //AU62: eyes turn right
                        if (headAngles.leftGazeDirection == GazeDirection.DOWNRIGHT || headAngles.leftGazeDirection == GazeDirection.RIGHT || headAngles.leftGazeDirection == GazeDirection.UPRIGHT) {
                            auFrameRight.setAUAP(62, rightLimitYaw / EYES_YAW_LIMIT, Side.RIGHT);
                        }
                        //AU63: eyes up
                        if (headAngles.leftGazeDirection == GazeDirection.UPRIGHT || headAngles.leftGazeDirection == GazeDirection.UP || headAngles.leftGazeDirection == GazeDirection.UPLEFT) {
                            auFrameRight.setAUAP(63, rightLimitPitch / EYES_PITCH_LIMIT, Side.RIGHT);
                        }
                        //AU64: eyes down
                        if (headAngles.leftGazeDirection == GazeDirection.DOWNRIGHT || headAngles.leftGazeDirection == GazeDirection.DOWN || headAngles.leftGazeDirection == GazeDirection.DOWNLEFT) {
                            auFrameRight.setAUAP(64, rightLimitPitch / EYES_PITCH_LIMIT, Side.RIGHT);
                        }*/

                        setGazeRestPosition(new AUKeyFrame(gazeId + "_back", end, auFrameLeft), new AUKeyFrame(gazeId + "_back", end, auFrameRight));
                    }
                }
            }
        }

        signals.clear();
        return outputKeyframe;
    }

    //not to be used anymore, because gaze doesn't work as other keyframe generators anymore.
    //we used to compute everything here, however then we cannot handle all influence cases.
    //therefore, now we compute body keyframes for GazeSignals first, then we compute other body keyframes
    //then we finish with eyes keyframes
    @Override
    protected void generateKeyframes(List<Signal> inputSignals, List<Keyframe> outputKeyframe) {
        //
    }

    private static Comparator<Signal> gazeComp = new Comparator<Signal>() {
        private double getValue(Signal s) {
            return (s instanceof GazeSignal) && ((GazeSignal) s).isGazeShift()
                    ? s.getTimeMarker("ready").getValue()
                    : s.getEnd().getValue();
        }

        @Override
        public int compare(Signal o1, Signal o2) {
            return (int) Math.signum(getValue(o1) - getValue(o2));
        }
    };

    @Override
    protected Comparator<Signal> getComparator() {
        return gazeComp;
    }


    /**
     * Add a {@see Keyframe} for the eyes movement.
     *
     * @param outputKeyframe List of {@code Keyframe} where we add the eye
     * {@code Keyframe}.
     * @param gazeId ID of the {@code GazeSignal} this {@code Keyframe} is part
     * of.
     * @param timeMarkerName Name of TimeMaker. Unused in the new
     * FaceKeyframePerformer: to remove ?
     * @param time Time of {@code Keyframe}.
     * @param side {@code Side} of the eye: (@code "left"} or {@code "right"}.
     * @param gazeDirection {@code GazeDirection} of the eye.
     * @param yaw Ratio of horizontal movement of the eye compared to physical
     * eyeball limits.
     * @param pitch Ratio of vertical movement of the eye compared to physical
     * eyeball limits.
     */
    private void addEyesAUKeyFrame(List<Keyframe> outputKeyframe, String gazeId, String timeMarkerName, double time, // TODO FIXME timeMarkerName is never used, why keep it ?
                                   Side side, GazeDirection gazeDirection, double yaw, double pitch) {
        AUAPFrame auFrame = new AUAPFrame((int) (time * Constants.FRAME_PER_SECOND));
        boolean changed = false;

        if (gazeDirection.equals(GazeDirection.FRONT)) {
            setupAUAPFrameForEyeSide(auFrame, side);
            AUKeyFrame auKeyFrame = new AUKeyFrame(gazeId, time, auFrame);
            outputKeyframe.add(auKeyFrame);
        } else {
            //AU61: eyes turn left
            if (gazeDirection == GazeDirection.DOWNLEFT || gazeDirection == GazeDirection.LEFT || gazeDirection == GazeDirection.UPLEFT) {
                auFrame.setAUAP(61, yaw / EYES_YAW_LIMIT, side);
                changed = true;
            }
            //AU62: eyes turn right
            if (gazeDirection == GazeDirection.DOWNRIGHT || gazeDirection == GazeDirection.RIGHT || gazeDirection == GazeDirection.UPRIGHT) {
                auFrame.setAUAP(62, yaw / EYES_YAW_LIMIT, side);
                changed = true;
            }
            //AU63: eyes up
            if (gazeDirection == GazeDirection.UPRIGHT || gazeDirection == GazeDirection.UP || gazeDirection == GazeDirection.UPLEFT) {
                auFrame.setAUAP(63, pitch / EYES_PITCH_LIMIT, side);
                changed = true;
            }
            //AU64: eyes down
            if (gazeDirection == GazeDirection.DOWNRIGHT || gazeDirection == GazeDirection.DOWN || gazeDirection == GazeDirection.DOWNLEFT) {
                auFrame.setAUAP(64, pitch / EYES_PITCH_LIMIT, side);
                changed = true;
            }

            if (changed) {
                AUKeyFrame auKeyFrame = new AUKeyFrame(gazeId, time, auFrame);
                outputKeyframe.add(auKeyFrame);
            }
        }
    }

    /**
     * Creates a SpinePhase for the head/shoulder movements of the gaze signal.
     *
     * @param type Type of {@code SpinePhase}: {@code start}, {@code end}...
     * @param startTime Start time of the {@code SpinePhase}.
     * @param endTime End time of the {@code SpinePhase}.
     * @param limitedYaw Signed ratio of horizontal movement of the head
     * compared to physical head limits.
     * @param limitedPitch Signed ratio of vertical movement of the head
     * compared to physical head limits.
     * @return The created {@code SpinePhase}.
     */
    private SpinePhase createSpinePhase(String type, double startTime, double endTime, double limitedYaw, double limitedPitch) {
        SpinePhase spinePhase = new SpinePhase(type, startTime, endTime);
        SpineDirection verticalTorsionDirection = new SpineDirection();
        SpineDirection sagittalTiltDirection = new SpineDirection();
        if (limitedYaw != 0) {
            verticalTorsionDirection.direction = limitedYaw > 0 ? SpineDirection.Direction.LEFTWARD : SpineDirection.Direction.RIGHTWARD;
            verticalTorsionDirection.flag = true;
            verticalTorsionDirection.value = Math.abs(limitedYaw);
        }
        if (limitedPitch != 0) {
            sagittalTiltDirection.direction = limitedPitch > 0 ? SpineDirection.Direction.BACKWARD : SpineDirection.Direction.FORWARD;
            sagittalTiltDirection.flag = true;
            sagittalTiltDirection.value = Math.abs(limitedPitch);
        }

        spinePhase.verticalTorsion = verticalTorsionDirection;
        spinePhase.sagittalTilt = sagittalTiltDirection;
        return spinePhase;
    }

    /**
     * Computes a GazeDirection given horizontal and vertical movements for the gaze.
     *
     * @param eyeYawAngle Horizontal gaze angle.
     * @param eyePitchAngle Vertical gaze angle.
     * @return The computed {@code GazeDirection}.
     */
    private GazeDirection computeGazeDirection(double eyeYawAngle, double eyePitchAngle) {
        if (Double.compare(0, eyeYawAngle) == 0
                && Double.compare(0, eyePitchAngle) == 0) {
            return GazeDirection.FRONT;
        }

        GazeDirection gazeDirection = GazeDirection.FRONT;
        if (eyeYawAngle <= 1E-6 && eyeYawAngle >= -1E-6) {
            if (eyePitchAngle > 1E-6) {
                gazeDirection = GazeDirection.UP;
            } else if (eyePitchAngle < -1E-6) {
                gazeDirection = GazeDirection.DOWN;
            }
            // if eyePitchAngle <= 1E-6 && eyePitchAngle >= -1E-6, gaze direction stays FRONT
        } else if (eyeYawAngle > 1E-6) {
            if (eyePitchAngle > 1E-6) {
                gazeDirection = GazeDirection.UPLEFT;
            } else if (eyePitchAngle < -1E-6) {
                gazeDirection = GazeDirection.DOWNLEFT;
            } else if (eyePitchAngle <= 1E-6 && eyePitchAngle >= -1E-6) {
                gazeDirection = GazeDirection.LEFT;
            }
        } else if (eyeYawAngle < 1E-6) {
            if (eyePitchAngle > 1E-6) {
                gazeDirection = GazeDirection.UPRIGHT;
            } else if (eyePitchAngle < -1E-6) {
                gazeDirection = GazeDirection.DOWNRIGHT;
            } else if (eyePitchAngle <= 1E-6 && eyePitchAngle >= -1E-6) {
                gazeDirection = GazeDirection.RIGHT;
            }
        }
        return gazeDirection;
    }

    /**
     * Interpolates a {@code HeadKeyframe} to use for eye angles computation.
     *
     * @param time The time where we want to interpolate a {@code HeadKeyframe}.
     * @param keyframes The list of {@code HeadKeyframes}.
     * @param headKeyframeGenerator The {@code HeadKeyframeGenerator}, used to do the
     * interpolation.
     * @return The computed {@code HeadKeyframe}.
     */
    private HeadKeyframe getHeadKeyframeAtTime(double time, List<Keyframe> keyframes, // TODO FIXME Is this method called anywhere ? If not, to be deleted.
                                               HeadKeyframeGenerator headKeyframeGenerator, double start, double end) {
        HeadKeyframe previousHeadKeyframe = findClosestHeadKeyframeAtTime(time, keyframes, true);
        HeadKeyframe nextHeadKeyframe = findClosestHeadKeyframeAtTime(time, keyframes, false);

        if (previousHeadKeyframe == null && nextHeadKeyframe != null) {
            previousHeadKeyframe = new HeadKeyframe ("head", new SpinePhase("end", start, start), "Neutral");
            return headKeyframeGenerator.interpolate(previousHeadKeyframe, nextHeadKeyframe, time);
            //return nextHeadKeyframe;
        } else if (previousHeadKeyframe != null) {
            if (nextHeadKeyframe == null) {
                nextHeadKeyframe = new HeadKeyframe ("head", new SpinePhase("end", end, end), "Neutral");
                return headKeyframeGenerator.interpolate(previousHeadKeyframe, nextHeadKeyframe, time);
                //return previousHeadKeyframe;
            } else {
                if (previousHeadKeyframe != nextHeadKeyframe) {
                    return headKeyframeGenerator.interpolate(previousHeadKeyframe, nextHeadKeyframe, time);
                } else {
                    return previousHeadKeyframe;
                }
            }
        } else {
            return null;
        }
    }

    private TorsoKeyframe getTorsoKeyframeAtTime(double time, List<Keyframe> keyframes, TorsoKeyframeGenerator torsoKeyframeGenerator) {
        // TODO FIXME Is this method called anywhere ? If not, to be deleted.
        // TODO FIXME torsoKeyframeGenerator is never used, why keep it ?
        TorsoKeyframe previousShoulderKeyframe = findClosestTorsoKeyframeAtTime(time, keyframes, true);
        TorsoKeyframe nextHeadKeyframe = findClosestTorsoKeyframeAtTime(time, keyframes, false);

        if (previousShoulderKeyframe == null && nextHeadKeyframe != null) {
            return nextHeadKeyframe;
        }
        if (previousShoulderKeyframe != null) {
            if (nextHeadKeyframe == null) {
                return previousShoulderKeyframe;
            }
            TorsoKeyframe interpol;
            if (previousShoulderKeyframe != nextHeadKeyframe) {
                //if (previousShoulderKeyframe.verticalTorsion.value == nextHeadKeyframe.verticalTorsion.value) {
                double t = (time - previousShoulderKeyframe.getOffset()) / (nextHeadKeyframe.getOffset() - previousShoulderKeyframe.getOffset());
                TorsoKeyframe result = new TorsoKeyframe();

                SpineDirection verticalDirection = new SpineDirection(previousShoulderKeyframe.verticalTorsion);
                verticalDirection.inverse();
                //result = -first
                result.verticalTorsion = verticalDirection;
                result.verticalTorsion.add(nextHeadKeyframe.verticalTorsion);
                //result = second+(-first)

                //result.lateralRoll.multiply(t);
                //result.sagittalTilt.multiply(t);
                result.verticalTorsion.multiply(t);
                //result = t*(second-first)

                result.verticalTorsion.add(previousShoulderKeyframe.verticalTorsion);
                //result = t*(second-first) + first

                if (result.verticalTorsion.value > TORSO_YAW_LIMIT) {
                    result.verticalTorsion.value = 1;
                }

                result.setOffset(time);
                result.setOnset(time);

                interpol = result;

                //} else {
                //    interpol = torsoKeyframeGenerator.interpolate(previousShoulderKeyframe, nextHeadKeyframe, time);
                //}
                return interpol;
            }
            return previousShoulderKeyframe;
        }
        return null;
    }

    /**
     * Gets the {@code HeadKeyframe} in a list that precedes or follows a
     * certain time.
     *
     * @param time The time at which we want to find the closest
     * {@code HeadKeyframe}.
     * @param keyframes The list of {@code HeadKeyframes}.
     * @param previous Previous or next {@code HeadKeyframe}: {@code "true"}
     * means previous, {@code "false"} means next.
     * @return The found {@code HeadKeyframe}.
     */
    private HeadKeyframe findClosestHeadKeyframeAtTime(double time, List<Keyframe> keyframes, boolean previous) {
        return (HeadKeyframe) findClosestKeyframeAtTime(time, keyframes, previous, HeadKeyframe.class);
    }

    /**
     * Indicates whether at least one of the given keyframes of the given type can be found at the given time
     * @param time value of time where we should look for the keyframes
     * @param keyframes list of keyframes to inspect
     * @param i 1 = HeadKeyFrame, 2 = ShoulderKeyframe, 3 = TorsoKeyframe
     * @return whether one of the given keyframes exists at the given time
     */
    private boolean findExistentKeyframeAtTime(double time, List<Keyframe> keyframes, int i) { // TODO FIXME Is this method called anywhere ? If not, to be deleted.
        if (keyframes.isEmpty()) {
            return false;
        }

        Class keyframeClassToTest;
        switch (i) {
            case 1 :
                keyframeClassToTest = HeadKeyframe.class;
                break;
            case 2 :
                keyframeClassToTest = ShoulderKeyframe.class;
                break;
            case 3 :
                keyframeClassToTest = TorsoKeyframe.class;
                break;
            default :
                // Class is not recognised
                return false;
        }

        for (Keyframe keyframe : keyframes) {
            if (keyframeClassToTest.isInstance(keyframe) && keyframe.getOffset() == time) { //right modality & already exists
                return true;
            }
        }
        return false;
    }

    /**
     * Gets the {@code TorsoKeyframe} in a list that precedes or follows a
     * certain time.
     *
     * @param time The time at which we want to find the closest
     * {@code HeadKeyframe}.
     * @param keyframes The list of {@code TorsoKeyframe}.
     * @param previous Previous or next {@code TorsoKeyframe}: {@code "true"}
     * means previous, {@code "false"} means next.
     * @return The found {@code TorsoKeyframe}.
     */
    private TorsoKeyframe findClosestTorsoKeyframeAtTime(double time, List<Keyframe> keyframes, boolean previous) {
        return (TorsoKeyframe) findClosestKeyframeAtTime(time, keyframes, previous, TorsoKeyframe.class);
    }

    /**
     * Gets the {@code Keyframe} of the given class in a list that precedes or follows a
     * certain time.
     *
     * @param time The time at which we want to find the closest
     * {@code HeadKeyframe}.
     * @param keyframes The list of {@code Keyframe}.
     * @param previous Previous or next {@code Keyframe}: {@code "true"}
     * means previous, {@code "false"} means next.
     * @param keyframeClass Class of the keyframe we want to find : must extend the Keyframe class
     * @return The found {@code Keyframe}.
     */
    private Keyframe findClosestKeyframeAtTime(double time, List<Keyframe> keyframes, boolean previous, Class<? extends Keyframe> keyframeClass) {
        Keyframe closestKeyframe = null;
        if (keyframes.isEmpty()) {
            return null;
        }

        for (Keyframe keyframe : keyframes) {
            if (keyframeClass.isInstance(keyframe)) { //right modality
                if ((previous && keyframe.getOffset() <= time) || (!previous && keyframe.getOffset() >= time)) { //previous or next
                    if (closestKeyframe != null) {
                        if (Math.abs(keyframe.getOffset() - time) < Math.abs(closestKeyframe.getOffset() - time)) {
                            closestKeyframe = keyframe;
                        }
                    } else {
                        closestKeyframe = keyframe;
                    }
                }
            }
        }
        return closestKeyframe;
    }

    @Override
    public void onTreeChange(TreeEvent te) {
        // nothing special for now
    }

    @Override
    public void onNodeChange(NodeEvent nodeEvent) {
        if (!currentGazes.isEmpty()) {
            List<Signal> signals = new ArrayList<>();
            Node changedNode = env.getNode(nodeEvent.getIdNode());

            this.cleanGazeShifts();

            for (GazeSignal gazeSignal : currentGazes.keySet()) {
                if (!gazeSignal.isGazeShift() && Timer.getTimeMillis()>currentGazes.get(gazeSignal)+(long)gazeSignal.getTimeMarker("relax").getValue()*1000) {
                    currentGazes.remove(gazeSignal);
                    continue;
                }
                Node targetNode = env.getNode(gazeSignal.getTarget());
                //Node originNode = env.getNode(gazeSignal.getOrigin()+ "_AudioTreeNode");
                if (changedNode.isAncestorOf(targetNode) /* || changedNode.isAncestorOf(originNode) */) {
                    signals.add(gazeSignal);
                }
            }

            if (!signals.isEmpty()) {
                ID id = IDProvider.createID("GazeFollow");
                for (SignalPerformer signalPerformer : performers) {
                    signalPerformer.performSignals(signals, id, new Mode(CompositionType.blend));
                }
            }
        }
    }

    @Override
    public void onLeafChange(LeafEvent le) {}

    @Override
    public void addSignalPerformer(SignalPerformer sp) {
        performers.add(sp);
    }

    @Override
    public void removeSignalPerformer(SignalPerformer sp) {
        performers.remove(sp);
    }

    ///////////////////////////// TOOL FUNCTIONS ////////////////////////////////

    /**
     * If the influence of the gaze is set, returns the influence. Otherwise, computes it according to the given HeadAngles.
     * - after 15° the head moves
     * - after 20°, the shoulders move as well
     * - after 30°, the torso moves
     * @param gaze the gaze for which we want the {@link Influence}
     * @param headAngles the gaze's rotation angle
     * @return the {@link Influence} of the given {@link GazeSignal}
     */
    private Influence computeGazeInfluence(GazeSignal gaze, HeadAndEyesAngles headAngles) {
        Influence gazeInfluence = gaze.getInfluence();
        if (gazeInfluence == null) {
            double headAngle = Math.max(Math.abs(headAngles.headPitchAngle), Math.abs(headAngles.headYawAngle));
            if (headAngle > 0.523599) { // 30°
                gazeInfluence = Influence.TORSO;
            } else if (headAngle > 0.349066) { // 20°
                gazeInfluence = Influence.SHOULDER;
            } else if (headAngle > 0.261799) { // 15°
                gazeInfluence = Influence.HEAD;
            } else {
                gazeInfluence = Influence.EYES;
            }
            gaze.setInfluence(gazeInfluence);
        }
        return gazeInfluence;
    }

    /**
     * Setups torsoSignal's phases, start, end and shoulder values according to the given data.
     * @param torsoSignal signal to be setup
     * @param spinePhase phase to add to torsoSignal
     * @param startKeyframe keyframe when the signal starts
     * @param endKeyframe keyframe when the signal ends
     * @param shouldersAngles angles for the movement
     * @param gazeInfluence influence of the gaze
     */
    private void setupTorsoSignalAtPosition (TorsoSignal torsoSignal, SpinePhase spinePhase, double startKeyframe,
                                             double endKeyframe, ShouldersAngles shouldersAngles, Influence gazeInfluence) {
        setupSignal(torsoSignal, spinePhase, startKeyframe + shouldersAngles.shoulderLatency, endKeyframe);
        if (gazeInfluence.ordinal() != Influence.TORSO.ordinal()) {
            torsoSignal.shoulder = true;
        }
    }

    /**
     * Adds the given phase to the signal and sets its start and end to the given values.
     * @param signal signal to setup
     * @param phaseToAdd phase to add to the signal
     * @param startValue value to set the signal's start to
     * @param endValue value to set the signal's end to
     */
    private void setupSignal (SpineSignal signal, SpinePhase phaseToAdd, double startValue, double endValue) {
        signal.getPhases().add(phaseToAdd);
        signal.getStart().setValue(startValue);
        signal.getEnd().setValue(endValue);
    }

    /**
     * Creates and returns a {@link HeadSignal} with directionShift set to true.
     * @return the HeadSignal
     */
    private HeadSignal createHeadSignalWithDirectionShift () {
        HeadSignal headSignal = new HeadSignal(IDProvider.createID("gazegenerator").toString());
        headSignal.setDirectionShift(true);
        return headSignal;
    }

    /**
     * Adds the given {@link SpineSignal} to the first compatible KeyframeGenerator.
     * @param spineSignal the signal to be added
     */
    private void addSignalToKeyframeGenerator (SpineSignal spineSignal) {
        for (KeyframeGenerator keyframeGenerator : otherModalitiesKFGenerators) {
            if (keyframeGenerator.accept(spineSignal)) {
                return;
            }
        }
    }

    /**
     * Adds the given {@link SpineSignal} to the first KeyframeGenerator that is compatible with both.
     * @param spineSignal1 first signal to be added
     * @param spineSignal2 second signal to be added
     */
    private void addTwoSignalsToKeyframeGenerator (SpineSignal spineSignal1, SpineSignal spineSignal2) {
        for (KeyframeGenerator keyframeGenerator : otherModalitiesKFGenerators) {
            if (keyframeGenerator.accept(spineSignal1) && keyframeGenerator.accept(spineSignal2)) {
                return;
            }
        }
    }

    /**
     * Creates and setups a {@link AUAPFrame} for the given side.
     * @param side the side to be used
     * @return the setup AUAPFrame
     */
    private AUAPFrame createAUAPFrameForEyeSide (Side side) {
        AUAPFrame auFrame = new AUAPFrame();
        setupAUAPFrameForEyeSide(auFrame, side);
        return auFrame;
    }

    /**
     * Sets AUAPs 61, 62, 63 and 64 to the given {@link AUAPFrame} with the given {@link Side}.
     * @param auFrame the frame to be setup
     * @param side the side to be used
     */
    private void setupAUAPFrameForEyeSide (AUAPFrame auFrame, Side side) {
        auFrame.setAUAP(61, 0, side);
        auFrame.setAUAP(62, 0, side);
        auFrame.setAUAP(63, 0, side);
        auFrame.setAUAP(64, 0, side);
    }

    /**
     * Indicates whether a gaze is still in progress
     * @return true if a gaze is still in progress, false otherwise
     */
    private boolean gazeStillInProgress () {
        return defaultGazeLeft.getAus().getAUAP(61, Side.LEFT).getValue() != 0 || defaultGazeRight.getAus().getAUAP(62, Side.RIGHT).getValue() != 0
                || defaultGazeLeft.getAus().getAUAP(63, Side.LEFT).getValue() != 0 || defaultGazeRight.getAus().getAUAP(63, Side.RIGHT).getValue() != 0
                || defaultGazeLeft.getAus().getAUAP(64, Side.LEFT).getValue() != 0|| defaultGazeRight.getAus().getAUAP(64, Side.RIGHT).getValue() != 0;
    }

    /**
     * As gaze is still in progress, returns to the default gaze before doing whatever comes next.
     * @param time time when the default gaze must be returned to
     * @param outputKeyframes the list to  which we must add the default gaze
     */
    private void handleGazeStillInProgress (double time, List<Keyframe> outputKeyframes) {
        defaultGazeLeft.setOnset(time);
        defaultGazeLeft.setOffset(time);
        defaultGazeRight.setOnset(time);
        defaultGazeRight.setOffset(time);
        outputKeyframes.add(defaultGazeLeft);
        outputKeyframes.add(defaultGazeRight);
    }

    /**
     * Utility class to compute angles from a character's head/eyes to a target.
     */
    public class HeadAndEyesAngles {

        //directions to target for leftEye and rightEye
        public GazeDirection leftEyeGazeDirection;
        public GazeDirection rightEyeGazeDirection;

        //raw angles to target
        public double leftEyeYawAngle;
        public double leftEyePitchAngle;
        public double rightEyeYawAngle;
        public double rightEyePitchAngle;
        public double headYawAngle;
        public double headPitchAngle;

        //ratio between no movement (0) and full movement (1)
        //full movement means the physical limit of the eyeball (resp. head): it can only move for 60 degrees (resp 90) or so
        public double leftEyeLimitedYaw;
        public double leftEyeLimitedPitch;
        public double rightEyeLimitedYaw;
        public double rightEyeLimitedPitch;
        public double headLimitedYaw;
        public double headLimitedPitch;

        //can the eye reach the target without moving other modalities (is target in a 60 degrees range)
        public boolean withinEyesLimit;
        //can the head reach the target without moving other modalities
        public boolean withinHeadAndEyesLimit;

        // load the list of characters in the environment
        Vec3d posTarget = new Vec3d();
        Vec3d headPosition = new Vec3d();
        Quaternion orient = new Quaternion();

        /**
         * Copy constructor
         *
         * @param headAndEyesAngles headAndEyesAngles to copy
         */
        public HeadAndEyesAngles(HeadAndEyesAngles headAndEyesAngles) {
            this.leftEyeGazeDirection = headAndEyesAngles.leftEyeGazeDirection;
            this.rightEyeGazeDirection = headAndEyesAngles.rightEyeGazeDirection;

            this.leftEyeYawAngle = headAndEyesAngles.leftEyeYawAngle;
            this.leftEyePitchAngle = headAndEyesAngles.leftEyePitchAngle;
            this.rightEyeYawAngle = headAndEyesAngles.rightEyeYawAngle;
            this.rightEyePitchAngle = headAndEyesAngles.rightEyePitchAngle;
            this.headYawAngle = headAndEyesAngles.headYawAngle;
            this.headPitchAngle = headAndEyesAngles.headPitchAngle;

            this.leftEyeLimitedYaw = headAndEyesAngles.leftEyeLimitedYaw;
            this.leftEyeLimitedPitch = headAndEyesAngles.leftEyeLimitedPitch;
            this.rightEyeLimitedYaw = headAndEyesAngles.rightEyeLimitedYaw;
            this.rightEyeLimitedPitch = headAndEyesAngles.rightEyeLimitedPitch;
            this.headLimitedYaw = headAndEyesAngles.headLimitedYaw;
            this.headLimitedPitch = headAndEyesAngles.headLimitedPitch;

            this.withinEyesLimit = headAndEyesAngles.withinEyesLimit;
            this.withinHeadAndEyesLimit = headAndEyesAngles.withinHeadAndEyesLimit;
        }

        /**
         * Adjust the eyes angles to the target with a head keyframe. Example:
         * look to a target directly in front of you eyes while having the head
         * down: the eyes will have to move upwards a little bit
         *
         * @param headKeyframe The HeadKeyframe giving us the head position
         * @return The new HeadAngles
         */
        public HeadAndEyesAngles adjustWithHeadKeyframe(HeadKeyframe headKeyframe) { // TODO FIXME Is this method called anywhere ? If not, to be deleted.
            HeadAndEyesAngles newHeadAngles = new HeadAndEyesAngles(this);
            if (headKeyframe == null) {
                return newHeadAngles;
            }
            /*if (headKeyframe.lateralRoll.flag) {  // Empty if is a waste of time
                //no influence
            }*/
            if (headKeyframe.sagittalTilt.flag && headKeyframe.sagittalTilt.direction != null) {
                //influence on pitch
                newHeadAngles.leftEyePitchAngle = (newHeadAngles.leftEyePitchAngle * EYES_PITCH_LIMIT + headKeyframe.getSignedSagittalTilt() * HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                newHeadAngles.rightEyePitchAngle = (newHeadAngles.rightEyePitchAngle * EYES_PITCH_LIMIT + headKeyframe.getSignedSagittalTilt() * HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                newHeadAngles.headPitchAngle = (newHeadAngles.headPitchAngle * EYES_PITCH_LIMIT + headKeyframe.getSignedSagittalTilt() * HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
            }
            if (headKeyframe.verticalTorsion.flag && headKeyframe.verticalTorsion.direction != null) {
                //influence on yaw
                //newHeadAngles.leftYawAngle = (newHeadAngles.leftYawAngle - headKeyframe.getSignedVerticalTorsion())
                newHeadAngles.leftEyeYawAngle = (newHeadAngles.leftEyeYawAngle * EYES_YAW_LIMIT - headKeyframe.getSignedVerticalTorsion() * HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                newHeadAngles.rightEyeYawAngle = (newHeadAngles.rightEyeYawAngle * EYES_YAW_LIMIT - headKeyframe.getSignedVerticalTorsion() * HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                newHeadAngles.headYawAngle = (newHeadAngles.headYawAngle * EYES_YAW_LIMIT - headKeyframe.getSignedVerticalTorsion() * HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
            }

            //recompute gaze direction
            newHeadAngles.leftEyeGazeDirection = computeGazeDirection(newHeadAngles.leftEyeYawAngle, newHeadAngles.leftEyePitchAngle);
            newHeadAngles.rightEyeGazeDirection = computeGazeDirection(newHeadAngles.rightEyeYawAngle, newHeadAngles.rightEyePitchAngle);

            //limit eyes angles
            newHeadAngles.limitEyesAngles();
            return newHeadAngles;
        }

        /**
         * Compute the ratios of head movement to target, constraining it within
         * physical limits. This computes headLimitedYaw and headLimitedPitch.
         *
         * @return {@code "true"} if the target is within Head movement limits.
         */
        public boolean limitHeadAndEyesAngle() {

            withinHeadAndEyesLimit = false;

            //head limit angle
            double headYawAngleAbs = Math.abs(headYawAngle);
            double headPitchAngleAbs = Math.abs(headPitchAngle);

            if (headYawAngleAbs < HEAD_YAW_LIMIT + EYES_YAW_LIMIT
                    && ((headPitchAngle >= 0 && headPitchAngleAbs < HEAD_PITCH_LIMIT_UP + EYES_PITCH_LIMIT)
                    || (headPitchAngle <= 0 && headPitchAngleAbs < HEAD_PITCH_LIMIT_DOWN + EYES_PITCH_LIMIT))) {
                withinHeadAndEyesLimit = true;
            }

            headLimitedYaw = Math.signum(headYawAngle) * Math.min(headYawAngleAbs, HEAD_YAW_LIMIT) / HEAD_YAW_LIMIT; // Math.signum(headYawAngle) *

            if (headPitchAngle >= 0.0872665) { // 5°
                headLimitedPitch = Math.signum(headPitchAngle) * Math.min(headPitchAngleAbs, HEAD_PITCH_LIMIT_UP) / HEAD_PITCH_LIMIT_UP; // Math.signum(headPitchAngle) *
            }
            //head looks up
            if (headPitchAngle < -0.0872665) { // 5°
                headLimitedPitch = Math.signum(headPitchAngle) * Math.min(headPitchAngleAbs, HEAD_PITCH_LIMIT_DOWN) / HEAD_PITCH_LIMIT_DOWN; // Math.signum(headPitchAngle) *
            }

            return withinHeadAndEyesLimit;
        }

        /**
         * Compute the ratios of eye movement to target, constraining it within
         * physical limits. This computes leftEyeLimitedYaw, leftEyeLimitedPitch,
         * rightEyeLimitedYaw, rightEyeLimitedPitch.
         *
         * @return {@code "true"} if the target is within Eyes movement limits.
         */
        public boolean limitEyesAngles() {

            double leftEyeYawAngleAbs = Math.abs(leftEyeYawAngle);
            double rightEyeYawAngleAbs = Math.abs(rightEyeYawAngle);
            double leftEyePitchAngleAbs = Math.abs(leftEyePitchAngle);
            double rightEyePitchAngleAbs = Math.abs(rightEyePitchAngle);

            //eyes limit angle
            withinEyesLimit = leftEyeYawAngleAbs < EYES_YAW_LIMIT
                    && rightEyeYawAngleAbs < EYES_YAW_LIMIT
                    && leftEyePitchAngleAbs < EYES_PITCH_LIMIT
                    && rightEyePitchAngleAbs < EYES_PITCH_LIMIT;

            // N.B. --> limited angles for the eyes have to be positive for both rotation direction
            leftEyeLimitedYaw = Math.abs((Math.min(leftEyeYawAngleAbs, EYES_YAW_LIMIT) - (Math.abs(headLimitedYaw) * HEAD_YAW_LIMIT)) / EYES_YAW_LIMIT); // Math.signum(leftEyeYawAngle) *
            rightEyeLimitedYaw = Math.abs((Math.min(rightEyeYawAngleAbs, EYES_YAW_LIMIT) - (Math.abs(headLimitedYaw) * HEAD_YAW_LIMIT)) / EYES_YAW_LIMIT); // Math.signum(rightEyeYawAngle) *
            leftEyeLimitedPitch = Math.abs((Math.min(leftEyePitchAngleAbs, EYES_PITCH_LIMIT) - (Math.abs(headLimitedPitch) * HEAD_PITCH_LIMIT_UP)) / EYES_PITCH_LIMIT); // ToDo: distinguish between up and down head pitch limit. now the two values are equal.
            rightEyeLimitedPitch = Math.abs((Math.min(rightEyePitchAngleAbs, EYES_PITCH_LIMIT) - (Math.abs(headLimitedPitch) * HEAD_PITCH_LIMIT_UP)) / EYES_PITCH_LIMIT);

            return withinEyesLimit;
        }

        /**
         * Constructor. Computes the head and eyes angles to a target with
         * offset positions.
         */
        public HeadAndEyesAngles(Environment env, GazeSignal gaze) {

            Vec3d leftEyeRelativeEulerAngles, rightEyeRelativeEulerAngles, headRelativeEulerAngles;

            //euler angles to target + offset, for left eye, right eye, head
            leftEyeYawAngle = 0;
            rightEyeYawAngle = 0;
            headYawAngle = 0;
            leftEyePitchAngle = 0;
            rightEyePitchAngle = 0;
            headPitchAngle = 0;

            //ratio of eye/head movement
            leftEyeLimitedYaw = 0;
            leftEyeLimitedPitch = 0;
            rightEyeLimitedYaw = 0;
            rightEyeLimitedPitch = 0;
            headLimitedYaw = 0;
            headLimitedPitch = 0;

            withinEyesLimit = false;
            withinHeadAndEyesLimit = false;

            // load the list of characters in the environment
            List<String> charactersInScene = new ArrayList<>();
            for (Node node : env.getTreeNode().getChildren()) {
                if (node instanceof MPEG4Animatable) {
                    MPEG4Animatable character = (MPEG4Animatable) node;
                    charactersInScene.add(character.getCharacterManager().getCurrentCharacterName());
                }
            }

            // take the MPEG4 for the agent target, i.e. the agent to look at
            MPEG4Animatable targetAgent = null;
            // take the MPEG4 for the agent whom is performing the gaze
            MPEG4Animatable currentAgent = null;
            if (gaze.getTarget() != null) {
                boolean gazeTargetNotEmpty = !gaze.getTarget().isEmpty();
                if (gazeTargetNotEmpty) {
                    for (Node node : env.getTreeNode().getChildren()) {
                        if (node instanceof MPEG4Animatable) {
                            MPEG4Animatable character = (MPEG4Animatable) node;
                            String currentCharacterName = character.getCharacterManager().getCurrentCharacterName();
                            if (currentCharacterName.equals(gaze.getTarget())) {
                                targetAgent = (MPEG4Animatable) node;
                            }
                            if (currentCharacterName.equals(gaze.getCharacterManager().getCurrentCharacterName())) {
                                currentAgent = (MPEG4Animatable) node;
                            }
                        }
                    }
                }

                //can compute angles to target only if we have an environment
                if (!gaze.getTarget().equals(cm.getCurrentCharacterName())) {
                    Node targetNode = null;
                    Vec3d sizeTarget = null;
                    String idTarget = "";
                    Vec3d vec2target = null;

                    if (gazeTargetNotEmpty) {
                        List<Leaf> environmentLeaves = env.getListLeaf();
                        String gazeTarget = gaze.getTarget();
                        if (gazeTarget.equals("Camera")) {
                            for (EnvironmentEventListener environmentEventListener : env.getListeners()) {
                                String listenerClassName = environmentEventListener.getClass().toString();
                                if (listenerClassName.contains("Mixer")) {
                                    Mixer camera = (Mixer) environmentEventListener;
                                    vec2target = camera.getGlobalCoordinates();
                                    break;
                                }
                            }
                        } else if (gazeTarget.equals("user")) {
                            Animatable user = (Animatable) env.getNode("user");
                            vec2target = user.getCoordinates();
                        } else {
                            boolean targetIsAgent = false;
                            //Check first if the target is an agent
                            for (String agent : charactersInScene) {
                                if (gazeTarget.equals(agent)) {
                                    targetIsAgent = true;

                                    // position head
                                    Vec3d headAgent = new Vec3d(targetAgent.getHeadNode().getCoordinateX(),
                                            targetAgent.getHeadNode().getCoordinateY(),
                                            targetAgent.getHeadNode().getCoordinateZ());

                                    // position skeleton
                                    Vec3d positionAgent = new Vec3d(targetAgent.getCoordinateX(),
                                            targetAgent.getCoordinateY(),
                                            targetAgent.getCoordinateZ());

                                    // orientation skeleton
                                /*Quaternion OrientAgent = new Quaternion( targetAgent.getRotationNode().getOrientation().x(),
                                                                            targetAgent.getRotationNode().getOrientation().y(),
                                                                                targetAgent.getRotationNode().getOrientation().z(),
                                                                                    targetAgent.getRotationNode().getOrientation().w());*/

                                    //Vec3d AgLeftEye = Vec3d.addition(headAgent, OrientAgent.rotate(headAnglesLeftEyeOffset));
                                    //Vec3d AgRightEye = Vec3d.addition(headAgent, OrientAgent.rotate(headAnglesRightEyeOffset));

                                    // find the point in the middle between the two eyes
                                    vec2target = new Vec3d(positionAgent.x(), headAgent.y(), positionAgent.z());
                                    break;
                                }
                            }
                            // if the target is not an agent I look the target in the environment objects
                            if (!targetIsAgent) {
                                // search the object (leaf) between environment objects
                                for (Leaf leafToCheck : environmentLeaves) {
                                    // once we find the object, take the ID
                                    if (leafToCheck.getIdentifier().equals(gaze.getTarget())) {
                                        idTarget = leafToCheck.getIdentifier();
                                        sizeTarget = leafToCheck.getSize();
                                        targetNode = leafToCheck;
                                        break;
                                    }
                                }
                                // if it is not a leaf but a TreeNode children
                                if (idTarget.equals("")) {
                                    Node n = env.getNode(gazeTarget);
                                    if (n != null) {
                                        TreeNode target = (TreeNode) n;
                                        idTarget = target.getIdentifier();
                                        sizeTarget = target.getScale();
                                        targetNode = target;
                                    }
                                }
                                if (targetNode == null) {
                                    targetNode = env.getNode(idTarget);
                                }
                            }
                        }
                    }

                    if (targetNode != null) {

                        TreeNode currentCharacterHeadFromUnity = GazeKeyframeGenerator.this.cm.getCurrentCharacterHeadFromUnity();

                        //if target is animatable, look at head (for now ! ideally it should be specified in the target attribute)
                        if (targetNode instanceof Animatable) {
                            vec2target = ((TreeNode) env.getNode(gaze.getTarget() + "_AudioTreeNode")).getGlobalCoordinates();
                            vec2target = new Vec3d(vec2target.x(), vec2target.y() + 0.09f, vec2target.z() + 0.13f); // TODO: offsets are in local values, they must be in global values
                        } else if (targetNode instanceof Leaf) {

                            Leaf targetLeaf = (Leaf) targetNode;

                            Leaf metadataLeafObjectToGazeAt = targetLeaf.getMetadataLeaf("objectToGazeAt");
                            while (metadataLeafObjectToGazeAt != null) {
                                String objectToGazeAtIdentifier = targetLeaf.getMetadataLeafValue("objectToGazeAt");
                                if (env.getNode(objectToGazeAtIdentifier) != null) {
                                    targetLeaf = (Leaf) env.getNode(objectToGazeAtIdentifier);
                                    metadataLeafObjectToGazeAt = targetLeaf.getMetadataLeaf("objectToGazeAt");
                                } else {
                                    metadataLeafObjectToGazeAt = null;
                                }
                            }

                            TreeNode targetLeafParent = targetLeaf.getParent();

                            Leaf targetCenterLeaf = new Leaf();
                            targetCenterLeaf.setIdentifier(targetLeaf.getIdentifier() + "_center");
                            targetCenterLeaf.setReference("object.center");
                            targetCenterLeaf.setSize(0, 0, 0);

                            TreeNode targetCenterParent = new TreeNode();
                            targetCenterParent.setCoordinates(targetLeaf.getSize().x() / 2, targetLeaf.getSize().y() / 2, targetLeaf.getSize().z() / 2);

                            targetLeafParent.addChildNode(targetCenterParent);
                            targetCenterParent.addChildNode(targetCenterLeaf);

                            vec2target = ((TreeNode) targetCenterParent).getGlobalCoordinates();

                            targetCenterParent.removeChild(targetCenterLeaf);
                            targetLeafParent.removeChild(targetCenterParent);
                        }

                        // skeleton position
                        Vec3d currentPosition = new Vec3d(currentAgent.getCoordinateX(),
                                currentAgent.getCoordinateY(),
                                currentAgent.getCoordinateZ());

                        // head position
                        this.headPosition = new Vec3d(currentAgent.getHeadNode().getCoordinateX(),
                                currentAgent.getHeadNode().getCoordinateY(),
                                currentAgent.getHeadNode().getCoordinateZ());

                        // headPosition don't have the right x and z position
                        headPosition.setX(currentPosition.x());
                        headPosition.setZ(currentPosition.z());

                        // update the Y coordinate only
                        // it is possible that Greta is standing in Ogre but is sitting in Unity
                        // in this case, we use the height (i.e. Y value) given by Unity
                        if (currentCharacterHeadFromUnity != null) {
                            this.headPosition.setX(currentCharacterHeadFromUnity.getGlobalCoordinates().x()
                                                    + (currentCharacterHeadFromUnity.getScaleX() / 2));
                            this.headPosition.setY(currentCharacterHeadFromUnity.getGlobalCoordinates().y()
                                                    + (currentCharacterHeadFromUnity.getScaleY() / 2));
                            this.headPosition.setZ(currentCharacterHeadFromUnity.getGlobalCoordinates().z()
                                                    - (currentCharacterHeadFromUnity.getScaleZ() / 2));
                        }

                        if (gaze.getTarget().equals("user")) {
                            //vec2target.add(headPosition);
                            this.posTarget.setX(headPosition.x() + vec2target.x());
                            this.posTarget.setY(headPosition.y() + vec2target.y());
                            this.posTarget.setZ(headPosition.z() + vec2target.z());
                        } else {
                            this.posTarget = vec2target;
                        }

                        // orientation skeleton
                        this.orient = new Quaternion(currentAgent.getRotationNode().getOrientation().x(),
                                currentAgent.getRotationNode().getOrientation().y(),
                                currentAgent.getRotationNode().getOrientation().z(),
                                currentAgent.getRotationNode().getOrientation().w());

                        Vec3d head = Vec3d.addition(headPosition, orient.rotate(headAnglesHeadOffset));
                        Vec3d leftEye = Vec3d.addition(headPosition, orient.rotate(headAnglesLeftEyeOffset));
                        Vec3d rightEye = Vec3d.addition(headPosition, orient.rotate(headAnglesRightEyeOffset));

                        leftEyeRelativeEulerAngles = env.getTargetRelativeEulerAngles(this.posTarget, leftEye, orient);
                        rightEyeRelativeEulerAngles = env.getTargetRelativeEulerAngles(this.posTarget, rightEye, orient);
                        headRelativeEulerAngles = env.getTargetRelativeEulerAngles(this.posTarget, head, orient);

                        leftEyeYawAngle = leftEyeRelativeEulerAngles.x();
                        leftEyePitchAngle = leftEyeRelativeEulerAngles.y();
                        rightEyeYawAngle = rightEyeRelativeEulerAngles.x();
                        rightEyePitchAngle = rightEyeRelativeEulerAngles.y();
                        headYawAngle = headRelativeEulerAngles.x();
                        headPitchAngle = headRelativeEulerAngles.y();
                    }
                } /*else if(gaze.getTarget().equals(cm.getCurrentCharacterName())) { // if look at my self just look down
                //gaze.setOffsetDirection(GazeDirection.DOWN);
                //gaze.setOffsetAngle(30);
            }*/
            }

            double offsetAngle = Math.toRadians(gaze.getOffsetAngle());
            //add offsets corresponding to offsetDirection
            if (gaze.getOffsetDirection() == GazeDirection.RIGHT
                    || gaze.getOffsetDirection() == GazeDirection.UPRIGHT
                    || gaze.getOffsetDirection() == GazeDirection.DOWNRIGHT) {
                leftEyeYawAngle -= offsetAngle;
                rightEyeYawAngle -= offsetAngle;
                headYawAngle -= offsetAngle;
            } //max PI/12 -> 15degrees
            else if (gaze.getOffsetDirection() == GazeDirection.LEFT
                    || gaze.getOffsetDirection() == GazeDirection.UPLEFT
                    || gaze.getOffsetDirection() == GazeDirection.DOWNLEFT) {
                leftEyeYawAngle += offsetAngle;
                rightEyeYawAngle += offsetAngle;
                headYawAngle += offsetAngle;
            } //max PI/12 -> 15degrees

            if (gaze.getOffsetDirection() == GazeDirection.DOWN
                    || gaze.getOffsetDirection() == GazeDirection.DOWNLEFT
                    || gaze.getOffsetDirection() == GazeDirection.DOWNRIGHT) {
                leftEyePitchAngle -= offsetAngle;
                rightEyePitchAngle -= offsetAngle;
                headPitchAngle -= offsetAngle;
            } //max PI/12 -> 15degrees
            else if (gaze.getOffsetDirection() == GazeDirection.UP
                    || gaze.getOffsetDirection() == GazeDirection.UPLEFT
                    || gaze.getOffsetDirection() == GazeDirection.UPRIGHT) {
                leftEyePitchAngle += offsetAngle;
                rightEyePitchAngle += offsetAngle;
                headPitchAngle += offsetAngle;
            } //max PI/12 -> 15degrees

            leftEyeGazeDirection = computeGazeDirection(leftEyeYawAngle, leftEyePitchAngle);
            rightEyeGazeDirection = computeGazeDirection(rightEyeYawAngle, rightEyePitchAngle);

            withinHeadAndEyesLimit = limitHeadAndEyesAngle();
            withinEyesLimit = limitEyesAngles();
        }
    }

    public class ShouldersAngles {
        //raw angles to target
        HeadAndEyesAngles headAngles;
        double shoulderYawAngle; //shoulder
        double shoulderPitchAngle; //shoulder
        double shoulderMinimumAlign = 0; // it is the minimum alignment of the shoulder if the designer decide to do not have a full alignment

        //ratio between no movement (0) and full movement (1)
        //full movement means the physical limit of the eyeball (resp. head): it can only move for 45/55 degrees (resp 90) or so
        double shoulderLimitedYaw;
        double shoulderLimitedPitch;

        //can the Shoulders reach the target without moving other modalities
        boolean withinShoulderLimit;

        double shoulderLatency;

        /**
         * Copy constructor
         *
         * @param shouldersAngles ShoulderAngles to copy
         */
        public ShouldersAngles(ShouldersAngles shouldersAngles, HeadAndEyesAngles headAngles) { // TODO FIXME Is this method called anywhere ? If not, to be deleted.
            this.headAngles = headAngles;
            this.shoulderYawAngle = shouldersAngles.shoulderYawAngle;
            this.shoulderPitchAngle = shouldersAngles.shoulderPitchAngle;
            this.shoulderMinimumAlign = shouldersAngles.shoulderMinimumAlign;

            this.shoulderLimitedYaw = shouldersAngles.shoulderLimitedYaw;
            this.shoulderLimitedPitch = shouldersAngles.shoulderLimitedPitch;
            this.withinShoulderLimit = shouldersAngles.withinShoulderLimit;

            this.shoulderLatency = shouldersAngles.shoulderLatency;
        }

        public boolean limitShouldersAngle() {
            double shoulderYawAngleAbs = Math.abs(shoulderYawAngle);
            double shoulderYawAngleSign = Math.signum(shoulderYawAngle);

            //shoulders limit angle
            if (shoulderYawAngleAbs <= TORSO_YAW_LIMIT + HEAD_YAW_LIMIT) {
                withinShoulderLimit = true;
            }

            // N.B. --> limited angles for the eyes have to be positive for both rotation direction
            if (shoulderYawAngleAbs > Math.toRadians(135)) { // withinShoulderLimit = false
                shoulderLimitedYaw = shoulderYawAngleSign;
                headAngles.headLimitedYaw = shoulderYawAngleSign; // Math.signum(headAngles.rightYawAngle) *
            } else {
                double shoulderEyeMinAngle = Math.abs(shoulderMinimumAlign)*TORSO_YAW_LIMIT + 0.261799; // sum of shoulder angle and 15° (eyes minimum angle)
                double ang = Math.abs(headAngles.headYawAngle) - shoulderEyeMinAngle;
                if (ang > Math.PI/2) { // 90°
                    headAngles.headLimitedYaw = shoulderYawAngleSign;
                } else {
                    headAngles.headLimitedYaw = shoulderYawAngleSign * ang / HEAD_YAW_LIMIT;
                }
            }
            double limitedYawAngle = Math.abs(shoulderMinimumAlign)*TORSO_YAW_LIMIT - Math.abs(headAngles.headLimitedYaw)*HEAD_YAW_LIMIT;
            headAngles.rightEyeLimitedYaw = (Math.abs(headAngles.rightEyeYawAngle) - limitedYawAngle) / EYES_YAW_LIMIT;
            headAngles.leftEyeLimitedYaw = (Math.abs(headAngles.leftEyeYawAngle) - limitedYawAngle) / EYES_YAW_LIMIT;

            // PITCH ANGLE
            if (Math.abs(headAngles.headPitchAngle) > HEAD_PITCH_LIMIT_UP || Math.abs(headAngles.headPitchAngle) > HEAD_PITCH_LIMIT_DOWN) {
                double ang = Math.abs(headAngles.headPitchAngle) - 0.174533; // 10°

                if (ang >HEAD_PITCH_LIMIT_UP || ang > HEAD_PITCH_LIMIT_DOWN) {
                    headAngles.headLimitedPitch = Math.signum(headAngles.headPitchAngle);
                    if (headAngles.headPitchAngle > 0) {
                        headAngles.rightEyePitchAngle = pitchAngleFromDirection(true, true);
                        headAngles.leftEyePitchAngle = pitchAngleFromDirection(false, true);
                    } else {
                        headAngles.rightEyePitchAngle = pitchAngleFromDirection(true, false);
                        headAngles.leftEyePitchAngle = pitchAngleFromDirection(false, false);
                    }
                } else {
                    if (headAngles.headPitchAngle > 0) {
                        headAngles.headLimitedPitch = Math.signum(headAngles.headPitchAngle) * ang/HEAD_PITCH_LIMIT_UP;
                        headAngles.rightEyePitchAngle = pitchAngleFromDirection(true, true);
                        headAngles.leftEyePitchAngle = pitchAngleFromDirection(false, true);
                    } else {
                        headAngles.headLimitedPitch = Math.signum(headAngles.headPitchAngle) * ang/HEAD_PITCH_LIMIT_DOWN;
                        headAngles.rightEyePitchAngle = pitchAngleFromDirection(true, false);
                        headAngles.leftEyePitchAngle = pitchAngleFromDirection(false, false);
                    }
                }
            } else {
                if (headAngles.headPitchAngle > 0) {
                    if (headAngles.headPitchAngle > 0.174533) { // 10°
                        headAngles.headLimitedPitch = Math.signum(headAngles.headPitchAngle) * Math.abs(headAngles.headPitchAngle)/HEAD_PITCH_LIMIT_UP;
                    } else {
                        headAngles.headLimitedPitch = 0;
                    }
                    //headAngles.headLimitedPitch =  Math.signum(headAngles.headPitchAngle) * (Math.abs(headAngles.headPitchAngle) - 0.174533)/HEAD_PITCH_LIMIT_UP; //Math.signum(headAngles.headPitchAngle) *
                    headAngles.rightEyePitchAngle = pitchAngleFromDirection(true, true);
                    headAngles.leftEyePitchAngle = pitchAngleFromDirection(false, true);
                } else {
                    if (headAngles.headPitchAngle < -0.174533) { // 10°
                        headAngles.headLimitedPitch = Math.signum(headAngles.headPitchAngle) * Math.abs(headAngles.headPitchAngle)/HEAD_PITCH_LIMIT_DOWN;
                    } else {
                        headAngles.headLimitedPitch = 0;
                    }
                    //headAngles.headLimitedPitch =  Math.signum(headAngles.headPitchAngle) * (Math.abs(headAngles.headPitchAngle) - 0.174533)/HEAD_PITCH_LIMIT_DOWN;// Math.signum(headAngles.headPitchAngle) *
                    headAngles.rightEyePitchAngle = pitchAngleFromDirection(true, false);
                    headAngles.leftEyePitchAngle = pitchAngleFromDirection(false, false);
                }
            }
            shoulderLimitedPitch = shoulderPitchAngle;

            return withinShoulderLimit;
        }

        /**
         * Returns the pitch angle value according to the given direction.
         * @param right true for right, false for left
         * @param up true for up, false for down
         * @return the pitch angle value
         */
        private double pitchAngleFromDirection (boolean right, boolean up) {
            return (Math.abs(right ? headAngles.rightEyePitchAngle : headAngles.leftEyePitchAngle)
                    - Math.abs(headAngles.headLimitedPitch) * (up ? HEAD_PITCH_LIMIT_UP : HEAD_PITCH_LIMIT_DOWN))
                    / EYES_PITCH_LIMIT;
        }

        /**
         * Constructor. Computes the head and eyes angles to a target with
         * offset positions.
         */
        public ShouldersAngles(Environment env, GazeSignal gaze, HeadAndEyesAngles headAngles) {
            this.headAngles = new HeadAndEyesAngles (headAngles);
            Vec3d shoulderRelativeEulerAngles;
            //euler angles to target + offset, for left eye, right eye, head
            shoulderYawAngle = 0;
            shoulderPitchAngle = 0;

            shoulderLimitedYaw = 0; // shoulder
            shoulderLimitedPitch = 0;

            //TODO : adapt with scale, character meshes
            Vec3d shoulder = Vec3d.addition(headAngles.headPosition, headAngles.orient.rotate(shoulderAnglesHeadOffset));

            // relative angle
            shoulderRelativeEulerAngles = env.getTargetRelativeEulerAngles(headAngles.posTarget, shoulder, headAngles.orient);

            // according to the angle amplitude, the head and shoulder will contribute with different movement
            this.shoulderYawAngle = shoulderRelativeEulerAngles.x();
            shoulderPitchAngle = 0;
            /**
             * for the coordination eyes-head-torso it is followed the paper:
             * "Gaze and Attention Management for Embodied Conversational Agents"
             * Authors: Pejsa T Andrist S Gleicher M Mutlu B
             **/
            double maxAngle = Math.abs(Math.max(this.headAngles.leftEyeYawAngle, this.headAngles.rightEyeYawAngle));
            if (maxAngle < 0.349066) { // 20°
                shoulderMinimumAlign = 0;
            } else if (maxAngle < 0.698132 && maxAngle >= 0.349066) { //  20° =< angle < 40°
                shoulderMinimumAlign = Math.signum(this.headAngles.leftEyeYawAngle) * Math.toRadians(0.8*Math.toDegrees(maxAngle*TORSO_YAW_LIMIT) - 1.45) / TORSO_YAW_LIMIT;
            } else if (maxAngle >= 0.698132) { //  angle => 40°
                shoulderMinimumAlign = Math.signum(this.headAngles.leftEyeYawAngle)
                        * Math.toRadians(0.43*Math.exp(0.03*Math.abs(Math.toDegrees(Math.max(this.headAngles.leftEyeYawAngle, this.headAngles.rightEyeYawAngle)))) + 0.19)
                        / TORSO_YAW_LIMIT; // *TORSO_YAW_LIMIT
                if (Math.abs(shoulderMinimumAlign*TORSO_YAW_LIMIT) > TORSO_YAW_LIMIT) {
                    shoulderMinimumAlign =  Math.signum(this.headAngles.leftEyeYawAngle);
                }
            }

            this.withinShoulderLimit = this.limitShouldersAngle();

            // shoulders latency
            // latency = 0.25*eyesrotation + 47.5  ---> value in ms
            // latency = latency/1000 ---> value in sec
            shoulderLatency = (0.25*Math.toDegrees(Math.max(Math.max(this.headAngles.leftEyeLimitedYaw *EYES_YAW_LIMIT, this.headAngles.rightEyeLimitedYaw *EYES_YAW_LIMIT), Math.max(this.headAngles.leftEyeLimitedPitch *EYES_PITCH_LIMIT, this.headAngles.rightEyeLimitedPitch *EYES_PITCH_LIMIT))) + 47.5)/1000;
        }
    }

}
