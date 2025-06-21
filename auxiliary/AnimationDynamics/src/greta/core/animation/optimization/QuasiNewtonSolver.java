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
