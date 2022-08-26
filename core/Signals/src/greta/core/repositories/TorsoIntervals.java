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

import greta.core.util.CharacterDependent;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.log.Logs;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.List;

/**
 *
 * @author Jing Huang
 */
public class TorsoIntervals extends CharacterDependentAdapter implements CharacterDependent {

    private XMLTree _tree;

    //private static final String xsdFile = IniManager.getGlobals().getValueString("TORSO_INTERVALS");
    private String xmlFile = getCharacterManagerStatic().getValueString("TORSO_INTERVALS");
    private XMLParser xmlparser;

    public double verticalR = 0;
    public double verticalL = 0;
    public double sagittalF = 0;
    public double sagittalB = 0;
    public double lateralL = 0;
    public double lateralR = 0;
    //public double collapseMin = 0;
    public double collapseMax = 0;

    public TorsoIntervals() {
        xmlparser = XML.createParser();
        xmlparser.setValidating(false);
        _tree = xmlparser.parseFile(xmlFile);
        loadTorsoIntervals();
    }

    @Override
    public void onCharacterChanged() {
        xmlFile = getCharacterManager().getValueString("TORSO_INTERVALS");
        _tree = xmlparser.parseFile(xmlFile);
        loadTorsoIntervals();
    }

    public void reloadData() {
        loadTorsoIntervals();
    }

    public void loadTorsoIntervals() {

        XMLTree rootNode = _tree.getRootNode();
        List<XMLTree> listValue = rootNode.getChildrenElement();

        for (int j = 0; j < listValue.size(); j++) {
            XMLTree value = listValue.get(j);
            if (value.getName().equalsIgnoreCase("VerticalTorsion")) {
                List<XMLTree> ele = value.getChildrenElement();
                for (int h = 0; h < ele.size(); h++) {
                    XMLTree amount = ele.get(h);
                    if (amount.getName().equalsIgnoreCase("vertical_amount")) {
                        XMLTree max = amount.findNodeCalled("max");
                        if (amount.getAttribute("direction").equalsIgnoreCase("Rightward")) {
                            verticalR = Double.parseDouble(max.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        } else {
                            verticalL = Double.parseDouble(max.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }
                    }
                }
            } else if (value.getName().equalsIgnoreCase("SagittalTilt")) {
                List<XMLTree> ele = value.getChildrenElement();
                for (int h = 0; h < ele.size(); h++) {
                    XMLTree amount = ele.get(h);
                    if (amount.getName().equalsIgnoreCase("sagittal_amount")) {
                        XMLTree max = amount.findNodeCalled("max");
                        if (amount.getAttribute("direction").equalsIgnoreCase("Forward")) {
                            sagittalF = Double.parseDouble(max.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        } else {
                            sagittalB = Double.parseDouble(max.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }
                    }
                }
            } else if (value.getName().equalsIgnoreCase("LateralRoll")) {
                //XMLTree min = amount.findNodeCalled("min");
                List<XMLTree> ele = value.getChildrenElement();
                for (int h = 0; h < ele.size(); h++) {
                    XMLTree amount = ele.get(h);
                    if (amount.getName().equalsIgnoreCase("lateral_amount")) {
                        XMLTree max = amount.findNodeCalled("max");
                        if (amount.getAttribute("direction").equalsIgnoreCase("Rightward")) {
                            lateralR = Double.parseDouble(max.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        } else {
                            lateralL = Double.parseDouble(max.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                        }
                    }
                }
            } else if (value.getName().equalsIgnoreCase("Collapse")) {
                XMLTree amount = value.findNodeCalled("collapse_amount");
                //XMLTree min = amount.findNodeCalled("min");
                XMLTree max = amount.findNodeCalled("max");
                //collapseMin = Double.parseDouble(min.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                collapseMax = Double.parseDouble(max.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
            } else {
                Logs.error("TorsoIntervals class: Torso Intervals error");
            }
        }
    }

    public void saveData(String id, double x, double y, double z) {

        XMLTree rootNode = _tree.getRootNode();
        List<XMLTree> list = rootNode.getChildrenElement();

        for (int i = 0; i < list.size(); i++) {
            XMLTree node = list.get(i);
            if (node.getName().equalsIgnoreCase("TorsoIntervals")) {
                XMLTree newnode = node.createChild(id);
                XMLTree X = newnode.createChild("X");
                XMLTree Y = newnode.createChild("Y");
                XMLTree Z = newnode.createChild("Z");
                X.addText(Double.toString(x));
                Y.addText(Double.toString(y));
                Z.addText(Double.toString(z));
            }
        }
        rootNode.save(xmlFile);
    }
}
