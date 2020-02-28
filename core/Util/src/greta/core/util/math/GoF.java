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
