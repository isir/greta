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
