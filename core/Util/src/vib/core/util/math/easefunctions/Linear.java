/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.math.easefunctions;

import vib.core.util.math.Function;

/**
 * Easing equation function for a simple linear tweening, with no easing.
 * @author Jing Huang
 * @author Andre-Marie Pez
 * It is the same as vib.core.util.math.X class
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
        return vib.core.util.math.Constante.ONE;
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
