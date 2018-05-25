/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.animation.optimization;

import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;

/**
 * NewtonRaphsonSolver is using newton tangent line to get close to the lower.
 * -f'(x)/f''(x) direction
 * @author Huang
 */
public class NewtonRaphsonSolver extends FunctionMinimizationSolver {

    public NewtonRaphsonSolver(Function f) {
        super(f);
    }

    @Override
    public RealMatrix solve() {
        double diff = 1000;
        while (Math.abs(diff) > _precision) {
            RealMatrix first = _f.getFirstDerivative(_X);
            RealMatrix second = _f.getSecondDerivative(_X);
            RealMatrix inversesecond = MatrixUtils.inverse(second);
            RealMatrix newX = _X.subtract(inversesecond.multiply(first));
            diff = newX.subtract(_X).getNorm();
            _X = newX;
            //System.out.println(_X.getEntry(0, 0));
        }
        return _X;
    }

}
