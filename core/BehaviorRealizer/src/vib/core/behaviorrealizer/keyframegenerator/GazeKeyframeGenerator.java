/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.behaviorrealizer.keyframegenerator;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import vib.core.keyframes.HeadKeyframe;
import vib.core.keyframes.Keyframe;
import vib.core.keyframes.face.AUKeyFrame;
import vib.core.repositories.AUAPFrame;
import vib.core.repositories.HeadLibrary;
import vib.core.signals.GazeSignal;
import vib.core.signals.HeadSignal;
import vib.core.signals.Signal;
import vib.core.signals.SignalEmitter;
import vib.core.signals.SignalPerformer;
import vib.core.signals.SpineDirection;
import vib.core.signals.SpinePhase;
import vib.core.util.Constants;
import vib.core.util.Mode;
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
 */
public class GazeKeyframeGenerator extends KeyframeGenerator implements EnvironmentEventListener, SignalEmitter{

    //eyes angular speed
    private static final double EYES_ANGULAR_SPEED = 5.2; //300 degs/s, 5.2 rad/s
    //constraint on horizontal eye movement
    private static final double EYES_YAW_LIMIT = Math.PI / 3; //0.6;
    //constraint on vertical eye movement
    private static final double EYES_PITCH_LIMIT = Math.PI / 3;//0.4;// Math.PI / 3;
    //constraint on horizontal head movement
    private static final double HEAD_YAW_LIMIT = Math.toRadians(HeadLibrary.getGlobalLibrary().getHeadIntervals().verticalLeftMax);//(2 * Math.PI / 360) * HeadLibrary.getGlobalLibrary().getHeadIntervals().verticalLeftMax;
    //constraint on vertical head movement
    private static final double HEAD_PITCH_LIMIT_UP = Math.toRadians(HeadLibrary.getGlobalLibrary().getHeadIntervals().sagittalUpMax);//(2 * Math.PI / 360) * HeadLibrary.getGlobalLibrary().getHeadIntervals().sagittalUpMax;
    private static final double HEAD_PITCH_LIMIT_DOWN = Math.toRadians(HeadLibrary.getGlobalLibrary().getHeadIntervals().sagittalDownMax);//(2 * Math.PI / 360) * HeadLibrary.getGlobalLibrary().getHeadIntervals().sagittalDownMax;
    private Environment env = null;
    private List<KeyframeGenerator> otherModalitiesKFGenerators;

    private Map<GazeSignal, Long> currentGazes;
    private List<SignalPerformer> performers;

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
     * other modalites.
     */
    public GazeKeyframeGenerator(List<KeyframeGenerator> otherGenerators) {
        super(GazeSignal.class);
        otherModalitiesKFGenerators = otherGenerators;
        currentGazes = new ConcurrentHashMap<GazeSignal, Long>();
        performers = new ArrayList<SignalPerformer>();
    }

