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
