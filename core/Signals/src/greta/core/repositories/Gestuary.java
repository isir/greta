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
package greta.core.repositories;

import greta.core.signals.gesture.CircleTrajectoryDescription;
import greta.core.signals.gesture.GesturePose;
import greta.core.signals.gesture.GestureSignal;
import greta.core.signals.gesture.Hand;
import greta.core.signals.gesture.SymbolicPosition;
import greta.core.signals.gesture.TouchPosition;
import greta.core.signals.gesture.TrajectoryDescription;
import greta.core.signals.gesture.UniformPosition;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.IniManager;
import greta.core.util.enums.Side;
import greta.core.util.math.Quaternion;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

/**
 * This class contains a lexicon of symbolical gestures
 *
 * @author Quoc Anh Le
 * @author Jing Huang
 * @author Andre-Marie Pez
 */
public class Gestuary extends SignalLibrary<GestureSignal> implements CharacterDependent {

    private static final String GESTUARY_PARAM_NAME;
    private static final String GESTUARY_XSD;
    public static Gestuary global_gestuary;

    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        if(this.characterManager!=null)
            this.characterManager.remove(this);
        this.characterManager = characterManager;
        characterManager.add(this);
    }

    static {
        GESTUARY_PARAM_NAME = "GESTUARY";
        GESTUARY_XSD = IniManager.getGlobals().getValueString("XSD_GESTUARY");
        global_gestuary = new Gestuary(CharacterManager.getStaticInstance());
    }

    public Gestuary(CharacterManager cm) {
        super();
        setCharacterManager(cm);
        setDefaultDefinition(getCharacterManager().getDefaultValueString(GESTUARY_PARAM_NAME));
        setDefinition(getCharacterManager().getValueString(GESTUARY_PARAM_NAME));
    }

    @Override
    protected List<SignalEntry<GestureSignal>> load(String definition) {

        List<SignalEntry<GestureSignal>> entries = new LinkedList<SignalEntry<GestureSignal>>();
        //load the gesture repository
        XMLParser parser = XML.createParser();
        XMLTree gestuary = parser.parseFileWithXSD(definition, GESTUARY_XSD);

        if (gestuary != null) {
            for (XMLTree gesture : gestuary.getChildrenElement()) {
                if (gesture.isNamed("gesture")) {

                    GestureSignal gestureSignal = new GestureSignal(gesture.getAttribute("id"));
                    gestureSignal.setCategory(gesture.getAttribute("category"));

                    for (XMLTree phase : gesture.getChildrenElement()) {
                        if (phase.isNamed("phase")) {
                            GesturePose gesturePhase = new GesturePose();
                            //TODO//gesturePhase.setTrajectory(phase.getAttribute("trajectory"));
                            for (XMLTree hand : phase.getChildrenElement()) {
                                if (hand.isNamed("hand")) {
                                    Hand gestureHand = new Hand(Side.valueOf(hand.getAttribute("side").toUpperCase()));
                                    if (hand.hasAttribute("openness")) {
                                        gestureHand.setOpenness(hand.getAttributeNumber("openness"));
                                    }
                                    for (XMLTree element : hand.getChildrenElement()) {
                                        if (element.isNamed("trajectory")) {
                                            if (gestureHand.getTrajectory() == null) {
                                                String type = element.getAttribute("type");
                                                if (type.equalsIgnoreCase("circle")) {
                                                    gestureHand.setTrajectory(new CircleTrajectoryDescription());
                                                } else
                                                    gestureHand.setTrajectory(new TrajectoryDescription());
                                            }
                                            readTrajectory(element, gestureHand.getTrajectory());
                                        }

                                        //TouchPosition
                                        if (element.isNamed("touchPosition")) {
                                            TouchPosition pos = new TouchPosition();
                                            pos.setId(element.getAttribute("value"));
                                            gestureHand.setPosition(pos);
                                        }

                                        //SymbolicPosition
                                        if (element.isNamed("symbolicPosition")) {
                                            SymbolicPosition pos = new SymbolicPosition();
                                            for (XMLTree child : element.getChildrenElement()) {
                                                if (child.isNamed("horizontalLocation")) {
                                                    pos.setHorizontalLocation(child.getAttribute("value"));
                                                    pos.setXFixed(Boolean.parseBoolean(child.getAttribute("fixed")));
                                                    pos.setXOverridable(Boolean.parseBoolean(child.getAttribute("overridable")));
                                                }
                                                if (child.isNamed("verticalLocation")) {
                                                    pos.setVerticalLocation(child.getAttribute("value"));
                                                    pos.setYFixed(Boolean.parseBoolean(child.getAttribute("fixed")));
                                                    pos.setYOverridable(Boolean.parseBoolean(child.getAttribute("overridable")));
                                                }
                                                if (child.isNamed("frontalLocation")) {
                                                    pos.setFrontalLocation(child.getAttribute("value"));
                                                    pos.setZFixed(Boolean.parseBoolean(child.getAttribute("fixed")));
                                                    pos.setZOverridable(Boolean.parseBoolean(child.getAttribute("overridable")));
                                                }
                                            }
                                            gestureHand.setPosition(pos);
                                        }

                                        //UniformPosition
                                        if (element.isNamed("uniformPosition")) {
                                            UniformPosition pos = new UniformPosition();
                                            for (XMLTree child : element.getChildrenElement()) {
                                                if (child.isNamed("horizontalLocation")) {
                                                    pos.setX(child.getAttributeNumber("value"));
                                                    pos.setXFixed(Boolean.parseBoolean(child.getAttribute("fixed")));
                                                    pos.setXOverridable(Boolean.parseBoolean(child.getAttribute("overridable")));
                                                }
                                                if (child.isNamed("verticalLocation")) {
                                                    pos.setY(child.getAttributeNumber("value"));
                                                    pos.setYFixed(Boolean.parseBoolean(child.getAttribute("fixed")));
                                                    pos.setYOverridable(Boolean.parseBoolean(child.getAttribute("overridable")));
                                                }
                                                if (child.isNamed("frontalLocation")) {
                                                    pos.setZ(child.getAttributeNumber("value"));
                                                    pos.setZFixed(Boolean.parseBoolean(child.getAttribute("fixed")));
                                                    pos.setZOverridable(Boolean.parseBoolean(child.getAttribute("overridable")));
                                                }
                                            }
                                            gestureHand.setPosition(pos);
                                        }

                                        if (element.isNamed("handShape")) {
                                            gestureHand.setHandShape(element.getAttribute("value"));
                                            gestureHand.setHandShapeOverridable(Boolean.parseBoolean(element.getAttribute("overridable")));
                                        }

                                        if (element.isNamed("wristOrientation")) {
                                            Quaternion xyz = getWristOrientationValue(element);
                                            if (element.hasAttribute("global")) {
                                                gestureHand.setWristOrientationGlobal(Boolean.parseBoolean(element.getAttribute("global")));
                                                gestureHand.setWristOrientationOverridable(Boolean.parseBoolean(element.getAttribute("overridable")));
                                            }
                                            gestureHand.setWristOrientation(xyz);
                                        }
                                    }

                                    if (gestureHand.getSide() == Side.LEFT) {
                                        gesturePhase.setLeftHand(gestureHand);
                                    } else {
                                        gesturePhase.setRightHand(gestureHand);
                                    }
                                }
                            }
                            gestureSignal.addPhase(gesturePhase);
                        }
                    }
                    entries.add(new SignalEntry<GestureSignal>(gestureSignal.getCategory() + "=" + gestureSignal.getId(), gestureSignal));
                }
            }
        }
        return new ArrayList<SignalEntry<GestureSignal>>(entries);
    }

    @Override
    protected void save(String definition, List<SignalEntry<GestureSignal>> paramToSave) {

        XMLTree gestuary = XML.createTree("gestuary");
        gestuary.setAttribute("xmlns:xsi", "http://www.w3.org/2001/XMLSchema-instance");
        gestuary.setAttribute("xsi:noNamespaceSchemaLocation", "../../Common/Data/xml/gestuary.xsd");
        Collections.sort(paramToSave, new Comparator<SignalEntry<GestureSignal>>() {
            @Override
            public int compare(SignalEntry<GestureSignal> o1, SignalEntry<GestureSignal> o2) {
                return String.CASE_INSENSITIVE_ORDER.compare(o1.getParamName(), o2.getParamName());
            }
        });
        for (SignalEntry<GestureSignal> entry : paramToSave) {
            GestureSignal gesture = entry.getSignal();
            if (gesture.getId().equalsIgnoreCase("newGesture")) {
                continue;
            }
            XMLTree gestureTree = gestuary.createChild("gesture");
            gestureTree.setAttribute("id", gesture.getId());
            gestureTree.setAttribute("category", gesture.getCategory());
            for (GesturePose phase : gesture.getPhases()) {
                XMLTree phaseTree = gestureTree.createChild("phase");
                saveHand(phaseTree, phase.getLeftHand());
                saveHand(phaseTree, phase.getRightHand());
            }

        }
        gestuary.save(definition);
    }

    private Quaternion getWristOrientationValue(XMLTree element) {
        return new Quaternion((float) element.getAttributeNumber("x"), (float) element.getAttributeNumber("y"), (float) element.getAttributeNumber("z"), (float) element.getAttributeNumber("w"));
    }

    @Override
    public void onCharacterChanged() {
        setDefinition(getCharacterManager().getValueString(GESTUARY_PARAM_NAME));
    }

    private void saveHand(XMLTree phaseTree, Hand hand) {

        if (hand == null) {
            return;
        }

        XMLTree handTree = phaseTree.createChild("hand");
        handTree.setAttribute("side", hand.getSide().name());
        if (hand.getOpenness() != 0) {
            handTree.setAttribute("openness", Double.toString(hand.getOpenness()));
        }

        if (hand.getPosition() != null) {

            if (hand.getPosition() instanceof TouchPosition) {
                TouchPosition pos = (TouchPosition) hand.getPosition();
                handTree.createChild("touchPosition").setAttribute("value", pos.getId());
            }
            else if (hand.getPosition() instanceof SymbolicPosition) {
                SymbolicPosition pos = (SymbolicPosition) hand.getPosition();
                XMLTree posTree = handTree.createChild("symbolicPosition");

                XMLTree horizontalTree = posTree.createChild("horizontalLocation");
                XMLTree verticalTree = posTree.createChild("verticalLocation");
                XMLTree frontalTree = posTree.createChild("frontalLocation");

                horizontalTree.setAttribute("value", pos.getHorizontalLocation());
                verticalTree.setAttribute("value", pos.getVerticalLocation());
                frontalTree.setAttribute("value", pos.getFrontalLocation());

                horizontalTree.setAttribute("fixed", Boolean.toString(pos.isXFixed()));
                verticalTree.setAttribute("fixed", Boolean.toString(pos.isYFixed()));
                frontalTree.setAttribute("fixed", Boolean.toString(pos.isZFixed()));

                horizontalTree.setAttribute("overridable", Boolean.toString(pos.isXOverridable()));
                verticalTree.setAttribute("overridable", Boolean.toString(pos.isYOverridable()));
                frontalTree.setAttribute("overridable", Boolean.toString(pos.isZOverridable()));
            }
            else if (hand.getPosition() instanceof UniformPosition) {
                UniformPosition pos = (UniformPosition) hand.getPosition();
                XMLTree posTree = handTree.createChild("uniformPosition");

                XMLTree horizontalTree = posTree.createChild("horizontalLocation");
                XMLTree verticalTree = posTree.createChild("verticalLocation");
                XMLTree frontalTree = posTree.createChild("frontalLocation");

                horizontalTree.setAttribute("value", Double.toString(pos.getX()));
                verticalTree.setAttribute("value", Double.toString(pos.getY()));
                frontalTree.setAttribute("value", Double.toString(pos.getZ()));

                horizontalTree.setAttribute("fixed", Boolean.toString(pos.isXFixed()));
                verticalTree.setAttribute("fixed", Boolean.toString(pos.isYFixed()));
                frontalTree.setAttribute("fixed", Boolean.toString(pos.isZFixed()));

                horizontalTree.setAttribute("overridable", Boolean.toString(pos.isXOverridable()));
                verticalTree.setAttribute("overridable", Boolean.toString(pos.isYOverridable()));
                frontalTree.setAttribute("overridable", Boolean.toString(pos.isZOverridable()));
            }
        }

        if (hand.getHandShape() != null) {
            XMLTree child = handTree.createChild("handShape");
            child.setAttribute("value", hand.getHandShape());
            child.setAttribute("overridable", Boolean.toString(hand.isHandShapeOverridable()));
        }

        if (hand.getWristOrientation() != null) {
            Quaternion q = hand.getWristOrientation();
            XMLTree child = handTree.createChild("wristOrientation");
            child.setAttribute("x", Double.toString(q.x()));
            child.setAttribute("y", Double.toString(q.y()));
            child.setAttribute("z", Double.toString(q.z()));
            child.setAttribute("w", Double.toString(q.w()));
            child.setAttribute("global", Boolean.toString(hand.isWristOrientationGlobal()));
            child.setAttribute("overridable", Boolean.toString(hand.isWristOrientationOverridable()));
        }

        saveTrajectory(handTree, hand.getTrajectory());
    }

    private void saveTrajectory(XMLTree phaseTree, TrajectoryDescription trajectory) {
        if (trajectory != null && trajectory.isUsed()) {
            XMLTree trajectoryTree = phaseTree.createChild("trajectory");
            trajectoryTree.setAttribute("type", trajectory.getName());
            for (int i = 0; i < 2; i++) {
                XMLTree paraTree = trajectoryTree.createChild("parameters");
                if (i == 0) {
                    paraTree.setAttribute("axis", "x");
                } else if (i == 1) {
                    paraTree.setAttribute("axis", "y");
                } else if (i == 2) {
                    paraTree.setAttribute("axis", "z");
                }
                paraTree.setAttribute("amplitude", Double.toString(trajectory.getAmplitude()[i]));
                paraTree.setAttribute("frequency", Double.toString(trajectory.getFrequency()[i]));
                paraTree.setAttribute("shift", Double.toString(trajectory.getShift()[i]));
                paraTree.setAttribute("spatialVariation", writeVariation(trajectory.getSpatialVariation()[i]));
                paraTree.setAttribute("temporalVariation", writeVariation(trajectory.getTemporalVariation()[i]));
            }
        }
    }

    private void readTrajectory(XMLTree trajectoryTree, TrajectoryDescription trajectory) {
        //XMLTree trajectoryTree = phaseTree.findNodeCalled("trajectory");
        if (trajectoryTree != null) {
            String type = trajectoryTree.getAttribute("type");
            trajectory.setName(type);
            double[] amplitude = {0, 0, 0};
            double[] frequency = {1, 1, 1};
            double[] shift = {0, 0, 0};
            TrajectoryDescription.Variation[] spatialVariation = {TrajectoryDescription.Variation.NONE, TrajectoryDescription.Variation.NONE, TrajectoryDescription.Variation.NONE};
            TrajectoryDescription.Variation[] temporalVariation = {TrajectoryDescription.Variation.NONE, TrajectoryDescription.Variation.NONE, TrajectoryDescription.Variation.NONE};

//            if (type.equalsIgnoreCase("Circle")) {
//
//            }
            for (XMLTree axisTree : trajectoryTree.getChildrenElement()) {
                if (axisTree.getName().equalsIgnoreCase("parameters")) {
                    int idx = 0;
                    if (axisTree.hasAttribute("axis")) {
                        String axis = axisTree.getAttribute("axis");
                        if (axis.equalsIgnoreCase("x")) {
                            idx = 0;
                        } else if (axis.equalsIgnoreCase("y")) {
                            idx = 1;
                        } else if (axis.equalsIgnoreCase("z")) {
                            idx = 2;
                        }
                    }
                    if (axisTree.hasAttribute("amplitude")) {
                        double amp = axisTree.getAttributeNumber("amplitude");
                        amplitude[idx] = amp;
                    }
                    if (axisTree.hasAttribute("frequency")) {
                        double fre = axisTree.getAttributeNumber("frequency");
                        frequency[idx] = fre;
                    }
                    if (axisTree.hasAttribute("shift")) {
                        double shi = axisTree.getAttributeNumber("shift");
                        shift[idx] = shi;
                    }
                    if (axisTree.hasAttribute("spatialVariation")) {
                        String spa = axisTree.getAttribute("spatialVariation");
                        spatialVariation[idx] = readVariation(spa);
                    }
                    if (axisTree.hasAttribute("temporalVariation")) {
                        String tem = axisTree.getAttribute("temporalVariation");
                        temporalVariation[idx] = readVariation(tem);
                    }
                }
            }
            trajectory.setAmplitude(amplitude);
            trajectory.setFrequency(frequency);
            trajectory.setShift(shift);
            trajectory.setSpatialVariation(spatialVariation);
            trajectory.setTemporalVariation(temporalVariation);
            trajectory.setUsed(true);
            if (trajectory instanceof CircleTrajectoryDescription) {
                //((CircleTrajectoryDescription)trajectory).makeCircle(A, B, radius);
            }
        }
    }

    private TrajectoryDescription.Variation readVariation(String v) {
        if (v.equalsIgnoreCase("+")) {
            return TrajectoryDescription.Variation.GREATER;
        } else if (v.equalsIgnoreCase("-")) {
            return TrajectoryDescription.Variation.SMALLER;
        } else {
            return TrajectoryDescription.Variation.NONE;
        }
    }

    private String writeVariation(TrajectoryDescription.Variation v) {
        if (v == TrajectoryDescription.Variation.GREATER) {
            return "+";
        } else if (v == TrajectoryDescription.Variation.SMALLER) {
            return "-";
        } else {
            return "=";
        }
    }

}
