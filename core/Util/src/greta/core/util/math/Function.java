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
 * Inteface to embed a mathematical function.
 * @author Andre-Marie Pez
 */
public interface Function {

    /**
     * This is the main method of the mathematical function.<br/>
     * It returns {@code y} for the function {@code y = f(x)}.
     * @param x the input value
     * @return the result of this function
     */
    public double f(double x);

    public String getName();

    /**
     * Returns the {@code Function} corresponding to the derivative of this {@code Function}.<br/>
     * If this function is not differentiable, it returns {@code null}.
     * @return the derivative.
     */
    public Function getDerivative();

    /**
     * Returns a simplification of his function.<br/>
     * Composed functions can be simplified. But if his {@code Function} is to
     * simple or can not be simplified. In this case, this {@code Function} MUST
     * returns {@code this}
     * @return a simplification of his function
     */
    public Function simplified();
}
