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
package greta.core.animation.common.symbolic;

import greta.core.animation.common.Frame.JointFrame;
import greta.core.animation.common.HandShapeTask;
import greta.core.animation.common.IKSkeletonParser;
import greta.core.animation.common.Joint;
import greta.core.animation.common.Skeleton;
import greta.core.animation.common.Target;
import greta.core.animation.common.body.Arm;
import greta.core.animation.common.body.Head;
import greta.core.animation.common.body.RestPoseFactory;
import greta.core.animation.common.body.Shoulder;
import greta.core.animation.common.body.Torse;
import greta.core.keyframes.GestureKeyframe;
import greta.core.keyframes.HeadKeyframe;
import greta.core.keyframes.ShoulderKeyframe;
import greta.core.keyframes.TorsoKeyframe;
import greta.core.repositories.HeadIntervals;
import greta.core.repositories.HeadLibrary;
import greta.core.repositories.TorsoIntervals;
import greta.core.repositories.TorsoLibrary;
import greta.core.signals.SpineDirection;
import greta.core.signals.gesture.SymbolicPosition;
import greta.core.signals.gesture.TouchPosition;
import greta.core.signals.gesture.TrajectoryDescription;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterDependentAdapter;
import greta.core.util.CharacterManager;
import greta.core.util.enums.Side;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.math.easefunctions.EaseInOutSine;
import greta.core.util.math.easefunctions.EaseOutBack;
import greta.core.util.math.easefunctions.EaseOutBounce;
import greta.core.util.math.easefunctions.EaseOutQuad;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.HashMap;
import java.util.List;

/**
 * converter all the value from file into each body parts
 *
 * @author Jing Huang
 */
/*
 * enum SymbolicPositionType {
 *
 * XEP, XP, XC, XCC, XOppC, YUpperEP, YUpperP, YUpperC, YCC, YLowerC, YLowerP,
 * YLowerEP, ZNear, ZMiddle, ZFar; }
 */
public class SymbolicConverter extends CharacterDependentAdapter implements CharacterDependent {

    Skeleton _skeleton = new Skeleton("agent");
    Skeleton _skeleton_original = new Skeleton("agent");
    HashMap<String, Skeleton> _chains = new HashMap<String, Skeleton>();
    private String xmlFile;
    private String touchFile;
    XMLTree _tree;
    RestPoseFactory _restPoseFactory = new RestPoseFactory();
    HeadIntervals _headIntervals = HeadLibrary.getGlobalLibrary().getHeadIntervals();
    TorsoIntervals _torsoIntervals = TorsoLibrary.getGlobalLibrary().getTorsoIntervals();
    HandShapeTask handshape = new HandShapeTask();
    HashMap<String, Double> _positionValuesX = new HashMap<String, Double>();
    HashMap<String, Double> _positionValuesY = new HashMap<String, Double>();
    HashMap<String, Double> _positionValuesZ = new HashMap<String, Double>();
    HashMap<String, String> _touchPosition = new HashMap<String, String>();
    HashMap<String, Vec3d> _touchPosOffset = new HashMap<String, Vec3d>();
    HashMap<String, Vec3d> _touchRotOffset = new HashMap<String, Vec3d>();
    double _scaleFactorX = 1f;
    double _scaleFactorY = 1f;
    double _scaleFactorZ = 1f;

    public SymbolicConverter(CharacterManager cm) {
        setCharacterManager(cm);
        reloadData();
    }

    @Override
    public void onCharacterChanged() {
        reloadData();
    }

    public void reloadData() {
        _positionValuesX.clear();
        _positionValuesY.clear();
        _positionValuesZ.clear();
        setSkeleton(xmlFile);
        _restPoseFactory.reloadData();
        handshape.reload();
        _torsoIntervals.reloadData();
        _headIntervals.reloadData();
        loadData();
    }

    private void setSkeleton(String fileName) {
        IKSkeletonParser p = new IKSkeletonParser();
        boolean re = p.loadFile(fileName);
        if (re) {
            p.readSkeletonInfo();
            p.buildFullSkeleton(_skeleton_original);
            p.buildFullSkeleton(_skeleton);
            p.buildSkeletonChains(_skeleton, _chains);
        } else {
            System.out.println("HumanAgent class  : skeleton file not loaded");

        }
    }

