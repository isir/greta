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
 */
package vib.core.behaviorrealizer.keyframegenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import vib.core.animation.mpeg4.MPEG4Animatable;
import vib.core.animation.mpeg4.bap.BAPType;
import vib.core.behaviorrealizer.keyframegenerator.GazeKeyframeGenerator.HeadAngles;
import vib.core.keyframes.HeadKeyframe;
import vib.core.keyframes.Keyframe;
import vib.core.keyframes.ShoulderKeyframe;
import vib.core.keyframes.TorsoKeyframe;
import vib.core.keyframes.face.AUAPFrameInterpolator;
import vib.core.keyframes.face.AUKeyFrame;
import vib.core.repositories.AUAPFrame;
import vib.core.repositories.AUExpression;
import vib.core.repositories.AUItem;
import vib.core.repositories.FaceLibrary;
import vib.core.repositories.HeadLibrary;
import vib.core.repositories.TorsoLibrary;
import vib.core.signals.*;
import vib.core.util.CharacterManager;
import vib.core.util.Constants;
import vib.core.util.IniParameter;
import vib.core.util.Mode;
import vib.core.util.audio.Mixer;
import vib.core.util.enums.CompositionType;
import vib.core.util.enums.GazeDirection;
import vib.core.util.enums.Influence;
import vib.core.util.enums.Side;
import vib.core.util.environment.Animatable;
import vib.core.util.environment.Environment;
import vib.core.util.environment.EnvironmentEventListener;
import vib.core.util.environment.Leaf;
import vib.core.util.environment.LeafEvent;
import vib.core.util.environment.Node;
import vib.core.util.environment.NodeEvent;
import vib.core.util.environment.TreeEvent;
import vib.core.util.environment.TreeNode;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.log.Logs;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;
import vib.core.util.time.Timer;

/**
 *
 * @author Mathieu Chollet
 * @author André-Marie Pez
 * @author Donatella Simonetti
 * @author Nawhal Sayarh
 */
public class GazeKeyframeGenerator extends KeyframeGenerator implements EnvironmentEventListener, SignalEmitter{

    /* eyes, head, shoulder/torso angular speed
     * this are the default value as found in the paper:
     * "Gaze and Attention Management for Embodied Conversational Agents"
     * authors: TOMISLAV PEJSA, SEAN ANDRIST, MICHAEL GLEICHER, and BILGE MUTLU */
    // default velocities for eyes, head and shoulder
    private static final double EYES_ANGULAR_SPEED = 2.61799;  // 150 degs/s, 2.62 rad/s
    private static final double HEAD_ANGULAR_SPEED = 0.872665; // 50 degs/s, 5.2 rad/s
    private static final double TORSO_ANGULAR_SPEED = 0.261799;//15 degs/s, 5.2 rad/s  

    //constraint on horizontal(yaw) an vertical(pitch) eye movement
    private static final double EYES_YAW_LIMIT = Math.toRadians(50); //Math.PI / 3; //0.6;
    private static final double EYES_PITCH_LIMIT = Math.PI / 3;//S Math.PI / 3;

    //constraint on horizontal(yaw) an vertical(pitch)  head movement
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
        defaultGazeLeft = new AUKeyFrame("restLeft", 0.0, new AUAPFrame());
        defaultGazeRight = new AUKeyFrame("restRight", 0.0, new AUAPFrame());
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
        // Spinephase for each body part involved in the gaze in order to store the information about the last position after a gazeShift
        SpinePhase lastShiftHead = new SpinePhase("head", 0, 0);
        SpinePhase lastShiftTorso = new SpinePhase("torso", 0, 0);

        // take the MPEG4 for the agent target, i.e. the agent to look at
        //MPEG4Animatable targetAgent = new MPEG4Animatable();
        // take the MPEG4 for the agent whom is performing the gaze
        MPEG4Animatable currentAgent = new MPEG4Animatable(cm);
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
            HeadAngles headAngles = new HeadAngles(this.env, gaze);
            
