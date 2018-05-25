/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vib.core.animation.optimization;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * QuasiNewtonSolver is using a new matrix to approximate the NewtonRaphson's Hessian Matrix
 * 
 * @author Huang
 */
public abstract class QuasiNewtonSolver extends FunctionMinimizationSolver{
    RealMatrix _inversesecondH;
    RealMatrix _firstG;
    public QuasiNewtonSolver(Function f) {
        super(f);
    }

    @Override
    public RealMatrix solve() {
        _firstG = _f.getFirstDerivative(_X);
        _inversesecondH = MatrixUtils.createRealIdentityMatrix(_X.getColumnDimension());
        double diff = _firstG.getNorm();
        while(diff > _precision){
            _lamda = computeLamda();
            _inversesecondH = computeApproximatedInverseHessian(_inversesecondH);
            diff = _firstG.getNorm();
            System.out.println(_X.getEntry(0, 0));
        }
        return _X;
    }
    
    protected abstract RealMatrix computeApproximatedInverseHessian(RealMatrix current);
    protected abstract double computeLamda();
    
}
