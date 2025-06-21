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
 *
 * @author Huang
 */
public abstract class FunctionMinimizationSolver implements AbstractSolver {
    public Function _f;
    public RealMatrix _X;
    public double _lamda = 1;
    public double _precision = 0.000001;

    public FunctionMinimizationSolver(Function f) {
        _f = f;
    }

     @Override
    public void setInitX(RealMatrix init) {
        _X = init;
    }

    public RealMatrix getX() {
        return _X;
    }

    public double getDirlength() {
        return _lamda;
    }

    public void setDirlength(double _dirlength) {
        this._lamda = _dirlength;
    }

    public double getPrecision() {
        return _precision;
    }

    public void setPrecision(double precision) {
        this._precision = precision;
    }

    @Override
    public abstract RealMatrix solve();
}
