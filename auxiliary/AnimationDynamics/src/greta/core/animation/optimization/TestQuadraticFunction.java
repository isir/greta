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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Huang
 */
public class TestQuadraticFunction implements Function{
    //y = 4x^2 +16x+  16
    @Override
    public RealMatrix getFirstDerivative(RealMatrix X) {
        double y = 8 * X.getEntry(0, 0) + 16;
        RealMatrix v = new Array2DRowRealMatrix(1, 1);
        v.setEntry(0, 0, y);
        return v;
    }

    @Override
    public RealMatrix getSecondDerivative(RealMatrix X) {
        RealMatrix v = new Array2DRowRealMatrix(1, 1);
        v.setEntry(0, 0, 8);
        return v;
    }

    @Override
    public RealMatrix f(RealMatrix X) {
        double x = X.getEntry(0, 0);
        double pr = x * x;
        double y = 4 * pr + 16 * x  + 16;
        RealMatrix v = new Array2DRowRealMatrix(1, 1);
        v.setEntry(0, 0, y);
        return v;
    }

}
