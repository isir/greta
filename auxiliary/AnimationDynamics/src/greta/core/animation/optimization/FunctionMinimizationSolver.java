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
