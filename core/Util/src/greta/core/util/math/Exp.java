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
