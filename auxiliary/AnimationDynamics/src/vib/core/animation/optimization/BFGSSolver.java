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
 * royden–Fletcher–Goldfarb–Shanno algorithm of quasinewton
 * @author Huang
 */
public class BFGSSolver extends QuasiNewtonSolver{

    public BFGSSolver(Function f) {
        super(f);
    }

    @Override
    protected RealMatrix computeApproximatedInverseHessian(RealMatrix current) {
        RealMatrix dx = current.multiply(_firstG).scalarMultiply(-_lamda);
        RealMatrix xk1 = _X.add(dx);
        RealMatrix firstGxk1 = _f.getFirstDerivative(xk1);
        RealMatrix dxT = dx.transpose();
        RealMatrix yk = firstGxk1.subtract(_firstG);
        RealMatrix ykT = yk.transpose();

        RealMatrix firstTerm = yk.multiply(dxT).multiply(MatrixUtils.inverse(ykT.multiply(dx)));
        firstTerm = MatrixUtils.createRealIdentityMatrix(firstTerm.getRowDimension()).subtract(firstTerm);
        RealMatrix secondTerm = dx.multiply(dxT).multiply(MatrixUtils.inverse(ykT.multiply(dx)));
        
        _firstG = firstGxk1;
        _X = xk1;
        return firstTerm.transpose().multiply(current).multiply(firstTerm).add(secondTerm);
    }

    @Override
    protected double computeLamda() {
        return 1;
    }
    
}
