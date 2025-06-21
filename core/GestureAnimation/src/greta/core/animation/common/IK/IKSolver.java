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
