/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.interpolation;

import vib.core.util.math.Quaternion;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public abstract class QuaternionInterpolation extends Interpolation <Quaternion>{

    public QuaternionInterpolation() {

    }

    public void setControlPoints(ArrayList<Quaternion> controlPoints)
    {
        super.setControlPoints(controlPoints);
        calculateLength();
    }


    @Override
    public abstract Quaternion getPosition(double t);

    @Override
    protected double calculateDistance(Quaternion p1, Quaternion p2) {
         double cosAngle = Quaternion.dot(p1, p2);
         if(cosAngle < -1) cosAngle = -1;
         if(cosAngle > 1) cosAngle = 1;
         double angle = (double) java.lang.Math.acos(java.lang.Math.abs(cosAngle));
         //System.out.println("angle"+angle);
         return angle;
    }

}
