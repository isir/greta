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
