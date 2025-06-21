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
