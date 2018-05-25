/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.math;

/**
 *
 * @author Andre-Marie Pez
 */
public final class Constante implements Function{
    public static final Constante ZERO = new Constante(0, "0");
    public static final Constante ONE = new Constante(1, "1");
    public static final Constante PI = new Constante(Math.PI, "\u03c0");
    public static final Constante E = new Constante(Math.E, "e");

    private final double cst;
    private final String name;

    public static Constante of(double value){
        if(value==ZERO.getValue()){
            return ZERO;
        }
        if(value==ONE.getValue()){
            return ONE;
        }
        if(value==PI.getValue()){
            return PI;
        }
        if(value==E.getValue()){
            return E;
        }
        return new Constante(value);
    }

    protected Constante(double value){
        this(value, ""+value);
    }
    public Constante(double value, String name){
        cst = value;
        this.name = name;
    }

    public double getValue(){
        return cst;
    }

    @Override
    public double f(double x) {
        return cst;
    }

    @Override
    public String getName() {
        return "Constante";
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Function getDerivative() {
        if(cst==0) {
            return this;
        }
        return ZERO;
    }

    public Function getAntiderivative() {
        return Product.of(this, X.x);
    }

    @Override
    public Function simplified() {
        return this;
    }

    public boolean equals(double value){
        return cst==value;
    }

    public boolean equals(Function f){
        if(f instanceof Constante){
            return cst==((Constante)f).cst;
        }

        //try to simplify f and check if the result is a constante ?

        return false;
    }

}
