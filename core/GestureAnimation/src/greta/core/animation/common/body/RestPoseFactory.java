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
package greta.core.animation.common.body;

import greta.core.animation.common.Frame.JointFrame;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.math.Quaternion;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Jing Huang
 */
public class RestPoseFactory extends CharacterDependentAdapter {

    private String xmlFileRESTPOSE;

    public String getXmlFileRESTPOSE() {
        return xmlFileRESTPOSE;
    }

    public void setXmlFileRESTPOSE(String _xmlFileRESTPOSE) {
        xmlFileRESTPOSE = _xmlFileRESTPOSE;
    }
    HashMap<String, HashMap<String, JointFrame>> _poseList = new HashMap<String, HashMap<String, JointFrame>>();
    HashMap<String, HashMap<String, JointFrame>> _leftHandList = new HashMap<String, HashMap<String, JointFrame>>();
    HashMap<String, HashMap<String, JointFrame>> _rightHandList = new HashMap<String, HashMap<String, JointFrame>>();
    XMLTree _tree;
    boolean _isInit = false;

    public RestPoseFactory() {
    }

    public void reloadData() {
        _poseList.clear();
        _leftHandList.clear();
        _rightHandList.clear();
        loadData();
    }

    public void loadData() {
        xmlFileRESTPOSE = getCharacterManager().getValueString("RESTPOSE_REPOSITORY");
        XMLParser xmlparser = XML.createParser();
        xmlparser.setValidating(false);
        // System.out.println("RestPoseFactory:"+xmlFileRESTPOSE);
        _tree = xmlparser.parseFile(xmlFileRESTPOSE);

        _isInit = true;
        XMLTree rootNode = _tree.getRootNode();
        List<XMLTree> list = rootNode.getChildrenElement();

        for (int i = 0; i < list.size(); i++) {
            XMLTree node = list.get(i);
            if (node.getName().equalsIgnoreCase("RestPose")) {
                String id = node.getAttribute("id");

                if (id != null) {
                    HashMap<String, JointFrame> rotations = new HashMap<String, JointFrame>();
                    List<XMLTree> listValue = node.getChildrenElement();
                    for (int j = 0; j < listValue.size(); j++) {
                        XMLTree finger = listValue.get(j);
                        String name = finger.getName().substring(2);
                        double x = finger.getAttributeNumber("x");
                        double y = finger.getAttributeNumber("y");
                        double z = finger.getAttributeNumber("z");
                        Quaternion rightrotation = new Quaternion();
                        rightrotation.fromEulerXYZByAngle((double) x, (double) y, (double) z);
                        JointFrame jf = new JointFrame();
                        jf._localrotation = rightrotation;
                        rotations.put("r_" + name, jf);

                        Quaternion leftrotation = new Quaternion();
                        leftrotation.fromEulerXYZByAngle((double) (x), (double) (-y), (double) (-z));
                        JointFrame jf2 = new JointFrame();
                        jf2._localrotation = leftrotation;
                        rotations.put("l_" + name, jf2);
                    }
                    _poseList.put(id, rotations);
                } else {
                    System.err.println("RestPose class : no such RestPose : " + id);
                }
            } else if (node.getName().equalsIgnoreCase("HandRestPose")) {
                String id = node.getAttribute("id");
                String side = node.getAttribute("side");
                if (id != null) {
                    HashMap<String, JointFrame> l_rotations = new HashMap<String, JointFrame>();
                    HashMap<String, JointFrame> r_rotations = new HashMap<String, JointFrame>();
                    if (side.equalsIgnoreCase("right")) {
                        List<XMLTree> listValue = node.getChildrenElement();
                        for (int j = 0; j < listValue.size(); j++) {
                            XMLTree finger = listValue.get(j);
                            String name = finger.getName().substring(2);
                            double x = finger.getAttributeNumber("x");
                            double y = finger.getAttributeNumber("y");
                            double z = finger.getAttributeNumber("z");
                            Quaternion rightrotation = new Quaternion();
                            rightrotation.fromEulerXYZByAngle((double) x, (double) y, (double) z);
                            JointFrame jf = new JointFrame();
                            jf._localrotation = rightrotation;
                            r_rotations.put("r_" + name, jf);
                        }
                        _rightHandList.put(id, r_rotations);

                    } else if (side.equalsIgnoreCase("left")) {
                        List<XMLTree> listValue = node.getChildrenElement();
                        for (int j = 0; j < listValue.size(); j++) {
                            XMLTree finger = listValue.get(j);
                            String name = finger.getName().substring(2);
                            double x = finger.getAttributeNumber("x");
                            double y = finger.getAttributeNumber("y");
                            double z = finger.getAttributeNumber("z");
                            Quaternion leftrotation = new Quaternion();
                            leftrotation.fromEulerXYZByAngle((double) (x), (double) (y), (double) (z));
                            JointFrame jf2 = new JointFrame();
                            jf2._localrotation = leftrotation;
                            l_rotations.put("l_" + name, jf2);
                        }
                        _leftHandList.put(id, l_rotations);

                    } else {
                        List<XMLTree> listValue = node.getChildrenElement();
                        for (int j = 0; j < listValue.size(); j++) {
                            XMLTree finger = listValue.get(j);
                            String name = finger.getName().substring(2);
                            double x = finger.getAttributeNumber("x");
                            double y = finger.getAttributeNumber("y");
                            double z = finger.getAttributeNumber("z");
                            Quaternion rightrotation = new Quaternion();
                            rightrotation.fromEulerXYZByAngle((double) x, (double) y, (double) z);
                            JointFrame jf = new JointFrame();
                            jf._localrotation = rightrotation;
                            r_rotations.put("r_" + name, jf);

                            Quaternion leftrotation = new Quaternion();
                            leftrotation.fromEulerXYZByAngle((double) (x), (double) (-y), (double) (-z));
                            JointFrame jf2 = new JointFrame();
                            jf2._localrotation = leftrotation;
                            l_rotations.put("l_" + name, jf2);
                        }
                        _leftHandList.put(id, l_rotations);
                        _rightHandList.put(id, r_rotations);
                    }
                } else {
                    System.err.println("RestPose class : no such HandRestPose : " + id);
                }

            }
        }
    }

    public HashMap<String, JointFrame> getPose(String name) {
        if (_poseList.containsKey(name)) {
            return _poseList.get(name);
        }
        return _poseList.get("middle");
    }

    public HashMap<String, JointFrame> getLeftHandPose(String name) {
        if (_leftHandList.containsKey(name)) {
            return _leftHandList.get(name);
        }
        return _leftHandList.get("middle");
    }

    public HashMap<String, JointFrame> getRightHandPose(String name) {
        if (_rightHandList.containsKey(name)) {
            return _rightHandList.get(name);
        }
        return _rightHandList.get("middle");
    }

    @Override
    public void onCharacterChanged() {
        reloadData();
    }
}
