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
package greta.core.util.math;

/**
 *
 * @author Andre-Marie Pez
 */
public final class X implements Function{

    public static final X x = new X();

    private X(){}

    @Override
    public double f(double x) {
        return x;
    }

    @Override
    public String getName() {
        return "Identity";
    }

    @Override
    public String toString() {
        return "x";
    }

    @Override
    public Function getDerivative() {
        return Constante.ONE;
    }

    public Function getAntiderivative() {
        return Product.of(this, this);
    }

    @Override
    public Function simplified() {
        return this;
    }

}
