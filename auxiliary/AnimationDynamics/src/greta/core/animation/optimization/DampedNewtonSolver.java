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
