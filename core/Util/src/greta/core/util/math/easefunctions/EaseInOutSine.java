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
package greta.core.util.math.easefunctions;

import greta.core.util.math.Constante;
import greta.core.util.math.Function;
import greta.core.util.math.Product;
import greta.core.util.math.Sin;
import greta.core.util.math.X;

/**
 *
 * @author Jing Huang
 * @author Andre-Marie Pez
 */
public class EaseInOutSine implements Function {

    /**
     * Easing equation function for a sinusoidal (sin(t)) easing in/out:
     * acceleration until halfway, then deceleration.
     *
     * @param x	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public double f(double x) {
        return (-0.5 * (Math.cos(Math.PI * x) - 1));
    }

    public String getName() {
        return "EaseInOutSine";
    }

    @Override
    public String toString() {
        return "(cos(PI*x)-1)/2";
    }


    @Override
    public Function getDerivative() {
        return Product.of(Constante.of(Math.PI/2), Sin.of(Product.of(Constante.PI,X.x)));
    }

    @Override
    public Function simplified() {
        return this;
    }
}