            // head angles give by the additional rotation of each cervical vertebrae
            Double agentHeadPitch = currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc1_tilt) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc2_tilt) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc3_tilt) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc4_tilt) + 
                                        currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc5_tilt) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc6_tilt) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc7_tilt);
            Double agentHeadYaw = currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc1_torsion) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc2_torsion) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc3_torsion) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc4_torsion) + 
                                        currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc5_torsion) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc6_torsion) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc7_torsion);
            Double agentHeadRoll = currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc1_roll) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc2_roll) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc3_roll) + currentAgent.getCurrentBAPFrame().getRadianValue(BAPType.vc4_roll) + 
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

            if (headActualAngle.y() < headAngles.headYawAngle) {
                headLatency = 0.0;
            }

            //times computation
            //start keyframe : all influences at original position
            double start = gaze.getStart().getValue();
            // ready and relax will be recomputed according to the influence
            double ready = gaze.getTimeMarker("ready").getValue();
            double relax = gaze.getTimeMarker("relax").getValue();
            //end keyframe : all influences at original position
            double end = gaze.getEnd().getValue();

            Influence gazeInfluence = computeGazeInfluence(gaze, headAngles);

            if (gazeInfluence.ordinal() >= Influence.SHOULDER.ordinal()) {
                // if the influence involve the torso we create the keyframe just for the torse that already include the movement of 
                // vt12 vertebrae (the same we move just for the shoulder). So we don't need to create the keyframe also for the shoulder
                //********************************************************************************//             
                ShouldersAngles shouldersAngles = new ShouldersAngles(envi, gaze, headAngles);

                // calculate the shoulder max speed depending on the rotation angle
                double shoulderMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(Math.abs(shouldersAngles.shoulderMinimumAlign*TORSO_YAW_LIMIT))/15 + 2)*Math.toDegrees(TORSO_ANGULAR_SPEED)));
                //double maxVelShoulderPitch = Math.toRadians(Math.abs((4/3 * Math.toDegrees(Math.abs(sha.shoulderLimitedYaw*TORSO_YAW_LIMIT))/15 + 2)*Math.toDegrees(TORSO_ANGULAR_SPEED)));

                double timeShoulderAtTarget = start + shouldersAngles.shoulderLatency + (Math.max(Math.abs(shouldersAngles.shoulderMinimumAlign), Math.abs(shouldersAngles.shoulderLimitedPitch)) / shoulderMaxSpeed);
                if (end == 0) {
                    ready = timeShoulderAtTarget;
                    relax = ready + 0.2;
                }
                
                if (timeShoulderAtTarget > ready) {
                    timeShoulderAtTarget = ready;
                }
                double timeBackShoulderAtZero = relax + (Math.max(Math.abs(shouldersAngles.shoulderMinimumAlign), Math.abs(shouldersAngles.shoulderLimitedPitch)) / shoulderMaxSpeed);
                
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
                    headMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(shouldersAngles.headAngles.headLimitedYaw *HEAD_YAW_LIMIT)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                } else {
                    if (shouldersAngles.headAngles.headLimitedPitch < 0.0) {
                        headMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(shouldersAngles.headAngles.headLimitedPitch *HEAD_PITCH_LIMIT_DOWN)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    } else {
                        headMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(shouldersAngles.headAngles.headLimitedPitch *HEAD_PITCH_LIMIT_UP)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    }
                }

                // time head reach the target position and come back
                double timeHeadAtTarget = start + headLatency + (Math.abs(shouldersAngles.headAngles.headLimitedYaw *HEAD_YAW_LIMIT)/ headMaxSpeed); // 0.1 is the latency time
                if (timeHeadAtTarget > ready) {
                    timeHeadAtTarget = ready;
                }
                
                if (end == 0) {
                    ready = timeHeadAtTarget;
                    relax = ready + 0.2;
                }
                
                double timeBackHeadAtZero = relax + (Math.abs(shouldersAngles.headAngles.headLimitedYaw * HEAD_YAW_LIMIT)/ headMaxSpeed);
                if (timeBackHeadAtZero > end) {
                    timeBackHeadAtZero = end;
                }
                
                if (end == 0) {
                    end = timeBackHeadAtZero;
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
                if (Math.abs(headAngles.headLimitedYaw) > Math.abs(headAngles.headLimitedPitch)) {
                    headMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(headAngles.headLimitedYaw *HEAD_YAW_LIMIT)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                } else {
                    if (headAngles.headLimitedPitch < 0.0) {
                        headMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(headAngles.headLimitedPitch *HEAD_PITCH_LIMIT_DOWN)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    } else {
                        headMaxSpeed = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(headAngles.headLimitedPitch *HEAD_PITCH_LIMIT_UP)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    }
                }


                // time head reach the target position and come back
                double timeHeadAtTarget = start + headLatency + Math.max(Math.abs(headAngles.headLimitedYaw *HEAD_YAW_LIMIT), Math.abs(headAngles.headLimitedPitch *HEAD_YAW_LIMIT)) / headMaxSpeed; // 0.1 is the latency time
                
                if (end == 0) {
                    ready = timeHeadAtTarget;
                    relax = ready + 0.2;
                }
                
                if (timeHeadAtTarget > ready) {
                    timeHeadAtTarget = ready;
                }
                double timeBackHeadAtZero = relax + Math.max(Math.abs(headAngles.headLimitedYaw *HEAD_YAW_LIMIT), Math.abs(headAngles.headLimitedPitch *HEAD_YAW_LIMIT)) / headMaxSpeed;
                
                if (end == 0) {
                    end = timeBackHeadAtZero;
                }
                
                if (timeBackHeadAtZero > end) {
                    timeBackHeadAtZero = end;
                }

                // headSignalToTarget head signal when look to the target
                HeadSignal headSignalToTarget = createHeadSignalWithDirectionShift();
                SpinePhase spinePhaseToTarget;
                spinePhaseToTarget = createSpinePhase("end", timeHeadAtTarget, timeHeadAtTarget, headAngles.headLimitedYaw, headAngles.headLimitedPitch); // ready
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
                // involve just the head. So it is create a torsoSignel with rotation equal to 0.0
                // torsoSignalTargetPosition torso signal at target position
                TorsoSignal torsoSignalTargetPosition = new TorsoSignal(IDProvider.createID("gazegenerator").toString());
                torsoSignalTargetPosition.setDirectionShift(true);
                SpinePhase spinePhaseTargetPosition = createSpinePhase("end", timeHeadAtTarget, timeHeadAtTarget, 0.0, 0.0); // ready
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
                // so we have to deleted the rotation of head and torso  
                
                // compute the time to go back in the front position
                TorsoKeyframeGenerator torsoKeyframeGenerator = (TorsoKeyframeGenerator)  this.otherModalitiesKFGenerators.get(5);
                TorsoKeyframe torsoKeyframe = (TorsoKeyframe) torsoKeyframeGenerator.getDefaultPosition();
                       
                double yawTorso = torsoKeyframe.verticalTorsion.value;
                double pitchTorso = torsoKeyframe.sagittalTilt.value;
                
                double angleTorso = Math.max(yawTorso, pitchTorso);
                
                double maxVel_shoulder = Math.toRadians(Math.abs((4/3 * Math.toDegrees(Math.abs(ang_t*TORSO_YAW_LIMIT))/15 + 2)*Math.toDegrees(TORSO_ANGULAR_SPEED)));
                double timeShoulderAtTarget = start + Math.max(Math.abs(ang_t), Math.abs(ang_t)) / maxVel_shoulder;
                
                if (ang_t == 0.0){
                    timeShoulderAtTarget = end;
                }
                
                // torso signal at target position 
                TorsoSignal torsoSignalTargetPosition = new TorsoSignal(IDProvider.createID("gazegenerator").toString());
                torsoSignalTargetPosition.setDirectionShift(true);
                SpinePhase spinePhaseTargetPosition;
                CharacterManager staticCharacterManager = CharacterManager.getStaticInstance();
                if (!gaze.isGazeShift()) {
                    //System.out.println(cm.getStaticInstance().defaultFrame.size() );
                    spinePhaseTargetPosition = createTorsoPhase("end", end, end, 0.0, 0.0); // ready
                    spinePhaseTargetPosition.setStartTime(end); // ready
                    spinePhaseTargetPosition.setEndTime(end);
                    torsoSignalTargetPosition.getEnd().setValue(end);
                } else {
                    spinePhaseTargetPosition = createTorsoPhase("end", timeShoulderAtTarget, timeShoulderAtTarget, 0.0, 0.0); // ready
                    spinePhaseTargetPosition.setStartTime(timeShoulderAtTarget); // ready
                    spinePhaseTargetPosition.setEndTime(timeShoulderAtTarget);
                    torsoSignalTargetPosition.getEnd().setValue(timeShoulderAtTarget);
                }   
                torsoSignalTargetPosition.getPhases().add(spinePhaseTargetPosition);
                torsoSignalTargetPosition.getStart().setValue(start + headLatency);
                addSignalToKeyframeGenerator(torsoSignalTargetPosition);
                
                
                // compute the time to go back in the front position
                HeadKeyframeGenerator headKeyframeGenerator = (HeadKeyframeGenerator)  this.otherModalitiesKFGenerators.get(2);
                HeadKeyframe headKeyframe = (HeadKeyframe) headKeyframeGenerator.getDefaultPosition();
                double yawHead = headKeyframe.verticalTorsion.value;
                double pitchHead = headKeyframe.sagittalTilt.value;
                
                double angleHead = Math.max(yawHead, pitchHead);
                
                double headMaxSpeed = Math.toRadians(Math.abs((4/3 * Math.toDegrees(ang_h*HEAD_YAW_LIMIT)/50 + 2/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                double timeHeadAtTarget = start + Math.max(Math.abs(ang_h), Math.abs(ang_h)) / headMaxSpeed;
                
                if (angleHead == 0.0){
                    timeHeadAtTarget = end;
                }
                // head signal at target position 
                HeadSignal headSignalWithDirectionShift = createHeadSignalWithDirectionShift();
                SpinePhase headPhase;
                if (!gaze.isGazeShift()) {
                    headPhase = createSpinePhase("end", end, end, 0.0, 0.0); // end
                    headPhase.setStartTime(end); // ready
                    headPhase.setEndTime(end);
                    headSignalWithDirectionShift.getEnd().setValue(end);
                } else {
                    headPhase = createSpinePhase("end", timeHeadAtTarget, timeHeadAtTarget, 0.0, 0.0); // ready
                    headPhase.setStartTime(timeHeadAtTarget); // ready
                    headPhase.setEndTime(timeHeadAtTarget);
                    headSignalWithDirectionShift.getEnd().setValue(timeHeadAtTarget);
                }
                headSignalWithDirectionShift.getPhases().add(hp1);
                headSignalWithDirectionShift.getStart().setValue(start+head_latency);
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
            HeadAngles headAngles = new HeadAngles(this.env, gaze);

            //euler angles to target + offset, for shoulder (same for torso)
            ShouldersAngles shoulderAngles = new ShouldersAngles(this.env, gaze, headAngles);      
            
            // check if the gaze expression is in the facelibrary 
            // if we look at a target there is no information in the library
            if ((gaze.getTarget() == null || gaze.getTarget().isEmpty()) && gaze.getOffsetDirection() == GazeDirection.FRONT && gaze.getOffsetAngle() == 0.0) {
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
                    boolean left = false;
                    boolean up = false;

                    double yaw = 0.0;
                    double pitch = 0.0;

                    // take the last gaze shift angle
                    double yawLeft = defaultGazeLeft.getAus().getAUAP(61, Side.LEFT).getNormalizedValue();
                    double yawRight = defaultGazeLeft.getAus().getAUAP(62, Side.RIGHT).getNormalizedValue();
                    double pitchUpLeft = defaultGazeLeft.getAus().getAUAP(63, Side.LEFT).getNormalizedValue();
                    double pitchUpRight = defaultGazeLeft.getAus().getAUAP(63, Side.RIGHT).getNormalizedValue();
                    double pitchDownLeft = defaultGazeLeft.getAus().getAUAP(64, Side.LEFT).getNormalizedValue();
                    double pitchDownRight = defaultGazeLeft.getAus().getAUAP(64, Side.RIGHT).getNormalizedValue();

                    if (yawLeft != 0.0  || pitchUpLeft != 0.0 || pitchDownLeft != 0.0){ // gazedirection = left
                        left = true;
                        yaw = yawLeft;
                        if (pitchUpLeft != 0.0) { // up
                            pitch = pitchUpLeft;
                            up = true;
                        } else { // down
                            pitch = pitchDownLeft;
                        }
                    } else { // gazedirection = right 
                        yaw = yawRight;
                        if (pitchUpRight != 0.0){ // up
                            up = true;
                            pitch = pitchDownRight;
                        } else { // down
                            pitch = pitchUpRight;
                        }
                    }

                    // calculate the max speed of the head depending on the rotation angle
                    double Amin = Math.toDegrees(Math.abs(yaw*EYES_YAW_LIMIT));                
                    //Amin_pitch = Math.toDegrees(Math.abs(Math.min(sha.ha.l_limitedPitch*EYES_PITCH_LIMIT, sha.ha.r_limitedPitch*EYES_PITCH_LIMIT)));              
                    double eyesMaxSpeed = Math.toRadians((2*Amin/75 + 1/6) * Math.toDegrees(EYES_ANGULAR_SPEED));
                    //maxVel_eyes_pitch = Math.toRadians((2*Amin_pitch/75 + 1/6)*Math.toDegrees(EYES_ANGULAR_SPEED)); 
                    double timeEyesAtTarget = Math.min(gaze.getStart().getValue() +Math.abs(yaw*EYES_YAW_LIMIT)/ eyesMaxSpeed, gaze.getStart().getValue() + Math.abs(yaw*EYES_YAW_LIMIT)/ eyesMaxSpeed);
                    
                    addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", gaze.getEnd().getValue(), Side.LEFT, GazeDirection.FRONT, 0.0, 0.0);
                    addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", gaze.getEnd().getValue(), Side.RIGHT, GazeDirection.FRONT, 0.0, 0.0);

                    AUAPFrame auFrameLeft = createAUAPFrameForEyeSide(Side.LEFT);
                    AUAPFrame auFrameRight = createAUAPFrameForEyeSide(Side.RIGHT);
                    setGazeRestPosition(new AUKeyFrame(gazeId + "_back", gaze.getEnd().getValue(), auFrameLeft), new AUKeyFrame(gazeId + "_back", gaze.getEnd().getValue(), auFrameRight));
                }
            } else {
                //euler angles to target + offset, for head
                HeadAngles headAngles = new HeadAngles(envi, gaze);
                //euler angles to target + offset, for shoulder (same for torso)
                ShouldersAngles sha = new ShouldersAngles(envi, gaze, headAngles);

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
                    if (keyframe.getOffset() >= start && keyframe.getOffset() <= end) { // if the offset is between start qnd end times of the eyes
                        if (keyframe instanceof HeadKeyframe) {
                            timesWithEyesKeyframes.add(keyframe.getOffset());
                        }
                        if (keyframe instanceof TorsoKeyframe) {
                            timesWithEyesKeyframes.add(keyframe.getOffset());
                        }
                    }
                }

                // check the influence. if null the influence is automatically calculated according to the gaze rotation angle 
                // - after 15° the head move
                // - with a gaze rotation more than 20, the shoulder start to move
                // - with a gaze rotation more than 30, the all torso start to move
                Influence gazeInfluence = computeGazeInfluence(gaze, headAngles);

                /*********************************************************************************************************************************************************************************************************************/

                // calculate the SHOULDER max speed depending on the rotation angle
                double maxVelShoulder = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(Math.abs(sha.shoulderMinimumAlign*TORSO_YAW_LIMIT))/15 + 2)*Math.toDegrees(TORSO_ANGULAR_SPEED)));
                double timeShoulderAtTarget;

                // HEAD
                double Amin;
                double AminPitch;
                double timeHeadAtTarget;
                double maxVelEyes;
                double maxVelEyesPitch;
                // EYES

                double timeEyesAtTarget;
                double timeBackEyesAtZero;

                if (gazeInfluence.ordinal()> Influence.HEAD.ordinal())
                {// time head reach the target position and come back
                    timeShoulderAtTarget = start + sha.shoulderLatency + Math.max(Math.abs(sha.shoulderMinimumAlign), Math.abs(sha.shoulderLimitedPitch)) / maxVelShoulder;
                    
                    // set ready and relax
                    ready = timeShoulderAtTarget;
                    if (end == 0)
                        relax = timeShoulderAtTarget + 0.2;// set as ready the time the last body parte reach the target position
                    
                    double timeBackShoulderAtZero = relax + Math.max(Math.abs(sha.shoulderMinimumAlign), Math.abs(sha.shoulderLimitedPitch)) / maxVelShoulder;
                    
                    // if end is not setted, we put the timeback of the last bodypart
                    if (end == 0)
                        end = timeBackShoulderAtZero;
                    
                    timesWithEyesKeyframes.add(timeShoulderAtTarget);
                    timesWithEyesKeyframes.add(timeBackShoulderAtZero);

                    // calculate the head max speed depending on the rotation angle
                    double maxVelHead = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(sha.headAngles.headLimitedYaw)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    if (sha.headAngles.headLimitedPitch < 0.0) {
                        double maxVelHeadPitch = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(sha.headAngles.headLimitedPitch)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    } else {
                        double maxVelHeadPitch = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(sha.headAngles.headLimitedPitch)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    }
                    // time head reach the target position and come back
                    timeHeadAtTarget = start + headLatency + Math.max(Math.abs(sha.headAngles.headLimitedYaw), Math.abs(sha.headAngles.headLimitedPitch)) / maxVelHead; // 0.1 is the latency time
                    double timeBackHeadAtZero = relax + Math.max(Math.abs(sha.headAngles.headLimitedYaw), Math.abs(sha.headAngles.headLimitedPitch)) / maxVelHead;
                    timesWithEyesKeyframes.add(timeHeadAtTarget);
                    timesWithEyesKeyframes.add(timeBackHeadAtZero);

                    // calculate the max speed of the head depending on the rotation angle
                    Amin = Math.toDegrees(Math.abs(Math.min(sha.headAngles.leftLimitedYaw *EYES_YAW_LIMIT, sha.headAngles.rightLimitedYaw *EYES_YAW_LIMIT)));
                    //AminPitch = Math.toDegrees(Math.abs(Math.min(sha.headAngles.leftLimitedPitch*EYES_PITCH_LIMIT, sha.headAngles.rightLimitedPitch*EYES_PITCH_LIMIT)));
                    maxVelEyes = Math.toRadians((2*Amin/75 + 1f/6)*Math.toDegrees(EYES_ANGULAR_SPEED));
                    //maxVelEyesPitch = Math.toRadians((2*AminPitch/75 + 1f/6)*Math.toDegrees(EYES_ANGULAR_SPEED));
                    timeEyesAtTarget = Math.min(start +Math.abs(sha.headAngles.leftLimitedYaw *EYES_YAW_LIMIT)/ maxVelEyes, start + Math.abs(sha.headAngles.rightLimitedYaw *EYES_YAW_LIMIT)/ maxVelEyes);
                    timeBackEyesAtZero = Math.min(relax + Math.abs(sha.headAngles.leftLimitedYaw *EYES_YAW_LIMIT)/ maxVelEyes, relax + Math.abs(sha.headAngles.rightLimitedYaw *EYES_YAW_LIMIT)/ maxVelEyes);
                    //timeBackEyesAtZero = Math.min(relax + Math.max(Math.abs(sha.headAngles.leftLimitedYaw), Math.abs(sha.headAngles.leftLimitedPitch))/ maxVelEyes, relax + Math.max(Math.abs(sha.headAngles.rightLimitedYaw), Math.abs(sha.headAngles.rightLimitedPitch)) / maxVelEyes);
                    timesWithEyesKeyframes.add(timeEyesAtTarget);
                    timesWithEyesKeyframes.add(timeBackEyesAtZero);

                } else if (gazeInfluence.ordinal()>Influence.EYES.ordinal()) {
                    // calculate the head max speed depending on the rotation angle
                    double maxVelHead = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(headAngles.headLimitedYaw)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    if (sha.headAngles.headLimitedPitch < 0.0) {
                        double maxVelHeadPitch = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(headAngles.headLimitedPitch)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    } else {
                        double maxVelHeadPitch = Math.toRadians(Math.abs((4f/3 * Math.toDegrees(headAngles.headLimitedPitch)/50 + 2f/5)*Math.toDegrees(HEAD_ANGULAR_SPEED)));
                    }
                    // time head reach the target position and come back
                    timeHeadAtTarget = start + headLatency + Math.max(Math.abs(headAngles.headLimitedYaw), Math.abs(headAngles.headLimitedPitch)) / maxVelHead; // 0.1 is the latency time
                    
                    // set ready and relax
                    ready = timeHeadAtTarget;
                    if (end == 0)
                        relax = ready + 0.4; // 0.4 é indicativo
                    
                    double timeBackHeadAtZero = relax + Math.max(Math.abs(headAngles.headLimitedYaw), Math.abs(headAngles.headLimitedPitch)) / maxVelHead;
                    
                    // if end is not setted, we put the timeback of the last bodypart
                    if (end == 0)
                        end = timeBackHeadAtZero;
                    
                    timesWithEyesKeyframes.add(timeHeadAtTarget);
                    timesWithEyesKeyframes.add(timeBackHeadAtZero);

                    // calculate the max speed of the head depending on the rotation angle
                    Amin = Math.toDegrees(Math.abs(Math.min(Math.abs(headAngles.leftLimitedYaw)*EYES_YAW_LIMIT, Math.abs(headAngles.rightLimitedYaw)*EYES_YAW_LIMIT)));
                    //AminPitch = Math.toDegrees(Math.abs(Math.min(headAngles.leftLimitedPitch*EYES_PITCH_LIMIT, headAngles.rightLimitedPitch*EYES_PITCH_LIMIT)));
                    maxVelEyes = Math.toRadians((2*Amin/75 + (1f/6))*Math.toDegrees(EYES_ANGULAR_SPEED));
                    //maxVelEyesPitch = Math.toRadians((2*AminPitch/75 + 1/6)*Math.toDegrees(EYES_ANGULAR_SPEED));
                    timeEyesAtTarget = Math.min(start +Math.abs(headAngles.leftLimitedYaw *EYES_YAW_LIMIT)/ maxVelEyes, start + Math.abs(headAngles.rightLimitedYaw *EYES_YAW_LIMIT)/ maxVelEyes);
                    timeBackEyesAtZero = Math.min(relax + Math.abs(headAngles.leftLimitedYaw *EYES_YAW_LIMIT)/ maxVelEyes, relax + Math.abs(headAngles.rightLimitedYaw *EYES_YAW_LIMIT) / maxVelEyes);
                    //timeEyesAtTarget = Math.min(start + Math.max(Math.abs(headAngles.leftLimitedYaw*EYES_YAW_LIMIT), Math.abs(headAngles.leftLimitedPitch*EYES_PITCH_LIMIT))/ maxVelEyes, start + Math.max(Math.abs(headAngles.rightLimitedYaw*EYES_YAW_LIMIT), Math.abs(headAngles.rightLimitedPitch*EYES_PITCH_LIMIT))/ maxVelEyes);
                    //timeBackEyesAtZero = Math.min(relax + Math.max(Math.abs(headAngles.leftLimitedYaw*EYES_YAW_LIMIT), Math.abs(headAngles.leftLimitedPitch*EYES_PITCH_LIMIT))/ maxVelEyes, relax + Math.max(Math.abs(headAngles.rightLimitedYaw*EYES_YAW_LIMIT), Math.abs(headAngles.rightLimitedPitch*EYES_PITCH_LIMIT)) / maxVelEyes);
                    timesWithEyesKeyframes.add(timeEyesAtTarget);
                    timesWithEyesKeyframes.add(timeBackEyesAtZero);
                } else {
                    // calculate the max speed of the head depending on the rotation angle
                    Amin = Math.toDegrees(Math.abs(Math.min(headAngles.leftLimitedYaw *EYES_YAW_LIMIT, headAngles.rightLimitedYaw *EYES_YAW_LIMIT)));
                    //AminPitch = Math.toDegrees(Math.abs(Math.min(headAngles.leftLimitedPitch*EYES_PITCH_LIMIT, headAngles.rightLimitedPitch*EYES_PITCH_LIMIT)));
                    maxVelEyes = Math.toRadians((2*Amin/75 + 1f/6)*Math.toDegrees(EYES_ANGULAR_SPEED));
                    //maxVelEyesPitch = Math.toRadians((2*AminPitch/75 + 1f/6)*Math.toDegrees(EYES_ANGULAR_SPEED));
                    timeEyesAtTarget = Math.min(start +Math.abs(headAngles.leftLimitedYaw *EYES_YAW_LIMIT)/ maxVelEyes, start + Math.abs(headAngles.rightLimitedYaw *EYES_YAW_LIMIT)/ maxVelEyes);
                    timeBackEyesAtZero = Math.min(relax + Math.abs(headAngles.leftLimitedYaw *EYES_YAW_LIMIT)/ maxVelEyes, relax + Math.abs(headAngles.rightLimitedYaw *EYES_YAW_LIMIT) / maxVelEyes);
                    timesWithEyesKeyframes.add(timeBackEyesAtZero);
                }

                //else, just use ready and relax of the gaze
                timesWithEyesKeyframes.add(ready);
                timesWithEyesKeyframes.add(relax);

                // sort the times frame
                Collections.sort(timesWithEyesKeyframes);

                // check if the shoulder start move after the head
                if (headLatency > sha.shoulderLatency) {
                    headLatency = 0.0;
                }

                //add to outputkeyframe the eyes keyframe at START time
                if (!gaze.isGazeShift()) {
                    addEyesAUKeyFrame(outputKeyframe, gazeId + "_to", "start", start, Side.LEFT, headAngles.leftGazeDirection, 0.0, 0.0);
                    addEyesAUKeyFrame(outputKeyframe, gazeId + "_to", "start", start, Side.RIGHT, headAngles.rightGazeDirection, 0.0, 0.0);
                }

                // add Keyframes for the eyes at every moment there is a body keyframe between start and end
                // for instance, when there is a nod, the eyes should keep on the target
                if (!timesWithEyesKeyframes.isEmpty()) {
                    Double latestTime = start;

                    double leftLimitYaw = 0.0;
                    double rightLimitYaw = 0.0;
                    double leftLimitPitch = 0.0;
                    double rightLimitPitch = 0.0;

                    for (Double time : timesWithEyesKeyframes) {
                        if (time > start && time < end && latestTime.compareTo(time) != 0) {
                            /*************************************************
                             * TO REACH THE TARGET
                             **************************************************/
                            if (time <= relax) {
                                /* -------------------------------------------------
                                 * INFLUENCE INVOLVES TORSO OR SHOULDER
                                 * --------------------------------------------------*/
                                if (gazeInfluence.ordinal()>=Influence.SHOULDER.ordinal()) {
                                    // if the eyes reach already the target position
                                    if (time >= timeEyesAtTarget) {
                                        leftLimitYaw = sha.headAngles.leftLimitedYaw;
                                        rightLimitYaw = sha.headAngles.rightLimitedYaw;
                                        leftLimitPitch = sha.headAngles.leftLimitedPitch;
                                        rightLimitPitch = sha.headAngles.rightLimitedPitch;
                                    }
                                }
                                /* -------------------------------------------------
                                * INFLUENCE INVOLVES THE HEAD    
                                * -------------------------------------------------- */    
                                else if (gaze.getInfluence().ordinal()>=Influence.HEAD.ordinal()){
                                    // if the eyes reach already the target position
                                    if (time >= timeEyesAtTarget) {
                                        leftLimitYaw = headAngles.leftLimitedYaw;
                                        rightLimitYaw = headAngles.rightLimitedYaw;
                                        leftLimitPitch = headAngles.leftLimitedPitch;
                                        rightLimitPitch = headAngles.rightLimitedPitch;
                                    }
                                        }
                                }else{
                                /* -------------------------------------------------
                                * INFLUENCE INVOLVES JUST THE EYES    
                                * -------------------------------------------------- */    
                                         // if the eyes reach already the target position
                                        if ( time >= timeEyesAtTarget){
                                            leftLimitYaw = Math.min(Math.abs(headAngles.leftLimitedYaw), EYES_YAW_LIMIT);
                                            rightLimitYaw = Math.min(Math.abs(headAngles.rightLimitedYaw), EYES_YAW_LIMIT);
                                            leftLimitPitch = Math.min(Math.abs(headAngles.leftLimitedPitch), EYES_PITCH_LIMIT);
                                            rightLimitPitch = Math.min(Math.abs(headAngles.rightLimitedPitch), EYES_PITCH_LIMIT);
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
                                        leftLimitYaw = 0.0;
                                        rightLimitYaw = 0.0;
                                        leftLimitPitch = 0.0;
                                        rightLimitPitch = 0.0;
                                    }
                                }
                            }

                            // add eyeskeyframe
                            if (leftLimitYaw != -10 && rightLimitYaw != -10 && leftLimitPitch != -10 && rightLimitPitch != -10) {
                                addEyesAUKeyFrame(outputKeyframe, gazeId + "_to", gazeId + "_to_kf" + time, time, Side.LEFT,
                                        headAngles.leftGazeDirection, leftLimitYaw, leftLimitPitch);
                                addEyesAUKeyFrame(outputKeyframe, gazeId + "_to", gazeId + "_to_kf" + time, time, Side.RIGHT,
                                        headAngles.rightGazeDirection, rightLimitYaw, rightLimitPitch);
                            }

                            latestTime = time;
                        }
                    }

                    // Add gazekeyframe for the END of the gaze
                    if (!gaze.isGazeShift()) {
                        if (gazeStillInProgress()) {
                            handleGazeStillInProgress(end, outputKeyframe);
                        } else {
                            addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", end, Side.LEFT, GazeDirection.FRONT, 0.0, 0.0);
                            addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", end, Side.RIGHT, GazeDirection.FRONT, 0.0, 0.0);

                            AUAPFrame auFrameLeft = createAUAPFrameForEyeSide(Side.LEFT);
                            AUAPFrame auFrameRight = createAUAPFrameForEyeSide(Side.RIGHT);
                            setGazeRestPosition(new AUKeyFrame(gazeId + "_back", end, auFrameLeft), new AUKeyFrame(gazeId + "_back", end, auFrameRight));
                        }
                    } else {
                        addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", timeBackEyesAtZero, Side.LEFT, headAngles.leftGazeDirection, leftLimitYaw, leftLimitPitch);
                        addEyesAUKeyFrame(outputKeyframe, gazeId + "_back", "end", timeBackEyesAtZero, Side.RIGHT, headAngles.rightGazeDirection, rightLimitYaw, rightLimitPitch);

                        AUAPFrame auFrameLeft = new AUAPFrame();
                        AUAPFrame auFrameRight = new AUAPFrame();
                        //AU61: eyes turn left
                        if (headAngles.leftGazeDirection == GazeDirection.DOWNLEFT || headAngles.leftGazeDirection == GazeDirection.LEFT || headAngles.leftGazeDirection == GazeDirection.UPLEFT) {
                            auFrameLeft.setAUAP(61, leftLimitYaw / EYES_YAW_LIMIT, Side.LEFT);
                        }
                        //AU62: eyes turn right
                        if (headAngles.leftGazeDirection == GazeDirection.DOWNRIGHT || headAngles.leftGazeDirection == GazeDirection.RIGHT || headAngles.leftGazeDirection == GazeDirection.UPRIGHT) {
                            auFrameLeft.setAUAP(62, leftLimitYaw / EYES_YAW_LIMIT, Side.LEFT);
                        }
                        //AU63: eyes up
                        if (headAngles.leftGazeDirection == GazeDirection.UPRIGHT || headAngles.leftGazeDirection == GazeDirection.UP || headAngles.leftGazeDirection == GazeDirection.UPLEFT) {
                            auFrameLeft.setAUAP(63, leftLimitPitch / EYES_PITCH_LIMIT, Side.LEFT);
                        }
                        //AU64: eyes down
                        if (headAngles.leftGazeDirection == GazeDirection.DOWNRIGHT || headAngles.leftGazeDirection == GazeDirection.DOWN || headAngles.leftGazeDirection == GazeDirection.DOWNLEFT) {
                            auFrameLeft.setAUAP(64, leftLimitPitch / EYES_PITCH_LIMIT, Side.LEFT);
                        }

                        //AU61: eyes turn left
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
                        }

                        setGazeRestPosition(new AUKeyFrame(gazeId + "_back", end, auFrameLeft), new AUKeyFrame(gazeId + "_back", end, auFrameRight));
                    }
                }
            }
        }

        signals.clear();
        return outputKeyframe;
    }

    //not to be used anymore, because gaze doesnot work as other keyframe generators anymore.
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
    private void addEyesAUKeyFrame(List<Keyframe> outputKeyframe, String gazeId, String timeMarkerName, double time,
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
        if (limitedYaw > 0) {
            verticalTorsionDirection.direction = SpineDirection.Direction.LEFTWARD;
            verticalTorsionDirection.flag = true;
            verticalTorsionDirection.value = Math.abs(limitedYaw);
        } else if (limitedYaw < 0) {
            verticalTorsionDirection.direction = SpineDirection.Direction.RIGHTWARD;
            verticalTorsionDirection.flag = true;
            verticalTorsionDirection.value = Math.abs(limitedYaw);
        }
        if (limitedPitch > 0) {
            sagittalTiltDirection.direction = SpineDirection.Direction.BACKWARD;
            sagittalTiltDirection.flag = true;
            sagittalTiltDirection.value = Math.abs(limitedPitch);
        } else if (limitedPitch < 0) {
            sagittalTiltDirection.direction = SpineDirection.Direction.FORWARD;
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
        if (Double.compare(0.0, eyeYawAngle) == 0
                && Double.compare(0.0, eyePitchAngle) == 0) {
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
    private HeadKeyframe getHeadKeyframeAtTime(double time, List<Keyframe> keyframes,
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
                    result.verticalTorsion.value = 1.0;
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
    private HeadKeyframe findClosestHeadKeyframeAtTime(double time, List<Keyframe> keyframes,
                                                       boolean previous) {
        HeadKeyframe closestKeyframe = null;
        if (keyframes.isEmpty()) {
            return null;
        }

        for (Keyframe keyframe : keyframes) {
            if (keyframe instanceof HeadKeyframe) { //right modality
                //if ((previous && keyframe.getOffset() <= time) || (!previous && keyframe.getOffset() >= time)) { //previous or next
                if ((previous && keyframe.getOffset() <= time) || (!previous && keyframe.getOffset() >= time)) { //previous or next
                    if (closestKeyframe != null) {
                        if (Math.abs(keyframe.getOffset() - time) < Math.abs(closestKeyframe.getOffset() - time)) {
                            closestKeyframe = (HeadKeyframe) keyframe;
                        }
                    } else {
                        closestKeyframe = (HeadKeyframe) keyframe;
                    }
                }
            }
        }
        return closestKeyframe;
    }

    /**
     * Indicates whether at least one of the given keyframes of the given type can be found at the given time
     * @param time value of time where we should look for the keyframes
     * @param keyframes list of keyframes to inspect
     * @param i 1 = HeadKeyFrame, 2 = ShoulderKeyframe, 3 = TorsoKeyframe
     * @return whether one of the given keyframes exists at the given time
     */
    private boolean findExistentKeyframeAtTime(double time, List<Keyframe> keyframes, int i) {
        if (keyframes.isEmpty()) {
            return false;
        }

        for (Keyframe keyframe : keyframes) {
            if (i == 1 && keyframe instanceof HeadKeyframe) { //right modality
                if (keyframe.getOffset() == time) { //already exists
                    return true;
                }
            } else if (i == 2 && keyframe instanceof ShoulderKeyframe) {
                if (keyframe.getOffset() == time) { //already exists
                    return true;
                }
            } else if (i == 3 && keyframe instanceof TorsoKeyframe) {
                if (keyframe.getOffset() == time) { //already exists
                    return true;
                }
            }
        }
        return false;
    }

    private TorsoKeyframe findClosestTorsoKeyframeAtTime(double time, List<Keyframe> keyframes,
                                                         boolean previous) {
        TorsoKeyframe closestKeyframe = null;
        if (keyframes.isEmpty()) {
            return null;
        }

        for (Keyframe keyframe : keyframes) {
            if (keyframe instanceof TorsoKeyframe) { //right modality
                //if ((previous && keyframe.getOffset() <= time) || (!previous && keyframe.getOffset() >= time)) { //previous or next
                if ((previous && keyframe.getOffset() <= time) || (!previous && keyframe.getOffset() >= time)) { //previous or next
                    if (closestKeyframe != null) {
                        if (Math.abs(keyframe.getOffset() - time) < Math.abs(closestKeyframe.getOffset() - time)) {
                            closestKeyframe = (TorsoKeyframe) keyframe;
                        }
                    } else {
                        closestKeyframe = (TorsoKeyframe) keyframe;
                    }
                }
            }
        }
        return closestKeyframe;
    }

    @Override
    public void onTreeChange(TreeEvent te) {
        //rien de special pour l'instant
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

    private Influence computeGazeInfluence(GazeSignal gaze, HeadAngles headAngles) {
        // check the influence. if null the influence is automatically calculated according to the gaze rotation angle
        // - after 15° the head move
        // - with a gaze rotation more than 20, the shoulder start to move
        // - with a gaze rotation more than 30, the all torso start to move
        Influence gazeInfluence = gaze.getInfluence();
        if (gazeInfluence == null) {
            if (Math.max(Math.abs(headAngles.headPitchAngle), Math.abs(headAngles.headYawAngle)) > 0.523599) { // 30°
                gazeInfluence = Influence.TORSO;
            } else if (Math.max(Math.abs(headAngles.headPitchAngle), Math.abs(headAngles.headYawAngle)) > 0.349066) { // 20°
                gazeInfluence = Influence.SHOULDER;
            } else if (Math.max(Math.abs(headAngles.headPitchAngle), Math.abs(headAngles.headYawAngle)) > 0.261799) { // 15°
                gazeInfluence = Influence.HEAD;
            } else {
                gazeInfluence = Influence.EYES;
            }
        }
        gaze.setInfluence(gazeInfluence);
        return gazeInfluence;
    }

    private void setupTorsoSignalAtPosition (TorsoSignal torsoSignal, SpinePhase spinePhase, double startKeyframe,
                                             double endKeyframe, ShouldersAngles shouldersAngles, Influence gazeInfluence) {
        setupSignal(torsoSignal, spinePhase, startKeyframe + shouldersAngles.shoulderLatency, endKeyframe);
        if (gazeInfluence.ordinal() != Influence.TORSO.ordinal()) {
            torsoSignal.shoulder = true;
        }
    }

    private void setupSignal (SpineSignal signal, SpinePhase phaseToAdd, double startValue, double endValue) {
        signal.getPhases().add(phaseToAdd);
        signal.getStart().setValue(startValue);
        signal.getEnd().setValue(endValue);
    }

    private HeadSignal createHeadSignalWithDirectionShift () {
        HeadSignal headSignal = new HeadSignal(IDProvider.createID("gazegenerator").toString());
        headSignal.setDirectionShift(true);
        return headSignal;
    }

    private void addSignalToKeyframeGenerator (SpineSignal spineSignal) {
        for (KeyframeGenerator keyframeGenerator : otherModalitiesKFGenerators) {
            if (keyframeGenerator.accept(spineSignal)) {
                return;
            }
        }
    }

    private void addTwoSignalsToKeyframeGenerator (SpineSignal spineSignal1, SpineSignal spineSignal2) {
        for (KeyframeGenerator keyframeGenerator : otherModalitiesKFGenerators) {
            if (keyframeGenerator.accept(spineSignal1) && keyframeGenerator.accept(spineSignal2)) {
                return;
            }
        }
    }

    private AUAPFrame createAUAPFrameForEyeSide (Side side) {
        AUAPFrame auFrame = new AUAPFrame();
        setupAUAPFrameForEyeSide(auFrame, side);
        return auFrame;
    }

    private void setupAUAPFrameForEyeSide (AUAPFrame auFrame, Side side) {
        auFrame.setAUAP(61, 0.0, side);
        auFrame.setAUAP(62, 0.0, side);
        auFrame.setAUAP(63, 0.0, side);
        auFrame.setAUAP(64, 0.0, side);
    }

    private boolean gazeStillInProgress () {
        return defaultGazeLeft.getAus().getAUAP(61, Side.LEFT).getValue() != 0 || defaultGazeRight.getAus().getAUAP(62, Side.RIGHT).getValue() != 0
                || defaultGazeLeft.getAus().getAUAP(63, Side.LEFT).getValue() != 0 || defaultGazeRight.getAus().getAUAP(63, Side.RIGHT).getValue() != 0
                || defaultGazeLeft.getAus().getAUAP(64, Side.LEFT).getValue() != 0|| defaultGazeRight.getAus().getAUAP(64, Side.RIGHT).getValue() != 0;
    }

    private void handleGazeStillInProgress (double time, List<Keyframe> outputKeyframe) {
        defaultGazeLeft.setOnset(time);
        defaultGazeLeft.setOffset(time);
        defaultGazeRight.setOnset(time);
        defaultGazeRight.setOffset(time);
        outputKeyframe.add(defaultGazeLeft);
        outputKeyframe.add(defaultGazeRight);
    }

    private void startGazeWithHead (double start, double ready, double relax, double end, GazeSignal gaze, double headLimitedYaw, double headLimitedPitch) {
        HeadSignal startGazeHeadSignal = createHeadSignalWithDirectionShift();
        SpinePhase startGazeHeadPhase = createSpinePhase("end", ready, relax, headLimitedYaw, headLimitedPitch);
        startGazeHeadPhase.setStartTime(ready);
        setupSignal(startGazeHeadSignal, startGazeHeadPhase, start, ready);
        if (!gaze.isGazeShift()) {
            HeadSignal endGazeHeadSignal = createHeadSignalWithDirectionShift();
            SpinePhase endGazeHeadPhase = createSpinePhase("end", end, end + 0.01, 0.0, 0.0);
            endGazeHeadPhase.setStartTime(end);
            setupSignal(endGazeHeadSignal, endGazeHeadPhase, relax, end);
            addTwoSignalsToKeyframeGenerator(startGazeHeadSignal, endGazeHeadSignal);
        } else {
            addSignalToKeyframeGenerator(startGazeHeadSignal);
        }
    }

    private double moveEyesToMaxPosition (double relax, double leftLimitedYaw, double leftLimitedPitch,
                                          double rightLimitedYaw, double rightLimitedPitch, List<Keyframe> outputKeyframe, String gazeId,
                                          GazeDirection leftEyeDirection, GazeDirection rightEyeDirection) {
        double timeEyesAtMax = Math.min(relax + Math.max(Math.abs(leftLimitedYaw), Math.abs(leftLimitedPitch)) / EYES_ANGULAR_SPEED,
                relax + Math.max(Math.abs(rightLimitedYaw), Math.abs(rightLimitedPitch)) / EYES_ANGULAR_SPEED);

        addEyesAUKeyFrame(outputKeyframe, gazeId + "_to", "backEyesAtMax",
                timeEyesAtMax, Side.LEFT, leftEyeDirection.opposite(), leftLimitedYaw, leftLimitedPitch);
        addEyesAUKeyFrame(outputKeyframe, gazeId + "_to", "backEyesAtMax",
                timeEyesAtMax, Side.RIGHT, rightEyeDirection.opposite(), rightLimitedYaw, rightLimitedPitch);
        return timeEyesAtMax;
    }

    /**
     * Utility class to compute angles from a character's head/eyes to a target.
     */
    public class HeadAngles {
        //directions to target for leftEye and rightEye
        public GazeDirection leftGazeDirection;
        public GazeDirection rightGazeDirection;
        //raw angles to target
        public double leftYawAngle; //leftEye
        public double leftPitchAngle; //leftEye
        public double rightYawAngle; //rightEye
        public double rightPitchAngle; //rightEye
        public double headYawAngle; //head
        public double headPitchAngle; //head
        //ratio between no movement (0.0) and full movement (1.0)
        //full movement means the physical limit of the eyeball (resp. head): it can only move for 60 degrees (resp 90) or so
        public double leftLimitedYaw;
        public double leftLimitedPitch;
        public double rightLimitedYaw;
        public double rightLimitedPitch;
        public double headLimitedYaw;
        public double headLimitedPitch;
        //can the eye reach the target without moving other modalities (is target in a 60 degrees range)
        public boolean withinEyesLimit;
        //can the head reach the target without moving other modalities
        public boolean withinHeadLimit;

        /**
         * Copy constructor
         *
         * @param headAngles HeadAngles to copy
         */
        public HeadAngles(HeadAngles headAngles) {
            this.leftGazeDirection = headAngles.leftGazeDirection;
            this.rightGazeDirection = headAngles.rightGazeDirection;

            this.leftYawAngle = headAngles.leftYawAngle;
            this.leftPitchAngle = headAngles.leftPitchAngle;
            this.rightYawAngle = headAngles.rightYawAngle;
            this.rightPitchAngle = headAngles.rightPitchAngle;
            this.headYawAngle = headAngles.headYawAngle;
            this.headPitchAngle = headAngles.headPitchAngle;

            this.leftLimitedYaw = headAngles.leftLimitedYaw;
            this.leftLimitedPitch = headAngles.leftLimitedPitch;
            this.rightLimitedYaw = headAngles.rightLimitedYaw;
            this.rightLimitedPitch = headAngles.rightLimitedPitch;
            this.headLimitedYaw = headAngles.headLimitedYaw;
            this.headLimitedPitch = headAngles.headLimitedPitch;

            this.withinEyesLimit = headAngles.withinEyesLimit;
            this.withinHeadLimit = headAngles.withinHeadLimit;
        }

        /**
         * Adjust the eyes angles to the target with a head keyframe. Example:
         * look to a target directly in front of you eyes while having the head
         * down: the eyes will have to move upwards a little bit
         *
         * @param headKeyframe The HeadKeyframe giving us the head position
         * @return The new HeadAngles
         */
        public HeadAngles adjustWithHeadKeyframe(HeadKeyframe headKeyframe) {
            HeadAngles newHeadAngles = new HeadAngles(this);
            if (headKeyframe == null) {
                return newHeadAngles;
            } else {
                /*if (headKeyframe.lateralRoll.flag) {  // Empty if is a waste of time
                    //no influence
                }*/
                if (headKeyframe.sagittalTilt.flag && headKeyframe.sagittalTilt.direction != null) {
                    //influence on pitch
                    newHeadAngles.leftPitchAngle = (newHeadAngles.leftPitchAngle * EYES_PITCH_LIMIT + headKeyframe.getSignedSagittalTilt() * HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                    newHeadAngles.rightPitchAngle = (newHeadAngles.rightPitchAngle * EYES_PITCH_LIMIT + headKeyframe.getSignedSagittalTilt() * HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                    newHeadAngles.headPitchAngle = (newHeadAngles.headPitchAngle * EYES_PITCH_LIMIT + headKeyframe.getSignedSagittalTilt() * HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                }
                if (headKeyframe.verticalTorsion.flag && headKeyframe.verticalTorsion.direction != null) {
                    //influence on yaw
                    //newHeadAngles.leftYawAngle = (newHeadAngles.leftYawAngle - headKeyframe.getSignedVerticalTorsion())
                    newHeadAngles.leftYawAngle = (newHeadAngles.leftYawAngle * EYES_YAW_LIMIT - headKeyframe.getSignedVerticalTorsion() * HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                    newHeadAngles.rightYawAngle = (newHeadAngles.rightYawAngle * EYES_YAW_LIMIT - headKeyframe.getSignedVerticalTorsion() * HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                    newHeadAngles.headYawAngle = (newHeadAngles.headYawAngle * EYES_YAW_LIMIT - headKeyframe.getSignedVerticalTorsion() * HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                }
            }

            //recompute gaze direction
            newHeadAngles.leftGazeDirection = computeGazeDirection(newHeadAngles.leftYawAngle, newHeadAngles.leftPitchAngle);
            newHeadAngles.rightGazeDirection = computeGazeDirection(newHeadAngles.rightYawAngle, newHeadAngles.rightPitchAngle);

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
        public boolean limitHeadAngle() {
            withinHeadLimit = false;
            //head limit angle
            if (Math.abs(headYawAngle) < HEAD_YAW_LIMIT + EYES_YAW_LIMIT
                    && ((headPitchAngle >= 0.0 && Math.abs(headPitchAngle) < HEAD_PITCH_LIMIT_UP + EYES_PITCH_LIMIT)
                    || (headPitchAngle <= 0.0 && Math.abs(headPitchAngle) < HEAD_PITCH_LIMIT_DOWN + EYES_PITCH_LIMIT))) {
                withinHeadLimit = true;
            }

            headLimitedYaw = Math.signum(headYawAngle) * Math.min(Math.abs(headYawAngle), HEAD_YAW_LIMIT) / HEAD_YAW_LIMIT; // Math.signum(headYawAngle) *

            if (headPitchAngle >= 0.0872665) { // 5°
                headLimitedPitch = Math.signum(headPitchAngle) * Math.min(Math.abs(headPitchAngle), HEAD_PITCH_LIMIT_UP) / HEAD_PITCH_LIMIT_UP; // Math.signum(headPitchAngle) *
            }
            //head looks up
            if (headPitchAngle < -0.0872665) { // 5°
                headLimitedPitch = Math.signum(headPitchAngle) * Math.min(Math.abs(headPitchAngle), HEAD_PITCH_LIMIT_DOWN) / HEAD_PITCH_LIMIT_DOWN; // Math.signum(headPitchAngle) *
            }

            return withinHeadLimit;
        }

        /**
         * Compute the ratios of eye movement to target, constraining it within
         * physical limits. This computes leftLimitedYaw, leftLimitedPitch,
         * rightLimitedYaw, rightLimitedPitch.
         *
         * @return {@code "true"} if the target is within Eyes movement limits.
         */
        public boolean limitEyesAngles() {
            //eyes limit angle
            withinEyesLimit = Math.abs(leftYawAngle) < EYES_YAW_LIMIT
                    && Math.abs(rightYawAngle) < EYES_YAW_LIMIT
                    && Math.abs(leftPitchAngle) < EYES_PITCH_LIMIT
                    && Math.abs(rightPitchAngle) < EYES_PITCH_LIMIT;
            // N.B. --> limited angles for the eyes have to be positive for both rotation direction 
            leftLimitedYaw = Math.abs((Math.min(Math.abs(leftYawAngle), EYES_YAW_LIMIT)- Math.abs(headLimitedYaw)*HEAD_YAW_LIMIT) / EYES_YAW_LIMIT); // Math.signum(leftYawAngle) *
            rightLimitedYaw = Math.abs((Math.min(Math.abs(rightYawAngle), EYES_YAW_LIMIT)- Math.abs(headLimitedYaw)*HEAD_YAW_LIMIT) / EYES_YAW_LIMIT); // Math.signum(rightYawAngle) *
            leftLimitedPitch = Math.abs((Math.min(Math.abs(leftPitchAngle), EYES_PITCH_LIMIT)- Math.abs(headLimitedPitch)*HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT); // ToDo: distinguish between up and down head pitch limit. now the two values are equal.
            rightLimitedPitch = Math.abs((Math.min(Math.abs(rightPitchAngle), EYES_PITCH_LIMIT)- Math.abs(headLimitedPitch)*HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT);

            return withinEyesLimit;
        }

        /**
         * Constructor. Computes the head and eyes angles to a target with
         * offset positions.
         */
        public HeadAngles(Environment env, GazeSignal gaze) {
            Vec3d leftRelativeEulerAngles, rightRelativeEulerAngles, headRelativeEulerAngles;
            //euler angles to target + offset, for left eye, right eye, head
            leftYawAngle = 0.0f;
            rightYawAngle = 0.0f;
            headYawAngle = 0.0f;
            leftPitchAngle = 0.0f;
            rightPitchAngle = 0.0f;
            headPitchAngle = 0.0f;
            //ratio of eye/head movement
            leftLimitedYaw = 0.0f;//left eye
            leftLimitedPitch = 0.0f;
            rightLimitedYaw = 0.0f; //right eye
            rightLimitedPitch = 0.0f;
            headLimitedYaw = 0.0f; // head
            headLimitedPitch = 0.0f;

            withinEyesLimit = false;
            withinHeadLimit = false;

            // load the list of characters in the environment  
            List<String> charactersInScene = new ArrayList<>();
            for (int i = 0; i < env.getTreeNode().getChildren().size(); ++i) {
                if (env.getTreeNode().getChildren().get(i) instanceof MPEG4Animatable) {
                    MPEG4Animatable ag = (MPEG4Animatable) env.getTreeNode().getChildren().get(i);
                    charactersInScene.add(ag.getCharacterManager().getCurrentCharacterName());
                }
            }

            // take the MPEG4 for the agent target, i.e. the agent to look at
            MPEG4Animatable targetAgent = new MPEG4Animatable(cm);
            // take the MPEG4 for the agent whom is performing the gaze
            MPEG4Animatable currentAgent = new MPEG4Animatable(gaze.getCharacterManager());
            if (gaze.getTarget() != null || !gaze.getTarget().isEmpty()) {
                for (int i = 0; i < env.getTreeNode().getChildren().size(); ++i) {
                    if (env.getTreeNode().getChildren().get(i) instanceof MPEG4Animatable) {
                        MPEG4Animatable ag = (MPEG4Animatable) env.getTreeNode().getChildren().get(i);
                        if (ag.getCharacterManager().getCurrentCharacterName().equals(gaze.getTarget())) {
                            targetAgent = (MPEG4Animatable) env.getTreeNode().getChildren().get(i);
                            
                        }
                        if (ag.getCharacterManager().getCurrentCharacterName().equals(gaze.getCharacterManager().getCurrentCharacterName())) {
                            currentAgent = (MPEG4Animatable) env.getTreeNode().getChildren().get(i);
                        }
                    }
                }
            }

            //can compute angles to target only if we have an environment
            if (env != null && !gaze.getTarget().equals(cm.getCurrentCharacterName())) {
                Node targetNode = null;
                Vec3d sizeTarget = null;
                String idTarget = "";
                Vec3d vec2target = null;

                if (gaze.getTarget() != null && !gaze.getTarget().isEmpty()) {
                    List<Leaf> environmentLeafs = env.getListLeaf();
                    String gazeTarget = gaze.getTarget();
                    if (gazeTarget.equals("Camera")) {
                        for (int iter = 0; iter<= env.getListeners().size()-1; iter++){
                            String listener = env.getListeners().get(iter).getClass().toString();
                            if (listener.contains("Mixer")){
                                Mixer Cam = (Mixer) env.getListeners().get(iter);
                                vec2target = Cam.getGlobalCoordinates();
                                break;
                            }
                        }
                    } else if (gazeTarget.equals("user")) {                       
                        Animatable us = (Animatable) env.getNode("user");
                        vec2target = us.getCoordinates(); 
                    } else {
                        int ok = 0; // if 0 the target is not an agent, if 1 the target is one of the agent in the scene
                        //Check first if the target is the agent
                        for (String agent : charactersInScene) {
                            if (gazeTarget.equals(agent)) {
                                ok = 1; // agent find

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

                                // find the poit in the meddle between the two eyes
                                vec2target = new Vec3d(positionAgent.x(), headAgent.y(), positionAgent.z());
                                break;
                            } 
                        }
                        // if the target is not the agent I look the target in the environment objects
                        if (ok ==0){
                            targetNode = env.getNode(gaze.getTarget());
                            // search the object (leaf) between evironment objects 
                            for (int iter=0; iter< environmentLeafs.size()-1; iter++) {
                                Leaf check = environmentLeafs.get(iter);
                                boolean test = check.getIdentifier().equals(gaze.getTarget());
                                // once find the object, take the ID
                                if (test) {
                                    idTarget = check.getIdentifier();
                                    sizeTarget = check.getSize();
                                    break;
                                }
                            }
                            // if it is not a leaf but a TreeNode children 
                            if (id_target.equals("")){
                                TreeNode target = (TreeNode) env.getNode(gazeTarget);
                                id_target = target.getIdentifier();
                                sizeTarget = target.getScale();
                            }
                            targetNode = env.getNode(idTarget);
                        }
                    }

                }

                if (targetNode != null || vec2target != null) {

                    //if target is animatable, look at head (for now ! ideally it should be specified in the target attribute)
                    if (targetNode instanceof Animatable) {
                        vec2target = ((TreeNode) env.getNode(gaze.getTarget() + "_AudioTreeNode")).getGlobalCoordinates();
                        vec2target = new Vec3d(vec2target.x(), vec2target.y() + 0.09f, vec2target.z() + 0.13f); // TODO: offsets are in local values, they must be in global values
                    } else {
                        if (vec2target == null) {
                            if (targetNode instanceof Leaf) {
                                targetNode = targetNode.getParent();
                            }
                            vec2target = ((TreeNode) targetNode).getGlobalCoordinates();
                            // the objects are placed on the floor. To take the height we need to take the size along y axis
                            vec2target.setY(vec2target.y() + sizeTarget.y()/2); // take the center of the Target long y axis (size.y / 2)
                        }
                    }

                    // skeleton position
                    Vec3d currentPosition = new Vec3d(currentAgent.getCoordinateX(),
                            currentAgent.getCoordinateY(),
                            currentAgent.getCoordinateZ());

                    // head position 
                    Vec3d headPosition = new Vec3d(currentAgent.getHeadNode().getCoordinateX(),
                            currentAgent.getHeadNode().getCoordinateY(),
                            currentAgent.getHeadNode().getCoordinateZ());

                    // headPosition don't have the right x and z position
                    headPosition.setX(currentPosition.x());
                    headPosition.setZ(currentPosition.z());
                    
                    Vec3d posTarget = new Vec3d();
                    if (gaze.getTarget() == "user"){
                        //vec2target.add(headPosition);
                        posTarget.setX(headPosition.x() + vec2target.x());
                        posTarget.setY(headPosition.y() + vec2target.y());
                        posTarget.setZ(headPosition.z() + vec2target.z());
                    }else{
                        posTarget = vec2target;
                    }
                    
                    // orientation skeleton
                    Quaternion orient = new Quaternion( currentAgent.getRotationNode().getOrientation().x(),
                            currentAgent.getRotationNode().getOrientation().y(),
                            currentAgent.getRotationNode().getOrientation().z(),
                            currentAgent.getRotationNode().getOrientation().w());

                    Vec3d head = Vec3d.addition(headPosition, orient.rotate(headAnglesHeadOffset));
                    Vec3d leftEye = Vec3d.addition(headPosition, orient.rotate(headAnglesLeftEyeOffset));
                    Vec3d rightEye = Vec3d.addition(headPosition, orient.rotate(headAnglesRightEyeOffset));

                    leftRelativeEulerAngles = env.getTargetRelativeEulerAngles(vec2target, leftEye, orient);
                    rightRelativeEulerAngles = env.getTargetRelativeEulerAngles(vec2target, rightEye, orient);
                    headRelativeEulerAngles = env.getTargetRelativeEulerAngles(vec2target, head, orient);

                    leftYawAngle = leftRelativeEulerAngles.x();
                    leftPitchAngle = leftRelativeEulerAngles.y();
                    rightYawAngle = rightRelativeEulerAngles.x();
                    rightPitchAngle = rightRelativeEulerAngles.y();
                    headYawAngle = headRelativeEulerAngles.x();
                    headPitchAngle = headRelativeEulerAngles.y();
            } /*else if(gaze.getTarget().equals(cm.getCurrentCharacterName())) { // if look at my self just look down
                //gaze.setOffsetDirection(GazeDirection.DOWN);
                //gaze.setOffsetAngle(30);
            }*/

            double offsetAngle = Math.toRadians(gaze.getOffsetAngle());
            //add offsets corresponding to offsetDirection
            if (gaze.getOffsetDirection() == GazeDirection.RIGHT
                    || gaze.getOffsetDirection() == GazeDirection.UPRIGHT
                    || gaze.getOffsetDirection() == GazeDirection.DOWNRIGHT) {
                leftYawAngle -= offsetAngle;
                rightYawAngle -= offsetAngle;
                headYawAngle -= offsetAngle;
            } //max PI/12 -> 15degrees
            else if (gaze.getOffsetDirection() == GazeDirection.LEFT
                    || gaze.getOffsetDirection() == GazeDirection.UPLEFT
                    || gaze.getOffsetDirection() == GazeDirection.DOWNLEFT) {
                leftYawAngle += offsetAngle;
                rightYawAngle += offsetAngle;
                headYawAngle += offsetAngle;
            } //max PI/12 -> 15degrees

            if (gaze.getOffsetDirection() == GazeDirection.DOWN
                    || gaze.getOffsetDirection() == GazeDirection.DOWNLEFT
                    || gaze.getOffsetDirection() == GazeDirection.DOWNRIGHT) {
                leftPitchAngle -= offsetAngle;
                rightPitchAngle -= offsetAngle;
                headPitchAngle -= offsetAngle;
            } //max PI/12 -> 15degrees
            else if (gaze.getOffsetDirection() == GazeDirection.UP
                    || gaze.getOffsetDirection() == GazeDirection.UPLEFT
                    || gaze.getOffsetDirection() == GazeDirection.UPRIGHT) {
                leftPitchAngle += offsetAngle;
                rightPitchAngle += offsetAngle;
                headPitchAngle += offsetAngle;
            } //max PI/12 -> 15degrees

            leftGazeDirection = computeGazeDirection(leftYawAngle, leftPitchAngle);
            rightGazeDirection = computeGazeDirection(rightYawAngle, rightPitchAngle);

            withinHeadLimit = limitHeadAngle();
            withinEyesLimit = limitEyesAngles();
        }

    }

    public class ShouldersAngles {

        //raw angles to target
        public HeadAngles headAngles;
        public double shoulderYawAngle; //shoulder
        public double shoulderPitchAngle; //shoulder
        public double shoulderMinimumAlign = 0.0f; // it is the minimum alignment of the shoulder if the designer decide to do not have a full alignment


        //ratio between no movement (0.0) and full movement (1.0)
        //full movement means the physical limit of the eyeball (resp. head): it can only move for 45/55 degrees (resp 90) or so
        public double shoulderLimitedYaw;
        public double shoulderLimitedPitch;

        //can the Shoulders reach the target without moving other modalities
        public boolean withinShoulderLimit;

        public double shoulderLatency;


        /**
         * Copy constructor
         *
         * @param shouldersAngles ShoulderAngles to copy
         */
        public ShouldersAngles(ShouldersAngles shouldersAngles, HeadAngles headAngles) {

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
            //shoulders limit angle
            if (Math.abs(shoulderYawAngle) <= TORSO_YAW_LIMIT + HEAD_YAW_LIMIT) {
                withinShoulderLimit = true;
            }

            // N.B. --> limited angles for the eyes have to be positive for both rotation direction
            if (Math.abs(shoulderYawAngle) > Math.toRadians(135)) { // withinShoulderLimit = false
                shoulderLimitedYaw = Math.signum(shoulderYawAngle) * 1.0; //  
                headAngles.headLimitedYaw = Math.signum(shoulderYawAngle) * 1.0; // Math.signum(headAngles.rightYawAngle) *
                headAngles.rightLimitedYaw = (Math.abs(headAngles.rightYawAngle) - Math.abs(shoulderMinimumAlign)*TORSO_YAW_LIMIT - Math.abs(headAngles.headLimitedYaw)*HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                headAngles.leftLimitedYaw = (Math.abs(headAngles.leftYawAngle) - Math.abs(shoulderMinimumAlign)*TORSO_YAW_LIMIT - Math.abs(headAngles.headLimitedYaw)*HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
            } else {
                double shoulderEyeMinAngle = Math.abs(shoulderMinimumAlign)*TORSO_YAW_LIMIT + 0.261799; // sum of shoulder angle and 15° (eyes minimum angle)
                double ang = Math.abs(headAngles.headYawAngle) - shoulderEyeMinAngle;
                if (ang > Math.PI/2) { // 90°
                    headAngles.headLimitedYaw = Math.signum(shoulderYawAngle) * 1.0;
                    headAngles.rightLimitedYaw = (Math.abs(headAngles.rightYawAngle) - Math.abs(shoulderMinimumAlign)*TORSO_YAW_LIMIT - Math.abs(headAngles.headLimitedYaw)*HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                    headAngles.leftLimitedYaw = (Math.abs(headAngles.leftYawAngle) - Math.abs(shoulderMinimumAlign)*TORSO_YAW_LIMIT - Math.abs(headAngles.headLimitedYaw)*HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                } else {
                    headAngles.headLimitedYaw = Math.signum(shoulderYawAngle)* ang / HEAD_YAW_LIMIT;
                    headAngles.rightLimitedYaw = (Math.abs(headAngles.rightYawAngle) - Math.abs(shoulderMinimumAlign)*TORSO_YAW_LIMIT - Math.abs(headAngles.headLimitedYaw)*HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                    headAngles.leftLimitedYaw = (Math.abs(headAngles.leftYawAngle) - Math.abs(shoulderMinimumAlign)*TORSO_YAW_LIMIT - Math.abs(headAngles.headLimitedYaw)*HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                }    
            }


            // PITCH ANGLE
            if (Math.abs(headAngles.headPitchAngle) > HEAD_PITCH_LIMIT_UP || Math.abs(headAngles.headPitchAngle) > HEAD_PITCH_LIMIT_DOWN) {
                double ang = Math.abs(headAngles.headPitchAngle) - 0.174533; // 10°

                if (ang >HEAD_PITCH_LIMIT_UP || ang > HEAD_PITCH_LIMIT_DOWN) {
                    headAngles.headLimitedPitch = Math.signum(headAngles.headPitchAngle) * 1.0;
                    if (headAngles.headPitchAngle > 0.0) {
                        headAngles.rightPitchAngle = (Math.abs(headAngles.rightPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                        headAngles.leftPitchAngle = (Math.abs(headAngles.leftPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                    } else {
                        headAngles.rightPitchAngle = (Math.abs(headAngles.rightPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_DOWN) / EYES_PITCH_LIMIT;
                        headAngles.leftPitchAngle = (Math.abs(headAngles.leftPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_DOWN) / EYES_PITCH_LIMIT;
                    }
                } else {

                    if (headAngles.headPitchAngle > 0.0) {
                        headAngles.headLimitedPitch = Math.signum(headAngles.headPitchAngle) * ang/HEAD_PITCH_LIMIT_UP;
                        headAngles.rightPitchAngle = (Math.abs(headAngles.rightPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                        headAngles.leftPitchAngle = (Math.abs(headAngles.leftPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                    } else {
                        headAngles.headLimitedPitch = Math.signum(headAngles.headPitchAngle) * ang/HEAD_PITCH_LIMIT_DOWN;
                        headAngles.rightPitchAngle = (Math.abs(headAngles.rightPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_DOWN) / EYES_PITCH_LIMIT;
                        headAngles.leftPitchAngle = (Math.abs(headAngles.leftPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_DOWN) / EYES_PITCH_LIMIT;
                    }
                }
            } else {
                if (headAngles.headPitchAngle > 0.0) {
                    if (headAngles.headPitchAngle > 0.174533) { // 10°
                        headAngles.headLimitedPitch = Math.signum(headAngles.headPitchAngle) * Math.abs(headAngles.headPitchAngle)/HEAD_PITCH_LIMIT_UP;
                    } else {
                        headAngles.headLimitedPitch = 0.0;
                    }
                    //headAngles.headLimitedPitch =  Math.signum(headAngles.headPitchAngle) * (Math.abs(headAngles.headPitchAngle) - 0.174533)/HEAD_PITCH_LIMIT_UP; //Math.signum(headAngles.headPitchAngle) *
                    headAngles.rightPitchAngle = (Math.abs(headAngles.rightPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                    headAngles.leftPitchAngle = (Math.abs(headAngles.leftPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                } else {
                    if (headAngles.headPitchAngle < -0.174533) { // 10°
                        headAngles.headLimitedPitch = Math.signum(headAngles.headPitchAngle) * Math.abs(headAngles.headPitchAngle)/HEAD_PITCH_LIMIT_DOWN;
                    } else {
                        headAngles.headLimitedPitch = 0.0;
                    }
                    //headAngles.headLimitedPitch =  Math.signum(headAngles.headPitchAngle) * (Math.abs(headAngles.headPitchAngle) - 0.174533)/HEAD_PITCH_LIMIT_DOWN;// Math.signum(headAngles.headPitchAngle) *
                    headAngles.rightPitchAngle = (Math.abs(headAngles.rightPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_DOWN) / EYES_PITCH_LIMIT;
                    headAngles.leftPitchAngle = (Math.abs(headAngles.leftPitchAngle) - Math.abs(headAngles.headLimitedPitch)*HEAD_PITCH_LIMIT_DOWN) / EYES_PITCH_LIMIT;
                }
            }

            shoulderLimitedPitch = shoulderPitchAngle;

            return withinShoulderLimit;
        }

        /**
         * Constructor. Computes the head and eyes angles to a target with
         * offset positions.
         */
        public ShouldersAngles(Environment env, GazeSignal gaze, HeadAngles headAngles) {
            this.headAngles = new HeadAngles (headAngles);
            Vec3d shoulderRelativeEulerAngles;
            //euler angles to target + offset, for left eye, right eye, head
            shoulderYawAngle = 0.0f;
            shoulderPitchAngle = 0.0f;

            shoulderLimitedYaw = 0.0f; // shoulder
            shoulderLimitedPitch = 0.0f;

            //withinShoulderLimit = false;

            // load the list of characters in the environment  
            List<String> environmentAgents = new ArrayList<String>();
            for (int i = 0; i < env.getTreeNode().getChildren().size(); ++i) {
                if (env.getTreeNode().getChildren().get(i) instanceof MPEG4Animatable) {
                    MPEG4Animatable ag = (MPEG4Animatable) env.getTreeNode().getChildren().get(i);
                    environmentAgents.add(ag.getCharacterManager().getCurrentCharacterName());
                }
            }

            // take the MPEG4 for the agent target, i.e. the agent to look at
            MPEG4Animatable targetAgent = new MPEG4Animatable(cm);
            // take the MPEG4 for the agent whom is performing the gaze
            MPEG4Animatable currentAgent = new MPEG4Animatable(cm);
            if (gaze.getTarget() != null || !gaze.getTarget().isEmpty()) {
                for (int i = 0; i < env.getTreeNode().getChildren().size(); ++i) {
                    if (env.getTreeNode().getChildren().get(i) instanceof MPEG4Animatable) {
                        MPEG4Animatable ag = (MPEG4Animatable) env.getTreeNode().getChildren().get(i);
                        if (ag.getCharacterManager().getCurrentCharacterName().equals(gaze.getTarget())) {
                            targetAgent = (MPEG4Animatable) env.getTreeNode().getChildren().get(i);
                        }
                        if (ag.getCharacterManager().getCurrentCharacterName().equals(gaze.getCharacterManager().getCurrentCharacterName())) {
                            currentAgent = (MPEG4Animatable) env.getTreeNode().getChildren().get(i);
                        }
                    }
                }
            }

            //can compute angles to target only if we have an environment
            if (env != null && !gaze.getTarget().equals(cm.getCurrentCharacterName())) {
                Vec3d sizeTarget = null;
                Node targetNode = null;
                String idTarget = "";
                Vec3d vec2target = null;

                if (gaze.getTarget() != null && !gaze.getTarget().isEmpty()) {
                    List<Leaf> environmentLeafs = env.getListLeaf();
                    String gazeTarget = gaze.getTarget();

                    if (gazeTarget.equals("Camera")) {
                        for (int iter = 0; iter<= env.getListeners().size()-1; iter++) {
                            String listener = env.getListeners().get(iter).getClass().toString();
                            if (listener.contains("Mixer")) {
                                Mixer Cam = (Mixer) env.getListeners().get(iter);
                                vec2target = Cam.getGlobalCoordinates();
                                break;
                            }
                        }
                    } else if (gazeTarget.equals("user")) {                       
                        Animatable us = (Animatable) env.getNode("user");
                        vec2target = us.getCoordinates(); 
                    } else {
                        int ok = 0  ; // if 0 the target is not an agent, if 1 the target is one of the agent in the scene
                        for (String agent : environmentAgents) {
                            if (gazeTarget.equals(agent)) {
                                ok = 1; // agent find

                                // head position
                                Vec3d headAgent = new Vec3d(targetAgent.getHeadNode().getCoordinateX(),
                                        targetAgent.getHeadNode().getCoordinateY(),
                                        targetAgent.getHeadNode().getCoordinateZ());
                                // skeleton position
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

                                    // find the point in the middle of the two eyes
                                vec2target = new Vec3d(positionAgent.x(), headAgent.y(), positionAgent.z());
                            }
                        }

                        if (ok ==0) {
                            // search the object (leaf) between evironment objects
                            for (int iter=0; iter< environmentLeafs.size()-1; iter++) {
                                Leaf check = environmentLeafs.get(iter);
                                boolean test = check.getIdentifier().equals(gaze.getTarget());
                                // once find the object, take the ID
                                if (test) {
                                    idTarget = check.getIdentifier();
                                    sizeTarget = check.getSize();
                                    break;
                                }
                                 // if it is not a leaf but a TreeNode children 
                                if (idTarget.equals("")){
                                    TreeNode target = (TreeNode) env.getNode(gazeTarget);
                                    id_target = target.getIdentifier();
                                    sizeTarget = target.getScale();
                                }
                                targetNode = env.getNode(idTarget);
                            }
                        }
                    }
                }

                // head position
                Vec3d headPosition = new Vec3d(currentAgent.getHeadNode().getCoordinateX(),
                        currentAgent.getHeadNode().getCoordinateY(),
                        currentAgent.getHeadNode().getCoordinateZ());
                // skeleton position
                Vec3d currentPosition = new Vec3d(currentAgent.getCoordinateX(),
                        currentAgent.getCoordinateY(),
                        currentAgent.getCoordinateZ());

                // headPosition has not the exact x and z position
                headPosition.setX(currentPosition.x());
                headPosition.setZ(currentPosition.z());
                    
                // skeleton orientation
                Quaternion orient = new Quaternion( currentAgent.getRotationNode().getOrientation().x(),
                        currentAgent.getRotationNode().getOrientation().y(),
                        currentAgent.getRotationNode().getOrientation().z(),
                        currentAgent.getRotationNode().getOrientation().w());

                if (targetNode != null || vec2target != null) {
                    //if target is animatable, look at head (for now ! ideally it should be specified in the target attribute)
                    if (targetNode instanceof Animatable) {
                        vec2target = ((TreeNode) env.getNode(gaze.getTarget() + "_AudioTreeNode")).getGlobalCoordinates();
                        vec2target = new Vec3d(vec2target.x(), vec2target.y() + 0.09f, vec2target.z() + 0.13f); // TODO: offsets are in local values, they must be in global values
                    } else {
                        if (vec2target == null) {
                            if (targetNode instanceof Leaf) {
                                targetNode = targetNode.getParent();
                            }
                            vec2target = ((TreeNode) targetNode).getGlobalCoordinates();
                            // the objects are placed on the floor. To take the height we need to take the size along y axis
                            vec2target.setY(vec2target.y() + sizeTarget.y()/2); // take the center of the Target long y axis (size.y / 2)
                        }
                    }

                }
                
                Vec3d posTarget = new Vec3d();
                if (gaze.getTarget() == "user"){
                    //vec2target.add(headPosition);
                    posTarget.setX(headPosition.x() + vec2target.x());
                    posTarget.setY(headPosition.y() + vec2target.y());
                    posTarget.setZ(headPosition.z() + vec2target.z());
                }else{
                    posTarget = vec2target;
                }
                
                //TODO : adapt with scale,character meshes
                Vec3d shoulder = Vec3d.addition(headPosition, orient.rotate(shoulderAnglesHeadOffset));

                // relative angle 
                sh_relativeEulerAngles = env.getTargetRelativeEulerAngles(posTarget, shoulder, orient);	

                // according to the angle amplitude, the head and shoulder will contribute with different movement 
                this.sh_yawAngle = sh_relativeEulerAngles.x();
                sh_pitchAngle = 0.0;
            }

            /**
             * for the coordination eyes-head-torso it is followed the paper:
             * "Gaze and Attention Management for Embodied Conversational Agents"
             * Authors: Pejsa T Andrist S Gleicher M Mutlu B
             **/
            if (Math.abs(Math.max(this.headAngles.leftYawAngle, this.headAngles.rightYawAngle)) < 0.349066) { // 20°
                shoulderMinimumAlign = 0.0;
            } else if (Math.abs(Math.max(this.headAngles.leftYawAngle, this.headAngles.rightYawAngle)) < 0.698132 &&  Math.abs(Math.max(this.headAngles.leftYawAngle, this.headAngles.rightYawAngle)) >= 0.349066) { //  20° =< angle < 40°
                shoulderMinimumAlign = Math.signum(this.headAngles.leftYawAngle)*Math.toRadians(0.8*Math.toDegrees(Math.abs(Math.max(this.headAngles.leftYawAngle, this.headAngles.rightYawAngle))*TORSO_YAW_LIMIT) - 1.45)/TORSO_YAW_LIMIT;
            } else if (Math.abs(Math.max(this.headAngles.leftYawAngle, this.headAngles.rightYawAngle)) >= 0.698132) { //  angle => 40°
                shoulderMinimumAlign = Math.signum(this.headAngles.leftYawAngle)*Math.toRadians(0.43*Math.exp(0.03*Math.abs(Math.toDegrees(Math.max(this.headAngles.leftYawAngle, this.headAngles.rightYawAngle)))) + 0.19)/TORSO_YAW_LIMIT; // *TORSO_YAW_LIMIT
                if (Math.abs(shoulderMinimumAlign*TORSO_YAW_LIMIT) > TORSO_YAW_LIMIT) {
                    shoulderMinimumAlign =  Math.signum(this.headAngles.leftYawAngle)*1.0;
                }
            }

            this.withinShoulderLimit = this.limitShouldersAngle();

            // shoulders latency
            // latency = 0.25*eyesrotation + 47.5  ---> value in ms
            // latency = latency/1000 ---> value in sec
            shoulderLatency = (0.25*Math.toDegrees(Math.max(Math.max(this.headAngles.leftLimitedYaw *EYES_YAW_LIMIT, this.headAngles.rightLimitedYaw *EYES_YAW_LIMIT), Math.max(this.headAngles.leftLimitedPitch *EYES_PITCH_LIMIT, this.headAngles.rightLimitedPitch *EYES_PITCH_LIMIT))) + 47.5)/1000;
        }
    }

}
