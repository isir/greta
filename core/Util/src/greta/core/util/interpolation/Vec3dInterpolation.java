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
public abstract class Vec3dInterpolation extends Interpolation<Vec3d>{


    public Vec3dInterpolation() {
    }

     public void setControlPoints(ArrayList<Vec3d> controlPoints)
    {
        _controlPoints = controlPoints;calculateLength();
    }
    protected double calculateDistance(Vec3d p1, Vec3d p2) {
        double value = 0;
        for (int i = 0; i < 3; ++i) {
            value += (p1.get(i) - p2.get(i)) * (p1.get(i) - p2.get(i));
        }
        return (double)java.lang.Math.sqrt((double)value);
    }

    protected double calculateLength() {
        double length = 0;
        for (int i = 0; i < _controlPoints.size() - 1; ++i) {
            double lengthTmp = calculateDistance(_controlPoints.get(i), _controlPoints.get(i + 1));
            _segmentLengths.add(lengthTmp);
            _normalizedSegmentLengths.add(lengthTmp);
            length += lengthTmp;
        }
        _normalizedPointPositions.add(0.0); // first point is at t = 0

        for (int i = 0; i < _normalizedSegmentLengths.size(); ++i) {
            _normalizedSegmentLengths.set(i,_normalizedSegmentLengths.get(i) / length);
            _normalizedPointPositions.add(_normalizedPointPositions.get(i) + _normalizedSegmentLengths.get(i));
        }

        assert (_normalizedPointPositions.size() == _controlPoints.size());

        return length;
    }

    public abstract Vec3d getPosition(double t);

    protected Vec3d getReflectedPosition(double t) {
        t = -t;
        Vec3d outerPosition = getPosition(0.0f);
        Vec3d innerPosition = getPosition(t);
        Vec3d difference = Vec3d.substraction(outerPosition, innerPosition);
        return Vec3d.addition(outerPosition, difference);
    }
}
