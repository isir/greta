/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.math;

/**
 *
 * @author André-Marie
 */


public class Sqrt implements Function{

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(Math.sqrt(((Constante)f).getValue()));
        }
        return new Sqrt(f);
    }

    private Function f;

    public Sqrt(){
        this(X.x);
    }

    public Sqrt(Function f){
        this.f = f;
    }


    @Override
    public double f(double x) {
        return Math.sqrt(f.f(x));
    }

    @Override
    public String getName() {
        return "Square Root";
    }

    @Override
    public String toString() {
        return "sqrt( "+f+" )";
    }


    @Override
    public Function getDerivative() {
        return null; //TODO
    }

    @Override
    public Function simplified() {
        return Sqrt.of(f.simplified());
    }

}
