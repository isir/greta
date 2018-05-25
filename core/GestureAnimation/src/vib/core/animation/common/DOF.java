/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common;

/**
 *
 * @author Jing Huang
 */

public class DOF {

    double _maxValue = 2 * (double) java.lang.Math.PI;
    double _minValue = -2 * (double) java.lang.Math.PI;

    static public enum DOFType {
        ROTATION_X,
        ROTATION_Y,
        ROTATION_Z,
        TRANSLATION_X,
        TRANSLATION_Y,
        TRANSLATION_Z
    }


    public DOF(double min, double max) {
        _maxValue = max;
        _minValue = min;
    }

    public double maxValue() {
        return _maxValue;
    }

    public void maxValue(double val) {
        _maxValue = val;
    }

    public double minValue() {
        return _minValue;
    }

    public void minValue(double val) {
        _minValue = val;
    }
}
