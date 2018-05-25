/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.repositories;

import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.util.List;

/**
 *
 * @author Jing Huang
 */
public class HeadIntervals implements CharacterDependent {

    private XMLTree _tree;

    String headIntervalsFile = CharacterManager.getValueString("HEAD_INTERVALS");

    public double verticalLeftMin, verticalLeftMax;
    public double verticalRightMin, verticalRightMax;
    public double sagittalDownMin, sagittalDownMax;
    public double sagittalUpMin, sagittalUpMax;
    public double lateralRightMin, lateralRightMax;
    public double lateralLeftMin, lateralLeftMax;

    /**
     * Default constructor.
     */
    public HeadIntervals() {
        loadHeadIntervals();
    }

    /**
     * Reloads the intervals
     */
    public void reloadData() {
        loadHeadIntervals();
    }

    @Override
    public void onCharacterChanged() {
        headIntervalsFile = CharacterManager.getValueString("HEAD_INTERVALS");
        loadHeadIntervals();
    }

    /**
     * loads the intervals stored in an XML file
     */
    private void loadHeadIntervals() {

        XMLParser xmlparser = XML.createParser();
        xmlparser.setValidating(false);
        _tree = xmlparser.parseFile(headIntervalsFile);

        XMLTree rootNodeBase = _tree.getRootNode();
        List<XMLTree> listNode = rootNodeBase.getChildrenElement();
        for (int inod = 0; inod < listNode.size(); inod++) {

            XMLTree nodeBase = listNode.get(inod);
            if (nodeBase.getName().equalsIgnoreCase("VerticalTorsion")) {

                List<XMLTree> nodeAmounts = nodeBase.getChildrenElement();
                for (int iAmount = 0; iAmount < nodeAmounts.size(); iAmount++) {
                    XMLTree nodeamount = nodeAmounts.get(iAmount);
                    if (nodeamount.getName().equalsIgnoreCase("vertical_amount")) {
                        String dir = nodeamount.getAttribute("direction");
                        List<XMLTree> list = nodeamount.getChildrenElement();
                        for (int i = 0; i < list.size(); i++) {
                            XMLTree node = list.get(i);
                            if (node.getName().equalsIgnoreCase("min")) {
                                if (dir.equalsIgnoreCase("Leftward")) {
                                    verticalLeftMin = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                } else {
                                    verticalRightMin = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                }

                            } else if (node.getName().equalsIgnoreCase("max")) {
                                if (dir.equalsIgnoreCase("Leftward")) {
                                    verticalLeftMax = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                } else {
                                    verticalRightMax = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                }
                            }
                        }
                    }

                }
            } else if (nodeBase.getName().equalsIgnoreCase("SagittalTilt")) {

                List<XMLTree> nodeAmounts = nodeBase.getChildrenElement();
                for (int iAmount = 0; iAmount < nodeAmounts.size(); iAmount++) {
                    XMLTree nodeamount = nodeAmounts.get(iAmount);
                    if (nodeamount.getName().equalsIgnoreCase("sagittal_amount")) {
                        String dir = nodeamount.getAttribute("direction");
                        List<XMLTree> list = nodeamount.getChildrenElement();
                        for (int i = 0; i < list.size(); i++) {
                            XMLTree node = list.get(i);
                            if (node.getName().equalsIgnoreCase("min")) {
                                if (dir.equalsIgnoreCase("Backward")) {
                                    sagittalUpMin = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                } else {
                                    sagittalDownMin = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                }

                            } else if (node.getName().equalsIgnoreCase("max")) {
                                if (dir.equalsIgnoreCase("Backward")) {
                                    sagittalUpMax = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                } else {
                                    sagittalDownMax = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                }
                            }
                        }

                    }

                }
            } else if (nodeBase.getName().equalsIgnoreCase("LateralRoll")) {

                List<XMLTree> nodeAmounts = nodeBase.getChildrenElement();
                for (int iAmount = 0; iAmount < nodeAmounts.size(); iAmount++) {
                    XMLTree nodeamount = nodeAmounts.get(iAmount);
                    if (nodeamount.getName().equalsIgnoreCase("lateral_amount")) {
                        String dir = nodeamount.getAttribute("direction");
                        List<XMLTree> list = nodeamount.getChildrenElement();
                        for (int i = 0; i < list.size(); i++) {
                            XMLTree node = list.get(i);
                            if (node.getName().equalsIgnoreCase("min")) {
                                if (dir.equalsIgnoreCase("Leftward")) {
                                    lateralLeftMin = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                } else {
                                    lateralRightMin = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                }

                            } else if (node.getName().equalsIgnoreCase("max")) {
                                if (dir.equalsIgnoreCase("Leftward")) {
                                    lateralLeftMax = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                } else {
                                    lateralRightMax = Double.parseDouble(node.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void saveData(String id, double x, double y, double z) {

        XMLTree rootNode = _tree.getRootNode();
        List<XMLTree> list = rootNode.getChildrenElement();

        for (int i = 0; i < list.size(); i++) {
            XMLTree node = list.get(i);
            if (node.getName().equalsIgnoreCase("HeadIntervals")) {
                XMLTree newnode = node.createChild(id);
                XMLTree X = newnode.createChild("X");
                XMLTree Y = newnode.createChild("Y");
                XMLTree Z = newnode.createChild("Z");
                X.addText(Double.toString(x));
                Y.addText(Double.toString(y));
                Z.addText(Double.toString(z));
            }
        }
        rootNode.save(headIntervalsFile);
    }
}
