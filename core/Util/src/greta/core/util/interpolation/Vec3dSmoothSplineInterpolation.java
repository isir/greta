/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.core.util.interpolation;

import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class Vec3dSmoothSplineInterpolation extends Vec3dInterpolation {

    protected ArrayList<Vec3d> _interpolatedControlPoints = new ArrayList<Vec3d>();
    protected Vec3dLinearInterpolation _linearInterpolation;
    protected int _iterations = 1 ;
    public Vec3dSmoothSplineInterpolation() {

    }

    public void setControlPoints(ArrayList<Vec3d> controlPoints, int iterations)
    {
        super.setControlPoints(controlPoints);
        _iterations = iterations;
    }

    public void generate(){
        _linearInterpolation = new Vec3dLinearInterpolation();
        _linearInterpolation.setControlPoints(generateInterpolatedPoints(_iterations));
    }
    ArrayList<Vec3d> generateInterpolatedPoints(int numIterations) {
        _interpolatedControlPoints.clear();
        _interpolatedControlPoints = _controlPoints;

        for (int iteration = 0; iteration < numIterations; ++iteration) {
            ArrayList<Vec3d> controlPointsTmp = new ArrayList<Vec3d>();

            for (int segment = 0; segment < _interpolatedControlPoints.size() - 1; ++segment) {
                Vec3d vertices[] = new Vec3d[4];

                if (segment == 0) {
                    vertices[0] = new Vec3d(_interpolatedControlPoints.get(segment));
                } else {
                    vertices[0] = new Vec3d(_interpolatedControlPoints.get(segment - 1));
                }

                vertices[1] = new Vec3d(_interpolatedControlPoints.get(segment));
                vertices[2] = new Vec3d(_interpolatedControlPoints.get(segment + 1));

                if (segment == _interpolatedControlPoints.size() - 2) {
                    vertices[3] = new Vec3d(_interpolatedControlPoints.get(segment + 1));
                } else {
                    vertices[3] = new Vec3d(_interpolatedControlPoints.get(segment + 2));
                }

                Vec3d newVertex = getPosition(vertices[01], vertices[1], vertices[2], vertices[3]);

                controlPointsTmp.add(_interpolatedControlPoints.get(segment));
                controlPointsTmp.add(newVertex);
            }

            controlPointsTmp.add(_interpolatedControlPoints.get(_interpolatedControlPoints.size() - 1));

            assert (controlPointsTmp.size() + 1 == _interpolatedControlPoints.size() * 2);

            _interpolatedControlPoints = controlPointsTmp;
        }

        return _interpolatedControlPoints;
    }

    public Vec3d getPosition(Vec3d p1, Vec3d p2, Vec3d p3, Vec3d p4) {

        Vec3d newVertex = Vec3d.addition(
                Vec3d.multiplication(p3, 9.0f),
                Vec3d.multiplication(p2, 9.0f));
        newVertex = Vec3d.substraction(newVertex, Vec3d.multiplication(p1, 1.0f));
        newVertex = Vec3d.substraction(newVertex, Vec3d.multiplication(p4, 1.0f));

        newVertex = Vec3d.division(newVertex, 16.0f);

        return newVertex;
    }

    @Override
    public Vec3d getPosition(double t) {
        if (t < 0.0f) {
            return getReflectedPosition(t);
        }
        return _linearInterpolation.getPosition(t);
    }
}
