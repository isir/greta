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
