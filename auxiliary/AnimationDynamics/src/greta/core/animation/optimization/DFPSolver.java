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
 * Davidon–Fletcher–Powell formula of quasinewton
 *
 * @author Huang
 */
public class DFPSolver extends QuasiNewtonSolver {

    public DFPSolver(Function f) {
        super(f);
    }

    @Override
    protected RealMatrix computeApproximatedInverseHessian(RealMatrix current) {
        RealMatrix dx = current.multiply(_firstG).scalarMultiply(-_lamda);
        RealMatrix xk1 = _X.add(dx);
        RealMatrix firstGxk1 = _f.getFirstDerivative(xk1);
        RealMatrix dxT = dx.transpose();
        RealMatrix yk = firstGxk1.subtract(_firstG);
        RealMatrix ykT = yk.transpose();

        RealMatrix secondTerm = dx.multiply(dxT).multiply(MatrixUtils.inverse(ykT.multiply(dx)));
        RealMatrix thirdTerm = current.multiply(yk).multiply(ykT).multiply(current.transpose()).multiply(MatrixUtils.inverse(ykT.multiply(current).multiply(yk)));

        _firstG = firstGxk1;
        _X = xk1;
        return current.add(secondTerm).subtract(thirdTerm);
    }

    @Override
    protected double computeLamda() {
        return 1;
    }

}
