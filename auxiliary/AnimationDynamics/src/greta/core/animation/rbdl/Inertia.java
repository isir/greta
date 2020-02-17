/*
 * This file is part of the auxiliaries of Greta.
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
package greta.core.animation.rbdl;

import greta.core.animation.math.Matrix3d;
import greta.core.animation.math.Vector3d;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)
 * jhuang@telecom-paristech.fr
 *
 */
public class Inertia {

    Matrix3d _rotationInertia = new Matrix3d();
    double _mass;

    public void setFromBox(double mass, double x, double y, double z) {
        _rotationInertia.setEntry(0, 0, mass / 12.0 * (y * y + z * z));
        _rotationInertia.setEntry(1, 1, mass / 12.0 * (z * z + x * x));
        _rotationInertia.setEntry(2, 2, mass / 12.0 * (x * x + y * y));
        _mass = mass;
    }

    public void setFromSphere(double mass, double radius) {
        _mass = mass;
        double r = 0.4 * mass * radius * radius;
        _rotationInertia.setEntry(0, 0, r);
        _rotationInertia.setEntry(1, 1, r);
        _rotationInertia.setEntry(2, 2, r);
    }

    public void setFromCylinder(double mass, int direction, double radius, double length) {
        double r2 = radius * radius;
        _mass = mass;
        double I = mass * ((0.25) * r2 + ((1.0) / (12.0)) * length * length);
        _rotationInertia.setEntry(0, 0, I);
        _rotationInertia.setEntry(1, 1, I);
        _rotationInertia.setEntry(2, 2, I);
        _rotationInertia.setEntry(direction - 1, direction - 1, mass * (0.5) * r2);

    }

    public void rotate(Matrix3d rotation) {
        _rotationInertia = rotation.multiple(_rotationInertia.multiple(rotation.transpose()));
    }

    public void translate(double x, double y, double z) {
        Matrix3d madd = new Matrix3d(y * y + z * z, x * y, x * z,
                y * x, x * x + z * z, y * z,
                z * x, z * y, x * x + y * y);
        madd.multiple(_mass);
        _rotationInertia.addToSelf(madd);
    }

    public Matrix3d getInertia() {
        return _rotationInertia;
    }

    public void setInertia(Matrix3d inertia) {
        this._rotationInertia = inertia;
    }

    public static Matrix3d generateRotationInertia(double x, double y, double z, double mass, Matrix3d rotation) {
        Matrix3d inertia = new Matrix3d();
        inertia.setEntry(0, 0, mass / 12.0 * (y * y + z * z));
        inertia.setEntry(1, 1, mass / 12.0 * (z * z + x * x));
        inertia.setEntry(2, 2, mass / 12.0 * (x * x + y * y));
        inertia = rotation.multiple(inertia.multiple(rotation.transpose()));
        return inertia;
    }

    public static Matrix3d generateRotationInertia(double x, double y, double z, double mass) {
        Matrix3d inertia = new Matrix3d();
        inertia.setEntry(0, 0, mass / 12.0 * (y * y + z * z));
        inertia.setEntry(1, 1, mass / 12.0 * (z * z + x * x));
        inertia.setEntry(2, 2, mass / 12.0 * (x * x + y * y));
        //inertia = rotation.multiple(inertia.multiple(rotation.transpose()));
        return inertia;
    }

    public static Matrix3d generateRotationInertia(Matrix3d inertia, Matrix3d rotation) {
        return rotation.multiple(inertia.multiple(rotation.transpose()));
    }

    public static Matrix3d generateRotationInertia(double x, double y, double z, double mass, Vector3d dir, Vector3d dirOriginal) {
        dir = dir.divide(dir.getNorm());
        dirOriginal = dirOriginal.divide(dirOriginal.getNorm());
        Matrix3d rotation = new Matrix3d();
        rotation.setEntry(0, 0, dirOriginal.dotProduct(dir));
        rotation.setEntry(0, 1, -dirOriginal.cross(dir).getNorm());
        rotation.setEntry(1, 0, dirOriginal.cross(dir).getNorm());
        rotation.setEntry(1, 1, dirOriginal.dotProduct(dir));
        rotation.setEntry(2, 2, 1);

        Matrix3d inertia = new Matrix3d();
        inertia.setEntry(0, 0, mass / 12.0 * (y * y + z * z));
        inertia.setEntry(1, 1, mass / 12.0 * (z * z + x * x));
        inertia.setEntry(2, 2, mass / 12.0 * (x * x + y * y));
        inertia = rotation.multiple(inertia.multiple(rotation.transpose()));
        return inertia;
    }
}
