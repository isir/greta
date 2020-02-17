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
public class Sin  implements Function{

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(Math.sin(((Constante)f).getValue()));
        }
        return new Sin(f);
    }

    private Function f;

    public Sin(){
        this(X.x);
    }

    public Sin(Function f){
        this.f = f;
    }


    @Override
    public double f(double x) {
        return Math.sin(f.f(x));
    }

    @Override
    public String getName() {
        return "Sin";
    }

    @Override
    public String toString() {
        return "sin( "+f+" )";
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
            if(cst.getValue()==0){
                return Constante.ZERO;
            }
            if(cst.getValue()==1){
                return new Cos(f);
            }
        }
        return Product.of(new Cos(f), fprim);
    }

    @Override
    public Function simplified() {
        return Sin.of(f.simplified());
    }

}
