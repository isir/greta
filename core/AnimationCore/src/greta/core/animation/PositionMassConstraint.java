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
package greta.core.animation;

import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class PositionMassConstraint extends MassConstraint {

    double _stiffness = 0.3f;
    double _damping = 0.1f;
    double _releasedLength = 0;
    Mass _obj = null;
    Vec3d _dragpoint = null;

    public Vec3d getDragpoint() {
        return _dragpoint;
    }

    public void setDragpoint(Vec3d dragpoint) {
        this._dragpoint = dragpoint;
    }

    public PositionMassConstraint(Mass obj, Vec3d dragpoint, double releasedLength) {
        _obj = obj;
        _dragpoint = dragpoint;
        _releasedLength = releasedLength;
        this._name = obj._name + "_" + dragpoint;
    }

    public PositionMassConstraint(Mass obj, Vec3d dragpoint) {
        _obj = obj;
        _dragpoint = dragpoint;
        _releasedLength = Vec3d.substraction(dragpoint, obj.getPosition()).length();
        this._name = obj._name + "_" + dragpoint;
    }

    public PositionMassConstraint(PositionMassConstraint mc) {
        this(mc._obj, mc._dragpoint, mc._releasedLength);
        this.setDamping(mc._damping);
        this.setStiffness(mc._stiffness);
        this._name = mc._obj._name + "_" + mc._dragpoint;
    }

    public double getDamping() {
        return _damping;
    }

    public void setDamping(double damping) {
        this._damping = damping;
    }

    public double getReleasedLength() {
        return _releasedLength;
    }

    public void setReleasedLength(double releasedLength) {
        this._releasedLength = releasedLength;
    }

    public double getStiffness() {
        return _stiffness;
    }

    public void setStiffness(double stiffness) {
        this._stiffness = stiffness;
    }

    @Override
    public Vec3d getForce() {
        double energy = getForceEnergy();
        Vec3d ret = Vec3d.multiplication(getForceDirection(), energy);
        return ret;
    }

    @Override
    public Vec3d getForceDirection() {
        Vec3d dir = Vec3d.substraction(_dragpoint, _obj.getPosition());
        if(dir.length() < 0.1f) {
            return new Vec3d();
        }
        return dir.normalized();
    }

    @Override
    public double getForceEnergy() {
        Vec3d vect = Vec3d.substraction(_dragpoint, _obj.getPosition());
        double lengthPos = vect.length();
        double dif = lengthPos - _releasedLength;
        if (Math.abs(dif) < 0.01f) {
            return 0;
        }
        double Felastic = _stiffness * dif;
        //double Felastic = _stiffness * dif / _releasedLength;  //need to check
        return Felastic * (1 - _damping);
    }

}
