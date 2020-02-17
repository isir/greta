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
public class Matrix3d {

    private double[][] data = new double[3][3];

    @Override
    public Matrix3d clone() {
        return new Matrix3d(this);
    }

    public Matrix3d() {
        clear();
    }

    public Matrix3d(Matrix3d m) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                data[y][x] = m.data[y][x];
            }
        }
    }

    public Matrix3d(double[][] m) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                data[y][x] = m[y][x];
            }
        }
    }

    public Matrix3d(double m0, double m1, double m2, double m3, double m4, double m5, double m6, double m7, double m8) {
        data[0][0] = m0;
        data[0][1] = m1;
        data[0][2] = m2;

        data[1][0] = m3;
        data[1][1] = m4;
        data[1][2] = m5;

        data[2][0] = m6;
        data[2][1] = m7;
        data[2][2] = m8;
    }

    public void clear() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                data[y][x] = 0;
            }
        }
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

    public void set(Matrix3d m) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                data[y][x] = m.data[y][x];
            }
        }
    }
    public double get(int i) {
        return data[i / 3][i % 3];
    }

    public void setToIdentity() {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                data[y][x] = (x == y ? 1 : 0);
            }
        }
    }

    public void transpose(Matrix3d m) {
        // be careful, <this> might be <m>
        Matrix3d tmp = new Matrix3d(this);
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                m.data[y][x] = tmp.data[x][y];
            }
        }
    }

    public void transpose() {
        transpose(this);
    }

    public boolean equals(Matrix3d m) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                if (data[y][x] != m.data[y][x]) {
                    return false;
                }
            }
        }
        return true;
    }

    public static Matrix3d addition(Matrix3d m1, Matrix3d m2) {
        Matrix3d answer = new Matrix3d();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                answer.data[y][x] = m1.data[y][x] + m2.data[y][x];
            }
        }
        return answer;
    }

    public static Matrix3d substraction(Matrix3d m1, Matrix3d m2) {
        Matrix3d answer = new Matrix3d();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                answer.data[y][x] = m1.data[y][x] - m2.data[y][x];
            }
        }
        return answer;
    }

    public static Matrix3d multiplication(Matrix3d m1, Matrix3d m2) {
        Matrix3d answer = new Matrix3d();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int i = 0; i < 3; i++) {
                    answer.data[y][x] += m1.data[y][i] * m2.data[i][x];
                }
            }
        }
        return answer;
    }

    public static Matrix3d multiplication(Matrix3d m, double f) {
        Matrix3d answer = new Matrix3d();
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                answer.data[y][x] = m.data[y][x] * f;
            }
        }
        return answer;
    }

    public static Vec3d multiplication(Matrix3d m1, Vec3d v) {
        Vec3d answer = new Vec3d();

        /*
         * for (int y = 0; y < 3; y++) { answer.set(y, m1.data[y][0] * v.get(0)
         * + m1.data[y][1] * v.get(1) + m1.data[y][2] * v.get(2));
        }
         */
        //opengl
        for (int y = 0; y < 3; y++) {
            answer.set(y, m1.data[y][0] * v.get(0) + m1.data[y][1] * v.get(1) + m1.data[y][2] * v.get(2));
        }

        return answer;
    }
}
