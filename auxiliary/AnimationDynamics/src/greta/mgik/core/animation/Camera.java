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
package greta.mgik.core.animation;

import greta.core.util.math.Matrix4d;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import greta.core.util.math.Vec4d;
import static org.lwjgl.opengl.GL11.*;

/**
 *
 * @author Jing Huang
 */
public class Camera {

    static boolean _spinning, _moving;
    static int _beginu, _beginv;
    static Quaternion _curquat = new Quaternion();
    static double _x, _y, _z;
    static double __zoom;
    static boolean ini = false;
    private double fovAngle;
    private double aspectRatio;
    private double nearPlane;
    private double farPlane;
    private boolean spinning, moving;
    private int beginu, beginv;
    private int h, w;
    private Quaternion curquat = new Quaternion();
    private Quaternion lastquat = new Quaternion();
    private double x, y, z;
    private double zoom;
    Matrix4d modelviewMatrix = new Matrix4d();
    Matrix4d projectionMatrix = new Matrix4d();

    public Camera() {
        fovAngle = 45.0f;
        aspectRatio = 1.0f;
        nearPlane = 0.1f;
        farPlane = 10000.0f;

        spinning = false;
        moving = false;
        beginu = 0;
        beginv = 0;
        x = y = z = (double) 0.0;
        zoom = 10.0f;
        curquat = trackBall(0, 0, 0, 0);
    }

    public void gluPerspective(double fovy, double aspect, double zNear, double zFar) {
        float sine, cotangent, deltaZ;
        float radians = (float) (fovy / 2 * 3.14159265 / 180);

        deltaZ = (float) (zFar - zNear);
        sine = (float) Math.sin(radians);

        if ((deltaZ == 0) || (sine == 0) || (aspect == 0)) {
            return;
        }

        cotangent = (float) Math.cos(radians) / sine;

        Matrix4d matrix = new Matrix4d();

        matrix.set(0 * 4, 0, cotangent / aspect);
        matrix.set(1 * 4, 1, cotangent);
        matrix.set(2 * 4, 2, -(zFar + zNear) / deltaZ);
        matrix.set(2 * 4, 3, -1);
        matrix.set(3 * 4, 2, -2 * zNear * zFar / deltaZ);
        matrix.set(3 * 4, 3, 0);

        glMultMatrixf(matrix.getData1DFloat());
    }

    public void resize(int _W, int _H) {
        h = _H;
        w = _W;
        glViewport(0, 0, w, h);
        glMatrixMode(GL_PROJECTION);
        glLoadIdentity();
        aspectRatio = (double) w / (double) h;
        gluPerspective(fovAngle, aspectRatio, nearPlane, farPlane);
        glMatrixMode(GL_MODELVIEW);
    }

    public void initPos() {
        if (!ini) {
            _spinning = spinning;
            _moving = moving;
            _beginu = beginu;
            _beginv = beginv;
            _curquat.setValue(curquat);
            _x = x;
            _y = y;
            _z = z;
            __zoom = zoom;
            ini = true;
        } else {
            spinning = _spinning;
            moving = _moving;
            beginu = _beginu;
            beginv = _beginv;
            curquat.setValue(_curquat);
            x = _x;
            y = _y;
            z = _z;
            zoom = __zoom;
        }
    }

    public void move(double dx, double dy, double dz) {
        x += dx;
        y += dy;
        z += dz;
    }

    public void beginRotate(int u, int v) {
        beginu = u;
        beginv = v;
        moving = true;
        spinning = false;
    }

    public void rotate(int u, int v) {
        if (moving) {
            lastquat = trackBall(
                    (2.0f * (double) beginu - (double) w) / (double) w,
                    ((double) h - 2.0f * (double) beginv) / (double) h,
                    (2.0f * (double) u - (double) w) / (double) w,
                    ((double) h - 2.0f * (double) v) / (double) h);
            beginu = u;
            beginv = v;
            spinning = true;
            curquat = Quaternion.multiplication(lastquat, curquat);
            // add_quats(lastquat, curquat, curquat);
        }
    }

    public void endRotate() {
        moving = false;
    }

    public void zoom(double z) {
        zoom += z;
    }

