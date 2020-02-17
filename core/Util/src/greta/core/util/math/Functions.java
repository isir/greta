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
 * contains some usfull functions
 * @author Andre-Marie Pez
 */
public class Functions {

    /**
     * Convert x from the interval [a b] to x' in the interval [c d]
     * @param x original value
     * @param a inferior bound of the original interval
     * @param b supperior bound of the original interval
     * @param c inferior bound of the new interval
     * @param d supperior bound of the new interval
     * @return the new value x'
     */
    public static double changeInterval(double x, double a, double b, double c, double d){
        return a==b ? c : (x-a)/(b-a) * (d-c) + c;
    }
}
