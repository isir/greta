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
package greta.core.animation.common.interpolation;

import greta.core.util.math.Quaternion;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class QuaternionSmoothSplineInterpolation extends QuaternionInterpolation {

    ArrayList<Quaternion> _interpolatedControlPoints = new ArrayList<Quaternion>();
    QuaternionSphericalLinearInterpolation _linearInterpolation;

    @Override
    public Quaternion getPosition(double t) {
        return _linearInterpolation.getPosition(t);
    }

    public QuaternionSmoothSplineInterpolation() {
    }

    public void setControlPoints(ArrayList<Quaternion> controlPoints, int iterations) {
        super.setControlPoints(controlPoints);
        _linearInterpolation = new QuaternionSphericalLinearInterpolation();
        _linearInterpolation.setControlPoints(generateInterpolatedPoints(iterations));
    }

    private ArrayList<Quaternion> generateInterpolatedPoints(int numIterations) {
        _interpolatedControlPoints.clear();
        _interpolatedControlPoints = _controlPoints;

        for (int iteration = 0; iteration < numIterations; ++iteration) {
            ArrayList<Quaternion> controlPointsTmp = new ArrayList<Quaternion>();

            for (int segment = 0; segment < _interpolatedControlPoints.size() - 1; ++segment) {
                Quaternion vertices[] = new Quaternion[4];

                if (segment == 0) {
                    vertices[0] = new Quaternion(_interpolatedControlPoints.get(segment));
                } else {
                    vertices[0] = new Quaternion(_interpolatedControlPoints.get(segment - 1));
                }

                vertices[1] = new Quaternion(_interpolatedControlPoints.get(segment));
                vertices[2] = new Quaternion(_interpolatedControlPoints.get(segment + 1));

                if (segment == _interpolatedControlPoints.size() - 2) {
                    vertices[3] = new Quaternion(_interpolatedControlPoints.get(segment + 1));
                } else {
                    vertices[3] = new Quaternion(_interpolatedControlPoints.get(segment + 2));
                }

                Quaternion newVertex0 = Quaternion.slerp(vertices[1], vertices[2], 0.5f, true);
                Quaternion newVertex1 = Quaternion.slerp(vertices[0], vertices[3], 0.5f, true);
                Quaternion newVertex = Quaternion.slerp(newVertex0, newVertex1, 0.1f, true);

                controlPointsTmp.add(_interpolatedControlPoints.get(segment));
                controlPointsTmp.add(newVertex);
            }

            controlPointsTmp.add(_interpolatedControlPoints.get(_interpolatedControlPoints.size() - 1));

            assert (controlPointsTmp.size() + 1 == _interpolatedControlPoints.size() * 2);

            _interpolatedControlPoints = controlPointsTmp;
        }

        return _interpolatedControlPoints;
    }
}