    private void cleanGazeShifts() {

       GazeSignal currentShift = null;
        long currentShiftTime = 0;
        for (GazeSignal gs : currentGazes.keySet()) {
            if (gs.isGazeShift()) {
                long realStartTime = currentGazes.get(gs) + (long) (gs.getStartValue() * 1000);
                if (realStartTime <= Timer.getTimeMillis()) {
                    if (currentShift == null || realStartTime > currentShiftTime) {
                        if (currentShift != null) {
                            // A more recent shift is found : currentShift is outdated
                            currentGazes.remove(currentShift);
                        }
                        currentShift = gs;
                        currentShiftTime = realStartTime;
                    } else {
                        if (realStartTime < currentShiftTime) {
                            // gs is outdated
                            currentGazes.remove(gs);
                        } else {
                            // realStartTime == currentShiftTime
                            // get the one from the latest signal packet:
                            if (currentGazes.get(gs) < currentGazes.get(currentShift)) {
                                currentGazes.remove(gs);
                            } else {
                                currentGazes.remove(currentShift);
                                currentShift = gs;
                                currentShiftTime = realStartTime;
                            }
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
            Collections.sort(signals, getComparator());
        }
        SpinePhase lastShift = null; ///TODO get the last from the previous anim
        for (Signal signal : signals) {
            GazeSignal gaze = (GazeSignal) signal;
            currentGazes.put(gaze, Long.valueOf(Timer.getTimeMillis()));

            //euler angles to target + offset, for head
            HeadAngles ha = new HeadAngles(env, gaze.getOrigin(), gaze.getTarget(),
                    gaze.getOffsetDirection(), gaze.getOffsetAngle());

            //limit
            ha.limitHeadAngle();

            //times computation
            //start keyframe : all influences at original position
            double start = gaze.getStart().getValue();
            //ready keyframe : all influences at gaze+offset position
            double ready = gaze.getTimeMarker("ready").getValue();
            //relax keyframe : all influences at gaze+offset position
            double relax = gaze.getTimeMarker("relax").getValue();
            //end keyframe : all influences at original position
            double end = gaze.getEnd().getValue();

            //keyframes generation
            Influence inf = gaze.getInfluence();
            if (inf.ordinal() >= Influence.HEAD.ordinal()) {
                HeadSignal hs1 = new HeadSignal(IDProvider.createID("gazegenerator").toString());
                hs1.setDirectionShift(true);
                SpinePhase hp1 = createHeadPhase("end", ready, ready, ha.h_limitedYaw, ha.h_limitedPitch);
                hp1.setStartTime(ready);
                hs1.getPhases().add(hp1);
                hs1.getStart().setValue(start);
                hs1.getEnd().setValue(ready);

                if(!gaze.isGazeShift()) {
                    HeadSignal hs2 = new HeadSignal(IDProvider.createID("gazegenerator").toString());
                    hs2.setDirectionShift(true);
                    SpinePhase hp2 = null;
                    if(lastShift==null){
                        hp2 = createHeadPhase("end", end, end, 0.0, 0.0) ;
                    } else {
                        hp2 = new SpinePhase(lastShift);
                    }
                    hp2.setStartTime(end);
                    hs2.getPhases().add(hp2);
                    hs2.getStart().setValue(relax);
                    hs2.getEnd().setValue(end);

                    for (KeyframeGenerator kg : otherModalitiesKFGenerators) {
                        if (kg.accept(hs1) && kg.accept(hs2)) {
                            break;
                        }
                    }
                }
                else {
                    lastShift = hp1;
                    for (KeyframeGenerator kg : otherModalitiesKFGenerators) {
                        if (kg.accept(hs1)) {
                            break;
                        }
                    }
                }

                /*old : we used to generate keyframes, but its better to use shifts so that other keyframe generators know whats going on
                 SpinePhase hp1 = new SpinePhase("start", start, start+0.01);
                 addHeadKeyframe(outputKeyframe, gazeId, hp1.getType(), hp1.getStartTime(), hp1.getEndTime(), 0.0, 0.0);

                 SpinePhase hp2 = new SpinePhase("ready", ready-0.01, ready);//+Math.max(Math.abs(h_limitedPitch), Math.abs(h_limitedYaw))/headAngularSpeedTo);
                 addHeadKeyframe(outputKeyframe, gazeId, hp2.getType(), hp2.getStartTime(), hp2.getEndTime(), h_limitedYaw, h_limitedPitch);

                 SpinePhase hp3 = new SpinePhase("relax", relax, relax+0.01);//+Math.max(Math.abs(h_limitedPitch), Math.abs(h_limitedYaw))/headAngularSpeedTo);
                 addHeadKeyframe(outputKeyframe, gazeId, hp3.getType(), hp3.getStartTime(), hp3.getEndTime(), h_limitedYaw, h_limitedPitch);

                 SpinePhase hp4 = new SpinePhase("end", end-0.01, end);
                 addHeadKeyframe(outputKeyframe, gazeId, hp4.getType(), hp4.getStartTime(), hp4.getEndTime(), 0.0, 0.0);*/

                if (inf.ordinal() >= Influence.SHOULDER.ordinal()) {
                    //unimplemented yet
                    if (inf.ordinal() >= Influence.TORSO.ordinal()) {
                        //unimplemented yet
                        if (inf.ordinal() >= Influence.WHOLE.ordinal()) {
                            //unimplemented yet
                        }
                    }
                }
            }
        }
        return outputKeyframe;
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
            Collections.sort(signals, getComparator());
        }
        for (Signal signal : signals) {
            GazeSignal gaze = (GazeSignal) signal;
            currentGazes.put(gaze, Long.valueOf(Timer.getTimeMillis()));
            String gazeId = gaze.getId();

            //euler angles to target + offset, for head
            HeadAngles ha = new HeadAngles(env, gaze.getOrigin(), gaze.getTarget(),
                    gaze.getOffsetDirection(), gaze.getOffsetAngle());
            ha.limitEyesAngles();

            HeadAngles ha_front = new HeadAngles(env, gaze.getOrigin(), null,
                    GazeDirection.FRONT, 0.0);
            ha_front.limitEyesAngles();

            //times computation
            //start keyframe : all influences at original position
            double start = gaze.getStart().getValue();
            //ready keyframe : all influences at gaze+offset position
            double ready = gaze.getTimeMarker("ready").getValue();
            //relax keyframe : all influences at gaze+offset position
            double relax = gaze.getTimeMarker("relax").getValue();
            //end keyframe : all influences at original position
            double end = gaze.getEnd().getValue();

            List<Double> timesWithEyesKeyframes = new ArrayList<Double>();
            for (Keyframe kf : outputKeyframe) {
                if (kf.getOffset() >= start && kf.getOffset() <= end) {
                    if (kf instanceof HeadKeyframe) {
                        timesWithEyesKeyframes.add(kf.getOffset());
                    }
                }
            }

            HeadKeyframeGenerator hkfg = null;
            for (KeyframeGenerator kg : otherModalitiesKFGenerators) {
                if (kg instanceof HeadKeyframeGenerator) {
                    hkfg = (HeadKeyframeGenerator) kg;
                }
            }


            //eyes start
            addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "start", start, Side.LEFT, ha.l_GazeDirection, 0.0, 0.0); //TODO: use shifts
            addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "start", start, Side.RIGHT, ha.r_GazeDirection, 0.0, 0.0); //TODO: use shifts

            if(gaze.getInfluence().ordinal()>Influence.EYES.ordinal())
            {
                //if(!scheduled) we compute adequate "ready" and "relax" for the eys
                //eyes start moving at "start" time marker, and reach their limit position at "eyesAtMax"
                double timeEyesAtTarget = Math.min(start + Math.max(Math.abs(ha.l_limitedYaw), Math.abs(ha.l_limitedPitch)) / EYES_ANGULAR_SPEED, start + Math.max(Math.abs(ha.r_limitedYaw), Math.abs(ha.r_limitedPitch)) / EYES_ANGULAR_SPEED);
                double timeBackEyesAtZero = Math.min(relax + Math.max(Math.abs(ha.l_limitedYaw), Math.abs(ha.l_limitedPitch)) / EYES_ANGULAR_SPEED, relax + Math.max(Math.abs(ha.r_limitedYaw), Math.abs(ha.r_limitedPitch)) / EYES_ANGULAR_SPEED);

                timesWithEyesKeyframes.add(timeEyesAtTarget);
                timesWithEyesKeyframes.add(timeBackEyesAtZero);
            }

            //else, just use ready and relax
            timesWithEyesKeyframes.add(ready);
            timesWithEyesKeyframes.add(relax);

            Collections.sort(timesWithEyesKeyframes);

            //add Keyframes for the eyes at every moment there is a body keyframe between start and end
            // for instance, when there is a nod, the eyes should keep on the target
            if (!timesWithEyesKeyframes.isEmpty()) {
                Double latestTime = start;
                for (Double time : timesWithEyesKeyframes) {
                    if (time > start && time < end && latestTime.compareTo(time) != 0) {
                        if (time <= relax) {
                            HeadKeyframe hkf = getHeadKeyframeAtTime(time, outputKeyframe, hkfg);
                            HeadAngles hasnew = ha.adjustWithHeadKeyframe(hkf);
                            addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", gazeId + "_to_kf" + time, time, Side.LEFT,
                                    hasnew.l_GazeDirection, hasnew.l_limitedYaw, hasnew.l_limitedPitch);
                            addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", gazeId + "_to_kf" + time, time, Side.RIGHT,
                                    hasnew.r_GazeDirection, hasnew.r_limitedYaw, hasnew.r_limitedPitch);
                        } else if (time > relax && time < end && !gaze.isGazeShift()) {
                            HeadKeyframe hkf = getHeadKeyframeAtTime(time, outputKeyframe, hkfg);
                            HeadAngles hasnew = ha_front.adjustWithHeadKeyframe(hkf);
                            addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", gazeId + "_to_kf" + time, time, Side.LEFT,
                                    hasnew.l_GazeDirection, hasnew.l_limitedYaw, hasnew.l_limitedPitch);
                            addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", gazeId + "_to_kf" + time, time, Side.RIGHT,
                                    hasnew.r_GazeDirection, hasnew.r_limitedYaw, hasnew.r_limitedPitch);
                        }
                        latestTime = time;
                    }
                }
            }
            //end
            if(!gaze.isGazeShift())
            {
                addEyesAUFeyFrame(outputKeyframe, gazeId + "_back", "end", end, Side.LEFT, GazeDirection.FRONT, 0.0, 0.0); //TODO: use shifts
                addEyesAUFeyFrame(outputKeyframe, gazeId + "_back", "end", end, Side.RIGHT, GazeDirection.FRONT, 0.0, 0.0); //TODO: use shifts
            }

            /*if (inf.ordinal() >= Influence.SHOULDER.ordinal()) {

             if (inf.ordinal() >= Influence.TORSO.ordinal()) {

             if (inf.ordinal() >= Influence.WHOLE.ordinal()) {
             }
             }
             }*/
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
        for (Signal signal : inputSignals) {

            GazeSignal gaze = (GazeSignal) signal;
            String gazeId = gaze.getId();
            //if (gaze.isScheduled()) {
            //angle computations, target, and offsets

            //euler angles to target
            //Vec3d[] relativeEulerAngles = new Vec3d[2]; //left eye, right eye
            Vec3d l_relativeEulerAngles, r_relativeEulerAngles, h_relativeEulerAngles;

            //euler angles to target + offset, for left eye, right eye, head
            double l_yawAngle = 0.0f;
            double r_yawAngle = 0.0f;;
            double h_yawAngle = 0.0f;
            double l_pitchAngle = 0.0f;
            double r_pitchAngle = 0.0f;
            double h_pitchAngle = 0.0f;


            //can compute angles to target only if we have an environment
            if (env != null) {
                Node originAudioTreeNode = env.getNode(gaze.getOrigin() + "_AudioTreeNode");
                Node targetNode = env.getNode(gaze.getTarget());

                if (originAudioTreeNode != null && targetNode != null) {
                    //if target is animatable, look at head (for now ! ideally it should be specified in the target attribute)
                    Vec3d target;
                    if (Animatable.class.isInstance(targetNode)) {
                        target = ((TreeNode) env.getNode(gaze.getTarget() + "_AudioTreeNode")).getGlobalCoordinates();
                        target = new Vec3d(target.x(), target.y() + 0.09f, target.z() + 0.13f);
                    } else {
                        target = ((TreeNode) targetNode).getGlobalCoordinates();
                    }

                    Vec3d head = ((TreeNode) originAudioTreeNode).getGlobalCoordinates();
                    head = new Vec3d(head.x(), head.y() + 0.09f, head.z() + 0.13f);
                    //TODO : adapt with scale and character meshes...
                    Vec3d l_eye = new Vec3d(head.x() + 0.03f, head.y(), head.z());
                    Vec3d r_eye = new Vec3d(head.x() - 0.03f, head.y(), head.z());

                    Quaternion orient = ((TreeNode) env.getNode(gaze.getOrigin())).getGlobalOrientation();

                    l_relativeEulerAngles =
                            env.getTargetRelativeEulerAngles(target, l_eye, orient);
                    r_relativeEulerAngles =
                            env.getTargetRelativeEulerAngles(target, r_eye, orient);
                    h_relativeEulerAngles =
                            env.getTargetRelativeEulerAngles(target, head, orient);
                    l_yawAngle = l_relativeEulerAngles.x();
                    l_pitchAngle = l_relativeEulerAngles.y();
                    r_yawAngle = r_relativeEulerAngles.x();
                    r_pitchAngle = r_relativeEulerAngles.y();
                    h_yawAngle = h_relativeEulerAngles.x();
                    h_pitchAngle = h_relativeEulerAngles.y();
                } else {
                    Logs.warning("Couldn't find target or origin in Environment for GazeSignal " + gaze.getId()
                            + ". Proceeding with offsets only");
                }
            }

            //add offsets correspondings to offsetdirection
            if (gaze.getOffsetDirection() == GazeDirection.RIGHT
                    || gaze.getOffsetDirection() == GazeDirection.UPRIGHT
                    || gaze.getOffsetDirection() == GazeDirection.DOWNRIGHT) {
                l_yawAngle -= gaze.getOffsetAngle();
                r_yawAngle -= gaze.getOffsetAngle();
                h_yawAngle -= gaze.getOffsetAngle();
            } //max PI/12 -> 15degrees
            else if (gaze.getOffsetDirection() == GazeDirection.LEFT
                    || gaze.getOffsetDirection() == GazeDirection.UPLEFT
                    || gaze.getOffsetDirection() == GazeDirection.DOWNLEFT) {
                l_yawAngle += gaze.getOffsetAngle();
                r_yawAngle += gaze.getOffsetAngle();
                h_yawAngle += gaze.getOffsetAngle();
            } //max PI/12 -> 15degrees

            if (gaze.getOffsetDirection() == GazeDirection.DOWN
                    || gaze.getOffsetDirection() == GazeDirection.DOWNLEFT
                    || gaze.getOffsetDirection() == GazeDirection.DOWNRIGHT) {
                l_pitchAngle -= gaze.getOffsetAngle();
                r_pitchAngle -= gaze.getOffsetAngle();
                h_pitchAngle -= gaze.getOffsetAngle();
            } //max PI/12 -> 15degrees
            else if (gaze.getOffsetDirection() == GazeDirection.UP
                    || gaze.getOffsetDirection() == GazeDirection.UPLEFT
                    || gaze.getOffsetDirection() == GazeDirection.UPRIGHT) {
                l_pitchAngle += gaze.getOffsetAngle();
                r_pitchAngle += gaze.getOffsetAngle();
                h_pitchAngle += gaze.getOffsetAngle();
            } //max PI/12 -> 15degrees


            GazeDirection l_eyeDirection = computeGazeDirection(l_yawAngle, l_pitchAngle);
            GazeDirection r_eyeDirection = computeGazeDirection(r_yawAngle, r_pitchAngle);

            //eyes limit angle
            boolean withinEyesLimit = false;
            if (Math.abs(l_yawAngle) < EYES_YAW_LIMIT
                    && Math.abs(r_yawAngle) < EYES_YAW_LIMIT
                    && Math.abs(l_pitchAngle) < EYES_PITCH_LIMIT
                    && Math.abs(r_pitchAngle) < EYES_PITCH_LIMIT) {
                withinEyesLimit = true;
            }
            double l_limitedYaw = Math.min(Math.abs(l_yawAngle), EYES_YAW_LIMIT);
            double r_limitedYaw = Math.min(Math.abs(r_yawAngle), EYES_YAW_LIMIT);
            double l_limitedPitch = Math.min(Math.abs(l_pitchAngle), EYES_PITCH_LIMIT);
            double r_limitedPitch = Math.min(Math.abs(r_pitchAngle), EYES_PITCH_LIMIT);

            //head limit angle
            boolean withinHeadAndEyesLimit = false;
            if (Math.abs(h_yawAngle) < HEAD_YAW_LIMIT + EYES_YAW_LIMIT
                    && ((h_pitchAngle >= 0.0 && Math.abs(h_pitchAngle) < HEAD_PITCH_LIMIT_UP + EYES_PITCH_LIMIT)
                    || (h_pitchAngle <= 0.0 && Math.abs(h_pitchAngle) < HEAD_PITCH_LIMIT_DOWN + EYES_PITCH_LIMIT))) {
                withinHeadAndEyesLimit = true;
            }
            double h_limitedYaw = Math.signum(h_yawAngle) * Math.min(Math.abs(h_yawAngle), HEAD_YAW_LIMIT) / HEAD_YAW_LIMIT;
            double h_limitedPitch = h_pitchAngle;
            if (h_pitchAngle > 0.0) {
                h_limitedPitch = Math.signum(h_pitchAngle) * Math.min(Math.abs(h_pitchAngle), HEAD_PITCH_LIMIT_UP) / HEAD_PITCH_LIMIT_UP;
            }
            //head looks up
            if (h_pitchAngle < 0.0) {
                h_limitedPitch = Math.signum(h_pitchAngle) * Math.min(Math.abs(h_pitchAngle), HEAD_PITCH_LIMIT_DOWN) / HEAD_PITCH_LIMIT_DOWN;
            }
            //head looks down

            //times computation
            //start keyframe : all influences at original position
            double start = gaze.getStart().getValue();
            //ready keyframe : all influences at gaze+offset position
            double ready = gaze.getTimeMarker("ready").getValue();
            //relax keyframe : all influences at gaze+offset position
            double relax = gaze.getTimeMarker("relax").getValue();
            //end keyframe : all influences at original position
            double end = gaze.getEnd().getValue();

            //keyframes generation
            Influence inf = gaze.getInfluence();
            if (inf.ordinal() == Influence.EYES.ordinal()) {
                //eyes movement only
                addEyesAUFeyFrame(outputKeyframe, gazeId, "start", start, Side.LEFT, l_eyeDirection, 0, 0);
                addEyesAUFeyFrame(outputKeyframe, gazeId, "start", start, Side.RIGHT, r_eyeDirection, 0, 0);

                addEyesAUFeyFrame(outputKeyframe, gazeId, "ready", ready, Side.LEFT, l_eyeDirection, l_limitedYaw, l_limitedPitch);
                addEyesAUFeyFrame(outputKeyframe, gazeId, "ready", ready, Side.RIGHT, r_eyeDirection, r_limitedYaw, r_limitedPitch);

                if(!gaze.isGazeShift())
                {
                    addEyesAUFeyFrame(outputKeyframe, gazeId, "relax", relax, Side.LEFT, l_eyeDirection, l_limitedYaw, l_limitedPitch);
                    addEyesAUFeyFrame(outputKeyframe, gazeId, "relax", relax, Side.RIGHT, r_eyeDirection, r_limitedYaw, r_limitedPitch);

                    addEyesAUFeyFrame(outputKeyframe, gazeId, "end", end, Side.LEFT, l_eyeDirection, 0, 0);
                    addEyesAUFeyFrame(outputKeyframe, gazeId, "end", end, Side.RIGHT, r_eyeDirection, 0, 0);
                }
            }
            if (inf.ordinal() >= Influence.HEAD.ordinal()) {
                if (withinEyesLimit) {
                    //if the target is inside the eyes range without moving the head or other body part
                    //if the target is outside of the eyes range without moving other body parts
                    double headAngularSpeedTo = Math.abs(h_limitedYaw / (ready - start));
                    double headAngularSpeedBack = Math.abs(h_limitedYaw / (end - relax));

                    //eyes start
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "start", start, Side.LEFT, l_eyeDirection, 0.0, 0.0);
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "start", start, Side.RIGHT, r_eyeDirection, 0.0, 0.0);

                    //eyes start moving at "start" time marker, and reach their limit position at "eyesAtMax"
                    double timeEyesAtTarget = Math.min(start + Math.max(Math.abs(l_limitedYaw), Math.abs(l_limitedPitch)) / EYES_ANGULAR_SPEED, start + Math.max(Math.abs(r_limitedYaw), Math.abs(r_limitedPitch)) / EYES_ANGULAR_SPEED);

                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "eyesAtMax",
                            timeEyesAtTarget, Side.LEFT, l_eyeDirection, l_limitedYaw, l_limitedPitch);
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "eyesAtMax",
                            timeEyesAtTarget, Side.RIGHT, r_eyeDirection, r_limitedYaw, r_limitedPitch);


                    //at ready, every part has reached target : eyes back to center
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "ready", ready, Side.LEFT, l_eyeDirection, 0.0, 0.0);
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "ready", ready, Side.RIGHT, r_eyeDirection, 0.0, 0.0);

                    //at relax, eyes are still at the center
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_back", "relax", relax, Side.LEFT, l_eyeDirection.opposite(), 0.0, 0.0);
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_back", "relax", relax, Side.RIGHT, r_eyeDirection.opposite(), 0.0, 0.0);

                    if(!gaze.isGazeShift())
                    {
                        //eyes move first and reach their max position
                        double timeBackEyesAtZero = Math.min(relax + Math.max(Math.abs(l_limitedYaw), Math.abs(l_limitedPitch)) / EYES_ANGULAR_SPEED, relax + Math.max(Math.abs(r_limitedYaw), Math.abs(r_limitedPitch)) / EYES_ANGULAR_SPEED);

                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "backEyesAtMax",
                                timeBackEyesAtZero, Side.LEFT, l_eyeDirection.opposite(), l_limitedYaw, l_limitedPitch);
                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "backEyesAtMax",
                                timeBackEyesAtZero, Side.RIGHT, r_eyeDirection.opposite(), r_limitedYaw, r_limitedPitch);

                        //end
                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_back", "end", end, Side.LEFT, l_eyeDirection.opposite(), 0.0, 0.0);
                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_back", "end", end, Side.RIGHT, r_eyeDirection.opposite(), 0.0, 0.0);
                    }
                    //head start

                    HeadSignal hs1 = new HeadSignal(IDProvider.createID("gazegenerator").toString());
                    hs1.setDirectionShift(true);
                    SpinePhase hp1 = createHeadPhase("end", ready, relax, h_limitedYaw, h_limitedPitch);
                    hp1.setStartTime(ready);
                    hs1.getPhases().add(hp1);
                    hs1.getStart().setValue(start);
                    hs1.getEnd().setValue(ready);

                    if(!gaze.isGazeShift())
                    {
                        HeadSignal hs2 = new HeadSignal(IDProvider.createID("gazegenerator").toString());
                        hs2.setDirectionShift(true);
                        SpinePhase hp2 = createHeadPhase("end", end, end + 0.01, 0.0, 0.0);
                        hp2.setStartTime(end);
                        hs2.getPhases().add(hp2);
                        hs2.getStart().setValue(relax);
                        hs2.getEnd().setValue(end);
                        for (KeyframeGenerator kg : otherModalitiesKFGenerators) {
                            if (kg.accept(hs1) && kg.accept(hs2)) {
                                break;
                            }
                        }
                    }
                    else
                    {
                        for (KeyframeGenerator kg : otherModalitiesKFGenerators) {
                            if (kg.accept(hs1)) {
                                break;
                            }
                        }
                    }
                    /*SpinePhase hp1 = new SpinePhase("start", start, start+0.01);
                     addHeadKeyframe(outputKeyframe, gazeId, hp1.getType(), hp1.getStartTime(), hp1.getEndTime(), 0.0, 0.0);

                     SpinePhase hp2 = new SpinePhase("ready", ready-0.01, ready);//+Math.max(Math.abs(h_limitedPitch), Math.abs(h_limitedYaw))/headAngularSpeedTo);
                     addHeadKeyframe(outputKeyframe, gazeId, hp2.getType(), hp2.getStartTime(), hp2.getEndTime(), h_limitedYaw, h_limitedPitch);

                     SpinePhase hp3 = new SpinePhase("relax", relax, relax+0.01);//+Math.max(Math.abs(h_limitedPitch), Math.abs(h_limitedYaw))/headAngularSpeedTo);
                     addHeadKeyframe(outputKeyframe, gazeId, hp3.getType(), hp3.getStartTime(), hp3.getEndTime(), h_limitedYaw, h_limitedPitch);

                     SpinePhase hp4 = new SpinePhase("end", end-0.01, end);
                     addHeadKeyframe(outputKeyframe, gazeId, hp4.getType(), hp4.getStartTime(), hp4.getEndTime(), 0.0, 0.0);*/
                } else {
                    //if the target is outside of the eyes range without moving other body parts
                    double headAngularSpeedTo = Math.abs(h_limitedYaw / (ready - start));
                    double headAngularSpeedBack = Math.abs(h_limitedYaw / (end - relax));

                    //eyes start
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "start", start, Side.LEFT, l_eyeDirection, 0.0, 0.0);
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "start", start, Side.RIGHT, r_eyeDirection, 0.0, 0.0);

                    //eyes start moving at "start" time marker, and reach their limit position at "eyesAtMax"
                    double timeEyesAtMax = Math.min(start + Math.max(Math.abs(l_limitedYaw), Math.abs(l_limitedPitch)) / EYES_ANGULAR_SPEED, start + Math.max(Math.abs(r_limitedYaw), Math.abs(r_limitedPitch)) / EYES_ANGULAR_SPEED);

                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "eyesAtMax",
                            timeEyesAtMax, Side.LEFT, l_eyeDirection, l_limitedYaw, l_limitedPitch);
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "eyesAtMax",
                            timeEyesAtMax, Side.RIGHT, r_eyeDirection, r_limitedYaw, r_limitedPitch);

                    //head has moved enough so that eyes can reach target at "eyesCanReachTarget"
                    double timesEyesCanReachTarget = Math.max(timeEyesAtMax,
                            ready - EYES_YAW_LIMIT / headAngularSpeedTo);

                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "eyesCanReachTarget",
                            timesEyesCanReachTarget, Side.LEFT, l_eyeDirection, l_limitedYaw, l_limitedPitch);
                    //ready-EYES_YAW_LIMIT/EYES_ANGULAR_SPEED, Side.LEFT, l_eyeDirection, l_limitedYaw, l_limitedPitch);
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "eyesCanReachTarget",
                            timesEyesCanReachTarget, Side.LEFT, l_eyeDirection, l_limitedYaw, l_limitedPitch);
                    //ready-EYES_YAW_LIMIT/EYES_ANGULAR_SPEED, Side.RIGHT, r_eyeDirection, r_limitedYaw, r_limitedPitch);

                    //at ready, every part has reached target : eyes back to center
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "ready", ready, Side.LEFT, l_eyeDirection, 0.0, 0.0);
                    addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "ready", ready, Side.RIGHT, r_eyeDirection, 0.0, 0.0);

                    if(!gaze.isGazeShift())
                    {
                        //at relax, eyes are still at the center
                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_back", "relax", relax, Side.LEFT, l_eyeDirection.opposite(), 0.0, 0.0);
                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_back", "relax", relax, Side.RIGHT, r_eyeDirection.opposite(), 0.0, 0.0);

                        //eyes move first and reach their max position
                        double timeBackEyesAtMax = Math.min(relax + Math.max(Math.abs(l_limitedYaw), Math.abs(l_limitedPitch)) / EYES_ANGULAR_SPEED, relax + Math.max(Math.abs(r_limitedYaw), Math.abs(r_limitedPitch)) / EYES_ANGULAR_SPEED);

                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "backEyesAtMax",
                                timeBackEyesAtMax, Side.LEFT, l_eyeDirection.opposite(), l_limitedYaw, l_limitedPitch);
                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "backEyesAtMax",
                                timeBackEyesAtMax, Side.RIGHT, r_eyeDirection.opposite(), r_limitedYaw, r_limitedPitch);

                        //head has moved enough so that eyes can reach target at "eyesCanReachTarget"
                        double timesBackEyesCanReachTarget = Math.max(timeBackEyesAtMax,
                                end - EYES_YAW_LIMIT / headAngularSpeedBack);
                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "backEyesCanReachTarget",
                                timesBackEyesCanReachTarget, Side.LEFT, l_eyeDirection.opposite(), l_limitedYaw, l_limitedPitch);
                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_to", "backEyesCanReachTarget",
                                timesBackEyesCanReachTarget, Side.RIGHT, r_eyeDirection.opposite(), r_limitedYaw, r_limitedPitch);

                        //end
                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_back", "end", end, Side.LEFT, l_eyeDirection.opposite(), 0.0, 0.0);
                        addEyesAUFeyFrame(outputKeyframe, gazeId + "_back", "end", end, Side.RIGHT, r_eyeDirection.opposite(), 0.0, 0.0);
                    }
                    //head start

                    HeadSignal hs1 = new HeadSignal(IDProvider.createID("gazegenerator").toString());
                    hs1.setDirectionShift(true);
                    SpinePhase hp1 = createHeadPhase("end", ready, relax, h_limitedYaw, h_limitedPitch);
                    hp1.setStartTime(ready);
                    hs1.getPhases().add(hp1);
                    hs1.getStart().setValue(start);
                    hs1.getEnd().setValue(ready);

                    if(!gaze.isGazeShift())
                    {
                        HeadSignal hs2 = new HeadSignal(IDProvider.createID("gazegenerator").toString());
                        hs2.setDirectionShift(true);
                        SpinePhase hp2 = createHeadPhase("end", end, end + 0.01, 0.0, 0.0);
                        hp2.setStartTime(end);
                        hs2.getPhases().add(hp2);
                        hs2.getStart().setValue(relax);
                        hs2.getEnd().setValue(end);

                        for (KeyframeGenerator kg : otherModalitiesKFGenerators) {
                            if (kg.accept(hs1) && kg.accept(hs2)) {
                                break;
                            }
                        }
                    }
                    else
                    {
                        for (KeyframeGenerator kg : otherModalitiesKFGenerators) {
                            if (kg.accept(hs1)) {
                                break;
                            }
                        }
                    }


                    /*SpinePhase hp1 = new SpinePhase("start", start, start+0.01);

                     addHeadKeyframe(outputKeyframe, gazeId, hp1.getType(), hp1.getStartTime(), hp1.getEndTime(), 0.0, 0.0);

                     SpinePhase hp2 = new SpinePhase("ready", ready-0.01, ready);//+Math.max(Math.abs(h_limitedPitch), Math.abs(h_limitedYaw))/headAngularSpeedTo);
                     addHeadKeyframe(outputKeyframe, gazeId, hp2.getType(), hp2.getStartTime(), hp2.getEndTime(), h_limitedYaw, h_limitedPitch);

                     SpinePhase hp3 = new SpinePhase("relax", relax, relax+0.01);//+Math.max(Math.abs(h_limitedPitch), Math.abs(h_limitedYaw))/headAngularSpeedTo);
                     addHeadKeyframe(outputKeyframe, gazeId, hp3.getType(), hp3.getStartTime(), hp3.getEndTime(), h_limitedYaw, h_limitedPitch);

                     SpinePhase hp4 = new SpinePhase("end", end-0.01, end);
                     addHeadKeyframe(outputKeyframe, gazeId, hp4.getType(), hp4.getStartTime(), hp4.getEndTime(), 0.0, 0.0);*/
                }

                //eyes limit facekeyframe
                //then add new facekeyfrace when each body part starts moving
                    /*if (withinHeadLimit) {
                 } else {
                 }*/
                if (inf.ordinal() >= Influence.SHOULDER.ordinal()) {

                    if (inf.ordinal() >= Influence.TORSO.ordinal()) {

                        if (inf.ordinal() >= Influence.WHOLE.ordinal()) {
                        }
                    }
                }
                //do the reverse for coming back

                // }
            }

            //outputKeyframe = adjustKeyframesWithOtherModalities(outputKeyframe);
        }
        /*Collections.sort(outputKeyframe, keyframeComparator);
         for(Keyframe kf:adjustKeyframesWithOtherModalities(outputKeyframe))
         {
         outputKeyframe.add(kf);
         }*/
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
     * Setter for the environment (needed to compute angles to targets).
     *
     * @param env The Environment in which we compute angles.
     */
    public void setEnvironment(Environment env) {
        this.env = env;
        env.addEnvironementListener(this);
    }


    /**
     * Add a {@code Keyframe} for the eyes movement.
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
    private void addEyesAUFeyFrame(List<Keyframe> outputKeyframe, String gazeId, String timeMarkerName, double time,
            Side side, GazeDirection gazeDirection, double yaw, double pitch) {
        AUAPFrame auFrame = new AUAPFrame((int) (time * Constants.FRAME_PER_SECOND));
        boolean changed = false;

        if (gazeDirection.equals(GazeDirection.FRONT)) {
            auFrame.setAUAP(61, 0.0, side);
            auFrame.setAUAP(62, 0.0, side);
            auFrame.setAUAP(63, 0.0, side);
            auFrame.setAUAP(64, 0.0, side);
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
     * Creates a SpinePhase for the head movements of the gaze signal.
     *
     * @param type Type of {@code SpinePhase}: {@code start}, {@code end}...
     * @param startTime Start time of the {@code SpinePhase}.
     * @param endTime End time of the {@code SpinePhase}.
     * @param h_limitedYaw Signed ratio of horizontal movement of the head
     * compared to physical head limits.
     * @param h_limitedPitch Signed ratio of vertical movement of the head
     * compared to physical head limits.
     * @return The created {@code SpinePhase}.
     */
    private SpinePhase createHeadPhase(String type, double startTime, double endTime, double h_limitedYaw, double h_limitedPitch) {

        SpinePhase hp = new SpinePhase(type, startTime, endTime);

        SpineDirection verticalTorsionDirection = new SpineDirection();
        SpineDirection sagittalTiltDirection = new SpineDirection();
        if (h_limitedYaw > 0) {
            verticalTorsionDirection.direction = SpineDirection.Direction.LEFTWARD;
            verticalTorsionDirection.flag = true;
            verticalTorsionDirection.value = Math.abs(h_limitedYaw);
        } else if (h_limitedYaw < 0) {
            verticalTorsionDirection.direction = SpineDirection.Direction.RIGHTWARD;
            verticalTorsionDirection.flag = true;
            verticalTorsionDirection.value = Math.abs(h_limitedYaw);
        }
        if (h_limitedPitch > 0) {
            sagittalTiltDirection.direction = SpineDirection.Direction.BACKWARD;
            sagittalTiltDirection.flag = true;
            sagittalTiltDirection.value = Math.abs(h_limitedPitch);
        } else if (h_limitedPitch < 0) {
            sagittalTiltDirection.direction = SpineDirection.Direction.FORWARD;
            sagittalTiltDirection.flag = true;
            sagittalTiltDirection.value = Math.abs(h_limitedPitch);
        }

        hp.verticalTorsion = verticalTorsionDirection;
        hp.sagittalTilt = sagittalTiltDirection;
        return hp;
    }

    /**
     * Computes a GazeDirection given horizontal and vertical movements for the
     * gaze.
     *
     * @param eyeYawAngle Horizontal gaze angle.
     * @param eyePitchAngle Vertical gaze angle.
     * @return The computed {@code GazeDirection}.
     */
    private GazeDirection computeGazeDirection(double eyeYawAngle, double eyePitchAngle) {
        GazeDirection returnGD = GazeDirection.FRONT;
        if ((new Double(0.0)).compareTo(eyeYawAngle) == 0
                && (new Double(0.0)).compareTo(eyePitchAngle) == 0){
            return returnGD;
        }
        if (eyeYawAngle <= 1E-6 && eyeYawAngle >= -1E-6) {
            if (eyePitchAngle > 1E-6) {
                returnGD = GazeDirection.UP;
            } else if (eyePitchAngle < -1E-6) {
                returnGD = GazeDirection.DOWN;
            } else if (eyePitchAngle <= 1E-6 && eyePitchAngle >= -1E-6) {
                returnGD = GazeDirection.FRONT;
            }
        } else if (eyeYawAngle > 1E-6) {
            if (eyePitchAngle > 1E-6) {
                returnGD = GazeDirection.UPLEFT;
            } else if (eyePitchAngle < -1E-6) {
                returnGD = GazeDirection.DOWNLEFT;
            } else if (eyePitchAngle <= 1E-6 && eyePitchAngle >= -1E-6) {
                returnGD = GazeDirection.LEFT;
            }
        } else if (eyeYawAngle < 1E-6) {
            if (eyePitchAngle > 1E-6) {
                returnGD = GazeDirection.UPRIGHT;
            } else if (eyePitchAngle < -1E-6) {
                returnGD = GazeDirection.DOWNRIGHT;
            } else if (eyePitchAngle <= 1E-6 && eyePitchAngle >= -1E-6) {
                returnGD = GazeDirection.RIGHT;
            }
        }
        return returnGD;
    }

    /**
     * Interpolates a {@code HeadKeyframe} to use for eye angles computation.
     *
     * @param time The time where we want to interpolate a {@code HeadKeyframe}.
     * @param keyframes The list of {@code HeadKeyframes}.
     * @param hkfg The {@code HeadKeyframeGenerator}, used to do the
     * interpolation.
     * @return The computed {@code HeadKeyframe}.
     */
    private HeadKeyframe getHeadKeyframeAtTime(double time, List<Keyframe> keyframes, HeadKeyframeGenerator hkfg) {
        HeadKeyframe previoushkf = findClosestHeadKeyframeAtTime(time, keyframes, true);
        HeadKeyframe nexthkf = findClosestHeadKeyframeAtTime(time, keyframes, false);

        if (previoushkf == null && nexthkf != null) {
            return nexthkf;
        } else if (previoushkf != null && nexthkf == null) {
            return previoushkf;
        } else if (previoushkf != null && nexthkf != null) {
            if (previoushkf != nexthkf) {
                return hkfg.interpolate(previoushkf, nexthkf, time);
            } else {
                return previoushkf;
            }
        } else {
            return null;
        }
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
        HeadKeyframe closestkf = null;
        if (keyframes.isEmpty()) {
            return null;
        }

        for (Keyframe kf : keyframes) {
            if (kf instanceof HeadKeyframe) { //right modality
                if ((previous && kf.getOffset() <= time) || (!previous && kf.getOffset() >= time)) { //previous or next

                    if (closestkf != null) {
                        if (Math.abs(kf.getOffset() - time) < Math.abs(closestkf.getOffset() - time)) {
                            closestkf = (HeadKeyframe) kf;
                        }
                    } else {
                        closestkf = (HeadKeyframe) kf;
                    }
                }
            }
        }
        return closestkf;
    }

    @Override
    public void onTreeChange(TreeEvent te) {
        //rien de special pour l'instant
    }

    @Override
    public void onNodeChange(NodeEvent ne) {
        if(!currentGazes.isEmpty())
        {
            List<Signal> lst = new ArrayList<Signal>();
            Node moved = env.getNode(ne.getIdNode());

            this.cleanGazeShifts();

            for(GazeSignal gs : currentGazes.keySet()) {
                if(!gs.isGazeShift() && Timer.getTimeMillis()>currentGazes.get(gs)+(long)gs.getTimeMarker("relax").getValue()*1000) {
                    currentGazes.remove(gs);
                    continue;
                }
                Node targetNode = env.getNode(gs.getTarget());
                //Node origineNode = env.getNode(gs.getOrigin()+ "_AudioTreeNode");
                if(moved.isAncestorOf(targetNode) /* || moved.isAncestorOf(origineNode) */ ) {
                    lst.add(gs);
                }
            }

            if( ! lst.isEmpty()){
                ID id = IDProvider.createID("GazeFollow");
                for(SignalPerformer sp : performers) {
                    sp.performSignals(lst, id, new Mode(CompositionType.blend));
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


    private static Vec3d headAngles_head_offset = new Vec3d(0, 0.0534, 0); //new Vec3d(0, 0.0534, 0.0617);
    private static Vec3d headAngles_l_eye_offset = new Vec3d(headAngles_head_offset.x() + 0.0304, headAngles_head_offset.y(), 0.0617);
    private static Vec3d headAngles_r_eye_offset = new Vec3d(headAngles_head_offset.x() - 0.0304, headAngles_head_offset.y(), 0.0617);

    /**
     * Utility class to compute angles from a character's head/eyes to a target.
     */
    public class HeadAngles {

        //directions to target for l_eye and r_eye
        public GazeDirection l_GazeDirection;
        public GazeDirection r_GazeDirection;
        //raw angles to target
        public double l_yawAngle = 0.0f; //l_eye
        public double l_pitchAngle = 0.0f; //l_eye
        public double r_yawAngle = 0.0f; //r_eye
        public double r_pitchAngle = 0.0f; //r_eye
        public double h_yawAngle = 0.0f; //head
        public double h_pitchAngle = 0.0f; //head
        //ratio between no movement (0.0) and full movement (1.0)
        //full movement means the physical limit of the eyeball (resp. head): it can only move for 60 degrees (resp 90) or so
        public double l_limitedYaw = 0.0f;
        public double l_limitedPitch = 0.0f;
        public double r_limitedYaw = 0.0f;
        public double r_limitedPitch = 0.0f;
        public double h_limitedYaw = 0.0f;
        public double h_limitedPitch = 0.0f;
        //can the eye reach the target without moving other modalities (is target in a 60 degrees range)
        public boolean withinEyesLimit = false;
        //can the head reach the target without moving other modalities
        public boolean withinHeadLimit = false;

        /**
         * Copy constructor
         *
         * @param ha HeadAngles to copy
         */
        public HeadAngles(HeadAngles ha) {
            this.l_GazeDirection = ha.l_GazeDirection;
            this.r_GazeDirection = ha.r_GazeDirection;

            this.l_yawAngle = ha.l_yawAngle;
            this.l_pitchAngle = ha.l_pitchAngle;
            this.r_yawAngle = ha.r_yawAngle;
            this.r_pitchAngle = ha.r_pitchAngle;
            this.h_yawAngle = ha.h_yawAngle;
            this.h_pitchAngle = ha.h_pitchAngle;

            this.l_limitedYaw = ha.l_limitedYaw;
            this.l_limitedPitch = ha.l_limitedPitch;
            this.r_limitedYaw = ha.r_limitedYaw;
            this.r_limitedPitch = ha.r_limitedPitch;
            this.h_limitedYaw = ha.h_limitedYaw;
            this.h_limitedPitch = ha.h_limitedPitch;

            this.withinEyesLimit = ha.withinEyesLimit;
            this.withinHeadLimit = ha.withinHeadLimit;
        }

        /**
         * Adjust the eyes angles to the target with a head keyframe. Example:
         * look to a target directly in front of you eyes while having the head
         * down: the eyes will have to move upwards a little bit
         *
         * @param hkf The HeadKeyframe giving us the head position
         * @return The new HeadAngles
         */
        public HeadAngles adjustWithHeadKeyframe(HeadKeyframe hkf) {
            HeadAngles hanew = new HeadAngles(this);
            if (hkf == null) {
                return hanew;
            } else {
                if (hkf.lateralRoll.flag) {
                    //no influence
                }
                if (hkf.sagittalTilt.flag && hkf.sagittalTilt.direction != null) {
                    //influence on pitch
                        hanew.l_pitchAngle = (hanew.l_pitchAngle * EYES_PITCH_LIMIT + hkf.getSignedSagittalTilt() * HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                        hanew.r_pitchAngle = (hanew.r_pitchAngle * EYES_PITCH_LIMIT + hkf.getSignedSagittalTilt() * HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                        hanew.h_pitchAngle = (hanew.h_pitchAngle * EYES_PITCH_LIMIT + hkf.getSignedSagittalTilt() * HEAD_PITCH_LIMIT_UP) / EYES_PITCH_LIMIT;
                }
                if (hkf.verticalTorsion.flag && hkf.verticalTorsion.direction != null) {
                    //influence on yaw
                        hanew.l_yawAngle = (hanew.l_yawAngle * EYES_YAW_LIMIT - hkf.getSignedVerticalTorsion() * HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                        hanew.r_yawAngle = (hanew.r_yawAngle * EYES_YAW_LIMIT - hkf.getSignedVerticalTorsion() * HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                        hanew.h_yawAngle = (hanew.h_yawAngle * EYES_YAW_LIMIT - hkf.getSignedVerticalTorsion() * HEAD_YAW_LIMIT) / EYES_YAW_LIMIT;
                }
            }

            //recompute gaze direction
            hanew.l_GazeDirection = computeGazeDirection(hanew.l_yawAngle, hanew.l_pitchAngle);
            hanew.r_GazeDirection = computeGazeDirection(hanew.r_yawAngle, hanew.r_pitchAngle);

            //limit eyes angles
            hanew.limitEyesAngles();
            return hanew;
        }

        /**
         * Compute the ratios of head movement to target, constraining it within
         * physical limits. This computes h_limitedYaw and h_limitedPitch.
         *
         * @return {@code "true"} if the target is within Head movement limits.
         */
        public boolean limitHeadAngle() {
            withinHeadLimit = false;
            //head limit angle
            if (Math.abs(h_yawAngle) < HEAD_YAW_LIMIT + EYES_YAW_LIMIT
                    && ((h_pitchAngle >= 0.0 && Math.abs(h_pitchAngle) < HEAD_PITCH_LIMIT_UP + EYES_PITCH_LIMIT)
                    || (h_pitchAngle <= 0.0 && Math.abs(h_pitchAngle) < HEAD_PITCH_LIMIT_DOWN + EYES_PITCH_LIMIT))) {
                withinHeadLimit = true;
            }
            h_limitedYaw = Math.signum(h_yawAngle) * Math.min(Math.abs(h_yawAngle), HEAD_YAW_LIMIT) / HEAD_YAW_LIMIT;
            h_limitedPitch = h_pitchAngle;
            if (h_pitchAngle > 0.0) {
                h_limitedPitch = Math.signum(h_pitchAngle) * Math.min(Math.abs(h_pitchAngle), HEAD_PITCH_LIMIT_UP) / HEAD_PITCH_LIMIT_UP;
            }
            //head looks up
            if (h_pitchAngle < 0.0) {
                h_limitedPitch = Math.signum(h_pitchAngle) * Math.min(Math.abs(h_pitchAngle), HEAD_PITCH_LIMIT_DOWN) / HEAD_PITCH_LIMIT_DOWN;
            }

            return withinHeadLimit;
        }

        /**
         * Compute the ratios of eye movement to target, constraining it within
         * physical limits. This computes l_limitedYaw, l_limitedPitch,
         * r_limitedYaw, r_limitedPitch.
         *
         * @return {@code "true"} if the target is within Eyes movement limits.
         */
        public boolean limitEyesAngles() {
            //eyes limit angle
            withinEyesLimit = false;
            if (Math.abs(l_yawAngle) < EYES_YAW_LIMIT
                    && Math.abs(r_yawAngle) < EYES_YAW_LIMIT
                    && Math.abs(l_pitchAngle) < EYES_PITCH_LIMIT
                    && Math.abs(r_pitchAngle) < EYES_PITCH_LIMIT) {
                withinEyesLimit = true;
            }
            l_limitedYaw = Math.min(Math.abs(l_yawAngle), EYES_YAW_LIMIT) / EYES_YAW_LIMIT;
            r_limitedYaw = Math.min(Math.abs(r_yawAngle), EYES_YAW_LIMIT) / EYES_YAW_LIMIT;
            l_limitedPitch = Math.min(Math.abs(l_pitchAngle), EYES_PITCH_LIMIT) / EYES_PITCH_LIMIT;
            r_limitedPitch = Math.min(Math.abs(r_pitchAngle), EYES_PITCH_LIMIT) / EYES_PITCH_LIMIT;

            return withinEyesLimit;
        }

        /**
         * Constructor. Computes the head and eyes angles to a target with
         * offset positions.
         */
        public HeadAngles(Environment env, String source, String target, GazeDirection offsetDirection, double offsetAngle) {
            Vec3d l_relativeEulerAngles, r_relativeEulerAngles, h_relativeEulerAngles;

            //euler angles to target + offset, for left eye, right eye, head
            l_yawAngle = 0.0f;
            r_yawAngle = 0.0f;;
            h_yawAngle = 0.0f;
            l_pitchAngle = 0.0f;
            r_pitchAngle = 0.0f;
            h_pitchAngle = 0.0f;

            //ratio of eye/head movement
            l_limitedYaw = 0.0f;
            r_limitedYaw = 0.0f;;
            h_limitedYaw = 0.0f;
            l_limitedPitch = 0.0f;
            r_limitedPitch = 0.0f;
            h_limitedPitch = 0.0f;

            withinEyesLimit = false;
            withinHeadLimit = false;

            //can compute angles to target only if we have an environment
            if (env != null) {
                Node originAudioTreeNode = env.getNode(source + "_AudioTreeNode"); //source_AudioTreeNode
                Node targetNode = null;
                if (target != null && !target.isEmpty()) {
                    targetNode = env.getNode(target);
                }

                if (originAudioTreeNode != null) {

                    Vec3d headPosition = ((TreeNode) originAudioTreeNode).getGlobalCoordinates();

                    if (targetNode != null) {
                        Vec3d vec2target;
                        //if target is animatable, look at head (for now ! ideally it should be specified in the target attribute)
                        if (Animatable.class.isInstance(targetNode)) {
                            vec2target = ((TreeNode) env.getNode(target + "_AudioTreeNode")).getGlobalCoordinates();
                            vec2target = new Vec3d(vec2target.x(), vec2target.y() + 0.09f, vec2target.z() + 0.13f); // TODO: offsets are in local values, they must be in global values
                        } else {
                            if(targetNode instanceof Leaf){
                                targetNode = targetNode.getParent();
                            }
                            vec2target = ((TreeNode) targetNode).getGlobalCoordinates();
                        }
                        Quaternion orient = ((TreeNode) env.getNode(source)).getGlobalOrientation();

                        //TODO : adapt with scale,character meshes
                        Vec3d head = Vec3d.addition(headPosition, orient.rotate(headAngles_head_offset));
                        Vec3d l_eye = Vec3d.addition(headPosition, orient.rotate(headAngles_l_eye_offset));
                        Vec3d r_eye = Vec3d.addition(headPosition, orient.rotate(headAngles_r_eye_offset));

                        l_relativeEulerAngles =
                                env.getTargetRelativeEulerAngles(vec2target, l_eye, orient);
                        r_relativeEulerAngles =
                                env.getTargetRelativeEulerAngles(vec2target, r_eye, orient);
                        h_relativeEulerAngles =
                                env.getTargetRelativeEulerAngles(vec2target, head, orient);
                        l_yawAngle = l_relativeEulerAngles.x();
                        l_pitchAngle = l_relativeEulerAngles.y();
                        r_yawAngle = r_relativeEulerAngles.x();
                        r_pitchAngle = r_relativeEulerAngles.y();
                        h_yawAngle = h_relativeEulerAngles.x();
                        h_pitchAngle = h_relativeEulerAngles.y();
                    } else {
                        //look in front
                    }
                } else {
                    Logs.warning("Couldn't find target " + target + " or source " + source + " in Environment for GazeSignal. "
                            + "Proceeding with offsets only");
                }
            }

            //add offsets correspondings to offsetdirection
            if (offsetDirection == GazeDirection.RIGHT
                    || offsetDirection == GazeDirection.UPRIGHT
                    || offsetDirection == GazeDirection.DOWNRIGHT) {
                l_yawAngle -= offsetAngle;
                r_yawAngle -= offsetAngle;
                h_yawAngle -= offsetAngle;
            } //max PI/12 -> 15degrees
            else if (offsetDirection == GazeDirection.LEFT
                    || offsetDirection == GazeDirection.UPLEFT
                    || offsetDirection == GazeDirection.DOWNLEFT) {
                l_yawAngle += offsetAngle;
                r_yawAngle += offsetAngle;
                h_yawAngle += offsetAngle;
            } //max PI/12 -> 15degrees

            if (offsetDirection == GazeDirection.DOWN
                    || offsetDirection == GazeDirection.DOWNLEFT
                    || offsetDirection == GazeDirection.DOWNRIGHT) {
                l_pitchAngle -= offsetAngle;
                r_pitchAngle -= offsetAngle;
                h_pitchAngle -= offsetAngle;
            } //max PI/12 -> 15degrees
            else if (offsetDirection == GazeDirection.UP
                    || offsetDirection == GazeDirection.UPLEFT
                    || offsetDirection == GazeDirection.UPRIGHT) {
                l_pitchAngle += offsetAngle;
                r_pitchAngle += offsetAngle;
                h_pitchAngle += offsetAngle;
            } //max PI/12 -> 15degrees

            l_GazeDirection = computeGazeDirection(l_yawAngle, l_pitchAngle);
            r_GazeDirection = computeGazeDirection(r_yawAngle, r_pitchAngle);

            withinHeadLimit = limitHeadAngle();
            withinEyesLimit = limitEyesAngles();
        }
    }
}
