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
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class JacobiIterativeMethod {
    public RealMatrix _X;  //output X
    RealMatrix _DInverse;
    RealMatrix _D;
    RealMatrix _R;
    RealMatrix _A;
    RealMatrix _b;

    public JacobiIterativeMethod(RealMatrix A, RealMatrix b) throws Exception {
       _A = A;
       _b = b;
       int l = A.getColumnDimension();
       int h = A.getRowDimension();
      // _D = MatrixUtils.createRealDiagonalMatrix(diagonal);
       for(int i = 0; i < h; ++i){
           for(int j = 0; j < l; ++j){

           }
       }
    }

    public double converge() {
       RealMatrix X = _DInverse.multiply(_b.subtract(_R.multiply(_X)));
       double diff = X.subtract(_X).getNorm();
       _X = X;
        return diff;
    }
}
