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
 * @author André-Marie
 */
public class ACos implements Function{

    public static Function of(Function f){
        if(f instanceof Constante){
            return Constante.of(Math.acos(((Constante)f).getValue()));
        }
        return new ACos(f);
    }

    private Function f;

    public ACos(){
        this(X.x);
    }

    public ACos(Function f){
        this.f = f;
    }


    @Override
    public double f(double x) {
        return Math.acos(f.f(x));
    }

    @Override
    public String getName() {
        return "Arc Cosinus";
    }

    @Override
    public String toString() {
        return "acos( "+f+" )";
    }

    @Override
    public Function getDerivative() {
        return null; //TODO
    }

    @Override
    public Function simplified() {
        return ACos.of(f.simplified());
    }

}
