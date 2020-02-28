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
public class Inverse implements Function{

    public static Function of(){
        return of(X.x);
    }

    public static Function of(double d){
        return Constante.of(1/d);
    }

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(1/((Constante)f).getValue());
        }
        return new Inverse(f);
    }

    private Function f;

    private Inverse(Function f){
        this.f = f;
    }

    protected Function getOperand(){
        return f;
    }

    @Override
    public double f(double x) {
        return 1 / f.f(x);
    }

    @Override
    public String getName() {
        return "Inverse";
    }

    @Override
    public String toString() {
        return "1/("+f+")";
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
                //TODO NAN !
            }
        }
        return Product.of(Constante.of(-1), fprim, Inverse.of(Product.of(f,f)));
    }

    @Override
    public Function simplified() {
        return Inverse.of(f.simplified());
    }
}
