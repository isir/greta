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
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class Mass {

    public static double DamplingFactor = 0.2f;
    String _name = "";
    double _mass = 1;
    Vec3d _position;
    Vec3d _oldPosition;
    Vec3d _acceleration = new Vec3d();
    boolean _movable = true;
    ArrayList<MassConstraint> _internalConstraintList = new ArrayList<MassConstraint>();
    //ArrayList<MassConstraint> _externalConstraintList = new ArrayList<MassConstraint>();

    public Mass(boolean movable) {
        _movable = movable;
    }

    public Mass(String name) {
        _name = name;
    }

    public Mass(Mass mass) {
        _movable = mass._movable;
        _mass = mass._mass;
        _position = new Vec3d(mass._position);
        _oldPosition = new Vec3d(mass._oldPosition);
        _acceleration = new Vec3d(mass._acceleration);
        _name = mass._name;
        _internalConstraintList = mass.getInternalConstraintList();
    }

    public Mass(Vec3d position, double mass, boolean movable) {
        _position = position;
        _oldPosition = position;
        _mass = mass;
        _movable = movable;
    }

    public int findInternalConstraint(String name) {
        for (MassConstraint mc : _internalConstraintList) {
            if (mc._name.equalsIgnoreCase(name)) {
                return _internalConstraintList.indexOf(mc);
            }
        }
        return -1;
    }

    public void addInternalConstraint(MassConstraint mc) {
        int i = findInternalConstraint(mc._name);
        if (i < 0) {
            _internalConstraintList.add(mc);
        } else {
            _internalConstraintList.set(i, mc);
        }
    }

    public void removeInternalConstraintByIndex(int i) {
        if (_internalConstraintList.size() < i || i < 0) {
            return;
        }
        _internalConstraintList.remove(i);
    }

    public ArrayList<MassConstraint> getInternalConstraintList() {
        return _internalConstraintList;
    }

    public void setInternalConstraintList(ArrayList<MassConstraint> constraintList) {
        this._internalConstraintList = constraintList;
    }

    public void addInternalConstraintList(ArrayList<MassConstraint> constraintList) {
        for (MassConstraint mc : constraintList) {
            addInternalConstraint(mc);
        }
    }

    public void removeInternalConstraint(String name) {
        int i = findInternalConstraint(name);
        if (i < 0) {
            return;
        }
        _internalConstraintList.remove(i);
    }


/*
    public int findExternalConstraint(String name) {
        for (MassConstraint mc : _externalConstraintList) {
            if (mc._name.equalsIgnoreCase(name)) {
                return _externalConstraintList.indexOf(mc);
            }
        }
        return -1;
    }

    public void addExternalConstraint(MassConstraint mc) {
        int i = findExternalConstraint(mc._name);
        if (i < 0) {
            _externalConstraintList.add(mc);
        } else {
            _externalConstraintList.set(i, mc);
        }
    }

    public void removeExternalConstraintByIndex(int i) {
        if (_externalConstraintList.size() < i || i < 0) {
            return;
        }
        _externalConstraintList.remove(i);
    }

    public ArrayList<MassConstraint> getExternalConstraintList() {
        return _externalConstraintList;
    }

    public void setExternalConstraintList(ArrayList<MassConstraint> constraintList) {
        this._externalConstraintList = constraintList;
    }

    public void addExternalConstraintList(ArrayList<MassConstraint> constraintList) {
        for (MassConstraint mc : constraintList) {
            addExternalConstraint(mc);
        }
    }

    public void removeExternalConstraint(String name) {
        int i = findExternalConstraint(name);
        if (i < 0) {
            return;
        }
        _externalConstraintList.remove(i);
    }*/

    public String getName() {
        return _name;
    }

    public void setName(String name) {
        this._name = name;
    }

    public void setMass(double mass) {
        _mass = mass;
    }

    public double getMass() {
        return _mass;
    }

    public void setPosition(Vec3d position) {
        _position = position;
        _oldPosition = position;
    }

    public Vec3d getPosition() {
        return _position;
    }

    public void setMovable(boolean movable) {
        _movable = movable;
    }

    public boolean isMovable() {
        return _movable;
    }

    public void addForce(Vec3d force) {
        //System.out.println(force + "  " + _mass);
        _acceleration.add(Vec3d.division(force, _mass));
    }

    public Vec3d getAcceleration() {
        //System.out.println(_acceleration);
        return _acceleration;
    }

    public void resetAcceleration() {
        _acceleration = new Vec3d(0);
    }

    //verlet intergration      if do not put into .h file, they can not use for Cuda, or need .cu file
    public void move(double time, double dampling) {
        if (!_movable) {
            _acceleration.set(0, 0, 0);
            return;
        }
        Vec3d temp = _position;
        _position = Vec3d.addition(_position,
                Vec3d.multiplication(
                Vec3d.addition(Vec3d.substraction(_position, _oldPosition), Vec3d.multiplication(_acceleration, time)), ((double) (1.0 - dampling))));
        _oldPosition = temp;
    }

    public void move() {
        if (!_movable) {
            _acceleration.set(0, 0, 0);
            return;
        }
        Vec3d temp = _position;
        _position = Vec3d.addition(_position,
                Vec3d.multiplication(
                Vec3d.addition(Vec3d.substraction(_position, _oldPosition), Vec3d.multiplication(_acceleration, 0.25f)), ((double) (1.0 - DamplingFactor))));

        _oldPosition = temp;
    }

    public void applyConstraints() {
        //double internal = 0;
        for (MassConstraint mc : _internalConstraintList) {
            if (mc != null) {
                addForce(mc.getForce());
            }
        }
    }
}
