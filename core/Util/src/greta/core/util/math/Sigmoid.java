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
public class Sigmoid implements Function{

    Constante lambda;
    Function sig;

    public Sigmoid(){
        this(1);
    }

    public Sigmoid(double lambda){
        this.lambda = new Constante(lambda, "\u03bb");
        sig = Inverse.of(Sum.of(Constante.ONE, Exp.of(Product.of(Constante.of(-1),this.lambda,X.x))));
    }

    @Override
    public double f(double x) {
        return sig.f(x);
    }

    @Override
    public String getName() {
        return "Sigmoid";
    }

    @Override
    public String toString() {
        return sig.toString();
    }

    @Override
    public Function getDerivative() {
        return Product.of(lambda, this, Sum.of(Constante.ONE, Product.of(Constante.of(-1), this)));
    }

    @Override
    public Function simplified() {
        return this;
    }

}
