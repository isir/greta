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

import greta.core.ideationalunits.IdeationalUnit;
import greta.core.ideationalunits.IdeationalUnitFactory;
import greta.core.keyframes.ExpressivityParameters;
import greta.core.keyframes.GestureKeyframe;
import greta.core.keyframes.Keyframe;
import greta.core.repositories.SignalFiller;
import greta.core.signals.Signal;
import greta.core.signals.gesture.GesturePose;
import greta.core.signals.gesture.GestureSignal;
import greta.core.signals.gesture.Hand;
import greta.core.signals.gesture.PointingSignal;
import greta.core.signals.gesture.UniformPosition;
import greta.core.util.enums.Side;
import greta.core.util.environment.Environment;
import greta.core.util.environment.Node;
import greta.core.util.environment.TreeNode;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Quoc Anh Le
 * @author André-Marie Pez
 * @author Mathieu Chollet
 */
public class GestureKeyframeGenerator extends KeyframeGenerator {

    private List<GestureSignal> restPoses;
    private double timeDistanceThreshold = 0.5; //seconds

    private Environment environment;
    private GestureModifier modifier;

    public GestureKeyframeGenerator() {
        super(GestureSignal.class);
        restPoses = new ArrayList<GestureSignal>();
        restPoses.add(createFirstestPose("rest=along"));
        modifier = new ExpressiveGestureModifier();
    }

    public void setEnvironment(Environment env) {
        environment = env;
    }

    public void setGestureModifier(GestureModifier modifier) {
        if (modifier == null) {
            modifier = new DefaultGestureModifier();
        }
        this.modifier = modifier;
    }

    public void fillPointing(PointingSignal ps) {
        if (ps.isFilled() || environment == null) {
            return;
        }
        Vec3d targetPosition;
        Vec3d originPosition;
        Quaternion originOrientation;
        Node or = environment.getNode(ps.getOrigin());

        Node targetNode = environment.getNode(ps.getTarget());

        //System.out.println("Agent " + ps.getOrigin() + ",  Target " + ps.getTarget());
        if (or == null || targetNode == null) {
            System.out.println("Object or agent not found");
            return;
        }

        //find object coordinates
        if (targetNode instanceof TreeNode) {
            targetPosition = ((TreeNode) targetNode).getGlobalCoordinates();
        } else {
            targetPosition = targetNode.getParent().getGlobalCoordinates();
        }
        if (or instanceof TreeNode) {
            originPosition = ((TreeNode) or).getGlobalCoordinates();
            originOrientation = ((TreeNode) or).getGlobalOrientation();
        } else {
            originPosition = or.getParent().getGlobalCoordinates();
            originOrientation = or.getParent().getGlobalOrientation();
        }
        originPosition.setY(originPosition.y() + 1.6);

        //System.out.println("Agent Position: " + originPosition.x() + " " + originPosition.y() + " " + originPosition.z());
        //System.out.println("Object Position: " + targetPosition.x() + " " + targetPosition.y() + " " + targetPosition.z());
        //determine wrist position and hand orientation
        double spc = ps.getSPC() * 0.7 + 0.3;
        Vec3d wristPosition = ((Vec3d.substraction(targetPosition, originPosition)).normalized());
        wristPosition.multiply(spc);

        //System.out.println("Wrist Position: " + wristPosition.x() + " " + wristPosition.y() + " " + wristPosition.z());
        Vec3d wristPositionFinal = originOrientation.inverse().rotate(wristPosition);

        //Elisabetta: the computation of the hand direction (qfinal variable) is missing.
        //The following commented code is just an attempt, but it's not working correctly
        //The angle between wrist position and the hand direction vector (0,-1,0) is:
        //the arccosine of the dot product between the 2 vectors: acos((a.x * b.x) + (a.y * b.y) + (a.z * b.z))
        /*double angle = Math.acos(-1.0*wristPositionFinal.y());
         Quaternion qt = new Quaternion(Vec3d.cross3(wristPositionFinal,new Vec3d(0,-1,0)), angle);
         originOrientation = originOrientation.inverse();
         Quaternion qfinal = Quaternion.multiplication(qt, originOrientation);*/
        Quaternion qfinal = new Quaternion();

        //System.out.println("Wrist Position2: " + wristPositionFinal.x() + " " + wristPositionFinal.y() + " " + wristPositionFinal.z());
        if (wristPositionFinal.z() < 0) {
            //to avoid pointing behind the agent
            return;
        }
        if (wristPositionFinal.x() > 0) {
            //left hand
            ps.setMode(Side.LEFT);
        } else {
            //right hand
            ps.setMode(Side.RIGHT);
        }

        Hand hl = null;
        Hand hr = null;

        String handShape = ps.getReference();
        if (handShape.startsWith("hand_shape=")) {
            handShape = handShape.substring(11);
        } else {
            handShape = "FORM_POINT1";
        }
        if (ps.getMode() == Side.LEFT) {
            hl = new Hand(Side.LEFT, handShape, new UniformPosition(wristPositionFinal.x(), wristPositionFinal.y(), wristPositionFinal.z()), qfinal, null);
        }
        if (ps.getMode() == Side.RIGHT) {
            hr = new Hand(Side.RIGHT, handShape, new UniformPosition(-wristPositionFinal.x(), wristPositionFinal.y(), wristPositionFinal.z()), qfinal, null);
        }
        if (ps.getMode() == Side.BOTH) {
            hr = new Hand(Side.BOTH, handShape, new UniformPosition(-wristPositionFinal.x(), wristPositionFinal.y(), wristPositionFinal.z()), qfinal, null);
            hl = new Hand(Side.BOTH, handShape, new UniformPosition(wristPositionFinal.x(), wristPositionFinal.y(), wristPositionFinal.z()), qfinal, null);
        }
        ps.addPhase(new GesturePose(hl, hr));
        ps.setFilled(true);
    }

