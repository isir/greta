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
