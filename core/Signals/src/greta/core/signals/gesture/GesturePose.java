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
package greta.core.signals.gesture;

import greta.core.util.enums.Side;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Quoc Anh Le
 * @author Andre-Marie Pez
 * @author Brian Ravenet
 */
public class GesturePose {

    //BRIAN : I added this boolean to keep track if the phase is the last of the gesture
    private boolean isStrokeEnd = false; //to be set to true if the last phase of the gesture
    private Hand leftHand; // configuration of the left hand
    private Hand rightHand;// configuration of the right hand
    double relativeTime = 0;

    public GesturePose(Hand leftHand, Hand rightHand) {
        this.leftHand = leftHand;
        this.rightHand = rightHand;
    }


    public GesturePose() {
        this(null, null);
    }

    public GesturePose(GesturePose ref) {
        this(ref.leftHand == null ? null : new Hand(ref.leftHand),
             ref.rightHand == null ? null : new Hand(ref.rightHand));
    }

    public Hand getLeftHand() {
        return leftHand;
    }

    public void setLeftHand(Hand leftHand) {
        this.leftHand = leftHand;
    }


    public Hand getRightHand() {
        return rightHand;
    }

    public void setRightHand(Hand rightHand) {
        this.rightHand = rightHand;
    }

    public void setRelativeTime(double d) {
        relativeTime = d;
    }

    public double getRelativeTime(){
        return relativeTime;
    }
    /**
     * @return the isStrokeEnd
     */
    public boolean isIsStrokeEnd() {
        return isStrokeEnd;
    }

    /**
     * @param isStrokeEnd the isStrokeEnd to set
     */
    public void setIsStrokeEnd(boolean isStrokeEnd) {
        this.isStrokeEnd = isStrokeEnd;
    }

    public void switchHands() {

        Hand originaLeftHand = leftHand;
        Hand originalRightHand = rightHand;

        Hand mirrorLeftHand = null;
        Hand mirrorRightHand = null;

        if (originaLeftHand != null) {

            mirrorRightHand = new Hand(originaLeftHand);
            mirrorRightHand.setSide(Side.RIGHT);

            Quaternion originaLeftHandWristOrientation = originaLeftHand.getWristOrientation();
            if (originaLeftHandWristOrientation != null) {
                Vec3d mirrorRightHandWristOrientationEulerXYZ = originaLeftHandWristOrientation.getEulerAngleXYZ();
                mirrorRightHandWristOrientationEulerXYZ.setY(-mirrorRightHandWristOrientationEulerXYZ.y());
                mirrorRightHandWristOrientationEulerXYZ.setZ(-mirrorRightHandWristOrientationEulerXYZ.z());
                Quaternion mirrorRightHandWristOrientation = new Quaternion();
                mirrorRightHandWristOrientation.fromEulerXYZ(
                        mirrorRightHandWristOrientationEulerXYZ.x(),
                        mirrorRightHandWristOrientationEulerXYZ.y(),
                        mirrorRightHandWristOrientationEulerXYZ.z()
                );
                mirrorRightHand.setWristOrientation(mirrorRightHandWristOrientation);
            }
        }
        if (originalRightHand != null) {

            mirrorLeftHand = new Hand(originalRightHand);
            mirrorLeftHand.setSide(Side.LEFT);

            Quaternion originaRightHandWristOrientation = originalRightHand.getWristOrientation();
            if (originaRightHandWristOrientation != null) {
                Vec3d mirrorLeftHandWristOrientationEulerXYZ = originaRightHandWristOrientation.getEulerAngleXYZ();
                mirrorLeftHandWristOrientationEulerXYZ.setY(-mirrorLeftHandWristOrientationEulerXYZ.y());
                mirrorLeftHandWristOrientationEulerXYZ.setZ(-mirrorLeftHandWristOrientationEulerXYZ.z());
                Quaternion mirrorLeftHandWristOrientation = new Quaternion();
                mirrorLeftHandWristOrientation.fromEulerXYZ(
                        mirrorLeftHandWristOrientationEulerXYZ.x(),
                        mirrorLeftHandWristOrientationEulerXYZ.y(),
                        mirrorLeftHandWristOrientationEulerXYZ.z()
                );
                mirrorLeftHand.setWristOrientation(mirrorLeftHandWristOrientation);
            }
        }

        leftHand = mirrorLeftHand;
        rightHand = mirrorRightHand;
    }

    @Override
    public String toString() {
        return "left: "+leftHand+" right: "+rightHand; //To change body of generated methods, choose Tools | Templates.
    }

}