    public Skeleton getSkeleton() {
        return _skeleton;
    }

    public Skeleton getOriginalSkeleton() {
        return _skeleton_original;
    }

    public Skeleton getSkeletonChain(String name) {
        return _chains.get(name);
    }

    void loadData() {
        xmlFile = getCharacterManager().getValueString("IK_SKELETON");
        touchFile = getCharacterManager().getValueString("TOUCHPOINT");
        XMLParser xmlparser = XML.createParser();
        xmlparser.setValidating(false);
        _tree = xmlparser.parseFile(xmlFile);

        XMLTree rootNodeBase = _tree.getRootNode();
        List<XMLTree> listNode = rootNodeBase.getChildrenElement();
        for (int inod = 0; inod < listNode.size(); inod++) {

            XMLTree nodeBase = listNode.get(inod);
            if (nodeBase.getName().equalsIgnoreCase("symblicposition")) {
                List<XMLTree> list = nodeBase.getChildrenElement();
                for (int i = 0; i < list.size(); i++) {
                    XMLTree node = list.get(i);
                    if (node.getName().equalsIgnoreCase("x")) {
                        List<XMLTree> listValue = node.getChildrenElement();
                        for (int j = 0; j < listValue.size(); j++) {
                            XMLTree value = listValue.get(j);
                            String name = value.getName();
                            double v = Double.parseDouble(value.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                            _positionValuesX.put(name, (double) v);
                        }
                    } else if (node.getName().equalsIgnoreCase("y")) {
                        List<XMLTree> listValue = node.getChildrenElement();
                        for (int j = 0; j < listValue.size(); j++) {
                            XMLTree value = listValue.get(j);
                            String name = value.getName();
                            double v = Double.parseDouble(value.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                            _positionValuesY.put(name, (double) v);
                        }
                    } else if (node.getName().equalsIgnoreCase("z")) {
                        List<XMLTree> listValue = node.getChildrenElement();
                        for (int j = 0; j < listValue.size(); j++) {
                            XMLTree value = listValue.get(j);
                            String name = value.getName();
                            double v = Double.parseDouble(value.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                            _positionValuesZ.put(name, (double) v);
                        }
                    }
                }
            }
        }
        try{
        _tree = xmlparser.parseFile(touchFile);
        rootNodeBase = _tree.getRootNode();
        listNode = rootNodeBase.getChildrenElement();
        for (int inod = 0; inod < listNode.size(); inod++) {
            XMLTree node = listNode.get(inod);
            if (node.getName().equalsIgnoreCase("touchpoint")) {
                _touchPosition.put(node.getAttribute("id"), node.getAttribute("reference"));
                List<XMLTree> offsetNodes = node.getChildrenElement();
                for(int ioffs = 0; ioffs < offsetNodes.size();ioffs++)
                {
                    XMLTree off = offsetNodes.get(ioffs);
                    if (off.getName().equalsIgnoreCase("posOffset")) {
                        _touchPosOffset.put(node.getAttribute("id"), new Vec3d(Double.valueOf(off.getAttribute("x")),Double.valueOf(off.getAttribute("y")),Double.valueOf(off.getAttribute("z"))));
                    }
                    if (off.getName().equalsIgnoreCase("rotOffset")) {
                        _touchRotOffset.put(node.getAttribute("id"), new Vec3d(Double.valueOf(off.getAttribute("x")),Double.valueOf(off.getAttribute("y")),Double.valueOf(off.getAttribute("z"))));
                    }
                }
            }
        }
        }catch(Exception e)
        {
            System.err.println("Problem while opening TouchPoint description file");
        }


    }

    public RestPoseFactory getRestPoseFactory() {
        return _restPoseFactory;
    }

    public void setRestPoseFactory(RestPoseFactory _restPoseFactory) {
        this._restPoseFactory = _restPoseFactory;
    }

    public void setXPositionValue(String type, double value) {
        _positionValuesX.put(type, new Double(value));
    }

    public void setYPositionValue(String type, double value) {
        _positionValuesY.put(type, new Double(value));
    }

    public void setZPositionValue(String type, double value) {
        _positionValuesZ.put(type, new Double(value));
    }

    public boolean hasThePositionValue(String name) {
        if (_positionValuesX.containsKey(name)) {
            return true;
        } else if (_positionValuesY.containsKey(name)) {
            return true;
        } else if (_positionValuesZ.containsKey(name)) {
            return true;
        } else {
            System.out.println("SymbolicConverter class : no such type value : " + name.toString());
            return false;
        }
    }

    public double getValue(String name) {
        if (_positionValuesX.containsKey(name)) {
            return _positionValuesX.get(name) * _scaleFactorX;
        } else if (_positionValuesY.containsKey(name)) {
            return _positionValuesY.get(name) * _scaleFactorY;
        } else if (_positionValuesZ.containsKey(name)) {
            return _positionValuesZ.get(name) * _scaleFactorZ;
        } else {
            System.out.println("SymbolicConverter class : no such type value : " + name.toString());
            return 0;
        }
    }

    public double getSymbolicValue(String name) {
        if (_positionValuesX.containsKey(name)) {
            return _positionValuesX.get(name);
        } else if (_positionValuesY.containsKey(name)) {
            return _positionValuesY.get(name);
        } else if (_positionValuesZ.containsKey(name)) {
            return _positionValuesZ.get(name);
        } else {
            System.out.println("SymbolicConverter class : no such type value : " + name.toString());
            return 0;
        }
    }

    public double getSpaceVariationPosition(String name, double spacialparameter) {
        //System.out.println(spacialparameter);
        return getValue(name) * getSpaceVarianceParameter(name, spacialparameter);
    }

    public double getSpaceVarianceParameter(String type, double spacialparameter) {

        double fx, fy, fz;
        if (spacialparameter >= 0) {
            fx = 1.3f;
            fy = 0.60f;
            fz = 0.25f;
        } else {
            fx = 0.7f;
            fy = 0.25f;
            fz = 0.25f;
        }
        if (_positionValuesX.containsKey(type)) {
            return fx * spacialparameter + 1;
        } else if (_positionValuesY.containsKey(type)) {
            return fy * spacialparameter + 1;
        } else if (_positionValuesZ.containsKey(type)) {
            return fz * spacialparameter + 1;
        } else {
            System.out.println("symbolicConverter class: no SpaceVarianceParameter.");
            return 1;
        }
    }

    /*
    public Vec3d getDirection(Direction d, String side) {
        if (d == Direction.OUTWARD) {
            if (side.equalsIgnoreCase("LEFT")) {
                return new Vec3d(1, 0.0f, 0).normalized();
            } else {
                return new Vec3d(-1, 0.0f, 0).normalized();
            }
        } else if (d == Direction.INWARD) {
            if (side.equalsIgnoreCase("LEFT")) {
                return new Vec3d(-1, 0.0f, 0).normalized();
            } else {
                return new Vec3d(1, 0.0f, 0).normalized();
            }
        } else if (d == Direction.AWAY) {
            return new Vec3d(0, 0, 1);
        } else if (d == Direction.TOWARD) {
            return new Vec3d(0, 0, -1);
        } else if (d == Direction.DOWN) {
            return new Vec3d(0, -1, 0);
        } else if (d == Direction.UP) {
            return new Vec3d(0, 1, 0);
        } else {
            if (d == null) {
                System.out.println("symbolicConverter class: no direction defined : null" + side);
            } else {
                System.out.println("symbolicConverter class: no direction defined : " + d.toString() + side);
            }
            return new Vec3d(0, 1, 0);
        }
    }
    */

    public HashMap<String, JointFrame> getHandShape(String shape, Side side) {
        //System.out.println(shape);
        return handshape.getResult(shape, side);
    }

    public HeadIntervals getHeadIntervals() {
        return _headIntervals;
    }

    public HashMap<String, Double> getPositionValuesX() {
        return _positionValuesX;
    }

    public HashMap<String, Double> getPositionValuesY() {
        return _positionValuesY;
    }

    public HashMap<String, Double> getPositionValuesZ() {
        return _positionValuesZ;
    }

    public TorsoIntervals getTorsoIntervals() {
        return _torsoIntervals;
    }

    public Double getPositionValueX(String id) {
        if (_positionValuesX.containsKey(id)) {
            return _positionValuesX.get(id);
        }
        return null;
    }

    public Double getPositionValueY(String id) {
        if (_positionValuesY.containsKey(id)) {
            return _positionValuesY.get(id);
        }
        return null;
    }

    public Double getPositionValueZ(String id) {
        if (_positionValuesZ.containsKey(id)) {
            return _positionValuesZ.get(id);
        }
        return null;
    }

    public Shoulder getShoulder(ShoulderKeyframe keyframe) {
        Shoulder shoulder = new Shoulder((double)keyframe.getOffset());
        String side = keyframe.getSide();
        shoulder.setSide(side);
        double front = keyframe.getFront();
        double up = keyframe.getUp();
        shoulder.compute(front, up);
        /*if (side.equalsIgnoreCase("LEFT")) {
        } else if (side.equalsIgnoreCase("RIGHT")) {
        }*/
        return shoulder;
    }

    public Arm getArm(GestureKeyframe gesture) {
        Arm arm = new Arm((double) gesture.getOffset());
        arm.setSkeleton(_skeleton);
        arm.setSide(gesture.getSide());
        arm.setPhase(gesture.getPhaseType());
        arm.setExpressivityParameters(gesture.getParameters());
        TrajectoryDescription trajectory = gesture.getTrajectoryType();
        arm.setTrajectory(trajectory);
        // arm.setInterpolationFuntion(gesture.getInterpolationFunction());
        /**
         * wait quoc anh the gesure class for trajectory
         */
        //arm.setTrajectory(null);
        //select function
        double pwr = arm.getExpressivityParameters().pwr;

        if (pwr >= 0.1) {
            if (pwr < 0.3) {
                arm.setFunction(new EaseInOutSine());
            } else if (pwr < 0.5) {
                arm.setFunction(new EaseOutQuad());
            } else if (pwr < 0.7) {
                arm.setFunction(new EaseOutQuad());
            } else if (pwr < 0.95) {
                arm.setFunction(new EaseOutBack()); //overshoot
            } else if (pwr >= 0.95) {
                arm.setFunction(new EaseOutBounce()); //rebounce
            }
        }


        HashMap<String, JointFrame> result = getHandShape(gesture.getHand().getHandShape(), gesture.getSide());
        arm.addJointFrames(result);
        //arm.setHand(getHand(gesture));
        if (gesture.getHand().getPosition() instanceof SymbolicPosition) {
            SymbolicPosition pos = (SymbolicPosition) gesture.getHand().getPosition();
            if (gesture.isIsScript() || pos.getHorizontalLocation().equals("XR") || pos.getHorizontalLocation().equals("XRF")) {
                //gesture.getScriptName()   i can not get script per hand,~~~ so i can not apply other restpos, i dont know why he is doing that,  rest pos needs to define per part
                arm.setRestPosName("middle");
                if (gesture.getSide() == Side.LEFT) {
                    HashMap<String, JointFrame> jf = _restPoseFactory.getLeftHandPose(arm.getRestPosName());
                    for (String name : jf.keySet()) {
                        JointFrame jframe = jf.get(name);
                        arm.addJointFrame(name, jframe);
                        Joint j = _skeleton.getJoint(name);
                        j.setLocalRotation(jframe._localrotation);
                        j.updateLocally();
                    }
                    Joint j = _skeleton.getJoint("l_wrist");
                    arm.setTarget(new Target(new Vec3d(j.getWorldPosition()), new Vec3d(0, 0, 1)));
                    arm.setWrist(new Quaternion(),true);
                } else {
                    HashMap<String, JointFrame> jf = _restPoseFactory.getRightHandPose(arm.getRestPosName());
                    for (String name : jf.keySet()) {
                        JointFrame jframe = jf.get(name);
                        arm.addJointFrame(name, jframe);
                        Joint j = _skeleton.getJoint(name);
                        j.setLocalRotation(jframe._localrotation);
                        j.updateLocally();
                    }
                    Joint j = _skeleton.getJoint("r_wrist");
                    arm.setTarget(new Target(new Vec3d(j.getWorldPosition()), new Vec3d(0, 0, 1)));
                    arm.setWrist(new Quaternion(),true);
                }
                return arm;
            }
        }

        if (gesture.getSide() == Side.LEFT) {
            //IF SYMBOLIC POSITION
            if (gesture.getHand().getPosition() instanceof SymbolicPosition) {
                SymbolicPosition pos = (SymbolicPosition) gesture.getHand().getPosition();
                arm.setPosition(new Vec3d(
                        -getSpaceVariationPosition(pos.getHorizontalLocation(), (double) gesture.getParameters().spc),
                        getSpaceVariationPosition(pos.getVerticalLocation(), (double) gesture.getParameters().spc),
                        getSpaceVariationPosition(pos.getFrontalLocation(), (double) gesture.getParameters().spc)));
            }//ELSE IF TOUCH POSITION
            else if (gesture.getHand().getPosition() instanceof TouchPosition) {
                TouchPosition pos = (TouchPosition) gesture.getHand().getPosition();
                Joint j = _skeleton.getJoint(_touchPosition.get(pos.getId()));
                arm.setPosition(Vec3d.addition(j.getWorldPosition(),_touchPosOffset.get(pos.getId())));
                //arm.setPosition(new Vec3d(7,35,15));
            }
            arm.getTarget().setEnergy((double) (0.1 + gesture.getParameters().pwr * 0.1));
//            Vec3d palm1 = getDirection(gesture.getHand().getPalmOrientation(), "LEFT");
//            Vec3d palm2 = getDirection(gesture.getHand().getPalmOrientationSupplementary(), "LEFT");
//            Vec3d finger1 = getDirection(gesture.getHand().getFingersDirection(), "LEFT");
//            Vec3d finger2 = getDirection(gesture.getHand().getFingersOrientationSupplementary(), "LEFT");
//            double palmR = gesture.getHand().getPalmOrientationRatio();
//            double fingerR = gesture.getHand().getFingersOrientationRatio();
//            Vec3d palm = Vec3d.interpolation(palm1, palm2, (double) palmR);
//            Vec3d finger = Vec3d.interpolation(finger1, finger2, (double) fingerR);

            Vec3d d = new Vec3d();
            //IF SYMBOLIC POSITION
            if (gesture.getHand().getPosition() instanceof SymbolicPosition) {

                SymbolicPosition pos = (SymbolicPosition) gesture.getHand().getPosition();
                d = getDirectionByPosition(pos.getHorizontalLocation(), pos.getVerticalLocation(), pos.getFrontalLocation(), "LEFT");
                d = Vec3d.addition(new Vec3d(d.x(), d.y(), d.z()), Vec3d.multiplication(new Vec3d(-1, 0, 0), (double) gesture.getHand().getOpenness()));
                //System.out.println("left d: " + d);

            }//ELSE IF TOUCH POSITION
            else if (gesture.getHand().getPosition() instanceof TouchPosition) {
                TouchPosition pos = (TouchPosition) gesture.getHand().getPosition();
                d= _touchRotOffset.get(pos.getId());
            }
            arm.setUpDirectionVector(d.normalized());
            arm.setOpenness(gesture.getHand().getOpenness());

//            Quaternion q = new Quaternion();
//            Vec3d orth = Vec3d.cross3(palm, finger);
//            q.fromRotatedBasis(palm, finger, new Vec3d(orth.x(), orth.y(), orth.z()), new Vec3d(-1, 0, 0), new Vec3d(0, -1, 0), new Vec3d(0, 0, 1));
            arm.setWrist(gesture.getHand().getWristOrientation(), true);  //need to decide use boolean (global or local rotaion (false is global orientation))


        } else {
            //IF SYMBOLIC POSITION
            if (gesture.getHand().getPosition() instanceof SymbolicPosition) {
                SymbolicPosition pos = (SymbolicPosition) gesture.getHand().getPosition();
                arm.setPosition(new Vec3d(
                        getSpaceVariationPosition(pos.getHorizontalLocation(), (double) gesture.getParameters().spc),
                        getSpaceVariationPosition(pos.getVerticalLocation(), (double) gesture.getParameters().spc),
                        getSpaceVariationPosition(pos.getFrontalLocation(), (double) gesture.getParameters().spc)));
            }//ELSE IF TOUCH POSITION
            else if (gesture.getHand().getPosition() instanceof TouchPosition) {
                TouchPosition pos = (TouchPosition) gesture.getHand().getPosition();
                Joint j = _skeleton.getJoint(_touchPosition.get(pos.getId()));
                arm.setPosition(Vec3d.addition(j.getWorldPosition(),_touchPosOffset.get(pos.getId())));

            }
            arm.getTarget().setEnergy((double) (0.1 + gesture.getParameters().pwr * 0.1));
//            Vec3d palm1 = getDirection(gesture.getHand().getPalmOrientation(), "RIGHT");
//            Vec3d palm2 = getDirection(gesture.getHand().getPalmOrientationSupplementary(), "RIGHT");
//            Vec3d finger1 = getDirection(gesture.getHand().getFingersDirection(), "RIGHT");
//            Vec3d finger2 = getDirection(gesture.getHand().getFingersOrientationSupplementary(), "RIGHT");
//            double palmR = gesture.getHand().getPalmOrientationRatio();
//            double fingerR = gesture.getHand().getFingersOrientationRatio();
//            Vec3d palm = Vec3d.interpolation(palm1, palm2, (double) palmR);
//            Vec3d finger = Vec3d.interpolation(finger1, finger2, (double) fingerR);

            Vec3d d = new Vec3d();
            //IF SYMBOLIC POSITION
            if (gesture.getHand().getPosition() instanceof SymbolicPosition) {

                SymbolicPosition pos = (SymbolicPosition) gesture.getHand().getPosition();
                d = getDirectionByPosition(pos.getHorizontalLocation(), pos.getVerticalLocation(), pos.getFrontalLocation(), "RIGHT");
                d = Vec3d.addition(new Vec3d(d.x(), d.y(), d.z()), Vec3d.multiplication(new Vec3d(1, 0, 0), (double) gesture.getHand().getOpenness()));
//            Vec3d d = this.getDirectionByPosition(arm.getPosition(),"right");
//            d = Vec3d.addition(new Vec3d(d.x(), d.y(), d.z()), Vec3d.multiplication(new Vec3d(1, 0, 0), (double) gesture.getParameters().openness));


            }//ELSE IF TOUCH POSITION
            else if (gesture.getHand().getPosition() instanceof TouchPosition) {
                TouchPosition pos = (TouchPosition) gesture.getHand().getPosition();
                d= _touchRotOffset.get(pos.getId());
            }
            arm.setUpDirectionVector(d.normalized());
            arm.setOpenness(gesture.getHand().getOpenness());

//            Quaternion q = new Quaternion();
//            Vec3d orth = Vec3d.cross3(palm, finger);
//            q.fromRotatedBasis(palm, finger, new Vec3d(orth.x(), orth.y(), orth.z()), new Vec3d(1, 0, 0), new Vec3d(0, -1, 0), new Vec3d(0, 0, -1));
            arm.setWrist(gesture.getHand().getWristOrientation(), true);
        }

        return arm;
    }

    public Torse getTorse(TorsoKeyframe t) {
        Torse torse = new Torse((double) t.getOffset());
        torse.setSkeleton(_skeleton);
        torse.setExpressivityParameters(t.getParameters());
        //select function
        double pwr = torse.getExpressivityParameters().pwr;

        if (pwr >= 0.1) {
            if (pwr < 0.3) {
                torse.setFunction(new EaseInOutSine());
            } else if (pwr < 0.5) {
                torse.setFunction(new EaseOutQuad());
            } else if (pwr < 0.7) {
                torse.setFunction(new EaseOutQuad());
            } else if (pwr < 0.95) {
                torse.setFunction(new EaseOutBack()); //overshoot
            } else if (pwr >= 0.95) {
                torse.setFunction(new EaseOutBounce()); //rebounce
            }
        }

        Quaternion q = new Quaternion();
        if (t.verticalTorsion.flag == true) {
            if (t.verticalTorsion.direction == SpineDirection.Direction.LEFTWARD) {
                double v = t.verticalTorsion.value * _torsoIntervals.verticalL;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(0, 1, 0), radian);
                q = Quaternion.multiplication(q, r);
            } else {
                double v = t.verticalTorsion.value * _torsoIntervals.verticalR;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(0, 1, 0), -radian);
                q = Quaternion.multiplication(q, r);
            }
        }
        if (t.sagittalTilt.flag == true) {
            if (t.sagittalTilt.direction == SpineDirection.Direction.FORWARD) {
                double v = t.sagittalTilt.value * _torsoIntervals.sagittalF;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(1, 0, 0), radian);
                q = Quaternion.multiplication(q, r);
            } else {
                double v = t.sagittalTilt.value * _torsoIntervals.sagittalB;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(1, 0, 0), -radian);
                q = Quaternion.multiplication(q, r);
            }
        }
        if (t.lateralRoll.flag == true) {
            if (t.lateralRoll.direction == SpineDirection.Direction.LEFTWARD) {
                double v = t.lateralRoll.value * _torsoIntervals.lateralL;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(0, 0, 1), -radian);
                q = Quaternion.multiplication(q, r);
            } else {
                double v = t.lateralRoll.value * _torsoIntervals.lateralR;
                double radian = (double) (v * java.lang.Math.PI / 180.0 * 0.45);
                Quaternion r = new Quaternion();
                r.setAxisAngle(new Vec3d(0, 0, 1), radian);
                q = Quaternion.multiplication(q, r);
            }
        }
        torse.setRotation(q);
        if (t.collapse.flag == true) {
            String list[] = {"vt3", "vt5", "vt12", "vl5"};
            Quaternion each = Quaternion.slerp(new Quaternion(), q, 0.25f, true);
            Quaternion each2 = Quaternion.slerp(new Quaternion(), q, 0.5f, true);
            JointFrame jfq = new JointFrame();
            jfq._localrotation = new Quaternion(q);
            JointFrame jf = new JointFrame();
            jf._localrotation = new Quaternion(each);
            JointFrame jf2 = new JointFrame();
            jf2._localrotation = new Quaternion(each2);

            torse.addJointFrame("vt2", jf2);
            torse.addJointFrame("vt5", jfq);
            torse.addJointFrame("vt12", jfq);
            torse.addJointFrame("vl1", jf2);
//            String list[] = {"vt3", "vt5", "vt12"};
//            Quaternion each = Quaternion.slerp(new Quaternion(), q, 0.34f, true);
//            for (String name : list) {
//                JointFrame jf = new JointFrame();
//                jf._localrotation = new Quaternion(each);
//                torse.addJointFrame(name, jf);
//            }
        } else {
            JointFrame jf = new JointFrame();
            jf._localrotation = new Quaternion(q);
            torse.addJointFrame("vl5", jf);
        }
        return torse;
    }

    public Head getHead(HeadKeyframe h) {
        Head head = new Head((double) h.getOffset());
        head.setSkeleton(_skeleton);
        head.setExpressivityParameters(h.getParameters());

        //select function
        double pwr = head.getExpressivityParameters().pwr;

        if (pwr >= 0.1) {
            if (pwr < 0.3) {
                head.setFunction(new EaseInOutSine());
            } else if (pwr < 0.5) {
                head.setFunction(new EaseOutQuad());
            } else if (pwr < 0.7) {
                head.setFunction(new EaseOutQuad());
            } else if (pwr < 0.95) {
                head.setFunction(new EaseOutBack()); //overshoot
            } else if (pwr >= 0.95) {
                head.setFunction(new EaseOutBounce()); //rebounce
            }
        }

        double rx = 0;
        double ry = 0;
        double rz = 0;
        if (h.verticalTorsion.flag == true) {
            if (h.verticalTorsion.direction == SpineDirection.Direction.LEFTWARD) {
                ry = Math.toRadians(_headIntervals.verticalLeftMax);
            } else {
                ry = -Math.toRadians(_headIntervals.verticalRightMax);
            }
            ry *= h.verticalTorsion.value;
        }
        if (h.sagittalTilt.flag == true) {
            if (h.sagittalTilt.direction == SpineDirection.Direction.BACKWARD) {
                rx = -Math.toRadians(_headIntervals.sagittalUpMax);
            } else {
                rx = Math.toRadians(_headIntervals.sagittalDownMax);
            }
            rx *= h.sagittalTilt.value;
        }
        if (h.lateralRoll.flag == true) {
            if (h.lateralRoll.direction == SpineDirection.Direction.LEFTWARD) {
                rz = -Math.toRadians(_headIntervals.lateralLeftMax);
            } else {
                rz = Math.toRadians(_headIntervals.lateralRightMax);
            }
            rz *= h.lateralRoll.value;
        }
        //System.out.println("head....  " + q.angle() +"    "+q.axis());
        Quaternion q = new Quaternion(new Vec3d(0, 1, 0), (double) ry);
        q.multiply(new Quaternion(new Vec3d(1, 0, 0), (double) rx));
        q.multiply(new Quaternion(new Vec3d(0, 0, 1), (double) rz));

        head.setRotation(q);
        {
            Quaternion qvc1 = new Quaternion(new Vec3d(0, 1, 0), (double) (ry / 3.0));
            qvc1.multiply(new Quaternion(new Vec3d(1, 0, 0), (double) (rx * 0.7)));
            qvc1.multiply(new Quaternion(new Vec3d(0, 0, 1), (double) (rz * 0.1)));

            Quaternion qvc4 = new Quaternion(new Vec3d(0, 1, 0), (double) (ry / 3.0));
            qvc4.multiply(new Quaternion(new Vec3d(1, 0, 0), (double) (rx * 0.2)));
            qvc4.multiply(new Quaternion(new Vec3d(0, 0, 1), (double) (rz * 0.3)));

            Quaternion qvc7 = new Quaternion(new Vec3d(0, 1, 0), (double) (ry / 3.0));
            qvc7.multiply(new Quaternion(new Vec3d(1, 0, 0), (double) (rx * 0.1)));
            qvc7.multiply(new Quaternion(new Vec3d(0, 0, 1), (double) (rz * 0.6)));

            JointFrame jf1 = new JointFrame();
            jf1._localrotation = qvc1;
            head.addJointFrame("vc1", jf1);

            JointFrame jf4 = new JointFrame();
            jf4._localrotation = qvc4;
            head.addJointFrame("vc4", jf4);

            JointFrame jf7 = new JointFrame();
            jf7._localrotation = qvc7;
            head.addJointFrame("vc7", jf7);

        }
        return head;
    }

    public double getScaleFactorX() {
        return _scaleFactorX;
    }

    public void setScaleFactorX(double _scaleFactorX) {
        this._scaleFactorX = _scaleFactorX;
    }

    public double getScaleFactorY() {
        return _scaleFactorY;
    }

    public void setScaleFactorY(double _scaleFactorY) {
        this._scaleFactorY = _scaleFactorY;
    }

    public double getScaleFactorZ() {
        return _scaleFactorZ;
    }

    public void setScaleFactorZ(double _scaleFactorZ) {
        this._scaleFactorZ = _scaleFactorZ;
    }

    Vec3d getDirectionByPosition(String x, String y, String z, String side) {
        Vec3d dir = new Vec3d(0, 0.5f, 0);
        if (side.equalsIgnoreCase("left")) {
            if (x.equalsIgnoreCase("XEP")) {
                dir.add(new Vec3d(0, 0, 0.3f));
            } else if (x.equalsIgnoreCase("XC")) {
                dir.add(new Vec3d(-0.1f, 0, 0));
            } else if (x.equalsIgnoreCase("XCC")) {
                dir.add(new Vec3d(-0.2f, 0, 0));
            } else if (x.equalsIgnoreCase("XOppC")) {
                dir.add(new Vec3d(-0.3f, 0, 0));
            }

            if (y.equalsIgnoreCase("YUpperEP")) {
                dir.add(new Vec3d(0, 0, -0.3f));
            } else if (y.equalsIgnoreCase("YLowerEP")) {
                dir.add(new Vec3d(0, 0, 0.3f));
            }

            if (z.equalsIgnoreCase("ZNear")) {
                dir.add(new Vec3d(0, 0, -0.1f));
            } else if (z.equalsIgnoreCase("ZMiddle")) {
                dir.add(new Vec3d(0, 0, -0.05f));
            }
        } else {
            if (x.equalsIgnoreCase("XEP")) {
                dir.add(new Vec3d(0, 0, 0.3f));
            } else if (x.equalsIgnoreCase("XC")) {
                dir.add(new Vec3d(0.1f, 0, 0));
            } else if (x.equalsIgnoreCase("XCC")) {
                dir.add(new Vec3d(0.2f, 0, 0));
            } else if (x.equalsIgnoreCase("XOppC")) {
                dir.add(new Vec3d(0.3f, 0, 0));
            }

            if (y.equalsIgnoreCase("YUpperEP")) {
                dir.add(new Vec3d(0, 0, -0.3f));
            } else if (y.equalsIgnoreCase("YLowerEP")) {
                dir.add(new Vec3d(0, 0, 0.3f));
            }
            if (z.equalsIgnoreCase("ZNear")) {
                dir.add(new Vec3d(0, 0, -0.1f));
            } else if (z.equalsIgnoreCase("ZMiddle")) {
                dir.add(new Vec3d(0, 0, -0.05f));
            }

        }
        return dir.normalized();
    }
}
