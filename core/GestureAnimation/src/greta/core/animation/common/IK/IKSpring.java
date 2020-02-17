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

import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class IKSpring {

    Vec3d _nodesInternalForces[] = new Vec3d[2];
    int _nodes_handles[] = new int[2];
    double _stiffness = 0.3f;
    double _damping = 0.1f;
    double _releasedLength = 0;
    double _currentInternalForce = 0;
    boolean _actived = true;
    IKMassSpring _pSystem;

    public IKSpring(IKMassSpring system, int node1_handle, int node2_handle, double stiffness, double damping) {
        _pSystem = system;
        _stiffness = stiffness;
        _damping = damping;
        _nodes_handles[0] = node1_handle;
        _nodes_handles[1] = node2_handle;
        setEquilibriumLengthByNodesDistance();
    }

    public IKSpring(IKMassSpring system, int node1_handle, int node2_handle) {
        _pSystem = system;
        _nodes_handles[0] = node1_handle;
        _nodes_handles[1] = node2_handle;
        setEquilibriumLengthByNodesDistance();
    }

    public IKMass getMass1() {
        return _pSystem.getMass(_nodes_handles[0]);
    }

    public IKMass getMass2() {
        return _pSystem.getMass(_nodes_handles[1]);
    }

    public int getMass1Handle() {
        return _nodes_handles[0];
    }

    public int getMass2Handle() {
        return _nodes_handles[1];
    }

    public void setEquilibriumLength(double length) {
        _releasedLength = length;
        if (length < 0) {
            setEquilibriumLengthByNodesDistance();
        }
    }

    public void setEquilibriumLengthByNodesDistance() {
        Vec3d vect = Vec3d.substraction(_pSystem.getMass(_nodes_handles[0]).getPosition(), _pSystem.getMass(_nodes_handles[1]).getPosition());
        double lengthPos = vect.length();
        setEquilibriumLength(lengthPos);
    }

    public void setDamping(double damping) {
        _damping = damping;
    }

    public void setStiffness(double stiffness) {
        _stiffness = stiffness;
    }

    public void computerInternalForce(IKMass node1, IKMass node2) {
        if (!_actived) {
            return;
        }
        Vec3d vect = Vec3d.substraction(node1.getPosition(), node2.getPosition());
        double lengthPos = vect.length();
        double dif = lengthPos - _releasedLength;
        double Felastic = -_stiffness * dif;
        _currentInternalForce = Felastic * (1 - _damping);
        vect.normalize();
        _nodesInternalForces[0] = Vec3d.multiplication(vect,_currentInternalForce);
        node1.addForce(_nodesInternalForces[0]);
        _nodesInternalForces[1] = Vec3d.multiplication(vect, _currentInternalForce).opposite();
        node2.addForce(_nodesInternalForces[1]);
    }

    public void computerInternalForce() {
        if (!_actived) {
            return;
        }

        IKMass node1 = _pSystem.getMass(_nodes_handles[0]);
        IKMass node2 = _pSystem.getMass(_nodes_handles[1]);
        computerInternalForce(node1, node2);
    }

    public boolean constraitActived(double differenceLength) {
        return false;
    }

    public Vec3d getNode1InternalForce() {
        return _nodesInternalForces[0];
    }

    public Vec3d getNode2InternalForce() {
        return _nodesInternalForces[1];
    }

    public double getEquilibriumLength() {
        return _releasedLength;
    }

    public double getStiffness() {
        return _stiffness;
    }

    public double getDamping() {
        return _damping;
    }
}
