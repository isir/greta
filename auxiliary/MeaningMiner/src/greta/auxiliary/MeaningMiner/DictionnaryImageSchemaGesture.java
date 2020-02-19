/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.MeaningMiner;

import greta.core.signals.gesture.GesturePose;
import greta.core.signals.gesture.Hand;
import greta.core.signals.gesture.UniformPosition;
import greta.core.util.enums.Side;
import greta.core.util.math.Quaternion;
import greta.core.util.xml.DefaultXMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 *
 * @author Brian
 */
public class DictionnaryImageSchemaGesture {

    private String MODELPATH = "./Common/Data/MeaningMiner/";
    private String MODELNAME = "imageschemaGesture";
    private HashMap<String, List<HashMap<String, String>>> dict;

    public DictionnaryImageSchemaGesture() {
        dict = new HashMap<>();
        DefaultXMLParser parser = new DefaultXMLParser();
        XMLTree tree = parser.parseFile(MODELPATH + MODELNAME + ".xml");
        for (XMLTree imageschema : tree.getChildrenElement()) {
            List<HashMap<String, String>> listPhases = new ArrayList<>();
            String imScName = imageschema.getAttribute("type");
            for (XMLTree phase : imageschema.getChildrenElement()) {
                HashMap<String, String> phaseParameter = new HashMap<>();

                String leftHandZ = null;
                leftHandZ = phase.getAttribute("leftHandZ");
                if (leftHandZ != "") {
                    phaseParameter.put("leftHandZ", leftHandZ);
                }

                String leftHandY = null;
                leftHandY = phase.getAttribute("leftHandY");
                if (leftHandY != "") {
                    phaseParameter.put("leftHandY", leftHandY);
                }

                String leftHandX = null;
                leftHandX = phase.getAttribute("leftHandX");
                if (leftHandX != "") {
                    phaseParameter.put("leftHandX", leftHandX);
                }

                String rightHandZ = null;
                rightHandZ = phase.getAttribute("rightHandZ");
                if (rightHandZ != "") {
                    phaseParameter.put("rightHandZ", rightHandZ);
                }

                String rightHandY = null;
                rightHandY = phase.getAttribute("rightHandY");
                if (rightHandY != "") {
                    phaseParameter.put("rightHandY", rightHandY);
                }

                String rightHandX = null;
                rightHandX = phase.getAttribute("rightHandX");
                if (rightHandX != "") {
                    phaseParameter.put("rightHandX", rightHandX);
                }

                String leftOrientation = null;
                leftOrientation = phase.getAttribute("leftOrientation");
                if (leftOrientation != "") {
                    phaseParameter.put("leftOrientation", leftOrientation);
                }

                String rightOrientation = null;
                rightOrientation = phase.getAttribute("rightOrientation");
                if (rightOrientation != "") {
                    phaseParameter.put("rightOrientation", rightOrientation);
                }

                String leftShape = null;
                leftShape = phase.getAttribute("leftShape");
                if (leftShape != "") {
                    phaseParameter.put("leftShape", leftShape);
                }

                String rightShape = null;
                rightShape = phase.getAttribute("rightShape");
                if (rightShape != "") {
                    phaseParameter.put("rightShape", rightShape);
                }

                String opennessLeft = null;
                opennessLeft = phase.getAttribute("opennessLeft");
                if (opennessLeft != "") {
                    phaseParameter.put("opennessLeft", opennessLeft);
                }

                String opennessRight = null;
                opennessRight = phase.getAttribute("opennessRight");
                if (opennessRight != "") {
                    phaseParameter.put("opennessRight", opennessRight);
                }

                String bothHand = null;
                bothHand = phase.getAttribute("bothHand");
                if (bothHand != "") {
                    phaseParameter.put("bothHand", bothHand);
                }

                listPhases.add(phaseParameter);
            }
            dict.put(imScName, listPhases);
        }

    }

