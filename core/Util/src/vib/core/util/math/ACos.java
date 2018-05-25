/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.math;

/**
 *
 * @author André-Marie
 */


public class ACos implements Function{

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(Math.acos(((Constante)f).getValue()));
        }
        return new ACos(f);
    }

    private Function f;

    public ACos(){
        this(X.x);
    }

    public ACos(Function f){
        this.f = f;
    }


    @Override
    public double f(double x) {
        return Math.acos(f.f(x));
    }

    @Override
    public String getName() {
        return "Arc Cosinus";
    }

    @Override
    public String toString() {
        return "acos( "+f+" )";
    }

    @Override
    public Function getDerivative() {
        return null; //TODO
    }
    
    @Override
    public Function simplified() {
        return ACos.of(f.simplified());
    }

}

