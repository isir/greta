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
package greta.core.keyframes;

import greta.core.signals.gesture.Hand;
import greta.core.signals.gesture.SymbolicPosition;
import greta.core.signals.gesture.TouchPosition;
import greta.core.signals.gesture.TrajectoryDescription;
import greta.core.util.enums.Side;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.math.Quaternion;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Jing Huang
 */
public class KeyframeFileReader implements KeyframeEmitter {

    ArrayList<KeyframePerformer> performers = new ArrayList<KeyframePerformer>();
    XMLParser xmlparser = XML.createParser();

    public void load(String file) {

        xmlparser.setValidating(false);
        XMLTree _tree = xmlparser.parseFile(file);

        ArrayList<Keyframe> _keyframes = new ArrayList<Keyframe>();

        XMLTree rootNode = _tree.getRootNode();
        List<XMLTree> list = rootNode.getChildrenElement();

        for (int i = 0; i < list.size(); i++) {
            XMLTree node = list.get(i);
            String modality = node.getAttribute("modality");
            if (node.getName().equalsIgnoreCase("keyframe")) {
                if (modality.equalsIgnoreCase("gesture")) {
                    Hand gestureHand = new Hand(Side.valueOf(node.getAttribute("side").toUpperCase()));
                    String t = node.getAttribute("trajectory");
                    if (t == null) {
                        t = "LINEAR";
                    }

                    GestureKeyframe keyframe = new GestureKeyframe(node.getAttribute("id"), node.getAttribute("phase"), new TrajectoryDescription(), node.getAttributeNumber("time"), node.getAttributeNumber("time"), gestureHand, "", false);

                    // GestureKeyframe(String id, String phaseType, double onset, double offset, Hand hand, String scriptName, boolean isScript)
                    _keyframes.add(keyframe);

                    for (XMLTree element : node.getChildrenElement()) {
                        //Different treatment if Symbolic or Touch position
                        //SymbolicPosition
                        if (element.isNamed("horizontalLocation")) {
                            SymbolicPosition pos = (SymbolicPosition) gestureHand.getPosition();
                            pos.setHorizontalLocation(element.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }
                        if (element.isNamed("verticalLocation")) {
                            SymbolicPosition pos = (SymbolicPosition) gestureHand.getPosition();
                            pos.setVerticalLocation(element.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }
                        if (element.isNamed("frontalLocation")) {
                            SymbolicPosition pos = (SymbolicPosition) gestureHand.getPosition();
                            pos.setFrontalLocation(element.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }
                        //TouchPosition
                        if (element.isNamed("touchPosition")) {
                            TouchPosition pos = new TouchPosition();
                            pos.setId(element.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                            gestureHand.setPosition(pos);
                        }
                        if (element.isNamed("handShape")) {
                            gestureHand.setHandShape(element.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }
                        float x = 0, y = 0, z = 0;
                        if (element.isNamed("wristOrientationX")) {
                            x = getWristOrientationValue(element);
                        }
                        if (element.isNamed("wristOrientationY")) {
                            y = getWristOrientationValue(element);
                        }
                        if (element.isNamed("wristOrientationZ")) {
                            z = getWristOrientationValue(element);
                        }
                        Quaternion q = new Quaternion();
                        q.fromEulerXYZByAngle(x, y, z);
                        gestureHand.setWristOrientation(q);

                        if (element.getName().equalsIgnoreCase("SPC")) {
                            keyframe.getParameters().spc = Double.parseDouble(element.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }
                        if (element.getName().equalsIgnoreCase("TMP")) {
                            keyframe.getParameters().tmp = Double.parseDouble(element.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }
                        if (element.getName().equalsIgnoreCase("PWR")) {
                            keyframe.getParameters().pwr = Double.parseDouble(element.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }
                        if (element.getName().equalsIgnoreCase("FLD")) {
                            keyframe.getParameters().fld = Double.parseDouble(element.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }
                        if (element.getName().equalsIgnoreCase("Tension")) {
                            keyframe.getParameters().tension = Double.parseDouble(element.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }

                    }

                } else if (modality.equalsIgnoreCase("head")) {
                    //HeadKeyframe keyframe = new HeadKeyframe(node.getAttribute("category"), node.getAttributeNumber("onset"), node.getAttributeNumber("offset"));
                    //_keyframes.addKeyframeEmitter(keyframe);
                } else if (modality.equalsIgnoreCase("torso")) {
                    //TorsoKeyframe keyframe = new TorsoKeyframe(node.getAttribute("category"), node.getAttributeNumber("onset"), node.getAttributeNumber("offset"));
                    //_keyframes.addKeyframeEmitter(keyframe);
                } else {
                    Logs.warning(this.getClass().getSimpleName() + ": modality \"" + modality + "\" not supported yet.");
                }
            }

        }
        if (!_keyframes.isEmpty()) {
            ID id = IDProvider.createID(file);
            for (KeyframePerformer performer : performers) {
                // TODO : Mode management in progress
                performer.performKeyframes(_keyframes, id);
            }
        }
    }

    private float getWristOrientationValue(XMLTree element) {
        try {
            return Float.parseFloat(element.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
        } catch (Throwable t) {
            Logs.error("class " + this.getClass().getName() + ": " + element.getName() + " is not float");
        }
        return 0;
    }

    @Override
    public void addKeyframePerformer(KeyframePerformer performer) {
        performers.add(performer);
    }

    @Override
    public void removeKeyframePerformer(KeyframePerformer performer) {
        performers.remove(performer);
    }

    public java.io.FileFilter getFileFilter() {
        return new java.io.FileFilter() {

            @Override
            public boolean accept(File pathName) {
                String fileName = pathName.getName().toLowerCase();
                if (fileName.endsWith(".xml")) {
                    try {
                        xmlparser.setValidating(false);
                        return xmlparser.parseFile(pathName.getAbsolutePath()).getName().equalsIgnoreCase("keyframes");
                    } catch (Exception e) {
                    }
                }
                return false;
            }
        };
    }
}
