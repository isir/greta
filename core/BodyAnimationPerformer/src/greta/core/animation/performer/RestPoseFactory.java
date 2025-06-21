/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.core.animation.performer;

import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
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
public class RestPoseFactory extends CharacterDependentAdapter{

    private String xmlFileRESTPOSE = getCharacterManager().getValueString("RESTPOSE_REPOSITORY");

    public String getXmlFileRESTPOSE() {
        return xmlFileRESTPOSE;
    }

    public void setXmlFileRESTPOSE(String _xmlFileRESTPOSE) {
        xmlFileRESTPOSE = _xmlFileRESTPOSE;
    }
    HashMap<String, HashMap<String, Quaternion>> _poseList = new HashMap<String, HashMap<String, Quaternion>>();
    HashMap<String, HashMap<String, Quaternion>> _leftHandList = new HashMap<String, HashMap<String, Quaternion>>();
    HashMap<String, String> _leftHandShapesList = new HashMap<String, String>();
    HashMap<String, HashMap<String, Quaternion>> _rightHandList = new HashMap<String, HashMap<String, Quaternion>>();
    HashMap<String, String> _rightHandShapesList = new HashMap<String, String>();
    XMLTree _tree;
    boolean _isInit = false;

    public RestPoseFactory(CharacterManager cm) {
        setCharacterManager(cm);
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
                    HashMap<String, Quaternion> rotations = new HashMap<String, Quaternion>();
                    List<XMLTree> listValue = node.getChildrenElement();
                    for (int j = 0; j < listValue.size(); j++) {
                        XMLTree finger = listValue.get(j);
                        String name = finger.getName().substring(2);
                        double x = finger.getAttributeNumber("x");
                        double y = finger.getAttributeNumber("y");
                        double z = finger.getAttributeNumber("z");
                        Quaternion rightrotation = new Quaternion();
                        rightrotation.fromEulerXYZByAngle((double) x, (double) y, (double) z);
                        rotations.put("r_" + name, rightrotation);

                        Quaternion leftrotation = new Quaternion();
                        leftrotation.fromEulerXYZByAngle((double) (x), (double) (-y), (double) (-z));
                        rotations.put("l_" + name, leftrotation);
                    }
                    _poseList.put(id, rotations);
                } else {
                    System.err.println("RestPose class : no such RestPose : " + id);
                }
            } else if (node.getName().equalsIgnoreCase("HandRestPose")) {
                String id = node.getAttribute("id");
                String side = node.getAttribute("side");
                String handShape = node.getAttribute("handShape");
                if (id != null) {
                    HashMap<String, Quaternion> l_rotations = new HashMap<String, Quaternion>();
                    HashMap<String, Quaternion> r_rotations = new HashMap<String, Quaternion>();
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
                            r_rotations.put("r_" + name, rightrotation);
                        }
                        _rightHandList.put(id, r_rotations);
                        _rightHandShapesList.put(id, handShape);

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
                            l_rotations.put("l_" + name, leftrotation);
                        }
                        _leftHandList.put(id, l_rotations);
                        _leftHandShapesList.put(id, handShape);

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
                            r_rotations.put("r_" + name, rightrotation);

                            Quaternion leftrotation = new Quaternion();
                            leftrotation.fromEulerXYZByAngle((double) (x), (double) (-y), (double) (-z));
                            l_rotations.put("l_" + name, leftrotation);
                        }
                        _leftHandList.put(id, l_rotations);
                        _rightHandList.put(id, r_rotations);
                        _rightHandShapesList.put(id, handShape);
                        _leftHandShapesList.put(id, handShape);
                    }
                } else {
                    System.err.println("RestPose class : no such HandRestPose : " + id);
                }

            }
        }
    }

    public HashMap<String, Quaternion> getPose(String name) {
        if (_poseList.containsKey(name)) {
            return _poseList.get(name);
        }
        return _poseList.get("middle");
    }

    public String getLeftHandShape(String name) {
        if (_leftHandShapesList.containsKey(name)) {
            return _leftHandShapesList.get(name);
        }
        return _leftHandShapesList.get("middle");
    }

    public String getRightHandShape(String name) {
        if (_rightHandShapesList.containsKey(name)) {
            return _rightHandShapesList.get(name);
        }
        return _rightHandShapesList.get("middle");
    }

    public HashMap<String, Quaternion> getLeftHandPose(String name) {
        if (_leftHandList.containsKey(name)) {
            return _leftHandList.get(name);
        }
        return _leftHandList.get("middle");
    }

    public HashMap<String, Quaternion> getRightHandPose(String name) {
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
