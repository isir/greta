/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.math.easefunctions;

import vib.core.util.math.Function;

/**
 * Easing equation function for a simple linear tweening, with no easing.
 * @author Jing Huang
 * @author Andre-Marie Pez
 * It is the same as vib.core.util.math.X class
 */
public class Linear implements Function{

    public double f(double x) {
        return x;
    }

    public String getName() {
        return "Linear";
    }

    @Override
    public Function getDerivative() {
        return vib.core.util.math.Constante.ONE;
    }

    @Override
    public String toString() {
        return "x";
    }

    @Override
    public Function simplified() {
        return this;
    }

}
