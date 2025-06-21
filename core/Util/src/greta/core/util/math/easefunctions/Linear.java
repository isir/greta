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

import greta.core.util.math.Function;

/**
 * Easing equation function for a simple linear tweening, with no easing.
 * @author Jing Huang
 * @author Andre-Marie Pez
 * It is the same as greta.core.util.math.X class
 */
public class Linear implements Function{

    public double f(double x) {
        return x;
    }

    public String getName() {
        return "Linear";
    }

    @Override
    public Function getDerivative() {
        return greta.core.util.math.Constante.ONE;
    }

    @Override
    public String toString() {
        return "x";
    }

    @Override
    public Function simplified() {
        return this;
    }

}
