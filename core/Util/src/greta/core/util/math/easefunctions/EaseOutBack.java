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
 * Easing equation function for a back (overshooting cubic easing:
 * <code>(s+1)*t^3 - s*t^2</code>)<br/>
 * easing out: decelerating from zero velocity.
 *
 * @author Jing Huang
 * @author Andre-Marie Pez
 */
public class EaseOutBack implements Function {

    /**
     * Overshoot ammount: higher s means greater overshoot (0 produces cubic
     * easing with no overshoot, and the default value of 1.70158 produces an
     * overshoot of 10 percent).
     */
    double s = 0.20158;

    @Override
    public double f(double x) {
        x -= 1.0;
        return x * x * ((s + 1) * x + s) + 1;
    }

    @Override
    public String getName() {
        return "EaseOutBack";
    }

    @Override
    public Function getDerivative() {
        return Sum.of(Constante.of(s + 3), Product.of(Constante.of(-6 - 4 * s), X.x), Product.of(Constante.of(3 * s + 3), X.x, X.x));
    }

    @Override
    public Function simplified() {
        return this;
    }
}
