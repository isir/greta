/*
 * This file is part of Greta.
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
package greta.core.animation.common.IK;

import greta.core.animation.common.Joint;
import greta.core.animation.common.Target;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public abstract class IKSolver {

    //protected LinkedList<Joint> _links = new LinkedList<Joint>();
    protected int _maxTries = 50;
    protected double _targetTreshold = 0.5f;

    public abstract String getIKSolverName();

    public IKSolver() {

    }
    public IKSolver(int maxTries, double targetTreshold) {
        _maxTries = maxTries;
        _targetTreshold = targetTreshold;
    }

    public abstract boolean compute(ArrayList<Joint> list, Target t, boolean b);
    public abstract boolean computeWithPriority(ArrayList<Joint> list, Target t, boolean b, int i);
    public void setupChain(ArrayList<Joint> list) {

    }

    public double getTargetTreshold() {
        return _targetTreshold;
    }

    public void setTargetTreshold(double targetTreshold) {
        _targetTreshold = targetTreshold;
    }

    public int getMaxNumberOfTries() {
        return _maxTries;
    }

    public void setMaxNumberOfTries(int tries) {
        _maxTries = tries;
    }


}
