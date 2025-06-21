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
