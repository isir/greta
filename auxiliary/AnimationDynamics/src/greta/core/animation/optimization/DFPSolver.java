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
