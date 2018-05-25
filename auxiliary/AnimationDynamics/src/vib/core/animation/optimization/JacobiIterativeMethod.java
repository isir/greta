/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.animation.optimization;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
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
