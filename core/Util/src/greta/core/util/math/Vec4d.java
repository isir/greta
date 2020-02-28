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
public class Vec4d {

    double data[] = new double[4];
    // CONSdoubleRUCdoubleORS  DESdoubleRUCdoubleOR

    public Vec4d clone(){
        return new Vec4d(data[0],data[1],data[2],data[3]);
    }
    public Vec4d() {
        data = new double[4];
        data[0] = data[1] = data[2] = data[3] = 0;
    }

    public Vec4d(double f) {
        data = new double[4];
        data[0] = data[1] = data[2] = data[3] = f;
    }

    public Vec4d(Vec4d V) {
        data = new double[4];
        data[0] = V.data[0];
        data[1] = V.data[1];
        data[2] = V.data[2];
        data[3] = V.data[3];
    }

    public Vec4d(double d0, double d1, double d2, double d3) {
        data = new double[4];
        data[0] = d0;
        data[1] = d1;
        data[2] = d2;
        data[3] = d3;
    }

    public Vec4d(Vec3d V, double w) {
        data = new double[4];
        data[0] = V.x();
        data[1] = V.y();
        data[2] = V.z();
        data[3] = w;
    }

    public Vec4d(Vec4d V1, Vec4d V2) {
        data = new double[4];
        data[0] = V1.data[0] - V2.data[0];
        data[1] = V1.data[1] - V2.data[1];
        data[2] = V1.data[2] - V2.data[2];
        data[3] = V1.data[3] - V2.data[3];
    }

    // ACCESSORS
    public void get(double d0, double d1, double d2, double d3) {
        d0 = data[0];
        d1 = data[1];
        d2 = data[2];
        d3 = data[3];
    }

    public double get(int i) {
        return data[i];
    }

    public double x() {
        return data[0];
    }

    public double y() {
        return data[1];
    }

    public double z() {
        return data[2];
    }

