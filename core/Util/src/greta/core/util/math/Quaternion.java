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
public class Quaternion {


    public static void main(final String[] args) {
        Quaternion q = new Quaternion();
        q.setAxisAngle(new Vec3d(1,0.5,-0.3), 1);
        q.normalize();
        Matrix3d m = q.getRotationMatrix();

        Vec3d v = new Vec3d(3,9,-10);

        Vec3d out1 = Matrix3d.multiplication(m, v);
        Vec3d out2 = Quaternion.multiplication(q, v);

        System.out.println(out1 +" " + out2);
    }

    private double q[] = new double[4];

    @Override
    public Quaternion clone() {
        return new Quaternion(this);
    }

    public Quaternion(double q0, double q1, double q2, double q3) {
        q = new double[4];
        q[0] = q0;
        q[1] = q1;
        q[2] = q2;
        q[3] = q3;
    }

    public Quaternion() {
        this(0, 0, 0, 1);
    }

    public Quaternion(Vec3d axis, double angle) {
        q = new double[4];
        setAxisAngle(axis, angle);
    }


    /*
     * ! Copy ructor.
     */
    public Quaternion(Quaternion Q) {
        this(Q.q[0],Q.q[1],Q.q[2],Q.q[3]);
    }

    public Quaternion(Vec3d from, Vec3d to) {
        double epsilon = 1E-10f;

        double fromSqNorm = from.length();
        fromSqNorm = fromSqNorm * fromSqNorm;
        double toSqNorm = to.length();
        toSqNorm = toSqNorm * toSqNorm;
        // Identity Quaternion when one vector is null
        if ((fromSqNorm < epsilon) || (toSqNorm < epsilon)) {
            q[0] = q[1] = q[2] = 0.0f;
            q[3] = 1.0f;
        } else {
            Vec3d axis = from.cross3(to);
            double axisSqNorm = axis.length();
            axisSqNorm = axisSqNorm * axisSqNorm;
            // Aligned vectors, pick any axis, not aligned with from or to
            if (axisSqNorm < epsilon) {
                //if(Vec3f.addition(from.normalized(), to.normalized()).length() < 1){
                //setAxisAngle(axis, Math.PI);
                axis = from.orthogonalVec();
                //}
                //return;
            }

            double angle = Math.asin(Math.sqrt(axisSqNorm / (fromSqNorm * toSqNorm)));
            //double va = from.dot3(to.normalized());
            //angle = Math.acos(va);
            if (from.dot3(to.normalized()) < 0.0) {
                angle = Math.PI - angle;
            }
            setAxisAngle(axis, angle);
        }
    }

    public boolean equalsTo(Quaternion qi) {
        if (q[0] == qi.x() && q[1] == qi.y() && q[2] == qi.z() && q[3] == qi.w()) {
            return true;
        }
        return false;
    }
    /*
     * ! Sets the Quaternion as a rotation of axis \p axis and angle \p angle
     * (in radians). \p axis does not need to be normalized. A null \p axis will
     * result in an identity Quaternion.
     */

    public void setAxisAngle(Vec3d axis, double angle) {
        double norm = axis.length();
        if (norm < 1E-8) {
            // Null rotation
            q[0] = 0;
            q[1] = 0;
            q[2] = 0;
            q[3] = 1;
        } else {
            double sin_half_angle = java.lang.Math.sin(angle / 2.0);
            q[0] = sin_half_angle * axis.get(0) / norm;
            q[1] = sin_half_angle * axis.get(1) / norm;
            q[2] = sin_half_angle * axis.get(2) / norm;
            q[3] = java.lang.Math.cos(angle / 2.0);
        }
        this.normalize();
    }

    /*
     * ! Sets the Quaternion value. See the Quaternion(double , double , double ,
     * double ) ructor documentation.
     */
    public void setValue(double q0, double q1, double q2, double q3) {
        q[0] = q0;
        q[1] = q1;
        q[2] = q2;
        q[3] = q3;
    }

    public void setValue(Quaternion Q) {
        for (int i = 0; i < 4; ++i) {
            q[i] = Q.q[i];
        }
    }

    public double get(int i) {
        return q[i];
    }

    public void setValue(int index, double value) {
        q[index] = value;
    }

    public void multiply(double scale) {
        q[0] = q[0] * scale;
        q[1] = q[1] * scale;
        q[2] = q[2] * scale;
        q[3] = q[3] * scale;
    }

    public void divide(double scale) {
        q[0] = q[0] / scale;
        q[1] = q[1] / scale;
        q[2] = q[2] / scale;
        q[3] = q[3] / scale;
    }

    public void add(Quaternion v) {
        q[0] = q[0] + v.get(0);
        q[1] = q[1] + v.get(1);
        q[2] = q[2] + v.get(2);
        q[3] = q[3] + v.get(3);
    }

    public void minus(Quaternion v) {
        q[0] = q[0] - v.get(0);
        q[1] = q[1] - v.get(1);
        q[2] = q[2] - v.get(2);
        q[3] = q[3] - v.get(3);
    }

    public static Quaternion multiplication(Quaternion v, double scale) {
        return new Quaternion(
                v.get(0) * scale,
                v.get(1) * scale,
                v.get(2) * scale,
                v.get(3) * scale);
    }

    public static Quaternion division(Quaternion v, double scale) {
        return new Quaternion(
                v.get(0) / scale,
                v.get(1) / scale,
                v.get(2) / scale,
                v.get(3) / scale);
    }

    public static Quaternion addition(Quaternion q, Quaternion v) {
        return new Quaternion(
                q.get(0) + v.get(0),
                q.get(1) + v.get(1),
                q.get(2) + v.get(2),
                q.get(3) + v.get(3));
    }

    public static Quaternion substraction(Quaternion q, Quaternion v) {
        return new Quaternion(
                q.get(0) - v.get(0),
                q.get(1) - v.get(1),
                q.get(2) - v.get(2),
                q.get(3) - v.get(3));
    }

    public static Quaternion multiplication(Quaternion a, Quaternion b) {
        return new Quaternion(a.q[3] * b.q[0] + b.q[3] * a.q[0] + a.q[1] * b.q[2] - a.q[2] * b.q[1],
                a.q[3] * b.q[1] + b.q[3] * a.q[1] + a.q[2] * b.q[0] - a.q[0] * b.q[2],
                a.q[3] * b.q[2] + b.q[3] * a.q[2] + a.q[0] * b.q[1] - a.q[1] * b.q[0],
                a.q[3] * b.q[3] - b.q[0] * a.q[0] - a.q[1] * b.q[1] - a.q[2] * b.q[2]);
    }

