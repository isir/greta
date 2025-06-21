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
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class LinearFunction implements Function {

    public RealMatrix _para;

    public RealMatrix multiply(RealMatrix X){
        return _para.multiply(X.transpose());
    }

    @Override
    public RealMatrix getFirstDerivative(RealMatrix X) {
        return _para;
    }

    @Override
    public RealMatrix getSecondDerivative(RealMatrix X) {
        RealMatrix second = new Array2DRowRealMatrix(_para.getRowDimension(), _para.getColumnDimension());
        return second;
    }

    @Override
    public RealMatrix f(RealMatrix X) {
        return multiply(X);
    }

}
