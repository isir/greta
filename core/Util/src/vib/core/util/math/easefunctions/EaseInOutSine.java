/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.math.easefunctions;

import vib.core.util.math.Constante;
import vib.core.util.math.Function;
import vib.core.util.math.Product;
import vib.core.util.math.Sin;
import vib.core.util.math.X;

/**
 *
 * @author Jing Huang
 * @author Andre-Marie Pez
 */
public class EaseInOutSine implements Function {

    /**
     * Easing equation function for a sinusoidal (sin(t)) easing in/out:
     * acceleration until halfway, then deceleration.
     *
     * @param x	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public double f(double x) {
        return (-0.5 * (Math.cos(Math.PI * x) - 1));
    }

    public String getName() {
        return "EaseInOutSine";
    }

    @Override
    public String toString() {
        return "(cos(PI*x)-1)/2";
    }


    @Override
    public Function getDerivative() {
        return Product.of(Constante.of(Math.PI/2), Sin.of(Product.of(Constante.PI,X.x)));
    }

    @Override
    public Function simplified() {
        return this;
    }
}