    public void apply() {
        glLoadIdentity();
        glTranslatef((float) x, (float) y, (float) z);
        Matrix4d m = curquat.matrix();
        //build_rotmatrix(m, curquat);
        glTranslatef(0.0f, 0.0f, -(float) zoom);
        glMultMatrixf(m.getData1DFloat());

        float[] projection = new float[16];
        float[] modelview = new float[16];
        glMatrixMode(GL_PROJECTION);
        glGetFloatv(GL_PROJECTION_MATRIX, projection);
        glMatrixMode(GL_MODELVIEW);
        glGetFloatv(GL_MODELVIEW_MATRIX, modelview);
        modelviewMatrix = new Matrix4d(modelview[0], modelview[1], modelview[2], modelview[3],
                modelview[4], modelview[5], modelview[6], modelview[7],
                modelview[8], modelview[9], modelview[10], modelview[11],
                modelview[12], modelview[13], modelview[14], modelview[15]);
        modelviewMatrix.transpose();
        projectionMatrix = new Matrix4d(projection[0], projection[1], projection[2], projection[3],
                projection[4], projection[5], projection[6], projection[7],
                projection[8], projection[9], projection[10], projection[11],
                projection[12], projection[13], projection[14], projection[15]);
        projectionMatrix.transpose();

    }

    public Vec4d getScreenCoordinateOf(Vec4d v) {
        //Vec4d r = Matrix4d.multiplication(projectionMatrix,  Matrix4d.multiplication(modelviewMatrix, v) );
        Matrix4d mf = Matrix4d.multiplication(projectionMatrix, modelviewMatrix);
        Vec4d r = Matrix4d.multiplication(mf, v);
        r.set(0, (r.x() / r.w() + 1.0f) * 0.5f * w);
        r.set(1, (r.y() / r.w() + 1.0f) * 0.5f * h);
        return r;
    }

    public Vec4d get3DTrackPositionOfScreenCoordinateOf(Vec4d ref, double screenX, double screenY) {
        Matrix4d mf = Matrix4d.multiplication(projectionMatrix, modelviewMatrix);
        Vec4d r = Matrix4d.multiplication(mf, ref);
        //double xS = (r.x() / r.w()  + 1.0f)  * 0.5f * w;
        //double yS = (r.y() / r.w()  + 1.0f)  * 0.5f * h;
        r.set(0, (screenX * 2.0f / w - 1.0f) * r.w());
        r.set(1, (screenY * 2.0f / h - 1.0f) * r.w());
        Matrix4d inv = new Matrix4d();
        mf.inverse(inv);
        Vec4d pos = Matrix4d.multiplication(inv, r);
        return pos;
    }

    Quaternion trackBall(double p1x, double p1y, double p2x, double p2y) {
        Quaternion q = new Quaternion();

        if (p1x == p2x && p1y == p2y) {
            /* Zero rotation */
            return q;
        }

        double TRACKBALLSIZE = 0.8f;

        /*
         * First, figure out z-coordinates for projection of P1 and P2 to
         * deformed sphere
         */
        Vec3d p1 = new Vec3d(p1x, p1y, tb_project_to_sphere(TRACKBALLSIZE, p1x, p1y));
        Vec3d p2 = new Vec3d(p2x, p2y, tb_project_to_sphere(TRACKBALLSIZE, p2x, p2y));

        /*
         *  Now, we want the cross product of P1 and P2
         */
        Vec3d a = Vec3d.cross3(p2, p1);   /* Axis of rotation */

        /*
         *  Figure out how much to rotate around that axis.
         */
        Vec3d d = Vec3d.substraction(p1, p2);
        double t = d.length() / (2.0f * TRACKBALLSIZE);

        /*
         * Avoid problems with out-of-control values...
         */
        if (t > 1.0) {
            t = 1.0f;
        }
        if (t < -1.0) {
            t = -1.0f;
        }

        double phi = (double) (2.0 * java.lang.Math.asin(t));  /* how much to rotate about axis */

        q.setAxisAngle(a, phi);

        return q;
    }

    double tb_project_to_sphere(double r, double x, double y) {
        double d, t, z;
        d = (double) java.lang.Math.sqrt(x * x + y * y);
        if (d < r * 0.70710678118654752440) {    /* Inside sphere */

            z = (double) java.lang.Math.sqrt(r * r - d * d);
        } else {           /* On hyperbola */

            t = r / 1.41421356237309504880f;
            z = t * t / d;
        }
        return z;
    }
}
