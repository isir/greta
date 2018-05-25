/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.math;

/**
 *
 * @author Andre-Marie Pez
 */
public class Abs implements Function {

    public static Function of() {
        return of(X.x);
    }

    public static Function of(double value) {
        return Constante.of(Math.abs(value));
    }

    public static Function of(Function function) {
        if (function instanceof Constante) {
            return Constante.of(Math.abs(((Constante) function).getValue()));
        }
        //TODO : if function is always positive return function
        // and if function is always negative return Product(-1,function)
        return new Abs(function);
    }
    private Function operand;

    private Abs(Function function) {
        operand = function;
    }

    @Override
    public double f(double x) {
        return Math.abs(operand.f(x));
    }

    @Override
    public String getName() {
        return "abs";
    }

    @Override
    public String toString() {
        return "abs(" + operand + ")";
    }

    @Override
    public Function getDerivative() {
        Function fprim = operand.getDerivative();
        if (fprim == null) {
            return null;
        }
        if(Constante.ZERO.equals(fprim)){
            return Constante.ZERO;
        }
        return new AbsDerivative(operand, fprim);
    }

    @Override
    public Function simplified() {
        return of(operand.simplified());
    }

    private static class AbsDerivative implements Function {

        Function op;
        Function opnderived;
        private AbsDerivative(Function f, Function fnprim) {
            op = f;
            opnderived = fnprim;
        }

        @Override
        public double f(double x) {
            return Math.signum(op.f(x))*opnderived.f(x);
        }

        @Override
        public String getName() {
            return "AbsDerivative";
        }

        @Override
        public String toString() {
            return "("+op+") * ("+opnderived+") / abs("+op+")";
        }

        @Override
        public Function getDerivative() {
            Function fprim = opnderived.getDerivative();
            if (fprim == null) {
                return null;
            }
            if(Constante.ZERO.equals(fprim)){
                return Constante.ZERO;
            }
            return new AbsDerivative(op, fprim);
        }

        @Override
        public Function simplified() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    }
}
