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
package greta.mgik.core.animation;

import greta.core.animation.math.Vector3d;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public abstract class IKSolver {

    int m_maxTries;
    double m_targetThreshold;
    double m_stepweight;
    double finalDis;
    boolean m_activeRoot;

    public IKSolver(int maxTries, double targetThreshold) {
        m_maxTries = maxTries;
        m_targetThreshold = targetThreshold;
        m_stepweight = 0.5;
        m_activeRoot = false;
    }

    public abstract void solveOneStep(Skeleton chain, ArrayList<Vector3d> targets);

    public boolean solve(Skeleton chain,  ArrayList<Vector3d> targets){

        chain.update();
        double distance = 0;
        for (int i = 0; i < chain.m_endeffectors.size(); ++i) {
            distance += (targets.get(i).substract( chain.m_joint_globalPositions.get(chain.m_endeffectors.get(i)))).getNorm();
        }

        int tries = 0;
        while (tries < m_maxTries
                && (distance > m_targetThreshold) || tries == 0) {
            ++tries;
            solveOneStep(chain, targets);
            distance = 0;
            for (int i = 0; i < chain.m_endeffectors.size(); ++i) {
                distance += (targets.get(i).substract( chain.m_joint_globalPositions.get(chain.m_endeffectors.get(i)))).getNorm();
            }
        }
        finalDis = distance;
        return true;
    }

    double getDistance() {
        return finalDis;
    }

    double getSingleStepValue() {
        return m_stepweight;
    }

    void setSingleStepValue(double v) {
        this.m_stepweight = v;
    }

    double getTargetThreshold() {
        return m_targetThreshold;
    }

    void setTargetThreshold(double targetThreshold) {
        m_targetThreshold = targetThreshold;
    }

    int getMaxNumberOfTries() {
        return m_maxTries;
    }

    void setMaxNumberOfTries(int tries) {
        m_maxTries = tries;
    }

    double castPiRange(double value) {
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
            /*value -= 3.14159265 * 2;
             if (value < minV){*/
            value = maxV;
            //}
        }
        if (value < minV) {
            /*value += 3.14159265 * 2;
             if (value > maxV) {*/
            value = minV;
            //}
        }
        return value;
    }

    void setRootActive(boolean bo) {
        m_activeRoot = bo;
    }
}