    public static Quaternion division(Quaternion a, Quaternion b) {
        Quaternion p = new Quaternion(b);
        p.invert();
        return multiplication(a, p);
    }

    public boolean equals(Quaternion a) {
        return q[3] == a.q[3] && q[2] == a.q[2] && q[1] == a.q[1] && q[0] == a.q[0];
    }

    public boolean notEquals(Quaternion a) {
        return q[3] != a.q[3] || q[2] != a.q[2] || q[1] != a.q[1] || q[0] != a.q[0];
    }

    public void multiply(Quaternion q) {
        setValue(multiplication(this, q));
    }

    public void divide(Quaternion q) {
        this.multiply(q.inverse());
    }

    public static Vec3d multiplication(Quaternion q, Vec3d v) {
        Quaternion r = new Quaternion(q);
        return r.rotate(v);
    }

    public double x() {
        return q[0];
    }

    public double y() {
        return q[1];
    }

    public double z() {
        return q[2];
    }

    public double w() {
        return q[3];
    }

    //Matrix
    public void setFromRotationMatrix(Matrix3d mat) {
        double m[][] = mat.getData();
        // Compute one plus the trace of the matrix
        double onePlusdoublerace = 1.0f + m[0][0] + m[1][1] + m[2][2];

        if (onePlusdoublerace > 1E-5) {
            // Direct computation
            double s = java.lang.Math.sqrt(onePlusdoublerace) * 2.0;
            q[0] = (m[2][1] - m[1][2]) / s;
            q[1] = (m[0][2] - m[2][0]) / s;
            q[2] = (m[1][0] - m[0][1]) / s;
            q[3] = 0.25f * s;
        } else {
            // Computation depends on major diagonal term
            if ((m[0][0] > m[1][1]) & (m[0][0] > m[2][2])) {
                double s = java.lang.Math.sqrt(1.0 + m[0][0] - m[1][1] - m[2][2]) * 2.0;
                q[0] = 0.25f * s;
                q[1] = (m[0][1] + m[1][0]) / s;
                q[2] = (m[0][2] + m[2][0]) / s;
                q[3] = (m[1][2] - m[2][1]) / s;
            } else if (m[1][1] > m[2][2]) {
                double s = java.lang.Math.sqrt(1.0 + m[1][1] - m[0][0] - m[2][2]) * 2.0;
                q[0] = (m[0][1] + m[1][0]) / s;
                q[1] = 0.25f * s;
                q[2] = (m[1][2] + m[2][1]) / s;
                q[3] = (m[0][2] - m[2][0]) / s;
            } else {
                double s = java.lang.Math.sqrt(1.0 + m[2][2] - m[0][0] - m[1][1]) * 2.0;
                q[0] = -(m[0][2] + m[2][0]) / s;
                q[1] = -(m[1][2] + m[2][1]) / s;
                q[2] = -0.25f * s;
                q[3] = (m[0][1] - m[1][0]) / s;
            }
        }
        normalize();
    }

    public void setFromRotationMatrixNew(Matrix3d mat){
        double m[][] = mat.getData();
        double tr = m[0][0] + m[1][1] + m[2][2];

        if (tr > 0) {
          double S = Math.sqrt(tr+1.0) * 2; // S=4*qw
          q[3] = 0.25 * S;
          q[0] = (m[2][1] - m[1][2]) / S;
          q[1] = (m[0][2] - m[2][0]) / S;
          q[2] = (m[1][0] - m[0][1]) / S;
        } else if ((m[0][0] > m[1][1])&(m[0][0] > m[2][2])) {
          double S = Math.sqrt(1.0 + m[0][0] - m[1][1] - m[2][2]) * 2; // S=4*qx
          q[3] = (m[2][1] - m[1][2]) / S;
          q[0] = 0.25 * S;
          q[1] = (m[0][1] + m[1][0]) / S;
          q[2] = (m[0][2] + m[2][0]) / S;
        } else if (m[1][1] > m[2][2]) {
          double S = Math.sqrt(1.0 + m[1][1] - m[0][0] - m[2][2]) * 2; // S=4*qy
          q[3] = (m[0][2] - m[2][0]) / S;
          q[0] = (m[0][1] + m[1][0]) / S;
          q[1] = 0.25 * S;
          q[2] = (m[1][2] + m[2][1]) / S;
        } else {
          double S = Math.sqrt(1.0 + m[2][2] - m[0][0] - m[1][1]) * 2; // S=4*qz
          q[3] = (m[1][0] - m[0][1]) / S;
          q[0] = (m[0][2] + m[2][0]) / S;
          q[1] = (m[1][2] + m[2][1]) / S;
          q[2] = 0.25 * S;
        }
    }

    public void setFromRotatedBasis(Vec3d X, Vec3d Y, Vec3d Z) {
        double m[][] = new double[3][3];
        double normX = X.length();
        double normY = Y.length();
        double normZ = Z.length();
        for (int i = 0; i < 3; ++i) {
            m[i][0] = X.get(i) / normX;
            m[i][1] = Y.get(i) / normY;
            m[i][2] = Z.get(i) / normZ;
        }
        Matrix3d mat = new Matrix3d(m);
        setFromRotationMatrix(mat);
    }

    //@{
    public Vec3d axis() {
        Vec3d res = new Vec3d(q[0], q[1], q[2]);
        double sinus = res.length();
        if (sinus > 1E-8) {
            res.divide(sinus);
        }
        return (java.lang.Math.acos(q[3]) <= java.lang.Math.PI / 2.0) ? res : res.opposite();
    }

    public double angle() {
        double angle = 2.0 * java.lang.Math.acos(q[3]);
        return angle <= java.lang.Math.PI ? angle : 2.0 * java.lang.Math.PI - angle;
    }

    public Vec3d rotate(Vec3d v) {
        /*
         * glview code double q00 = 2.0l * q[0] * q[0]; double q11 = 2.0l * q[1] *
         * q[1]; double q22 = 2.0l * q[2] * q[2];
         *
         * double q01 = 2.0l * q[0] * q[1]; double q02 = 2.0l * q[0] * q[2]; double
         * q03 = 2.0l * q[0] * q[3];
         *
         * double q12 = 2.0l * q[1] * q[2]; double q13 = 2.0l * q[1] * q[3];
         *
         * double q23 = 2.0l * q[2] * q[3];
         *
         * Vec3f r = Vec3f ((1.0 - q11 - q22)*v[0] + ( q01 - q23)*v[1] + ( q02 +
         * q13)*v[2], ( q01 + q23)*v[0] + (1.0 - q22 - q00)*v[1] + ( q12 -
         * q03)*v[2], ( q02 - q13)*v[0] + ( q12 + q03)*v[1] + (1.0 - q11 -
         * q00)*v[2] );
         */
//        Vec3f vn = new Vec3f(v);
        Quaternion vecQuat = new Quaternion(v.x(), v.y(), v.z(), 0.0f);
        Quaternion resQuat = multiplication(vecQuat, inverse());
        resQuat = multiplication(this, resQuat);
        Vec3d re = new Vec3d(resQuat.get(0), resQuat.get(1), resQuat.get(2));
        return re;
    }

