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
package greta.core.util.math;

/**
 *
 * @author Jing Huang
 */
public class Vec3d {

    @Override
    public Vec3d clone() {
        return new Vec3d(data[0], data[1], data[2]);
    }
    private double data[] = new double[3];

    public Vec3d() {
        data = new double[3];
        data[0] = data[1] = data[2] = 0;
    }

    public Vec3d(double f) {
        data = new double[3];
        data[0] = data[1] = data[2] = f;
    }

    public Vec3d(Vec3d V) {
        data = new double[3];
        data[0] = V.data[0];
        data[1] = V.data[1];
        data[2] = V.data[2];
    }

    public Vec3d(double d0, double d1, double d2) {
        data = new double[3];
        data[0] = d0;
        data[1] = d1;
        data[2] = d2;
    }

    public Vec3d(Vec3d V1, Vec3d V2) {
        data = new double[3];
        data[0] = V1.data[0] - V2.data[0];
        data[1] = V1.data[1] - V2.data[1];
        data[2] = V1.data[2] - V2.data[2];
    }

    public double get(int i) {
        return data[i];
    }

    public double x() {
        return data[0];
    }

    public void setX(double v) {
        data[0] = v;
    }

    public double y() {
        return data[1];
    }

    public void setY(double v) {
        data[1] = v;
    }

    public double z() {
        return data[2];
    }

    public void setZ(double v) {
        data[2] = v;
    }

    public double r() {
        return data[0];
    }

    public double g() {
        return data[1];
    }

    public double b() {
        return data[2];
    }

    public double length() {
        double l = new Double(java.lang.Math.sqrt(data[0] * data[0]
                + data[1] * data[1]
                + data[2] * data[2])).doubleValue();
        return l;
    }

    // MODIFIERS
    public void set(double d0, double d1, double d2) {
        data[0] = d0;
        data[1] = d1;
        data[2] = d2;
    }

    public void set(Vec3d v) {
        data[0] = v.x();
        data[1] = v.y();
        data[2] = v.z();
    }

    public void set(int index, double d0) {
        data[index] = d0;
    }

    public void scale(double d0, double d1, double d2) {
        data[0] *= d0;
        data[1] *= d1;
        data[2] *= d2;
    }

    public void divide(double d0, double d1, double d2) {
        data[0] /= d0;
        data[1] /= d1;
        data[2] /= d2;
    }

    public void normalize() {
        double l = length();
        if (l > 0) {
            data[0] /= l;
            data[1] /= l;
            data[2] /= l;
        }
    }

    public Vec3d normalized() {
        Vec3d v = new Vec3d(this);
        double l = length();
        if (l > 0) {
            v.data[0] /= l;
            v.data[1] /= l;
            v.data[2] /= l;
        }
        return v;
    }

    public void negate() {
        data[0] = -data[0];
        data[1] = -data[1];
        data[2] = -data[2];
    }

    public boolean equals(Vec3d V) {
        return ((data[0] == V.data[0])
                && (data[1] == V.data[1])
                && (data[2] == V.data[2]));
    }

    public boolean notEquals(Vec3d V) {
        return ((data[0] != V.data[0])
                || (data[1] != V.data[1])
                || (data[2] != V.data[2]));
    }

    public void add(Vec3d V) {
        data[0] += V.data[0];
        data[1] += V.data[1];
        data[2] += V.data[2];
    }

    public void minus(Vec3d V) {
        data[0] -= V.data[0];
        data[1] -= V.data[1];
        data[2] -= V.data[2];
    }

    public void multiply(double i) {
        data[0] = (data[0] * i);
        data[1] = (data[1] * i);
        data[2] = (data[2] * i);
    }

    public void divide(double i) {
        data[0] = (data[0] / i);
        data[1] = (data[1] / i);
        data[2] = (data[2] / i);
    }

    public Vec3d opposite() {
        return new Vec3d(-data[0], -data[1], -data[2]);
    }

