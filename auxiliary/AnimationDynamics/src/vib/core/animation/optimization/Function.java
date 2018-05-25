/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.animation.optimization;

import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public interface Function {
    public abstract RealMatrix getFirstDerivative(RealMatrix X);
    public abstract RealMatrix getSecondDerivative(RealMatrix X);
    public abstract RealMatrix f(RealMatrix X);
}
