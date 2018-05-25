/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.easingcurve;

/**
 *
 * @author Jing Huang
 */
public abstract class EasingCurveFuntion {
    String _t;  //type
    double _p = 0.3f; // period
    double _a = 1.0f;  //amplitude
    double _o = 1.70158f;  //overshoot

    abstract double getValue(double t);

}
