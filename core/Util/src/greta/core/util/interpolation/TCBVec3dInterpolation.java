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
package greta.core.util.interpolation;

import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * TCB Interpolation
 * @author Jing Huang
 */
public class TCBVec3dInterpolation extends Vec3dSmoothSplineInterpolation {

    private double _t = 0f;
    private double _c = 0.f;
    private double _b = 0.f;

    public TCBVec3dInterpolation() {
    }

    public void setControlPoints(ArrayList<Vec3d> controlPoints, int iterations) {
        super.setControlPoints(controlPoints, iterations);
    }

    public double getBias() {
        return _b;
    }

    public void setBias(double b) {
        this._b = b;
    }

    public double getContinuity() {
        return _c;
    }

    public void setContinuity(double c) {
        this._c = c;
    }

    public double getTension() {
        return _t;
    }

    public void setTension(double t) {
        this._t = t;
    }

    @Override
    ArrayList<Vec3d> generateInterpolatedPoints(int numIterations) {
        _interpolatedControlPoints.clear();
        _interpolatedControlPoints = _controlPoints;


        ArrayList<Vec3d> controlPointsTmp = new ArrayList<Vec3d>();

        for (int segment = 0; segment < _interpolatedControlPoints.size() - 1; ++segment) {
            Vec3d vertices[] = new Vec3d[4];

            if (segment == 0) {
                vertices[0] = new Vec3d(_interpolatedControlPoints.get(0));
            } else {
                vertices[0] = new Vec3d(_interpolatedControlPoints.get(segment - 1));
            }
            if (segment < _interpolatedControlPoints.size() - 2) {
                //vertices[0] = new Vec3d(_interpolatedControlPoints.get(segment - 1));
                vertices[1] = new Vec3d(_interpolatedControlPoints.get(segment));
                vertices[2] = new Vec3d(_interpolatedControlPoints.get(segment + 1));
                vertices[3] = new Vec3d(_interpolatedControlPoints.get(segment + 2));
            } else if (segment < _interpolatedControlPoints.size() - 1) {
                //vertices[0] = new Vec3d(_interpolatedControlPoints.get(segment - 1));
                vertices[1] = new Vec3d(_interpolatedControlPoints.get(segment));
                vertices[2] = new Vec3d(_interpolatedControlPoints.get(segment + 1));
                vertices[3] = new Vec3d(_interpolatedControlPoints.get(segment + 0));
            } else if (segment < _interpolatedControlPoints.size()) {
                //vertices[0] = new Vec3d(_interpolatedControlPoints.get(segment));
                vertices[1] = new Vec3d(_interpolatedControlPoints.get(segment));
                if (segment > 1) {
                    vertices[2] = new Vec3d(_interpolatedControlPoints.get(segment - 1));
                    vertices[3] = new Vec3d(_interpolatedControlPoints.get(segment - 1));
                } else {
                    vertices[2] = new Vec3d(_interpolatedControlPoints.get(segment));
                    vertices[3] = new Vec3d(_interpolatedControlPoints.get(segment));
                }

            }


            for (int iteration = 0; iteration < numIterations; ++iteration) {
                Vec3d newVertex = computePosition((double) iteration / (double) numIterations, vertices[0], vertices[1], vertices[2], vertices[3], _t, _c, _b);
                controlPointsTmp.add(newVertex);
            }
        }

        controlPointsTmp.add(_interpolatedControlPoints.get(_interpolatedControlPoints.size() - 1));

        assert (controlPointsTmp.size() + 1 == _interpolatedControlPoints.size() * 2);

        _interpolatedControlPoints = controlPointsTmp;


        return _interpolatedControlPoints;
    }

    public static Vec3d computePosition(double s, Vec3d p1, Vec3d p2, Vec3d p3, Vec3d p4, double tension, double continuity, double bias) {

        if (s == 0) {
            return p2;
        }
        if (s == 1) {
            return p3;
        }
        double h1 = 2 * java.lang.Math.pow(s, 3.0) - 3 * Math.pow(s, 2.0) + 1;
        double h2 = (-2) * Math.pow(s, 3.0) + 3 * Math.pow(s, 2);
        double h3 = Math.pow(s, 3.0) - 2 * Math.pow(s, 2.0) + s;
        double h4 = Math.pow(s, 3.0) - Math.pow(s, 2);

        double TDix = (1 - tension) * (1 + continuity) * (1 + bias) * (p2.x() - p1.x()) / 2.0 + (1 - tension) * (1 - continuity) * (1 - bias) * (p3.x() - p2.x()) / 2.0;
        double TDiy = (1 - tension) * (1 + continuity) * (1 + bias) * (p2.y() - p1.y()) / 2.0 + (1 - tension) * (1 - continuity) * (1 - bias) * (p3.y() - p2.y()) / 2.0;
        double TDiz = (1 - tension) * (1 + continuity) * (1 + bias) * (p2.z() - p1.z()) / 2.0 + (1 - tension) * (1 - continuity) * (1 - bias) * (p3.z() - p2.z()) / 2.0;

        double TSix = (1 - tension) * (1 - continuity) * (1 + bias) * (p3.x() - p2.x()) / 2.0 + (1 - tension) * (1 + continuity) * (1 - bias) * (p4.x() - p3.x()) / 2.0;
        double TSiy = (1 - tension) * (1 - continuity) * (1 + bias) * (p3.y() - p2.y()) / 2.0 + (1 - tension) * (1 + continuity) * (1 - bias) * (p4.y() - p3.y()) / 2.0;
        double TSiz = (1 - tension) * (1 - continuity) * (1 + bias) * (p3.z() - p2.z()) / 2.0 + (1 - tension) * (1 + continuity) * (1 - bias) * (p4.z() - p3.z()) / 2.0;

        double ppx = h1 * p2.x() + h2 * p3.x() + h3 * TDix + h4 * TSix;
        double ppy = h1 * p2.y() + h2 * p3.y() + h3 * TDiy + h4 * TSiy;
        double ppz = h1 * p2.z() + h2 * p3.z() + h3 * TDiz + h4 * TSiz;
        Vec3d r = new Vec3d((double) ppx, (double) ppy, (double) ppz);
//        System.out.println("PathInterpolation p1" + p1);
//        System.out.println("PathInterpolation added" + r);
//        System.out.println("PathInterpolation p2" + p2);
//        System.out.println("PathInterpolation p3" + p3);
//        System.out.println("PathInterpolation p4" + p4);
        return r;
    }
}
