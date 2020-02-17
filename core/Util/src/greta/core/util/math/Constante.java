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
public final class Constante implements Function{
    public static final Constante ZERO = new Constante(0, "0");
    public static final Constante ONE = new Constante(1, "1");
    public static final Constante PI = new Constante(Math.PI, "\u03c0");
    public static final Constante E = new Constante(Math.E, "e");

    private final double cst;
    private final String name;

    public static Constante of(double value){
        if(value==ZERO.getValue()){
            return ZERO;
        }
        if(value==ONE.getValue()){
            return ONE;
        }
        if(value==PI.getValue()){
            return PI;
        }
        if(value==E.getValue()){
            return E;
        }
        return new Constante(value);
    }

    protected Constante(double value){
        this(value, ""+value);
    }
    public Constante(double value, String name){
        cst = value;
        this.name = name;
    }

    public double getValue(){
        return cst;
    }

    @Override
    public double f(double x) {
        return cst;
    }

    @Override
    public String getName() {
        return "Constante";
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public Function getDerivative() {
        if(cst==0) {
            return this;
        }
        return ZERO;
    }

    public Function getAntiderivative() {
        return Product.of(this, X.x);
    }

    @Override
    public Function simplified() {
        return this;
    }

    public boolean equals(double value){
        return cst==value;
    }

    public boolean equals(Function f){
        if(f instanceof Constante){
            return cst==((Constante)f).cst;
        }

        //try to simplify f and check if the result is a constante ?

        return false;
    }

}