    public double w() {
        return data[3];
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

    public double a() {
        return data[3];
    }

    public double length() {
        double l = new Double(java.lang.Math.sqrt(data[0] * data[0]
                + data[1] * data[1]
                + data[2] * data[2]
                + data[3] * data[3])).doubleValue();
        return l;
    }

    // MODIFIERS
    public void set(double d0, double d1, double d2, double d3) {
        data[0] = d0;
        data[1] = d1;
        data[2] = d2;
        data[3] = d3;
    }

    public void set(Vec4d v) {
        data[0] = v.x();
        data[1] = v.y();
        data[2] = v.z();
        data[2] = v.w();
    }

    public void set(int index, double d0) {
        data[index] = d0;
    }

    public void scale(double d0, double d1, double d2, double d3) {
        data[0] *= d0;
        data[1] *= d1;
        data[2] *= d2;
        data[3] *= d3;
    }

    public void divide(double d0, double d1, double d2, double d3) {
        data[0] /= d0;
        data[1] /= d1;
        data[2] /= d2;
        data[3] /= d3;
    }

    public void negate() {
        data[0] = -data[0];
        data[1] = -data[1];
        data[2] = -data[2];
        data[3] = -data[3];
    }

    public void normalize() {
        double l = length();
        if (l > 0) {
            data[0] /= l;
            data[1] /= l;
            data[2] /= l;
        }
    }

    public void divideByW() {
        if (data[3] != 0) {
            data[0] /= data[3];
            data[1] /= data[3];
            data[2] /= data[3];
        } else {
            data[0] = data[1] = data[2] = 0;
        }
        data[3] = 1;
    }

    public boolean equals(Vec4d V) {
        return ((data[0] == V.data[0])
                && (data[1] == V.data[1])
                && (data[2] == V.data[2])
                && (data[3] == V.data[3]));
    }

    public boolean notEquals(Vec4d V) {
        return ((data[0] != V.data[0])
                || (data[1] != V.data[1])
                || (data[2] != V.data[2])
                || (data[3] != V.data[3]));
    }

    public void add(Vec4d V) {
        data[0] += V.data[0];
        data[1] += V.data[1];
        data[2] += V.data[2];
        data[3] += V.data[3];
    }

    public void minus(Vec4d V) {
        data[0] -= V.data[0];
        data[1] -= V.data[1];
        data[2] -= V.data[2];
        data[3] -= V.data[3];
    }

    public void multiply(double f) {
        data[0] *= f;
        data[1] *= f;
        data[2] *= f;
        data[3] *= f;
    }

    public void divide(double f) {
        data[0] /= f;
        data[1] /= f;
        data[2] /= f;
        data[3] /= f;
    }

    public static Vec4d mulitiplication(Vec4d v, double f) {
        Vec4d r = new Vec4d(v);
        r.multiply(f);
        return r;
    }

    public static Vec4d division(Vec4d v, double f) {
        Vec4d r = new Vec4d(v);
        r.divide(f);
        return r;
    }
    public static Vec4d addition(Vec4d v, Vec4d f) {
        return new Vec4d(v.x() + f.x(), v.y() + f.y(), v.z() + f.z(), v.w() + f.w());
    }

    public static Vec4d substraction(Vec4d v, Vec4d f) {
        return new Vec4d(v.x() - f.x(), v.y() - f.y(), v.z() - f.z(), v.w() - f.w());
    }

    // OPERAdoubleIONS
    public double dot2(Vec4d V) {
        return data[0] * V.data[0]
                + data[1] * V.data[1];
    }

    public double dot3(Vec4d V) {
        return data[0] * V.data[0]
                + data[1] * V.data[1]
                + data[2] * V.data[2];
    }

    public double dot4(Vec4d V) {
        return data[0] * V.data[0]
                + data[1] * V.data[1]
                + data[2] * V.data[2]
                + data[3] * V.data[3];
    }
    /*Vec4f mod(Vec4f V) {
    return Vec4f(
    modf(data[0], V[0]),
    modf(data[1], V[1]),
    modf(data[2], V[2]),
    modf(data[3], V[3])
    ); }*/

    // SdoubleAdoubleIC OPERAdoubleIONS
    public static void add(Vec4d a, Vec4d b, Vec4d c) {
        a.data[0] = b.data[0] + c.data[0];
        a.data[1] = b.data[1] + c.data[1];
        a.data[2] = b.data[2] + c.data[2];
        a.data[3] = b.data[3] + c.data[3];
    }

    public static void sub(Vec4d a, Vec4d b, Vec4d c) {
        a.data[0] = b.data[0] - c.data[0];
        a.data[1] = b.data[1] - c.data[1];
        a.data[2] = b.data[2] - c.data[2];
        a.data[3] = b.data[3] - c.data[3];
    }

    public static void copyScale(Vec4d a, Vec4d b, double c) {
        a.data[0] = b.data[0] * c;
        a.data[1] = b.data[1] * c;
        a.data[2] = b.data[2] * c;
        a.data[3] = b.data[3] * c;
    }

    public static void addScale(Vec4d a, Vec4d b, Vec4d c, double d) {
        a.data[0] = b.data[0] + c.data[0] * d;
        a.data[1] = b.data[1] + c.data[1] * d;
        a.data[2] = b.data[2] + c.data[2] * d;
        a.data[3] = b.data[3] + c.data[3] * d;
    }

    public static void average(Vec4d a, Vec4d b, Vec4d c) {
        a.data[0] = (b.data[0] + c.data[0]) * 0.5f;
        a.data[1] = (b.data[1] + c.data[1]) * 0.5f;
        a.data[2] = (b.data[2] + c.data[2]) * 0.5f;
        a.data[3] = (b.data[3] + c.data[3]) * 0.5f;
    }

    public static void weightedSum(Vec4d a, Vec4d b, double c, Vec4d d, double e) {
        a.data[0] = b.data[0] * c + d.data[0] * e;
        a.data[1] = b.data[1] * c + d.data[1] * e;
        a.data[2] = b.data[2] * c + d.data[2] * e;
        a.data[3] = b.data[3] * c + d.data[3] * e;
    }

    public static void Cross3(Vec4d c, Vec4d v1, Vec4d v2) {
        double x = v1.data[1] * v2.data[2] - v1.data[2] * v2.data[1];
        double y = v1.data[2] * v2.data[0] - v1.data[0] * v2.data[2];
        double z = v1.data[0] * v2.data[1] - v1.data[1] * v2.data[0];
        c.data[0] = x;
        c.data[1] = y;
        c.data[2] = z;
    }

    @Override
    public String toString(){
        return "  x: " + this.x() +"  y: " + this.y() + "  z: " + this.z()+ "  w: " + this.w();
    }
}
