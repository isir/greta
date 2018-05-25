/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.math.easefunctions;

import vib.core.util.math.Function;

/**
 *
 * Easing equation function for a bounce (exponentially decaying parabolic
 * bounce) easing out: decelerating from zero velocity.
 *
 * @author Jing Huang
 * @author Andre-Marie Pez
 */
public class EaseOutBounce implements Function {

    public static double easeOutBounce_helper(double t, double c, double amplitude) {
        if (t == 1.0) {
            return c;
        }
        double value = 0;
        if (t < (4 / 11.0)) {
            value = c * (7.5625f * t * t);
        } else if (t < (8 / 11.0)) {
            t -= (6 / 11.0);
            value = -amplitude * (1.f - (7.5625f * t * t + 0.75f)) + c;
        } else if (t < (10 / 11.0)) {
            t -= (9 / 11.0);
            value = -amplitude * (1.f - (7.5625f * t * t + .9375f)) + c;
        } else {
            t -= (21 / 22.0);
            value = -amplitude * (1.f - (7.5625f * t * t + .984375f)) + c;
        }
        return value;
    }

    @Override
    public double f(double x) {
        return easeOutBounce_helper(x, 1, 0.1);
    }

    @Override
    public String getName() {
        return "EaseOutBounce";
    }

    @Override
    public Function getDerivative() {
        return null;
    }

    @Override
    public Function simplified() {
        return this;
    }
}
