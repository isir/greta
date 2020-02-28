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
public class Matrix4d {

    private double[][] data = new double[4][4];

    double det4x4(double a1, double a2, double a3, double a4,
            double b1, double b2, double b3, double b4,
            double c1, double c2, double c3, double c4,
            double d1, double d2, double d3, double d4) {
        return a1 * det3x3(b2, b3, b4, c2, c3, c4, d2, d3, d4)
                - b1 * det3x3(a2, a3, a4, c2, c3, c4, d2, d3, d4)
                + c1 * det3x3(a2, a3, a4, b2, b3, b4, d2, d3, d4)
                - d1 * det3x3(a2, a3, a4, b2, b3, b4, c2, c3, c4);
    }

    double det3x3(double a1, double a2, double a3,
            double b1, double b2, double b3,
            double c1, double c2, double c3) {
        return a1 * det2x2(b2, b3, c2, c3)
                - b1 * det2x2(a2, a3, c2, c3)
                + c1 * det2x2(a2, a3, b2, b3);
    }

    double det2x2(double a, double b,
            double c, double d) {
        return a * d - b * c;
    }

    @Override
    public Matrix4d clone() {
        return new Matrix4d(this);
    }

    public Matrix4d() {
        clear();
    }

    public Matrix4d(Matrix4d m) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                data[y][x] = m.data[y][x];
            }
        }
    }

    public void copy(Matrix4d m) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                data[y][x] = m.data[y][x];
            }
        }
    }

    public Matrix4d(double[][] m) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                data[y][x] = m[y][x];
            }
        }
    }

    public Matrix4d(double[] m) {
        for (int u = 0; u < 16; u++) {
            data[u / 4][u % 4] = m[u];

        }
    }

    public Matrix4d(double m0, double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8, double m9, double m10, double m11, double m12, double m13, double m14, double m15) {
        data[0][0] = m0;
        data[0][1] = m1;
        data[0][2] = m2;
        data[0][3] = m3;
        data[1][0] = m4;
        data[1][1] = m5;
        data[1][2] = m6;
        data[1][3] = m7;
        data[2][0] = m8;
        data[2][1] = m9;
        data[2][2] = m10;
        data[2][3] = m11;
        data[3][0] = m12;
        data[3][1] = m13;
        data[3][2] = m14;
        data[3][3] = m15;

    }

    public void clear() {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                data[y][x] = 0;
            }
        }
    }

    public double[] getData1D() {
        return new double[]{
            data[0][0],
            data[0][1],
            data[0][2],
            data[0][3],
            data[1][0],
            data[1][1],
            data[1][2],
            data[1][3],
            data[2][0],
            data[2][1],
            data[2][2],
            data[2][3],
            data[3][0],
            data[3][1],
            data[3][2],
            data[3][3],};
    }

    public float[] getData1DFloat() {
        return new float[]{
            (float) data[0][0],
            (float) data[0][1],
            (float) data[0][2],
            (float) data[0][3],
            (float) data[1][0],
            (float) data[1][1],
            (float) data[1][2],
            (float) data[1][3],
            (float) data[2][0],
            (float) data[2][1],
            (float) data[2][2],
            (float) data[2][3],
            (float) data[3][0],
            (float) data[3][1],
            (float) data[3][2],
            (float) data[3][3],};
    }

    public double[][] getData() {
        return data;
    }

    public double get(int x, int y) {
        return data[y][x];
    }

    public void set(int x, int y, double v) {
        data[y][x] = v;
    }

    public void set(Matrix4d m) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                data[y][x] = m.data[y][x];
            }
        }
    }

    public double get(int i) {
        return data[i / 4][i % 4];
    }

    public void setToIdentity() {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                data[y][x] = (x == y ? 1 : 0);
            }
        }
    }

    public void transpose(Matrix4d m) {
        // be careful, <this> might be <m>
        Matrix4d tmp = new Matrix4d(this);
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                m.data[y][x] = tmp.data[x][y];
            }
        }
    }

    public void transpose() {
        transpose(this);
    }

    public boolean inverse(Matrix4d m) {
        double epsilon = (double) 1e-08;
        m.copy(this);
        double a1, a2, a3, a4, b1, b2, b3, b4, c1, c2, c3, c4, d1, d2, d3, d4;
        a1 = m.data[0][0];
        b1 = m.data[0][1];
        c1 = m.data[0][2];
        d1 = m.data[0][3];
        a2 = m.data[1][0];
        b2 = m.data[1][1];
        c2 = m.data[1][2];
        d2 = m.data[1][3];
        a3 = m.data[2][0];
        b3 = m.data[2][1];
        c3 = m.data[2][2];
        d3 = m.data[2][3];
        a4 = m.data[3][0];
        b4 = m.data[3][1];
        c4 = m.data[3][2];
        d4 = m.data[3][3];

        double det = det4x4(a1, a2, a3, a4, b1, b2, b3, b4, c1, c2, c3, c4, d1, d2, d3, d4);

        if (java.lang.Math.abs(det) < epsilon) {
            System.out.println("Matrix4::Inverse --- singular matrix, can't invert!\n");

            return false;
        }

        m.data[0][0] = det3x3(b2, b3, b4, c2, c3, c4, d2, d3, d4);
        m.data[1][0] = -det3x3(a2, a3, a4, c2, c3, c4, d2, d3, d4);
        m.data[2][0] = det3x3(a2, a3, a4, b2, b3, b4, d2, d3, d4);
        m.data[3][0] = -det3x3(a2, a3, a4, b2, b3, b4, c2, c3, c4);

        m.data[0][1] = -det3x3(b1, b3, b4, c1, c3, c4, d1, d3, d4);
        m.data[1][1] = det3x3(a1, a3, a4, c1, c3, c4, d1, d3, d4);
        m.data[2][1] = -det3x3(a1, a3, a4, b1, b3, b4, d1, d3, d4);
        m.data[3][1] = det3x3(a1, a3, a4, b1, b3, b4, c1, c3, c4);

        m.data[0][2] = det3x3(b1, b2, b4, c1, c2, c4, d1, d2, d4);
        m.data[1][2] = -det3x3(a1, a2, a4, c1, c2, c4, d1, d2, d4);
        m.data[2][2] = det3x3(a1, a2, a4, b1, b2, b4, d1, d2, d4);
        m.data[3][2] = -det3x3(a1, a2, a4, b1, b2, b4, c1, c2, c4);

        m.data[0][3] = -det3x3(b1, b2, b3, c1, c2, c3, d1, d2, d3);
        m.data[1][3] = det3x3(a1, a2, a3, c1, c2, c3, d1, d2, d3);
        m.data[2][3] = -det3x3(a1, a2, a3, b1, b2, b3, d1, d2, d3);
        m.data[3][3] = det3x3(a1, a2, a3, b1, b2, b3, c1, c2, c3);

        m.multiplication(1.0f / det);

        /* double fA0 = m.get(0, 0) * m.get(1, 1) - m.get(0, 1) * m.get(1, 0);
         double fA1 = m.get(0, 0) * m.get(1, 2) - m.get(0, 2) * m.get(1, 0);
         double fA2 = m.get(0, 0) * m.get(1, 3) - m.get(0, 3) * m.get(1, 0);
         double fA3 = m.get(0, 1) * m.get(1, 2) - m.get(0, 2) * m.get(1, 1);
         double fA4 = m.get(0, 1) * m.get(1, 3) - m.get(0, 3) * m.get(1, 1);
         double fA5 = m.get(0, 2) * m.get(1, 3) - m.get(0, 3) * m.get(1, 2);
         double fB0 = m.get(2, 0) * m.get(3, 1) - m.get(2, 1) * m.get(3, 0);
         double fB1 = m.get(2, 0) * m.get(3, 2) - m.get(2, 2) * m.get(3, 0);
         double fB2 = m.get(2, 0) * m.get(3, 3) - m.get(2, 3) * m.get(3, 0);
         double fB3 = m.get(2, 1) * m.get(3, 2) - m.get(2, 2) * m.get(3, 1);
         double fB4 = m.get(2, 1) * m.get(3, 3) - m.get(2, 3) * m.get(3, 1);
         double fB5 = m.get(2, 2) * m.get(3, 3) - m.get(2, 3) * m.get(3, 2);
         double fDet = fA0 * fB5 - fA1 * fB4 + fA2 * fB3 + fA3 * fB2 - fA4 * fB1 + fA5 * fB0;


         if (Math.abs(fDet) <= epsilon) {
         System.out.println(m + " matrix cant inverse ");
         return false;
         }

         m.data[0][0] = +m.get(1, 1) * fB5 - m.get(1, 2) * fB4 + m.get(1, 3) * fB3;
         m.data[1][0] = -m.get(1, 0) * fB5 + m.get(1, 2) * fB2 - m.get(1, 3) * fB1;
         m.data[2][0] = +m.get(1, 0) * fB4 - m.get(1, 1) * fB2 + m.get(1, 3) * fB0;
         m.data[3][0] = -m.get(1, 0) * fB3 + m.get(1, 1) * fB1 - m.get(1, 2) * fB0;
         m.data[0][1] = -m.get(0, 1) * fB5 + m.get(0, 2) * fB4 - m.get(0, 3) * fB3;
         m.data[1][1] = +m.get(0, 0) * fB5 - m.get(0, 2) * fB2 + m.get(0, 3) * fB1;
         m.data[2][1] = -m.get(0, 0) * fB4 + m.get(0, 1) * fB2 - m.get(0, 3) * fB0;
         m.data[3][1] = +m.get(0, 0) * fB3 - m.get(0, 1) * fB1 + m.get(0, 2) * fB0;
         m.data[0][2] = +m.get(3, 1) * fA5 - m.get(3, 2) * fA4 + m.get(3, 3) * fA3;
         m.data[1][2] = -m.get(3, 0) * fA5 + m.get(3, 2) * fA2 - m.get(3, 3) * fA1;
         m.data[2][2] = +m.get(3, 0) * fA4 - m.get(3, 1) * fA2 + m.get(3, 3) * fA0;
         m.data[3][2] = -m.get(3, 0) * fA3 + m.get(3, 1) * fA1 - m.get(3, 2) * fA0;
         m.data[0][3] = -m.get(2, 1) * fA5 + m.get(2, 2) * fA4 - m.get(2, 3) * fA3;
         m.data[1][3] = +m.get(2, 0) * fA5 - m.get(2, 2) * fA2 + m.get(2, 3) * fA1;
         m.data[2][3] = -m.get(2, 0) * fA4 + m.get(2, 1) * fA2 - m.get(2, 3) * fA0;
         m.data[3][3] = +m.get(2, 0) * fA3 - m.get(2, 1) * fA1 + m.get(2, 2) * fA0;

         m = multiplication(m, 1.0f / fDet);*/
        return true;
    }

    public boolean inverse() {
        return inverse(this);
    }

    public boolean equals(Matrix4d m) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                if (data[y][x] != m.data[y][x]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Matrix4d addition(Matrix4d m1, Matrix4d m2) {
        Matrix4d answer = new Matrix4d();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                answer.data[y][x] = m1.data[y][x] + m2.data[y][x];
            }
        }
        return answer;
    }

    public static Matrix4d substraction(Matrix4d m1, Matrix4d m2) {
        Matrix4d answer = new Matrix4d();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                answer.data[y][x] = m1.data[y][x] - m2.data[y][x];
            }
        }
        return answer;
    }

    public static Matrix4d multiplication(Matrix4d m1, Matrix4d m2) {
        Matrix4d answer = new Matrix4d();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                for (int i = 0; i < 4; i++) {
                    answer.data[y][x] += m1.data[y][i] * m2.data[i][x];
                }
            }
        }
        return answer;
    }

    public static Matrix4d multiplication(Matrix4d m, double f) {
        Matrix4d answer = new Matrix4d();
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                answer.data[y][x] = m.data[y][x] * f;
            }
        }
        return answer;
    }

    public static Vec4d multiplication(Matrix4d m1, Vec4d v) {
        Vec4d answer = new Vec4d();
        for (int y = 0; y < 4; y++) {
            answer.set(y, m1.data[y][0] * v.get(0) + m1.data[y][1] * v.get(1) + m1.data[y][2] * v.get(2) + m1.data[y][3] * v.get(3));
        }
        return answer;
    }

    public void multiplication(double f) {
        for (int y = 0; y < 4; y++) {
            for (int x = 0; x < 4; x++) {
                this.data[y][x] = this.data[y][x] * f;
            }
        }
    }
}
