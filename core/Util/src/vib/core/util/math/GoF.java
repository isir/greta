/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.math;

/**
 *
 * @author Andre-Marie Pez
 */
public class GoF implements Function{

    private Function g;
    private Function f;

    public GoF(Function g, Function f){
        this.f = f;
        this.g = g;
    }

    @Override
    public double f(double x) {
        return g.f(f.f(x));
    }

    @Override
    public String getName() {
        return "Gof";
    }

    @Override
    public String toString() {
        return "gof where g(x)="+g+" and f(x)="+f;
    }



    @Override
    public Function getDerivative() {
        Function fprim = f.getDerivative();
        Function gprim = g.getDerivative();
        if(fprim==null || gprim==null){
            return null;
        }
        if(f instanceof Constante){
            return Constante.ZERO;
        }
        if(gprim instanceof Constante || gprim instanceof X){
            return Product.of(gprim, fprim);
        }
        return Product.of(new GoF(gprim, f), fprim);
    }

    @Override
    public Function simplified() {
        Function gsimple = g.simplified();
        Function fsimple = f.simplified();

        if(gsimple instanceof Constante){
            return gsimple;
        }

        if(gsimple instanceof X){
            return fsimple;
        }

        if(fsimple instanceof Constante){
            return Constante.of(gsimple.f(((Constante)fsimple).getValue()));
        }

        return new GoF(gsimple, fsimple);
    }

}
