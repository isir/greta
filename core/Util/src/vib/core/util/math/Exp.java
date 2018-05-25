/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.math;

/**
 *
 * @author Andre-Marie Pez
 */
public class Exp implements Function{

    public static Function of(){
        return of(X.x);
    }

    public static Function of(double d){
        return Constante.of(Math.exp(d));
    }

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(Math.exp(((Constante)f).getValue()));
        }
        if(f instanceof Ln){
            return ((Ln)f).getOperand();
        }

        return new Exp(f);
    }

    private Function f;

    private Exp(Function f){
        this.f = f;
    }

    protected Function getOperand(){
        return f;
    }

    @Override
    public double f(double x) {
        return Math.exp(f.f(x));
    }

    @Override
    public String getName() {
        return "Exp";
    }

    @Override
    public String toString() {
        return "e^("+f+")";
    }

    @Override
    public Function getDerivative() {
        if(f instanceof Constante){
            return Constante.ZERO;
        }
        Function fprim = f.getDerivative();
        if(fprim==null){
            return null;
        }
        if(fprim instanceof Constante){
            Constante cst = (Constante)fprim;
            if(cst.equals(0)){
                return Constante.ZERO;
            }
            if(cst.getValue()==1){
                return Exp.of(f);
            }
        }
        return Product.of(Exp.of(f), fprim);
    }

    @Override
    public Function simplified() {
        return Exp.of(f.simplified());
    }

}