    public double multiply(Vec3d V) {
        return data[0] * V.data[0]
                + data[1] * V.data[1]
                + data[2] * V.data[2];
    }

    // OPERATIONS
    public double dot3(Vec3d V) {
        return data[0] * V.data[0]
                + data[1] * V.data[1]
                + data[2] * V.data[2];
    }

    public Vec3d cross3(Vec3d v2) {
        Vec3d c = new Vec3d();
        double x = data[1] * v2.data[2] - data[2] * v2.data[1];
        double y = data[2] * v2.data[0] - data[0] * v2.data[2];
        double z = data[0] * v2.data[1] - data[1] * v2.data[0];
        c.data[0] = x;
        c.data[1] = y;
        c.data[2] = z;
        return c;
    }

    public static Vec3d cross3(Vec3d v1, Vec3d v2) {
        Vec3d c = new Vec3d();
        double x = v1.data[1] * v2.data[2] - v1.data[2] * v2.data[1];
        double y = v1.data[2] * v2.data[0] - v1.data[0] * v2.data[2];
        double z = v1.data[0] * v2.data[1] - v1.data[1] * v2.data[0];
        c.data[0] = x;
        c.data[1] = y;
        c.data[2] = z;
        return c;
    }

    public static Vec3d multiplicationOfComponents(Vec3d v1, Vec3d v2) {
        Vec3d c = new Vec3d();
        double x = v1.data[0] * v2.data[0];
        double y = v1.data[1] * v2.data[1];
        double z = v1.data[2] * v2.data[2];
        c.data[0] = x;
        c.data[1] = y;
        c.data[2] = z;
        return c;
    }

    public static Vec3d divisionOfComponents(Vec3d v1, Vec3d v2) {
        Vec3d c = new Vec3d();
        double x = v1.data[0] / v2.data[0];
        double y = v1.data[1] / v2.data[1];
        double z = v1.data[2] / v2.data[2];
        c.data[0] = x;
        c.data[1] = y;
        c.data[2] = z;
        return c;
    }

    public static Vec3d addition(Vec3d a, Vec3d b) {
        return new Vec3d(a.data[0] + b.data[0], a.data[1] + b.data[1], a.data[2] + b.data[2]);
    }

    public static Vec3d substraction(Vec3d a, Vec3d b) {
        return new Vec3d(a.data[0] - b.data[0], a.data[1] - b.data[1], a.data[2] - b.data[2]);
    }

    public static Vec3d division(Vec3d v, double f) {
        Vec3d r = new Vec3d(v);
        r.divide(f);
        return r;
    }

    public static Vec3d multiplication(Vec3d v, double f) {
        Vec3d r = new Vec3d(v);
        r.multiply(f);
        return r;
    }

    public static Vec3d interpolation(Vec3d v1, Vec3d v2, double t) {
        Vec3d v = Vec3d.addition(v1, Vec3d.multiplication(Vec3d.substraction(v2, v1), t));
        return v;
    }

    @Override
    public String toString() {
        return "  x: " + this.x() + "  y: " + this.y() + "  z: " + this.z();
    }

    @Override
    public boolean equals(Object object) {
        if (object instanceof Vec3d) {
            Vec3d vect = (Vec3d) object;
            return data[0] == vect.x() && data[1] == vect.y() && data[2] == vect.z();
        }
        return false;
    }

    public Vec3d orthogonalVec() {
        // Find smallest component. Keep equal case for null values.
        if ((Math.abs(data[1]) >= 0.9 * Math.abs(data[0])) && (Math.abs(data[2]) >= 0.9 * Math.abs(data[0]))) {
            return new Vec3d(0.0f, -data[2], data[1]);
        } else if ((Math.abs(data[0]) >= 0.9 * Math.abs(data[1])) && (Math.abs(data[2]) >= 0.9 * Math.abs(data[1]))) {
            return new Vec3d(-data[2], 0.0f, data[0]);
        } else {
            return new Vec3d(-data[1], data[0], 0.0f);
        }
    }
}
