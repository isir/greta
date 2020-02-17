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
package greta.core.animation.common.interpolation;

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
