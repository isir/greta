/*
 * This file is part of the auxiliaries of Greta.
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
package greta.core.animation.optimization;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * GradientDescentSolver is using gradientdescent to compute the minimum by going down
 * with a step length in the -tangent direction(the first derivative).
 * @author Huang
 */
public class GradientDescentSolver extends FunctionMinimizationSolver {

    public GradientDescentSolver(Function f) {
        super(f);
        _lamda = 0.01;
    }

    @Override
    public RealMatrix solve() {
        double diff = 1000;
        while (Math.abs(diff) > _precision) {
            //RealMatrix currentY = _f.f(_X);
            RealMatrix r = _f.getFirstDerivative(_X);
            RealMatrix newX = _X.subtract(r.scalarMultiply(_lamda));
            //RealMatrix newY = _f.f(new_X);
            //diff = newY.subtract(currentY).getNorm();
            diff = newX.subtract(_X).getNorm();
            _X = newX;
            //System.out.println(_X.getEntry(0, 0));
        }
        return _X;
    }

}
