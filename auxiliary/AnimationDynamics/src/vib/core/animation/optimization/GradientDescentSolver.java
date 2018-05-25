/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.animation.optimization;

import org.apache.commons.math3.linear.RealMatrix;

/**
 * GradientDescentSolver is using gradientdescent to compute the minimum by going down 
 * with a step length in the -tangent direction(the first derivative).
 * @author Huang
 */
public class GradientDescentSolver extends FunctionMinimizationSolver {
    
    public GradientDescentSolver(Function f) {
        super(f);
        _lamda = 0.01;
    }

    @Override
    public RealMatrix solve() {
        double diff = 1000;
        while (Math.abs(diff) > _precision) {
            //RealMatrix currentY = _f.f(_X);
            RealMatrix r = _f.getFirstDerivative(_X);
            RealMatrix newX = _X.subtract(r.scalarMultiply(_lamda));
            //RealMatrix newY = _f.f(new_X);
            //diff = newY.subtract(currentY).getNorm();
            diff = newX.subtract(_X).getNorm();
            _X = newX;
            //System.out.println(_X.getEntry(0, 0));
        }
        return _X;
    }
 
}