    public Vec3d inverseRotate(Vec3d v) {
        return inverse().rotate(v);
    }

    public Quaternion conjugate() {
        return new Quaternion(-q[0], -q[1], -q[2], q[3]);
    }

    public void invert() {
        q[0] = -q[0];
        q[1] = -q[1];
        q[2] = -q[2];
    }

    public Quaternion inverse() {
        double scalar = 1 / (q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
        Quaternion res = conjugate();
        res.multiply(scalar);
        return res;
    }

    public void negate() {
        invert();
        q[3] = -q[3];
    }

    public double norm() {
        return java.lang.Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
    }

    public double normalize() {
        double norm = java.lang.Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
        for (int i = 0; i < 4; ++i) {
            q[i] /= norm;
        }
        return norm;
    }

    public Quaternion normalized() {
        double Q[] = new double[4];
        double norm = java.lang.Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2] + q[3] * q[3]);
        for (int i = 0; i < 4; ++i) {
            Q[i] = q[i] / norm;
        }
        return new Quaternion(Q[0], Q[1], Q[2], Q[3]);
    }

    /*
     * ! @name Associated matrix
     */
    //@{
    public Matrix4d matrix() {
        Matrix4d m = new Matrix4d();
        getMatrix(m.getData());
        return (m);
    }

    public void getMatrix(double m[][]) {
        double q00 = 2.0f * q[0] * q[0];
        double q11 = 2.0f * q[1] * q[1];
        double q22 = 2.0f * q[2] * q[2];

        double q01 = 2.0f * q[0] * q[1];
        double q02 = 2.0f * q[0] * q[2];
        double q03 = 2.0f * q[0] * q[3];

        double q12 = 2.0f * q[1] * q[2];
        double q13 = 2.0f * q[1] * q[3];

        double q23 = 2.0f * q[2] * q[3];

        m[0][0] = 1.0 - q11 - q22;
        m[1][0] = q01 + q23;
        m[2][0] = q02 - q13;

        m[0][1] = q01 - q23;
        m[1][1] = 1.0 - q22 - q00;
        m[2][1] = q12 + q03;

        m[0][2] = q02 + q13;
        m[1][2] = q12 - q03;
        m[2][2] = 1.0 - q11 - q00;

        m[0][3] = 0.0f;
        m[1][3] = 0.0f;
        m[2][3] = 0.0f;

        m[3][0] = 0.0f;
        m[3][1] = 0.0f;
        m[3][2] = 0.0f;
        m[3][3] = 1.0f;
    }

    public Matrix3d getRotationMatrix() {
        Matrix3d m = new Matrix3d();
        getRotationMatrix(m.getData());
        return (m);
    }

    public void getRotationMatrix(double m[][]) {

        double mat[][] = new double[4][4];
        getMatrix(mat);

        for (int i = 0; i < 3; ++i) {
            for (int j = 0; j < 3; ++j) {
                m[i][j] = mat[i][j];
            }
        }
    }
    /*
     * double * inverseMatrix() { static double m[4][4]; getInverseMatrix(m);
     * return (double *)(m); }
     *
     * void getInverseMatrix(double m[4][4]) { inverse().getMatrix(m); }
     *
     * void getInverseMatrix(double m[16]) { inverse().getMatrix(m); }
     *
     * void getInverseRotationMatrix(double m[3][3]) { static double mat[4][4];
     * getInverseMatrix(mat); for (int i=0; i<3; ++i) for (int j=0; j<3; ++j) //
     * Beware of transposition m[i][j] = mat[j][i]; }
     *
     *
     */

    public static Quaternion multipleRotation(Quaternion q, double times) {
        Vec3d axis = q.axis();
        double angle = q.angle();
        Quaternion q2 = new Quaternion();
        q2.setAxisAngle(axis, angle * times);
        return q2;
    }

    public static Quaternion divideRotation(Quaternion q, double times) {
        Vec3d axis = q.axis();
        double angle = q.angle();
        Quaternion q2 = new Quaternion();
        q2.setAxisAngle(axis, angle / times);
        return q2;
    }

    /*
     * ! @name linear lerp interpolation
     */
    public static Quaternion lerp(Quaternion q1, Quaternion q2, double t) {
        Quaternion res = Quaternion.multiplication(q1, 1 - t);
        res.add(Quaternion.multiplication(q2, (t)));
        res.normalize();
        return res;
    }

    /*
     * ! @name spherical linear Slerp interpolation
     */
    public static Quaternion slerp(Quaternion a, Quaternion b, double t, boolean allowFlip) {
        double cosAngle = Quaternion.dot(a, b);

        double c1, c2;
        // Linear interpolation for close orientations
        if ((1.0 - java.lang.Math.abs(cosAngle)) < 0.01) {
            c1 = 1.0 - t;
            c2 = t;
        } else {
            // Spherical interpolation
            double angle = java.lang.Math.acos(java.lang.Math.abs(cosAngle));
            double sinAngle = java.lang.Math.sin(angle);
            c1 = java.lang.Math.sin(angle * (1.0 - t)) / sinAngle;
            c2 = java.lang.Math.sin(angle * t) / sinAngle;
        }

        // Use the shortest path
        if (allowFlip && (cosAngle < 0.0)) {
            c1 = -c1;
        }
        Quaternion res = new Quaternion(c1 * a.get(0) + c2 * b.get(0), c1 * a.get(1) + c2 * b.get(1), c1 * a.get(2) + c2 * b.get(2), c1 * a.get(3) + c2 * b.get(3));
        res.normalize();
        return res;
    }

    public static Quaternion squad(Quaternion a, Quaternion tgA, Quaternion tgB, Quaternion b, double t) {
        Quaternion ab = Quaternion.slerp(a, b, t, false);
        Quaternion tg = Quaternion.slerp(tgA, tgB, t, false);
        return Quaternion.slerp(ab, tg, 2.0f * t * (1.0f - t), false);
    }

    //! Shoemake-Bezier interpolation using De Castlejau algorithm
    public static Quaternion bezier(Quaternion q1, Quaternion q2, Quaternion a, Quaternion b, double t) {
        // level 1
        Quaternion q11 = Quaternion.slerp(q1, a, t, false),
                q12 = Quaternion.slerp(a, b, t, false),
                q13 = Quaternion.slerp(b, q2, t, false);
        // level 2 and 3
        return Quaternion.slerp(Quaternion.slerp(q11, q12, t, false), Quaternion.slerp(q12, q13, t, false), t, false);
    }

    //! Given 3 quaternions, qn-1,qn and qn+1, calculate a control point to be used in spline interpolation
    public static Quaternion spline(Quaternion qnm1, Quaternion qn, Quaternion qnp1) {
        Quaternion qni = new Quaternion(qn.inverse());
        Quaternion a = Quaternion.substraction(qni, qnm1).log();
        Quaternion b = Quaternion.substraction(qni, qnp1).log();
        Quaternion c = Quaternion.division(Quaternion.addition(a, b), -4);
        return multiplication(qn, c.exp());
    }

    public static double dot(Quaternion a, Quaternion b) {
        return a.get(0) * b.get(0) + a.get(1) * b.get(1) + a.get(2) * b.get(2) + a.get(3) * b.get(3);
    }

    public Quaternion log() {
        double len = java.lang.Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2]);
        if (len < 1E-6) {
            return new Quaternion(this.get(0), this.get(1), this.get(2), 0.0);
        } else {
            double coef = java.lang.Math.acos(q[3]) / len;
            return new Quaternion(q[0] * coef, q[1] * coef, q[2] * coef, 0.0);
        }
    }

    public Quaternion exp() {
        double theta = java.lang.Math.sqrt(q[0] * q[0] + q[1] * q[1] + q[2] * q[2]);

        if (theta < 1E-6) {
            return new Quaternion(q[0], q[1], q[2], java.lang.Math.cos(theta));
        } else {
            double coef = java.lang.Math.sin(theta) / theta;
            return new Quaternion(q[0] * coef, q[1] * coef, q[2] * coef, java.lang.Math.cos(theta));
        }
    }

    public static Quaternion lnDif(Quaternion a, Quaternion b) {
        Quaternion dif = multiplication(a.inverse(), b);
        dif.normalize();
        return dif.log();
    }

    public static Quaternion squaddoubleangent(Quaternion before, Quaternion center, Quaternion after) {
        Quaternion l1 = Quaternion.lnDif(center, before);
        Quaternion l2 = Quaternion.lnDif(center, after);
        Quaternion e = new Quaternion();
        for (int i = 0; i < 4; ++i) {
            e.setValue(i, -0.25 * (l1.q[i] + l2.q[i]));
        }
        e = multiplication(center, e.exp());

        // if (Quaternion::dot(e,b) < 0.0)
        // e.negate();

        return e;
    }

    //// euler x - y - z  =>  z * y * x * v
    // this may namely run xyz or zyx  in greta z * y * x * v
    public void fromEulerXYZ(double roll, double pitch, double yaw) {
        double y = yaw / 2.0;
        double p = pitch / 2.0;
        double r = roll / 2.0;

        double sinp = java.lang.Math.sin(p);
        double siny = java.lang.Math.sin(y);
        double sinr = java.lang.Math.sin(r);
        double cosp = java.lang.Math.cos(p);
        double cosy = java.lang.Math.cos(y);
        double cosr = java.lang.Math.cos(r);

        q[0] = sinr * cosp * cosy - cosr * sinp * siny;
        q[1] = cosr * sinp * cosy + sinr * cosp * siny;
        q[2] = cosr * cosp * siny - sinr * sinp * cosy;
        q[3] = cosr * cosp * cosy + sinr * sinp * siny;

        normalize();

    }

    public void fromEulerXYZByAngle(double roll, double pitch, double yaw) {
        fromEulerXYZ(Math.toRadians(roll), Math.toRadians(pitch), Math.toRadians(yaw));
    }

    // z y x EULER angle,  OR x y z fixed angle checked from robotic definition
    //RX->RY->RZ  RZ * RY * RX
    public Vec3d getEulerAngleXYZ() {
        //atan2
        double roll = java.lang.Math.atan2(2 * (q[3] * q[0] + q[1] * q[2]), 1 - 2 * (q[0] * q[0] + q[1] * q[1]));
        double valueP = Math.max(-1, Math.min(1, 2 * (q[3] * q[1] - q[2] * q[0])));
        double pitch = java.lang.Math.asin(valueP);
        double yaw = java.lang.Math.atan2(2 * (q[3] * q[2] + q[0] * q[1]), 1 - 2 * (q[1] * q[1] + q[2] * q[2]));
        double v = q[3] * q[1] - q[2] * q[0];
        if (v > 0.5) {
            v = 0.5f;
        }
        if (v < -0.5) {
            v = -0.5f;
        }

        if (v == 0.5) {
            roll = 2.0 * java.lang.Math.atan2(q[0], q[3]);
            yaw = 0;
        } else if (v == -0.5) {
            roll = -2.0 * java.lang.Math.atan2(q[0], q[3]);
            yaw = 0;
        }
        return new Vec3d(roll, pitch, yaw);
        //arctan
        //double  roll = atan(   (2 * (q[3]* q[0] + q[1]* q[2]))  /   (1 - 2*( q[0] * q[0] + q[1] * q[1]))  );
        //double  pitch = asin(2* ( q[3] * q[1] - q[2] * q[0]  ));
        //double  yaw = atan( (2 * ( q[3] * q[2] +  q[0] * q[1]))  / (1 - 2*( q[1] * q[1] + q[2] * q[2]))   );

        //return Vec3f ( yaw, pitch ,roll);
    }

    public Vec3d getEulerAngleXYZByAngle() {
        Vec3d xyz = getEulerAngleXYZ();
        double roll = Math.toDegrees(xyz.x());
        double pitch = Math.toDegrees(xyz.y());
        double yaw = Math.toDegrees(xyz.z());
        return new Vec3d(roll, pitch, yaw);
    }

    public void fromEulerZYZ(double a, double b, double c) {
        q[3] = -java.lang.Math.cos((a - c) * 0.5) * java.lang.Math.sin(b * 0.5);
        q[0] = -java.lang.Math.sin((a - c) * 0.5) * java.lang.Math.sin(b * 0.5);
        q[1] = -java.lang.Math.sin((a + c) * 0.5) * java.lang.Math.cos(b * 0.5);
        q[2] =  java.lang.Math.sin((a + c) * 0.5) * java.lang.Math.cos(b * 0.5);
    }

    public Vec3d getEulerAngleZYZ() {
        double a = java.lang.Math.atan2((q[3] * q[1] + q[0] * q[2]), (q[0] * q[1] - q[3] * q[2]));
        double b = java.lang.Math.acos(-q[3] * q[3] - q[0] * q[0] + q[1] * q[1] + q[2] * q[2]);
        double c = -java.lang.Math.atan2((q[3] * q[1] - q[0] * q[2]), (q[0] * q[1] + q[3] * q[2]));
        return new Vec3d(a, b, c);
    }

    private static Vec3d toDegrees(Vec3d rad){
        double x = Math.toDegrees(rad.x());
        double y = Math.toDegrees(rad.y());
        double z = Math.toDegrees(rad.z());
        return new Vec3d(x, y, z);
    }

    // <editor-fold defaultstate="collapsed" desc="Euler XYZ convertions">
    //TODO check if it is the same as getEulerAngleXYZ
    public Vec3d toEulerXYZ() {
        Quaternion q = this.normalized();
        double r11 = 2 * (q.x() * q.y() + q.w() * q.z());
        double r12 = q.w() * q.w() + q.x() * q.x() - q.y() * q.y() - q.z() * q.z();
        double r21 = -2 * (q.x() * q.z() - q.w() * q.y());
        double r31 = 2 * (q.y() * q.z() + q.w() * q.x());
        double r32 = q.w() * q.w() - q.x() * q.x() - q.y() * q.y() + q.z() * q.z();
        return new Vec3d(Math.atan2(r31, r32), Math.asin(r21), Math.atan2(r11, r12));
    }

    public Vec3d toEulerXYZInDegrees() {
        return toDegrees(toEulerXYZ());
    }

    public static Quaternion fromXYZInDegrees(Vec3d xyz) {
        return fromXYZInDegrees(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromXYZInDegrees(double x, double y, double z) {
        return fromXYZ(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }

    public static Quaternion fromXYZ(Vec3d xyz) {
        return fromXYZ(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromXYZ(double x, double y, double z) {
        Quaternion q = new Quaternion();
        double rx = x / 2.0;
        double ry = y / 2.0;
        double rz = z / 2.0;
        double sinx = Math.sin(rx);
        double siny = Math.sin(ry);
        double sinz = Math.sin(rz);
        double cosx = Math.cos(rx);
        double cosy = Math.cos(ry);
        double cosz = Math.cos(rz);
        q.q[3] = cosx * cosy * cosz + sinx * siny * sinz; // w
        q.q[0] = sinx * cosy * cosz - cosx * siny * sinz; // x
        q.q[1] = cosx * siny * cosz + sinx * cosy * sinz; // y
        q.q[2] = cosx * cosy * sinz - sinx * siny * cosz; // z
        q.normalize();
        return q;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Euler XZY convertions">
    public Vec3d toEulerXZY() {
        Quaternion q = this.normalized();
        double r11 = -2 * (q.x() * q.z() - q.w() * q.y());
        double r12 = q.w() * q.w() + q.x() * q.x() - q.y() * q.y() - q.z() * q.z();
        double r21 = 2 * (q.x() * q.y() + q.w() * q.z());
        double r31 = -2 * (q.y() * q.z() - q.w() * q.x());
        double r32 = q.w() * q.w() - q.x() * q.x() + q.y() * q.y() - q.z() * q.z();
        return new Vec3d(Math.atan2(r31, r32), Math.atan2(r11, r12), Math.asin(r21));
    }

    public Vec3d toEulerXZYInDegrees() {
        return toDegrees(toEulerXZY());
    }

    public static Quaternion fromXZYInDegrees(Vec3d xyz) {
        return fromXZYInDegrees(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromXZYInDegrees(double x, double y, double z) {
        return fromXZY(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }

    public static Quaternion fromXZY(Vec3d xyz) {
        return fromXZY(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromXZY(double x, double y, double z) {
        Quaternion q = new Quaternion();
        double rx = x / 2.0;
        double ry = y / 2.0;
        double rz = z / 2.0;
        double sinx = Math.sin(rx);
        double siny = Math.sin(ry);
        double sinz = Math.sin(rz);
        double cosx = Math.cos(rx);
        double cosy = Math.cos(ry);
        double cosz = Math.cos(rz);
        q.q[3] = cosx * cosy * cosz - sinx * siny * sinz; // w
        q.q[0] = sinx * cosy * cosz + cosx * siny * sinz; // x
        q.q[1] = cosx * siny * cosz + sinx * cosy * sinz; // y
        q.q[2] = cosx * cosy * sinz - sinx * siny * cosz; // z
        q.normalize();
        return q;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Euler YXZ convertions">
    public Vec3d toEulerYXZ() {
        Quaternion q = this.normalized();
        double r11 = -2 * (q.x() * q.y() - q.w() * q.z());
        double r12 = q.w() * q.w() - q.x() * q.x() + q.y() * q.y() - q.z() * q.z();
        double r21 = 2 * (q.y() * q.z() + q.w() * q.x());
        double r31 = -2 * (q.x() * q.z() - q.w() * q.y());
        double r32 = q.w() * q.w() - q.x() * q.x() - q.y() * q.y() + q.z() * q.z();
        return new Vec3d(Math.asin(r21), Math.atan2(r31, r32), Math.atan2(r11, r12));
    }

    public Vec3d toEulerYXZInDegrees() {
        return toDegrees(toEulerYXZ());
    }

    public static Quaternion fromYXZInDegrees(Vec3d xyz) {
        return fromYXZInDegrees(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromYXZInDegrees(double x, double y, double z) {
        return fromXZY(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }

    public static Quaternion fromYXZ(Vec3d xyz) {
        return fromYXZ(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromYXZ(double x, double y, double z) {
        Quaternion q = new Quaternion();
        double rx = x / 2.0;
        double ry = y / 2.0;
        double rz = z / 2.0;
        double sinx = Math.sin(rx);
        double siny = Math.sin(ry);
        double sinz = Math.sin(rz);
        double cosx = Math.cos(rx);
        double cosy = Math.cos(ry);
        double cosz = Math.cos(rz);
        q.q[3] = cosx * cosy * cosz - sinx * siny * sinz; // w
        q.q[0] = sinx * cosy * cosz - cosx * siny * sinz; // x
        q.q[1] = cosx * siny * cosz + sinx * cosy * sinz; // y
        q.q[2] = cosx * cosy * sinz + sinx * siny * cosz; // z
        q.normalize();
        return q;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Euler YZX convertions">
    public Vec3d toEulerYZX() {
        Quaternion q = this.normalized();
        double r11 = 2 * (q.y() * q.z() + q.w() * q.x());
        double r12 = q.w() * q.w() - q.x() * q.x() + q.y() * q.y() - q.z() * q.z();
        double r21 = -2 * (q.x() * q.y() - q.w() * q.z());
        double r31 = 2 * (q.x() * q.z() + q.w() * q.y());
        double r32 = q.w() * q.w() + q.x() * q.x() - q.y() * q.y() - q.z() * q.z();
        return new Vec3d(Math.atan2(r11, r12), Math.atan2(r31, r32), Math.asin(r21));
    }

    public Vec3d toEulerYZXInDegrees() {
        return toDegrees(toEulerYZX());
    }

    public static Quaternion fromYZXInDegrees(Vec3d xyz) {
        return fromYZXInDegrees(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromYZXInDegrees(double x, double y, double z) {
        return fromYZX(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }

    public static Quaternion fromYZX(Vec3d xyz) {
        return fromYZX(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromYZX(double x, double y, double z) {
        Quaternion q = new Quaternion();
        double rx = x / 2.0;
        double ry = y / 2.0;
        double rz = z / 2.0;
        double sinx = Math.sin(rx);
        double siny = Math.sin(ry);
        double sinz = Math.sin(rz);
        double cosx = Math.cos(rx);
        double cosy = Math.cos(ry);
        double cosz = Math.cos(rz);
        q.q[3] = cosx * cosy * cosz + sinx * siny * sinz; // w
        q.q[0] = sinx * cosy * cosz - cosx * siny * sinz; // x
        q.q[1] = cosx * siny * cosz - sinx * cosy * sinz; // y
        q.q[2] = cosx * cosy * sinz + sinx * siny * cosz; // z
        q.normalize();
        return q;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Euler ZXY convertions">
    public Vec3d toEulerZXY() {
        Quaternion q = this.normalized();
        double r11 = 2 * (q.x() * q.z() + q.w() * q.y());
        double r12 = q.w() * q.w() - q.x() * q.x() - q.y() * q.y() + q.z() * q.z();
        double r21 = -2 * (q.y() * q.z() - q.w() * q.x());
        double r31 = 2 * (q.x() * q.y() + q.w() * q.z());
        double r32 = q.w() * q.w() - q.x() * q.x() + q.y() * q.y() - q.z() * q.z();

        return new Vec3d(Math.asin(r21), Math.atan2(r11, r12), Math.atan2(r31, r32));
    }

    public Vec3d toEulerZXYInDegrees() {
        return toDegrees(toEulerZXY());
    }

    public static Quaternion fromZXYInDegrees(Vec3d xyz) {
        return fromZXYInDegrees(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromZXYInDegrees(double x, double y, double z) {
        return fromZXY(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }

    public static Quaternion fromZXY(Vec3d xyz) {
        return fromZXY(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromZXY(double x, double y, double z) {
        Quaternion q = new Quaternion();
        double rx = x / 2.0;
        double ry = y / 2.0;
        double rz = z / 2.0;
        double sinx = Math.sin(rx);
        double siny = Math.sin(ry);
        double sinz = Math.sin(rz);
        double cosx = Math.cos(rx);
        double cosy = Math.cos(ry);
        double cosz = Math.cos(rz);
        q.q[3] = cosx * cosy * cosz + sinx * siny * sinz; // w
        q.q[0] = sinx * cosy * cosz + cosx * siny * sinz; // x
        q.q[1] = cosx * siny * cosz - sinx * cosy * sinz; // y
        q.q[2] = cosx * cosy * sinz - sinx * siny * cosz; // z
        q.normalize();
        return q;
    }
    // </editor-fold>

    // <editor-fold defaultstate="collapsed" desc="Euler ZYX convertions">
    public Vec3d toEulerZYX() {
        Quaternion q = this.normalized();
        double r11 = -2 * (q.y() * q.z() - q.w() * q.x());
        double r12 = q.w() * q.w() - q.x() * q.x() - q.y() * q.y() + q.z() * q.z();
        double r21 = 2 * (q.x() * q.z() + q.w() * q.y());
        double r31 = -2 * (q.x() * q.y() - q.w() * q.z());
        double r32 = q.w() * q.w() + q.x() * q.x() - q.y() * q.y() - q.z() * q.z();
        return new Vec3d(Math.atan2(r11, r12), Math.asin(r21), Math.atan2(r31, r32));
    }

    public Vec3d toEulerZYXInDegrees() {
        return toDegrees(toEulerZYX());
    }

    public static Quaternion fromZYXInDegrees(Vec3d xyz) {
        return fromZYXInDegrees(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromZYXInDegrees(double x, double y, double z) {
        return fromZYX(Math.toRadians(x), Math.toRadians(y), Math.toRadians(z));
    }

    public static Quaternion fromZYX(Vec3d xyz) {
        return fromZYX(xyz.x(), xyz.y(), xyz.z());
    }

    public static Quaternion fromZYX(double x, double y, double z) {
        Quaternion q = new Quaternion();
        double rx = x / 2.0;
        double ry = y / 2.0;
        double rz = z / 2.0;
        double sinx = Math.sin(rx);
        double siny = Math.sin(ry);
        double sinz = Math.sin(rz);
        double cosx = Math.cos(rx);
        double cosy = Math.cos(ry);
        double cosz = Math.cos(rz);
        q.q[3] = cosx * cosy * cosz - sinx * siny * sinz; // w
        q.q[0] = sinx * cosy * cosz + cosx * siny * sinz; // x
        q.q[1] = cosx * siny * cosz - sinx * cosy * sinz; // y
        q.q[2] = cosx * cosy * sinz + sinx * siny * cosz; // z
        q.normalize();
        return q;
    }
    // </editor-fold>


    /**
     * the function may get pb, use the next one. which is more stable
     */
    public void fromRotatedPlane(Vec3d a, Vec3d b, Vec3d a_original, Vec3d b_original) {

        if (a.equals(a_original)) {

            Quaternion oneplane[] = {new Quaternion(), new Quaternion(), new Quaternion(), new Quaternion()};

            Vec3d intersection = new Vec3d(Math.abs(a.x()), Math.abs(a.y()), Math.abs(a.z()));
            //intersection.normalize();
            double adif = b.dot3(b_original);
            //T  bdif = b.dot3(b_original)/ (b.length() * b_original.length() );
            double cosTheta = adif;//> bdif ? adif : bdif;
            cosTheta = 1 < cosTheta ? 1 : cosTheta;
            double angle = java.lang.Math.acos(cosTheta);
            oneplane[0].setAxisAngle(intersection, -angle);   // PB:here how to decide the angle sign
            oneplane[1].setAxisAngle(intersection, angle);
            oneplane[2].setAxisAngle(intersection, 3.1415926f - angle);   // PB:here how to decide the angle sign
            oneplane[3].setAxisAngle(intersection, 3.1415926f + angle);

            Vec3d b_final[] = {new Vec3d(), new Vec3d(), new Vec3d(), new Vec3d()};
            double finalb_length[] = new double[4];
            int min = 0;
            double minLength = 1000;
            for (int i = 0; i < 4; i++) {

                b_final[i] = Quaternion.multiplication(oneplane[i], b_original);
                finalb_length[i] = Vec3d.substraction(b_final[i], b).length();
                if (finalb_length[i] < minLength) {
                    minLength = finalb_length[i];
                    min = i;
                }
            }
            //std::cout<<"min"<<minLength<<std::endl;
            //std::cout<<minLength<<std::endl;
            setValue(oneplane[min].get(0), oneplane[min].get(1), oneplane[min].get(2), oneplane[min].get(3));

        } else if (b.equals(b_original)) {

            Quaternion oneplane[] = {new Quaternion(), new Quaternion(), new Quaternion(), new Quaternion()};

            Vec3d intersection = new Vec3d(Math.abs(b.x()), Math.abs(b.y()), Math.abs(b.z()));
            //intersection.normalize();
            double adif = a.dot3(a_original);
            //T  bdif = b.dot3(b_original)/ (b.length() * b_original.length() );
            double cosTheta = adif;//> bdif ? adif : bdif;
            cosTheta = 1 < cosTheta ? 1 : cosTheta;
            double angle = java.lang.Math.acos(cosTheta);
            oneplane[0].setAxisAngle(intersection, -angle);   // PB:here how to decide the angle sign
            oneplane[1].setAxisAngle(intersection, angle);
            oneplane[2].setAxisAngle(intersection, 3.1415926f - angle);   // PB:here how to decide the angle sign
            oneplane[3].setAxisAngle(intersection, 3.1415926f + angle);

            Vec3d a_final[] = {new Vec3d(), new Vec3d(), new Vec3d(), new Vec3d()};
            double final_length[] = new double[4];

            int min = 0;
            double minLength = 1000;
            for (int i = 0; i < 4; i++) {

                a_final[i] = Quaternion.multiplication(oneplane[i], a_original);
                final_length[i] = Vec3d.substraction(a_final[i], a).length();
                if (final_length[i] < minLength) {
                    minLength = final_length[i];
                    min = i;
                }
            }
            //std::cout<<"min"<<minLength<<std::endl;
            //std::cout<<minLength<<std::endl;
            setValue(oneplane[min].get(0), oneplane[min].get(1), oneplane[min].get(2), oneplane[min].get(3));

        } else {
            Vec3d n_original = Vec3d.cross3(a_original, b_original);
            Vec3d n = Vec3d.cross3(a, b);

            a.normalize();
            b.normalize();
            a_original.normalize();
            b_original.normalize();

            n_original.normalize();
            n.normalize();

            Quaternion coplane = new Quaternion();
            if (!n_original.equals(n)) {
                Vec3d intersection = Vec3d.cross3(n_original, n);

                double cosTheta = n_original.dot3(n);
                cosTheta = 1 < cosTheta ? 1 : cosTheta;
                double sinTheta = intersection.length();
                intersection.normalize();
                coplane.setAxisAngle(intersection, java.lang.Math.acos(cosTheta));
            }
            Vec3d a_one = Quaternion.multiplication(coplane, a_original);
            Vec3d b_one = Quaternion.multiplication(coplane, b_original);
            Vec3d n_one = Vec3d.cross3(a_one, b_one);
            n_one.normalize();
            //TODO:  can not know which is before which is after by rotation,  maybe wrong direction for rotation
            Quaternion oneplane[] = {new Quaternion(), new Quaternion(), new Quaternion(), new Quaternion()};

            Vec3d intersection = n;
            //intersection.normalize();
            double adif = a_one.dot3(a);
            //T  bdif = b.dot3(b_original)/ (b.length() * b_original.length() );
            double cosTheta = adif;//> bdif ? adif : bdif;
            cosTheta = 1 < cosTheta ? 1 : cosTheta;
            double angle = java.lang.Math.acos(cosTheta);
            oneplane[0].setAxisAngle(intersection, -angle);   // PB:here how to decide the angle sign
            oneplane[1].setAxisAngle(intersection, angle);
            oneplane[2].setAxisAngle(intersection, 3.1415926f - angle);   // PB:here how to decide the angle sign
            oneplane[3].setAxisAngle(intersection, 3.1415926f + angle);
            Quaternion finalq[] = {new Quaternion(), new Quaternion(), new Quaternion(), new Quaternion()};
            Vec3d a_final[] = {new Vec3d(), new Vec3d(), new Vec3d(), new Vec3d()};
            Vec3d b_final[] = {new Vec3d(), new Vec3d(), new Vec3d(), new Vec3d()};
            double final_length[] = new double[4];
            double finalb_length[] = new double[4];
            int min = 0;
            double minLength = 1000;
            for (int i = 0; i < 4; i++) {
                finalq[i] = Quaternion.multiplication(oneplane[i], coplane);
                a_final[i] = Quaternion.multiplication(finalq[i], a_original);
                b_final[i] = Quaternion.multiplication(finalq[i], b_original);
                final_length[i] = Vec3d.substraction(a_final[i], a).length();
                finalb_length[i] = Vec3d.substraction(b_final[i], b).length();
                double finalL = final_length[i] + finalb_length[i];
                if (finalL < minLength) {
                    minLength = finalL;
                    min = i;
                }
            }
            //std::cout<<"min"<<minLength<<std::endl;
            //std::cout<<minLength<<std::endl;
            setValue(finalq[min].get(0), finalq[min].get(1), finalq[min].get(2), finalq[min].get(3));
            this.normalize();
            //this->setValue(oneplane_n[0],oneplane_n[1],oneplane_n[2],oneplane_n[3]);
        }
    }

    public void fromRotatedTrianglePlane(Vec3d a_v, Vec3d b_v, Vec3d a_original_v, Vec3d b_original_v) {
//        double t = a_v.normalized().dot3(a_original_v.normalized());
//        if(t <= 0.01 ){
//            Vec3f tmp0 = a_v;
//            Vec3f tmp1 = a_original_v;
//            a_v = b_v;
//            a_original_v = b_original_v;
//            b_v = tmp0;
//            b_original_v = b_v;
//        }

        Vec3d a_original = a_original_v.normalized();
        Vec3d a = a_v.normalized();
        Vec3d b_original = b_original_v.normalized();
        Vec3d b = b_v.normalized();
        Vec3d n_original = a_original.cross3(b_original);
        Vec3d n = a.cross3(b);
        n_original.normalize();
        n.normalize();

        Quaternion q1 = new Quaternion();
        Vec3d axis_a = a_original.cross3(a);
        //Vec3f axis_a =  a.cross3(a_original);
        axis_a.normalize();
        double cosTheta_a = a_original.dot3(a);
        if (axis_a.length() < 0.1) {
            if (Vec3d.substraction(a_original, a).length() > 0.001f) {
                q1.setAxisAngle(n_original, 3.1415926f);
            }
        } else {
            q1.setAxisAngle(axis_a, Math.acos(cosTheta_a));
        }
        Vec3d a_middle = q1.rotate(a_original);

        //  if (Vec3f.substraction(a_v, a_middle).length() > 0.1f) {
        //std::cout<<"a1" <<a_original << a << q1 * a_original<<std::endl;
        Vec3d b_2 = Quaternion.multiplication(q1, b_original);
        b_2.normalize();
        Quaternion q2 = new Quaternion();
        Quaternion q3 = new Quaternion();
        Vec3d n_b2 = a.cross3(b_2);
        Vec3d n_b = a.cross3(b);
        Vec3d axis_n = n_b2.cross3(n_b);
        double cosTheta_n = n_b2.dot3(n_b);
        if (axis_n.length() < 0.1) {
            if (Vec3d.substraction(n_b2, n_b).length() > 0.001f) {
                q2.setAxisAngle(a, 3.1415926f);
            }
        } else {
            q2.setAxisAngle(axis_n, Math.acos(cosTheta_n));
            if (cosTheta_n > 0) {
                q3.setAxisAngle(axis_n, 3.1415926f - Math.acos(cosTheta_n));
            } else {
                q3.setAxisAngle(axis_n, 3.1415926f + Math.acos(cosTheta_n));
            }
        }

        Quaternion finalq = Quaternion.multiplication(q2, q1).normalized();
//            Quaternion finalq2 = Quaternion.multiplication(q3, q1);
//            finalq.normalize();
//            {
//                Vec3f a_final = finalq.rotate(a_original_v);
//                Vec3f b_final = finalq.rotate(b_original_v);
//                Vec3f a_final2 = finalq2.rotate(a_original_v);
//                Vec3f b_final2 = finalq2.rotate(b_original_v);
//                if (Vec3f.substraction(a_final, a_v).length() > 0.01 || Vec3f.substraction(b_final, b_v).length() > 0.01) {
//                    if (Vec3f.substraction(b_final2, b_v).length() < 0.01) {
//                        finalq = finalq2;
//                    } else {
//                        System.out.println("error in Quaternion triangle");
//                    }
//                }
//            }
        setValue(finalq.get(0), finalq.get(1), finalq.get(2), finalq.get(3));
        /*
         * } else { Quaternion q2 = new Quaternion(); Quaternion q3 = new
         * Quaternion(); Quaternion q21 = new Quaternion(); Quaternion q31 = new
         * Quaternion();
         *
         * Quaternion q32 = new Quaternion(); Quaternion q33 = new Quaternion();
         * Vec3f b_2 = Quaternion.multiplication(q1, b_original);
         * b_2.normalize(); Vec3f axis_n = a_v; double cosTheta_n =
         * b_v.dot3(b_2); Vec3f sign = b_v.cross3(b_2).normalized(); //if
         * (Vec3f.substraction(sign, a_v).length() > 0.01) { double angle =
         * Math.acos(cosTheta_n); q2.setAxisAngle(sign, angle);
         * q21.setAxisAngle(sign, -angle); // } else { //
         * q2.setAxisAngle(axis_n, -Math.acos(cosTheta_n)); // }
         *
         * q3.setAxisAngle(sign, 3.1415926f - angle);
         *
         * q31.setAxisAngle(sign, 3.1415926f + angle);
         *
         * q32.setAxisAngle(sign, -3.1415926f - angle);
         *
         * q33.setAxisAngle(sign, -3.1415926f + angle);
         *
         *
         * Quaternion finalq = Quaternion.multiplication(q2, q1); Quaternion
         * finalq2 = Quaternion.multiplication(q3, q1); Quaternion finalq21 =
         * Quaternion.multiplication(q21, q1); Quaternion finalq31 =
         * Quaternion.multiplication(q31, q1); Quaternion finalq32 =
         * Quaternion.multiplication(q32, q1); Quaternion finalq33 =
         * Quaternion.multiplication(q33, q1); finalq.normalize(); { Vec3f
         * a_final = finalq.rotate(a_original_v); Vec3f b_final =
         * finalq.rotate(b_original_v); Vec3f a_final2 =
         * finalq2.rotate(a_original_v); Vec3f b_final2 =
         * finalq2.rotate(b_original_v); Vec3f b_final3 =
         * finalq21.rotate(b_original_v); Vec3f b_final4 =
         * finalq31.rotate(b_original_v); Vec3f b_final5 =
         * finalq32.rotate(b_original_v); Vec3f b_final6 =
         * finalq33.rotate(b_original_v); if (Vec3f.substraction(a_final,
         * a_v).length() > 0.01 || Vec3f.substraction(b_final, b_v).length() >
         * 0.01) { if (Vec3f.substraction(b_final2, b_v).length() < 0.01) {
         * finalq = finalq2; } else if (Vec3f.substraction(b_final3,
         * b_v).length() < 0.01) { finalq = finalq21; } else if
         * (Vec3f.substraction(b_final4, b_v).length() < 0.01) { finalq =
         * finalq31; } else if (Vec3f.substraction(b_final5, b_v).length() <
         * 0.01) { finalq = finalq32; } else if (Vec3f.substraction(b_final6,
         * b_v).length() < 0.01) { finalq = finalq33; } else {
         * System.out.println("error in Quaternion triangle"); } } }
         * setValue(finalq.get(0), finalq.get(1), finalq.get(2), finalq.get(3));
         *
         *
         * }
         */

        //this->setValue(oneplane_n[0],oneplane_n[1],oneplane_n[2],oneplane_n[3]);
    }

    public void fromRotatedBasis(Vec3d a_v, Vec3d b_v, Vec3d c_v, Vec3d a_original_v, Vec3d b_original_v, Vec3d c_original_v) {
        Quaternion qi = new Quaternion();
        Quaternion qf = new Quaternion();
        qf.setFromRotatedBasis(a_v.normalized(), b_v.normalized(), c_v.normalized());
        qi.setFromRotatedBasis(a_original_v.normalized(), b_original_v.normalized(), c_original_v.normalized());
        Quaternion r = Quaternion.multiplication(qf, qi.inverse()).normalized();
        setValue(r.get(0), r.get(1), r.get(2), r.get(3));
    }

    @Override
    public String toString() {
        return "  x: " + this.x() + "  y: " + this.y() + "  z: " + this.z() + "  w: " + this.w();
    }
}
