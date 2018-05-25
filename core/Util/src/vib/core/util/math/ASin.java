/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.math;

/**
 *
 * @author André-Marie
 */


public class ASin implements Function{

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(Math.asin(((Constante)f).getValue()));
        }
        return new ASin(f);
    }

    private Function f;

    public ASin(){
        this(X.x);
    }

    public ASin(Function f){
        this.f = f;
    }


    @Override
    public double f(double x) {
        return Math.asin(f.f(x));
    }

    @Override
    public String getName() {
        return "Arc Sinus";
    }

    @Override
    public String toString() {
        return "asin( "+f+" )";
    }

    @Override
    public Function getDerivative() {
        return null; //TODO
    }

    @Override
    public Function simplified() {
        return ASin.of(f.simplified());
    }

}
