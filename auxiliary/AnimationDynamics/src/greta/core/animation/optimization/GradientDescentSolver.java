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
