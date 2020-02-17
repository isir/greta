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
package greta.core.animation.body;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class ExpressiveTorso {

    Arm _left;
    Arm _right;
    Vec3d _lastcenter;
    Vec3d _currentcenter;
    Quaternion _expR = new Quaternion();
    boolean _active = true;
    double _factor = 0.5;

    public double getExpressiveFactor() {
        return _factor;
    }

    public void setExpressiveFactor(double factor) {
        this._factor = factor;
    }

    public boolean isActive() {
        return _active;
    }

    public void setActive(boolean active) {
        this._active = active;
    }

    public ExpressiveTorso() {
        _left = null;
        _right = null;
        _lastcenter = null;
        _currentcenter = null;
    }

    public Quaternion getRotation() {
        if (_active != true) {
            return new Quaternion();
        }
        return new Quaternion(_expR);
    }

    public void compute(double intensity) {
        if (_active != true) {
            _expR = new Quaternion();
            return;
        }
        double weight = 0;
        _currentcenter = new Vec3d(0, 0, 0);
        double pwr = 0;
        if (_left != null) {
            Vec3d t = _left.getTarget();
            weight++;
            _currentcenter = Vec3d.addition(t, _currentcenter);
            if (_left.getExpressivityParameters() != null) {
                pwr += _left.getExpressivityParameters().pwr;
            }
        }

        if (_right != null) {
            Vec3d t = _right.getTarget();
            //System.out.println(t);
            weight++;
            _currentcenter = Vec3d.addition(t, _currentcenter);
            if (_right.getExpressivityParameters() != null) {
                pwr += _right.getExpressivityParameters().pwr;
            }
        }

        _currentcenter = Vec3d.multiplication(_currentcenter, 0.5f);
        if (_lastcenter == null) {
            _lastcenter = _currentcenter;
            _expR = new Quaternion();
            return;
        }
        if (weight > 0) {
            pwr /= weight;
        }

        Vec3d dif = Vec3d.substraction(_currentcenter, _lastcenter);
        Quaternion q0P = new Quaternion();
        double angleRB = -(double) (pwr * dif.y() / (dif.length()+1) * 0.3 * _factor);
        q0P.setAxisAngle(new Vec3d(1, 0, 0), angleRB);

        Quaternion q0 = new Quaternion();
        //selfrotation
        Vec3d original = new Vec3d(0, 0, 1);

        double angFinal = (double) ((double) pwr * dif.x() / (dif.length()+1) * 0.3f * _factor);
        //System.out.println(angFinal+ "  "+pwr);
        q0.setAxisAngle(new Vec3d(0, 1, 0), (double) (angFinal ));
        _expR = Quaternion.multiplication(q0, q0P);
        _expR.multiply((double)(intensity));
        //System.out.println(_expR);
        //_expR = new Quaternion();
        _lastcenter = _currentcenter;
    }

    public Arm getLeft() {
        return _left;
    }

    public void setArms(Arm left, Arm right) {
        this._left = left;
        this._right = right;
    }

    public Arm getRight() {
        return _right;
    }
}
