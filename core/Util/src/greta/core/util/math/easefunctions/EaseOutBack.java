/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