    @Override
    protected void generateKeyframes(List<Signal> inputSignals, List<Keyframe> outputKeyframes) {

        //I) update list of rest poses and set the stroke boolean to the last phase
        clearRestPoses();
        GestureSignal lastRest = restPoses.get(0);
        List<GestureSignal> onlyGestures = new ArrayList<GestureSignal>(inputSignals.size());
        for (Signal signal : inputSignals) {
            GestureSignal gesture = (GestureSignal) signal;
            if (gesture.getPhases().size() > 0) {
                gesture.getPhases().get(gesture.getPhases().size() - 1).setIsStrokeEnd(true);
            }
            if (gesture.getCategory() != null && gesture.getCategory().equalsIgnoreCase("rest")) {
                restPoses.add(gesture);
                gesture.setStartRestPose(lastRest.getEndRestPose());
                gesture.propagatePoses();
                lastRest = gesture;
            } else if (gesture.isFilled()) {
                onlyGestures.add(gesture);
            }
        }

        Collections.sort(restPoses, KeyframeGenerator.startComparator);
        Collections.sort(onlyGestures, KeyframeGenerator.startComparator);

        //II) set rest poses into gestures
        for (GestureSignal gesture : onlyGestures) {
            //TODO adapter la rest pose en fontion de la proximite temporelle des gestes
            gesture.setStartRestPose(getRestPoseAt(gesture.getStart().getValue()));
            gesture.setEndRestPose(getRestPoseAt(gesture.getEnd().getValue()));
        }

        // Ideational Units Processing
        List<IdeationalUnitFactory> processedIdeationalUnitFactories = new ArrayList<IdeationalUnitFactory>();
        for (GestureSignal currentGesture : onlyGestures) {
            IdeationalUnit currentGestureIdeationalUnit = currentGesture.getIdeationalUnit();
            if (currentGestureIdeationalUnit != null) {
                IdeationalUnitFactory currentGestureIdeationalUnitFactory = currentGestureIdeationalUnit.getParentFactory();
                if (!processedIdeationalUnitFactories.contains(currentGestureIdeationalUnitFactory)) {
                    processedIdeationalUnitFactories.add(currentGestureIdeationalUnitFactory);
                    currentGestureIdeationalUnitFactory.processIdeationalUnits(restPoses);
                }
            }
        }

        //III) add rest pose transition in gestures
        for (int i = 1/*ignore the first one*/; i < restPoses.size(); ++i) {
            GestureSignal rest = restPoses.get(i);
            //don't add it if it overlaps an other gesture
            boolean overlap = false;
            for (int j = 0; j < onlyGestures.size(); ++j) {
                GestureSignal gesture = onlyGestures.get(j);
                if (gesture.getStart().getValue() < rest.getEnd().getValue()
                        && rest.getStart().getValue() < gesture.getEnd().getValue()) {
                    overlap = true;
                    break;
                }
                if (j > 0) {
                    GestureSignal prevGesture = onlyGestures.get(j - 1);
                    if ((gesture.getStart().getValue() - prevGesture.getEnd().getValue() < timeDistanceThreshold)
                            && (gesture.getStart().getValue() >= rest.getStart().getValue()
                            && rest.getStart().getValue() >= prevGesture.getEnd().getValue())) {
                        overlap = true;
                        break;
                    }
                }
            }

            if (!overlap) {
                onlyGestures.add(rest);
            }
        }

        //IV) testing gestures overlapping and coarticulation
        Collections.sort(onlyGestures, strokeComparator);
        for (int i = 0; i < onlyGestures.size(); ++i) {
            GestureSignal g1 = onlyGestures.get(i);

            g1.propagatePoses();
            TimeMarker st_end1 = g1.getTimeMarker("stroke-end");
            TimeMarker relax1 = g1.getTimeMarker("relax");
            TimeMarker end1 = g1.getEnd();
            for (int j = i + 1; j < onlyGestures.size();) {
                GestureSignal g2 = onlyGestures.get(j);
                TimeMarker start2 = g2.getStart();
                TimeMarker ready2 = g2.getTimeMarker("ready");
                TimeMarker st_start2 = g2.getTimeMarker("stroke-start");
                if (start2.getValue() < end1.getValue() + timeDistanceThreshold) {
                    if (st_start2.getValue() < st_end1.getValue()) {
                        //TODO check side? can we make two gestures (strokes) at the same time?
                        onlyGestures.remove(j);
                        continue;
                    }
                    ++j;

                    //adapt positions
                    GesturePose hold1 = g1.getPhases().get(g1.getPhases().size() - 1);
                    GesturePose hold2 = g2.getPhases().get(0);
                    GesturePose rest2 = g2.getStartRestPose();
                    GesturePose commonPose = new GesturePose(
                            hold2.getLeftHand() == null ? rest2.getLeftHand() : hold1.getLeftHand(), //if doesn't use left, it return to the rest pose, else it start from g1
                            hold2.getRightHand() == null ? rest2.getRightHand() : hold1.getRightHand()//if doesn't use right, it return to the rest pose, else it start from g1
                    );
                    g1.setEndRestPose(commonPose);
                    g2.setStartRestPose(commonPose);
                    double newRetractionDuration = g1.getRetractionDuration();
                    end1.setValue(relax1.getValue() + newRetractionDuration);
                    double newPreparationDuration = g2.getPreparationDuration();
                    start2.setValue(ready2.getValue() - newPreparationDuration);

                    //adapt timings
                    if (start2.getValue() >= end1.getValue()) {
                        //don't change timming
                    } else {
                        double coartDuration = Math.max(newRetractionDuration, newPreparationDuration);

                        //first, try to reduce the hold of the second gesture
                        ready2.setValue(Math.min(st_start2.getValue(), relax1.getValue() + coartDuration));
                        //then, try to reduce the hold of the first gesture
                        relax1.setValue(Math.max(st_end1.getValue(), ready2.getValue() - coartDuration));
                        //finaly here, end1 and relax1 will equals. start2 and ready2 will equals
                        end1.setValue(relax1.getValue());
                        start2.setValue(ready2.getValue());

                        g1.setEndRestPose(hold1);
                        GesturePose newHold2 = new GesturePose(
                                hold2.getLeftHand() == null ? rest2.getLeftHand() : hold2.getLeftHand(), //if doesn't use left, it return to the rest pose, else it start from g1
                                hold2.getRightHand() == null ? rest2.getRightHand() : hold2.getRightHand()//if doesn't use right, it return to the rest pose, else it start from g1
                        );
                        g2.setStartRestPose(newHold2);

                    }
                } else {
                    break;
                }
            }
        }

        //V) create key frames
        GestureKeyframe lqst = null;
        for (GestureSignal gesture : onlyGestures) {
            if (!gesture.isFilled()) {
                continue;
            }
            LinkedList<GestureKeyframe> keyframes = new LinkedList<GestureKeyframe>();
            modifier.generateKeyframesForOneGesture(gesture, keyframes);
            outputKeyframes.addAll(keyframes);
            lqst = keyframes.getLast();
        }
//        if(lqst != null){
//            lqst = new GestureKeyframe("", "", lqst.getTrajectoryType(), lqst.getOffset() + 1, lqst.getOffset() + 1 , lqst.getHand(), lqst.getScriptName(), lqst.isIsScript());
//            outputKeyframes.add(lqst);
//        }
    }

