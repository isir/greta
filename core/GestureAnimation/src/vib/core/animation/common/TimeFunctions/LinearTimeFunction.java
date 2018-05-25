/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.TimeFunctions;

/**
 *
 * @author Jing Huang
 */
public class LinearTimeFunction implements TimeFunction {

    @Override
    public double getTime(double original) {
        return original;
    }

}
