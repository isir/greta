/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vib.core.animation.optimization;

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
