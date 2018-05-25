/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package vib.core.animation.optimization;

import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Huang
 */
public abstract class FunctionMinimizationSolver implements AbstractSolver {
    public Function _f;
    public RealMatrix _X;
    public double _lamda = 1;
    public double _precision = 0.000001;
    
    public FunctionMinimizationSolver(Function f) {
        _f = f;
    }
    
     @Override
    public void setInitX(RealMatrix init) {
        _X = init;
    }

    public RealMatrix getX() {
        return _X;
    }
    
    public double getDirlength() {
        return _lamda;
    }

    public void setDirlength(double _dirlength) {
        this._lamda = _dirlength;
    }

    public double getPrecision() {
        return _precision;
    }

    public void setPrecision(double precision) {
        this._precision = precision;
    }

    @Override
    public abstract RealMatrix solve();
}