    private GestureSignal createFirstestPose(String ref) {
        GestureSignal initialRestPose = new GestureSignal("firstRestPose");
        initialRestPose.setReference(ref);
        initialRestPose.getStart().setValue(Double.NEGATIVE_INFINITY);
        initialRestPose.getEnd().setValue(-10.0);
        SignalFiller.fill(initialRestPose);
        initialRestPose.propagatePoses();
        return initialRestPose;
    }

    private void clearRestPoses() {
        GestureSignal oldRestPose = restPoses.get(restPoses.size() - 1);
        GestureSignal lastRestPose = createFirstestPose(oldRestPose.getReference());
        restPoses.clear();
        restPoses.add(lastRestPose);
    }

    @Override
    protected Comparator<Signal> getComparator() {
        return startComparator;
    }

    private GesturePose getRestPoseAt(double time) {
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

    private static Comparator<GestureSignal> strokeComparator = new Comparator<GestureSignal>() {

        @Override
        public int compare(GestureSignal o1, GestureSignal o2) {
            return (int) Math.signum(o1.getTimeMarker("stroke-start").getValue() - o2.getTimeMarker("stroke-start").getValue());
        }
    };

    public static interface GestureModifier {

        public void generateKeyframesForOneGesture(GestureSignal gesture, List<GestureKeyframe> outputKeyframes);
    }

    public static class DefaultGestureModifier implements GestureModifier {

        @Override
        public void generateKeyframesForOneGesture(GestureSignal gesture, List<GestureKeyframe> outputKeyframes) {

            ExpressivityParameters ep = new ExpressivityParameters();
            ep.spc = gesture.getSPC();
            ep.tension = gesture.getTension();
            ep.fld = gesture.getFLD();
            ep.pwr = gesture.getPWR();
            ep.tmp = gesture.getTMP();
            ExpressivityParameters epprep = new ExpressivityParameters();
            epprep.spc = gesture.getSPC();
            epprep.tension = gesture.getTension();
            epprep.fld = gesture.getFLD() / 2;
            epprep.pwr = gesture.getPWR() / 2;
            epprep.tmp = gesture.getTMP() / 2;
            ExpressivityParameters epretr = new ExpressivityParameters();
            epretr.spc = gesture.getSPC();
            epretr.tension = gesture.getTension();
            epretr.fld = gesture.getFLD() / 2;
            epretr.pwr = gesture.getPWR() / 2;
            epretr.tmp = gesture.getTMP() / 2;

            double startTime = gesture.getStart().getValue();
            double lastTime = startTime;
            createKeyframe(outputKeyframes, gesture.getId() + "-start", startTime, gesture.getStartRestPose(), epprep);
            List<GesturePose> poses = gesture.getPhases();
            if (!poses.isEmpty()) {
                double strokeTime = gesture.getTimeMarker("stroke-start").getValue();

                double readyTime = gesture.getTimeMarker("ready").getValue();
                if (readyTime <= startTime) {
                    //start does not exist, we are ready or starting the stroke
                    outputKeyframes.clear();
                }

                if (readyTime < strokeTime) {
                    createKeyframe(outputKeyframes, gesture.getId() + "-ready", readyTime, poses.get(0), epprep);
                }

                ExpressivityParameters epstroke = epprep;
                for (GesturePose pose : poses) {
                    createKeyframe(outputKeyframes, gesture.getId() + "-stroke", strokeTime + pose.getRelativeTime(), pose, epstroke);
                    epstroke = ep;
                }

                lastTime = gesture.getTimeMarker("stroke-end").getValue();

                double relaxTime = gesture.getTimeMarker("relax").getValue();
                if (relaxTime > lastTime) {
                    createKeyframe(outputKeyframes, gesture.getId() + "-relax", relaxTime, poses.get(poses.size() - 1), epretr);
                    lastTime = relaxTime;
                }
            }

            double endTime = gesture.getEnd().getValue();
            if (lastTime < endTime) {
                createKeyframe(outputKeyframes, gesture.getId() + "-end", endTime, gesture.getEndRestPose(), epretr);
            }
        }

        protected void createKeyframe(List<GestureKeyframe> outputKeyframes, String id, double time, GesturePose pose, ExpressivityParameters params) {
            GestureKeyframe left = new GestureKeyframe(id + "-left", "useless", pose.getLeftHand().getTrajectory(), time, time, pose.getLeftHand(), null, false);
            left.setParameters(new ExpressivityParameters(params));
            outputKeyframes.add(left);
            GestureKeyframe right = new GestureKeyframe(id + "-right", "useless", pose.getRightHand().getTrajectory(), time, time, pose.getRightHand(), null, false);
            right.setParameters(new ExpressivityParameters(params));
            outputKeyframes.add(right);

        }
    }
}
