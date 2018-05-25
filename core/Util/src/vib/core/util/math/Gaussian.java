/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.math;

/**
 *
 * @author Florian Pecune
 */
public class Gaussian implements Function{

    Function gauss = Exp.of(Product.of(Constante.of(-1), X.x, X.x));
    @Override
    public double f(double x) {
        return gauss.f(x);
    }

    @Override
    public String getName() {
        return "Gaussian";
    }

    @Override
    public String toString() {
        return gauss.toString();
    }
    
    @Override
    public Function getDerivative() {
        return gauss.getDerivative();
    }

    @Override
    public Function simplified() {
        return this;
    }
    
}
