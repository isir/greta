/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.TimeFunctions;

import vib.core.animation.common.easingcurve.EquationFunctions;

/**
 *
 * @author Jing Huang
 */
public class OutBackTimeFunction implements TimeFunction{


    private double amplitude = 0.2f;
    private double overshoot =  0.20158f;
    @Override
    public double getTime(double original) {
        return EquationFunctions.easeOutBack(original, overshoot);
    }

}
