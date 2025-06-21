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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *   LMS least median squares algorithm
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class LinearRegression {

    public RealMatrix _Y;  //getOutputData y0, y1,...yn
    public RealMatrix _X;  //getInputData x0, x1,...xn
    //public RealMatrix _para;
    public LinearFunction _function = new LinearFunction();
    public double _dirlength = 0.1;

    public double _precsion = 1;
    public int _maxStep = 100;

    public LinearRegression(double[] Y, double[][] X, double[] paraInit, double step) throws Exception {
        if (Y.length != X.length || X[0].length != paraInit.length) {
            throw new Exception() {
                @Override
                public String toString() {
                    return "data length is not correct";
                }
            };
        }
        _Y = new Array2DRowRealMatrix(Y);
        _X = new Array2DRowRealMatrix(X);
        _function._para = new Array2DRowRealMatrix(paraInit);
        _dirlength = step;
    }

    public double converge() {
        RealMatrix computedY = _function.multiply(_X);
        RealMatrix errors = _Y.subtract(computedY);
        RealMatrix dj = errors.multiply(_X);
        dj.scalarMultiply(-2 / _X.getRowDimension());
        _function._para.subtract(dj.scalarMultiply(-_dirlength));
        return errors.getNorm();
    }

    /***
     *
     * @return parameters of the function by iteration
     */
    public RealMatrix solve(){
        int i = 0;
        double error = 1000;
        while(i < _maxStep && error < _precsion){
            error = converge();
        }
        return _function._para;
    }

    public double getDirlength() {
        return _dirlength;
    }

    public void setDirlength(double dirlength) {
        this._dirlength = dirlength;
    }

    public double getPrecsion() {
        return _precsion;
    }

    public void setPrecsion(double precsion) {
        this._precsion = precsion;
    }

    public int getMaxStep() {
        return _maxStep;
    }

    public void setMaxStep(int maxStep) {
        this._maxStep = maxStep;
    }


}
