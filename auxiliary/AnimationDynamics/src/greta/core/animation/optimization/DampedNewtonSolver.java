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

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Huang
 */
public class DampedNewtonSolver extends NewtonRaphsonSolver {
    public DampedNewtonSolver(Function f) {
        super(f);
    }

    @Override
    public RealMatrix solve() {
        double diff = 1000;
        while (Math.abs(diff) > _precision) {
            RealMatrix first = _f.getFirstDerivative(_X);
            RealMatrix second = _f.getSecondDerivative(_X);
            RealMatrix inversesecond = MatrixUtils.inverse(second);
            _lamda = computeLamda();
            RealMatrix newX = _X.subtract(inversesecond.scalarMultiply(_lamda).multiply(first));
            diff = newX.subtract(_X).getNorm();
            _X = newX;
            //System.out.println(_X.getEntry(0, 0));
        }
        return _X;
    }

    /***
     * todo: not know how to solve the lamda
     */
    protected double computeLamda() {
        return 1;
    }
}
