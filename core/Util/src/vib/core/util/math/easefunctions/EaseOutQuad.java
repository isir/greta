/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.math.easefunctions;

import vib.core.util.math.Constante;
import vib.core.util.math.Function;
import vib.core.util.math.Product;
import vib.core.util.math.Sum;
import vib.core.util.math.X;

/**
 *
 * @author Jing Huang
 * @author Andre-Marie Pez
 */
public class EaseOutQuad implements Function {

      /**
     * Easing equation function for a quadratic (t^2) easing out: decelerating
     * to zero velocity.
     *
     * @param x	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public double f(double x) {
        return -x * (x - 2);
    }

    public String getName() {
        return "EaseOutQuad";
    }

    @Override
    public Function getDerivative() {
        return Sum.of(Constante.of(2), Product.of(Constante.of(-2), X.x));
    }

    @Override
    public Function simplified() {
        return this;
    }
}
