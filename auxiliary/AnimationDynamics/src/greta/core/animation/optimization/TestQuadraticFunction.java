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
