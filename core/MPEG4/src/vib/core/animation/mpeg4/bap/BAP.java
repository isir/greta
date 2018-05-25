/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.bap;

import vib.core.util.animationparameters.AnimationParameter;

/**
 *
 * @author Jing Huang
 */
public class BAP extends AnimationParameter {

    private static int radianFactor = 100000;

    public BAP(boolean mask, int value) {
        super(mask, value);
    }

    public BAP() {
        super();
    }

    public BAP(int value) {
        super(value);
    }

    public BAP(BAP bap) {
        super(bap);
    }

    @Override
    public BAP clone() {
        BAP bap = new BAP(this);
        return bap;
    }

    /**
     * convert from angle in radian and set BAP value
     */
    public void setRadianValue(double value) {
        applyValue((int) (value * radianFactor));
    }

    /**
     * convert from angle in degree and set BAP value
     */
    public void setDegreeValue(double value){
        setRadianValue(Math.toRadians(value));
    }

    /**
     * @return BAP value in radians
     */
    public double getRadianValue() {
        return ((double)getValue()) / radianFactor;
    }

    /**
     * @return BAP value in degrees
     */
    public double getDegreeValue() {
        return Math.toDegrees(getRadianValue());
    }
}
