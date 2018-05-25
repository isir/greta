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
public interface AbstractSolver {
    public void setInitX(RealMatrix init);
    public RealMatrix solve();
}
