/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.animation.kinematics;

import greta.core.animation.math.Vector3d;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public abstract class IKSolver {

    int m_maxTries = 300;
    double m_targetThreshold = 0.001;
    double m_stepweight = 0.5;
    IKChain m_chain;
    public abstract String getIKSolverName();

    public IKSolver(int maxTries, double targetThreshold, double stepweight) {
        m_maxTries = maxTries;
        m_targetThreshold = targetThreshold;
        m_stepweight = stepweight;
    }

    public IKSolver(IKChain chain){
        m_chain = chain;
    }

    public abstract boolean solve(Vector3d target);

    public double getSingleStepValue() {
        return m_stepweight;
    }

    public void setSingleStepValue(double v) {
        this.m_stepweight = v;
    }

    public double getTargetThreshold() {
        return m_targetThreshold;
    }

    public void setTargetThreshold(double targetThreshold) {
        this.m_targetThreshold = targetThreshold;
    }

    public int getMaxNumberOfTries() {
        return m_maxTries;
    }

    public void setMaxNumberOfTries(int tries) {
        this.m_maxTries = tries;
    }

    public double castPiRange(double value) {
        while (value > 3.14159265) {
            value -= 3.14159265 * 2;
        }
        while (value < -3.14159265) {
            value += 3.14159265 * 2;
        }
        return value;
    }

    double clamp(double value, double minV, double maxV) {
        if (value > maxV) {
            value -= 3.14159265 * 2;
            if (value < minV){
                value = maxV;
            }
        }
        if (value < minV) {
            value += 3.14159265 * 2;
            if (value > maxV) {
                value = minV;
            }
        }
        return value;
    }
}
