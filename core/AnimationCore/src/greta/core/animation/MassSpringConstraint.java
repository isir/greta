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
package greta.core.animation;

import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class MassSpringConstraint extends MassConstraint {

    double _stiffness = 0.3f;
    double _damping = 0.1f;
    double _releasedLength = 0;
    Mass _obj = null;
    Mass _dragpoint = null;

    public MassSpringConstraint(Mass obj, Mass dragpoint, double releasedLength) {
        _obj = obj;
        _dragpoint = dragpoint;
        _releasedLength = releasedLength;
        this._name = obj._name + "_" + dragpoint._name;
    }

    public MassSpringConstraint(Mass obj, Mass dragpoint) {
        _obj = obj;
        _dragpoint = dragpoint;
        _releasedLength = Vec3d.substraction(dragpoint.getPosition(), obj.getPosition()).length();
        this._name = obj._name + "_" + dragpoint._name;
    }

    public MassSpringConstraint(MassSpringConstraint mc) {
        this(mc._obj, mc._dragpoint, mc._releasedLength);
        this.setDamping(mc._damping);
        this.setStiffness(mc._stiffness);
        this._name = mc._name;
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
        return Vec3d.substraction(_dragpoint.getPosition(), _obj.getPosition()).normalized();
    }

    @Override
    public double getForceEnergy() {
        Vec3d vect = Vec3d.substraction(_dragpoint.getPosition(), _obj.getPosition());
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
