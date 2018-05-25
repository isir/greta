/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.math;

/**
 *
 * @author Andre-Marie Pez
 */
public class Sigmoid implements Function{

    Constante lambda;
    Function sig;

    public Sigmoid(){
        this(1);
    }

    public Sigmoid(double lambda){
        this.lambda = new Constante(lambda, "\u03bb");
        sig = Inverse.of(Sum.of(Constante.ONE, Exp.of(Product.of(Constante.of(-1),this.lambda,X.x))));
    }

    @Override
    public double f(double x) {
        return sig.f(x);
    }

    @Override
    public String getName() {
        return "Sigmoid";
    }

    @Override
    public String toString() {
        return sig.toString();
    }

    @Override
    public Function getDerivative() {
        return Product.of(lambda, this, Sum.of(Constante.ONE, Product.of(Constante.of(-1), this)));
    }

    @Override
    public Function simplified() {
        return this;
    }

}