    public List<GesturePose> getGesturePoseforImageSchema(Set<String> imageSchemas) {

        List<GesturePose> toReturn = new ArrayList<>();
        boolean hasTwoPhases = false;
        boolean isBothHand = false;
        for (String s : imageSchemas) {
            List<HashMap<String, String>> phases = dict.get(s);
            if (phases != null) {
                if (phases.size() > 1) {
                    hasTwoPhases = true;
                }
                if (phases.get(0).get("bothHand") != null && Boolean.parseBoolean(phases.get(0).get("bothHand"))) {
                    isBothHand = Boolean.parseBoolean(phases.get(0).get("bothHand"));
                }
            }
        }
        Random r = new Random();

        UniformPosition hlsUniPos = new UniformPosition(0.30, 0, 0.7);
        UniformPosition hrsUniPos = new UniformPosition(0.30, 0, 0.7);

        Hand handleftStart = new Hand(Side.LEFT, "bend_curved", hlsUniPos, new Quaternion(), null);
        Hand handrightStart = new Hand(Side.RIGHT, "bend_curved", hrsUniPos, new Quaternion(), null);

        UniformPosition hleUniPos = new UniformPosition(0.30, 0, 0.7);
        UniformPosition hreUniPos = new UniformPosition(0.30, 0, 0.7);

        Hand handleftEnd = new Hand(Side.LEFT, "bend_curved", hleUniPos, new Quaternion(), null);
        Hand handrightEnd = new Hand(Side.RIGHT, "bend_curved", hreUniPos, new Quaternion(), null);

        GesturePose gp;
        GesturePose gp2;

        hlsUniPos.setXOverridable(true);
        hrsUniPos.setXOverridable(true);
        hlsUniPos.setYOverridable(true);
        hrsUniPos.setYOverridable(true);
        hlsUniPos.setZOverridable(true);
        hrsUniPos.setZOverridable(true);

        hleUniPos.setXOverridable(true);
        hreUniPos.setXOverridable(true);
        hleUniPos.setYOverridable(true);
        hreUniPos.setYOverridable(true);
        hleUniPos.setZOverridable(true);
        hreUniPos.setZOverridable(true);

        handleftStart.setHandShapeOverridable(true);
        handrightStart.setHandShapeOverridable(true);
        handleftStart.setWristOrientationOverridable(true);
        handrightStart.setWristOrientationOverridable(true);

        handleftEnd.setHandShapeOverridable(true);
        handrightEnd.setHandShapeOverridable(true);
        handleftEnd.setWristOrientationOverridable(true);
        handrightEnd.setWristOrientationOverridable(true);

        //if (isBothHand) {

            gp = new GesturePose(handleftStart, handrightStart);
            gp2 = new GesturePose(handleftEnd, handrightEnd);

        /*} else {
            if (r.nextBoolean()) {
                gp = new GesturePose(null, handrightStart);
                gp2 = new GesturePose(null, handrightEnd);
            } else {
                gp = new GesturePose(handleftStart, null);
                gp2 = new GesturePose(handleftEnd, null);
            }
        }
*/

        toReturn.add(gp);
        if (hasTwoPhases) {

            toReturn.add(gp2);
        }

        for (String s : imageSchemas) {
            List<HashMap<String, String>> phases = dict.get(s);
            if (phases != null) {
                String leftHandZ = null;
                leftHandZ = phases.get(0).get("leftHandZ");
                if (leftHandZ != null) {
                    hlsUniPos.setZ(Double.parseDouble(leftHandZ));
                    hlsUniPos.setZOverridable(false);
                }

                String leftHandY = null;
                leftHandY = phases.get(0).get("leftHandY");
                if (leftHandY != null) {
                    hlsUniPos.setY(Double.parseDouble(leftHandY));
                    hlsUniPos.setYOverridable(false);
                }

                String leftHandX = null;
                leftHandX = phases.get(0).get("leftHandX");
                if (leftHandX != null) {
                    hlsUniPos.setX(Double.parseDouble(leftHandX));
                    hlsUniPos.setXOverridable(false);
                }

                String rightHandZ = null;
                rightHandZ = phases.get(0).get("rightHandZ");
                if (rightHandZ != null) {
                    hrsUniPos.setZ(Double.parseDouble(rightHandZ));
                    hrsUniPos.setZOverridable(false);
                }

                String rightHandY = null;
                rightHandY = phases.get(0).get("rightHandY");
                if (rightHandY != null) {
                    hrsUniPos.setY(Double.parseDouble(rightHandY));
                    hrsUniPos.setYOverridable(false);
                }

                String rightHandX = null;
                rightHandX = phases.get(0).get("rightHandX");
                if (rightHandX != null) {
                    hrsUniPos.setX(Double.parseDouble(rightHandX));
                    hrsUniPos.setXOverridable(false);
                }

                String leftOrientation = null;
                leftOrientation = phases.get(0).get("leftOrientation");
                if (leftOrientation != null) {
                    String[] leftQuat = leftOrientation.split(",");
                    Quaternion q = new Quaternion();
                    q.fromEulerXYZ(Double.parseDouble(leftQuat[0]), Double.parseDouble(leftQuat[1]), Double.parseDouble(leftQuat[2]));
                    handleftStart.setWristOrientation(q);
                    handleftStart.setWristOrientationOverridable(false);
                    //handleftStart.setWristOrientation(new Quaternion(Double.parseDouble(leftQuat[0]), Double.parseDouble(leftQuat[1]), Double.parseDouble(leftQuat[2]), Double.parseDouble(leftQuat[3])));
                }

                String rightOrientation = null;
                rightOrientation = phases.get(0).get("rightOrientation");
                if (rightOrientation != null) {
                    String[] rightQuat = rightOrientation.split(",");
                    Quaternion q = new Quaternion();
                    q.fromEulerXYZ(Double.parseDouble(rightQuat[0]), Double.parseDouble(rightQuat[1]), Double.parseDouble(rightQuat[2]));
                    handrightStart.setWristOrientation(q);
                    handrightStart.setWristOrientationOverridable(false);
//handrightStart.setWristOrientation(new Quaternion(Double.parseDouble(rightQuat[0]), Double.parseDouble(rightQuat[1]), Double.parseDouble(rightQuat[2]), Double.parseDouble(rightQuat[3])));
                }

                String leftShape = null;
                leftShape = phases.get(0).get("leftShape");
                if (leftShape != null) {
                    handleftStart.setHandShape(leftShape);
                    handleftStart.setHandShapeOverridable(false);
                }

                String rightShape = null;
                rightShape = phases.get(0).get("rightShape");
                if (rightShape != null) {
                    handrightStart.setHandShape(rightShape);
                    handrightStart.setHandShapeOverridable(false);
                }

                String opennessLeft = null;
                opennessLeft = phases.get(0).get("opennessLeft");
                if (opennessLeft != null) {
                    handleftStart.setOpenness(Double.parseDouble(opennessLeft));
                }

                String opennessRight = null;
                opennessRight = phases.get(0).get("opennessRight");
                if (opennessRight != null) {
                    handrightStart.setOpenness(Double.parseDouble(opennessRight));
                }

                if (hasTwoPhases) {
                    HashMap<String, String> tempPose = null;
                    if (phases.size() > 1) {
                        tempPose = phases.get(1);
                    } else {
                        tempPose = phases.get(0);
                    }

                    String leftHandZ2 = null;
                    leftHandZ2 = tempPose.get("leftHandZ");
                    if (leftHandZ2 != null) {
                        hleUniPos.setZ(Double.parseDouble(leftHandZ2));
                        hleUniPos.setZOverridable(false);
                    }

                    String leftHandY2 = null;
                    leftHandY2 = tempPose.get("leftHandY");
                    if (leftHandY2 != null) {
                        hleUniPos.setY(Double.parseDouble(leftHandY2));
                        hleUniPos.setYOverridable(false);
                    }

                    String leftHandX2 = null;
                    leftHandX2 = tempPose.get("leftHandX");
                    if (leftHandX2 != null) {
                        hleUniPos.setX(Double.parseDouble(leftHandX2));
                        hleUniPos.setXOverridable(false);
                    }

                    String rightHandZ2 = null;
                    rightHandZ2 = tempPose.get("rightHandZ");
                    if (rightHandZ2 != null) {
                        hreUniPos.setZ(Double.parseDouble(rightHandZ2));
                        hreUniPos.setZOverridable(false);
                    }

                    String rightHandY2 = null;
                    rightHandY2 = tempPose.get("rightHandY");
                    if (rightHandY2 != null) {
                        hreUniPos.setY(Double.parseDouble(rightHandY2));
                        hreUniPos.setYOverridable(false);
                    }

                    String rightHandX2 = null;
                    rightHandX2 = tempPose.get("rightHandX");
                    if (rightHandX2 != null) {
                        hreUniPos.setX(Double.parseDouble(rightHandX2));
                        hreUniPos.setXOverridable(false);
                    }

                    String leftOrientation2 = null;
                    leftOrientation2 = tempPose.get("leftOrientation");
                    if (leftOrientation2 != null) {
                        String[] leftQuat2 = leftOrientation2.split(",");
                        Quaternion q = new Quaternion();
                        q.fromEulerXYZ(Double.parseDouble(leftQuat2[0]), Double.parseDouble(leftQuat2[1]), Double.parseDouble(leftQuat2[2]));
                        handleftEnd.setWristOrientation(q);
                        handleftEnd.setWristOrientationOverridable(false);
                        //handleftEnd.setWristOrientation(new Quaternion(Double.parseDouble(leftQuat2[0]), Double.parseDouble(leftQuat2[1]), Double.parseDouble(leftQuat2[2]), Double.parseDouble(leftQuat2[3])));
                    }

                    String rightOrientation2 = null;
                    rightOrientation2 = tempPose.get("rightOrientation");
                    if (rightOrientation2 != null) {
                        String[] rightQuat2 = rightOrientation2.split(",");
                        Quaternion q = new Quaternion();
                        q.fromEulerXYZ(Double.parseDouble(rightQuat2[0]), Double.parseDouble(rightQuat2[1]), Double.parseDouble(rightQuat2[2]));
                        handrightEnd.setWristOrientation(q);
                        handrightEnd.setWristOrientationOverridable(false);
                        //handrightEnd.setWristOrientation(new Quaternion(Double.parseDouble(rightQuat2[0]), Double.parseDouble(rightQuat2[1]), Double.parseDouble(rightQuat2[2]), Double.parseDouble(rightQuat2[3])));
                    }

                    String leftShape2 = null;
                    leftShape2 = tempPose.get("leftShape");
                    if (leftShape2 != null) {
                        handleftEnd.setHandShape(leftShape2);
                        handleftEnd.setHandShapeOverridable(false);
                    }

                    String rightShape2 = null;
                    rightShape2 = tempPose.get("rightShape");
                    if (rightShape2 != null) {
                        handrightEnd.setHandShape(rightShape2);
                        handrightEnd.setHandShapeOverridable(false);
                    }

                    String opennessLeft2 = null;
                    opennessLeft2 = tempPose.get("opennessLeft");
                    if (opennessLeft2 != null) {
                        handleftEnd.setOpenness(Double.parseDouble(opennessLeft2));
                    }

                    String opennessRight2 = null;
                    opennessRight2 = tempPose.get("opennessRight");
                    if (opennessRight2 != null) {
                        handrightEnd.setOpenness(Double.parseDouble(opennessRight2));
                    }

                }
            }

        }

        return toReturn;
    }

}
