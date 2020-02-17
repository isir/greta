/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.util.math;

/**
 *
 * @author Andre-Marie Pez
 */
public class Power implements Function{

    private Function base;
    private Function exponent;

    public static Function of(double base, double exponent){
        return Constante.of(Math.pow(base,exponent));
    }

    public static Function of(double base, Function exponent){
        return of(Constante.of(base), exponent);
    }

    public static Function of(Function base, double exponent){
        return of(base, Constante.of(exponent));
    }

    public static Function of(Function base, Function exponent) {
        if(Constante.ZERO.equals(exponent)){
            return Constante.ONE;
        }
        if(Constante.ONE.equals(exponent)){
            return base;
        }
        if(Constante.ONE.equals(base)){
            return Constante.ONE;//or base, it's the same
        }
        return new Power(base, exponent);
    }

    private Power(Function base, Function exponent) {
        this.base = base;
        this.exponent = exponent;
    }

    public Function getBase(){
        return base;
    }

    public Function getExponent(){
        return exponent;
    }

    @Override
    public double f(double x) {
        return Math.pow(base.f(x), exponent.f(x));
    }

    @Override
    public String getName() {
        return "Power";
    }

    @Override
    public String toString() {
        return "("+base+")^("+exponent+")";
    }

    @Override
    public Function getDerivative() {
        Function basePrim = base.getDerivative();
        Function exponentPrim = exponent.getDerivative();
        if(basePrim==null || exponentPrim==null){
            return null;
        }
        return Product.of(this, Sum.of(Product.of(exponent,basePrim,Inverse.of(base)), Product.of(exponentPrim, Ln.of(base))));
    }

    @Override
    public Function simplified() {
        return of(base.simplified(), exponent.simplified());
    }

}
