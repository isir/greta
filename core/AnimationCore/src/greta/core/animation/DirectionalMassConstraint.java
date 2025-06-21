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
public class DirectionalMassConstraint extends MassConstraint {

    private Vec3d _direction = new Vec3d();
    private double _pwr = 0;
    int _max_active_loop = 30;
    int _active_loop= 0;

    public DirectionalMassConstraint(Vec3d dir, double pwr) {
        _direction.set(dir.x(), dir.y(), dir.z());
        _pwr = pwr;
        this._name = dir.toString();
    }

    public DirectionalMassConstraint(DirectionalMassConstraint mc) {
        this(mc._direction, mc._pwr);
        this._name = mc._direction.toString();
    }

    public Vec3d getDirection() {
        return _direction;
    }

    public void setDirection(Vec3d direction) {
        this._direction = direction;
    }

    public double getPower() {
        return _pwr;
    }

    public void setPower(double pwr) {
        this._pwr = pwr;
    }

    @Override
    public Vec3d getForce() {
        if(_active_loop > _max_active_loop){
            setActive(false);
        }
        if(_active){
            _active_loop++;
            return new Vec3d(Vec3d.multiplication(getForceDirection(), _pwr));
        }
        return new Vec3d(0,0,0);
    }

    @Override
    public Vec3d getForceDirection() {
        return _direction.normalized();
    }

    @Override
    public double getForceEnergy() {
        return _pwr;
    }

}
