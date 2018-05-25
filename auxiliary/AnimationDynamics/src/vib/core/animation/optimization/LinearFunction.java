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
