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
 * QuasiNewtonSolver is using a new matrix to approximate the NewtonRaphson's Hessian Matrix
 *
 * @author Huang
 */
public abstract class QuasiNewtonSolver extends FunctionMinimizationSolver{
    RealMatrix _inversesecondH;
    RealMatrix _firstG;
    public QuasiNewtonSolver(Function f) {
        super(f);
    }

    @Override
    public RealMatrix solve() {
        _firstG = _f.getFirstDerivative(_X);
        _inversesecondH = MatrixUtils.createRealIdentityMatrix(_X.getColumnDimension());
        double diff = _firstG.getNorm();
        while(diff > _precision){
            _lamda = computeLamda();
            _inversesecondH = computeApproximatedInverseHessian(_inversesecondH);
            diff = _firstG.getNorm();
            System.out.println(_X.getEntry(0, 0));
        }
        return _X;
    }

    protected abstract RealMatrix computeApproximatedInverseHessian(RealMatrix current);
    protected abstract double computeLamda();

}
