/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.IK;

import vib.core.animation.common.Joint;
import vib.core.animation.common.Target;
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
