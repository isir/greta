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
package greta.core.animation.math;

import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)  jhuang@telecom-paristech.fr
 */
public class Matrix3d extends MatrixNd<Matrix3d> {

    public Matrix3d() {
        super(3,3);
        this.setColumn(0, new double[]{0, 0, 0});
        this.setColumn(1, new double[]{0, 0, 0});
        this.setColumn(2, new double[]{0, 0, 0});
    }

    public Matrix3d(double v1, double v2, double v3,
            double v4, double v5, double v6,
            double v7, double v8, double v9) {
        super(3,3);
        this.setRow(0, new double[]{v1, v2, v3});
        this.setRow(1, new double[]{v4, v5, v6});
        this.setRow(2, new double[]{v7, v8, v9});
    }

    public Matrix3d(RealMatrix m) {
        super(3,3);
        this.setRow(0, m.getRow(0));
        this.setRow(1, m.getRow(1));
        this.setRow(2, m.getRow(2));
    }

    public void set(double v1, double v2, double v3,
            double v4, double v5, double v6,
            double v7, double v8, double v9) {
        this.setRow(0, new double[]{v1, v2, v3});
        this.setRow(1, new double[]{v4, v5, v6});
        this.setRow(2, new double[]{v7, v8, v9});
    }

    public void set(double v[][]) {
        this.setRow(0, new double[]{v[0][0], v[0][1], v[0][2]});
        this.setRow(1, new double[]{v[1][0], v[1][1], v[1][2]});
        this.setRow(2, new double[]{v[2][0], v[2][1], v[2][2]});
    }

    public void toIdentity() {
        this.setRow(0, new double[]{1, 0, 0});
        this.setRow(1, new double[]{0, 1, 0});
        this.setRow(2, new double[]{0, 0, 1});
    }

    public void toMIdentity(double m) {
        this.setRow(0, new double[]{m, 0, 0});
        this.setRow(1, new double[]{0, m, 0});
        this.setRow(2, new double[]{0, 0, m});
    }

    public void toZero() {
        this.setColumn(0, new double[]{0, 0, 0});
        this.setColumn(1, new double[]{0, 0, 0});
        this.setColumn(2, new double[]{0, 0, 0});
    }

    public static Matrix3d zero(){
        return new Matrix3d();
    }

    public static Matrix3d identity(){
        Matrix3d sm = new Matrix3d();
        sm.setEntry(0, 0, 1);
        sm.setEntry(1, 1, 1);
        sm.setEntry(2, 2, 1);
        return sm;
    }

    @Override
    public Matrix3d copyData(RealMatrix arv) {
        return new Matrix3d(arv);
    }

    @Override
    public Matrix3d transpose(){
        return copyData(super.transpose());
    }

    public Vector3d multiple(Vector3d v) {
        return new Vector3d(this.operate(v));
    }

    public static Matrix3d rotX(double angle_rad) {
        double s, c;
        s = Math.sin(angle_rad);
        c = Math.cos(angle_rad);
        return new Matrix3d(
                1., 0., 0.,
                0., c, s,
                0., -s, c
        );
    }

    public static Matrix3d rotY(double angle_rad) {
        double s, c;
        s = Math.sin(angle_rad);
        c = Math.cos(angle_rad);
        return new Matrix3d(
                c, 0., -s,
                0., 1., 0.,
                s, 0., c
        );
    }

    public static Matrix3d rotZ(double angle_rad) {
        double s, c;
        s = Math.sin(angle_rad);
        c = Math.cos(angle_rad);
        return new Matrix3d(
                c, s, 0.,
                -s, c, 0.,
                0., 0., 1.
        );
    }

    public void fromAxisAngle(Vector3d a1, double angle) {
            a1.normalize();
            double c = Math.cos(angle);
            double s = Math.sin(angle);
            double t = 1.0 - c;

            setEntry(0, 0, c + a1.getEntry(0)*a1.getEntry(0)*t);
            setEntry(1, 1, c + a1.getEntry(1)*a1.getEntry(1)*t);
            setEntry(2, 2, c + a1.getEntry(2)*a1.getEntry(2)*t);

            double tmp1 = a1.getEntry(0)*a1.getEntry(1)*t;
            double tmp2 = a1.getEntry(2)*s;
            setEntry(1, 0, tmp1 + tmp2);
            setEntry(0, 1, tmp1 - tmp2);

            tmp1 = a1.getEntry(0)*a1.getEntry(2)*t;
            tmp2 = a1.getEntry(1)*s;
            setEntry(0, 2, tmp1 + tmp2);
            setEntry(2, 0, tmp1 - tmp2);

            tmp1 = a1.getEntry(1)*a1.getEntry(2)*t;
            tmp2 = a1.getEntry(0)*s;
            setEntry(2, 1, tmp1 + tmp2);
            setEntry(1, 2, tmp1 - tmp2);
}

    public static void main(final String[] args){
        double v1 = 0.0;
        double v2 = 0.0;
        double v3 = 0.0;
        double v4 = 0.0;
        double v5 = 0.0;
        double v6 = 0.0;
        double v7 = 0.0;
        double v8 = 0.0;
        double v9 = 0.0;
        Matrix3d m = new Matrix3d();
        m.setColumn(0, new double[]{0, 0, 0});
        m.setColumn(1, new double[]{0, 0, 0});
        m.setColumn(2, new double[]{0, 0, 0});
        System.out.println(m);
    }
}
