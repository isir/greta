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
