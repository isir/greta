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
public class Cos implements Function{

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(Math.cos(((Constante)f).getValue()));
        }
        return new Cos(f);
    }

    private Function f;

    public Cos(){
        this(X.x);
    }

    public Cos(Function f){
        this.f = f;
    }


    @Override
    public double f(double x) {
        return Math.cos(f.f(x));
    }

    @Override
    public String getName() {
        return "Cos";
    }

    @Override
    public String toString() {
        return "cos( "+f+" )";
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
                return Product.of(Constante.of(-1), new Sin(f));
            }
        }
        return Product.of(Constante.of(-1), new Sin(f), fprim);
    }

    @Override
    public Function simplified() {
        return Cos.of(f.simplified());
    }

}
