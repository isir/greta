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
import greta.core.util.math.Sum;
import greta.core.util.math.X;

/**
 *
 * @author Jing Huang
 * @author Andre-Marie Pez
 */
public class EaseOutQuad implements Function {

      /**
     * Easing equation function for a quadratic (t^2) easing out: decelerating
     * to zero velocity.
     *
     * @param x	Current time (in frames or seconds).
     * @return	The correct value.
     */
    public double f(double x) {
        return -x * (x - 2);
    }

    public String getName() {
        return "EaseOutQuad";
    }

    @Override
    public Function getDerivative() {
        return Sum.of(Constante.of(2), Product.of(Constante.of(-2), X.x));
    }

    @Override
    public Function simplified() {
        return this;
    }
}
