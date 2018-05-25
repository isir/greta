/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.math;

/**
 *
 * @author Andre-Marie Pez
 */
public final class X implements Function{

    public static final X x = new X();

    private X(){}

    @Override
    public double f(double x) {
        return x;
    }

    @Override
    public String getName() {
        return "Identity";
    }

    @Override
    public String toString() {
        return "x";
    }

    @Override
    public Function getDerivative() {
        return Constante.ONE;
    }

    public Function getAntiderivative() {
        return Product.of(this, this);
    }

    @Override
    public Function simplified() {
        return this;
    }

}
