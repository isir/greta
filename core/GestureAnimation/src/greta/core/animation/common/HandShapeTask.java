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
package greta.core.animation.common;

import greta.core.animation.common.Frame.JointFrame;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.enums.Side;
import greta.core.util.math.Quaternion;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.HashMap;
import java.util.List;

/**
 *  load all the hand shape from xml files
 * @author Jing Huang
 */
public class HandShapeTask extends CharacterDependentAdapter {

    private static String xmlFile = getCharacterManagerStatic().getValueString("HANDSHAPE_REPOSITORY");
    XMLTree _tree;
    HashMap<String, HashMap<String, JointFrame>> _leftshapeList = new HashMap<String, HashMap<String, JointFrame>>();
    HashMap<String, HashMap<String, JointFrame>> _rightshapeList = new HashMap<String, HashMap<String, JointFrame>>();

    public static void main(String[] args) {
        HandShapeTask t = new HandShapeTask();
    }

    public HandShapeTask() {
        loadData();
    }

//    HandShape getHandShape(String name) {
//        if (name.equalsIgnoreCase("empty")) {
//            return null;
//        }
//        return HandShape.valueOf(name.toLowerCase());
//    }

    public void reload(){
        xmlFile = getCharacterManager().getValueString("HANDSHAPE_REPOSITORY");
        _leftshapeList.clear();
        _rightshapeList.clear();
        loadData();
    }
    void loadData() {

        XMLParser xmlparser = XML.createParser();
        xmlparser.setValidating(false);
        _tree = xmlparser.parseFile(xmlFile);

        XMLTree rootNode = _tree.getRootNode();
        List<XMLTree> list = rootNode.getChildrenElement();

        for (int i = 0; i < list.size(); i++) {
            XMLTree node = list.get(i);
            if (node.getName().equalsIgnoreCase("HandShape")) {
                String id = node.getAttribute("id");
                String handshape = id;
                if (handshape != null) {
                    HashMap<String, JointFrame> rightrotations = new HashMap<String, JointFrame>();
                    HashMap<String, JointFrame> leftrotations = new HashMap<String, JointFrame>();
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
                        rightrotations.put("r_" + name, jf);


                        Quaternion leftrotation = new Quaternion();
                        leftrotation.fromEulerXYZByAngle((double) (x), (double) (-y), (double) (-z));
                        JointFrame jf2 = new JointFrame();
                        jf2._localrotation = leftrotation;
                        leftrotations.put("l_" + name, jf2);
                    }
                    _rightshapeList.put(handshape, rightrotations);
                    _leftshapeList.put(handshape, leftrotations);
                } else {
                    System.err.println("HandShapeTask class : no such handshape : " + id);
                }
            }

        }

    }

    public HashMap<String, JointFrame> getResult(String shape, Side side) {
        if (side == Side.LEFT) {
            if (_leftshapeList.containsKey(shape)) {
                return _leftshapeList.get(shape);
            }
        } else if (side == Side.RIGHT) {
            if (_rightshapeList.containsKey(shape)) {
                return _rightshapeList.get(shape);
            }
        }
        System.err.println("HandShapeTask: no such handshape: " + shape);
        return new HashMap<String, JointFrame>();

    }

    void addHandShape(String shape, HashMap<String, JointFrame> input, int side) {
        if (side == 0) {
            _leftshapeList.put(shape, input);
        } else if (side == 1) {
            _rightshapeList.put(shape, input);
        }

    }

    @Override
    public void onCharacterChanged() {
        reload();
    }
}
