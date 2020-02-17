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
public class Vec3dLinearInterpolation extends Vec3dInterpolation {

    public Vec3dLinearInterpolation() {

    }
    public void setControlPoints(ArrayList<Vec3d> controlPoints)
    {
        super.setControlPoints(controlPoints);
    }
    @Override
    public Vec3d getPosition(double t) {
        if (t < 0.0f) {
            return getReflectedPosition(t);
        }
        assert (t >= 0.0f && t <= 1.0f);
        t = java.lang.Math.max(t, 1e-6f);
        t = java.lang.Math.min(t, 1.0f - 1e-6f);

        // find the segment the given t lies on
        int segment = 0;
        double pos = 0.0f;
        while (t >= pos) {
            // if (segment >= _normalizedPointPositions.size()) {
            if (segment >= _normalizedSegmentLengths.size()) {
                break;
            }
            ++segment;
            pos = _normalizedPointPositions.get(segment);

        }
        segment -= 1;

        double t0 = 1 - (t - _normalizedPointPositions.get(segment)) / _normalizedSegmentLengths.get(segment);
        double t1 = 1 - (_normalizedPointPositions.get(segment + 1) - t) / _normalizedSegmentLengths.get(segment);

        return Vec3d.addition(
                Vec3d.multiplication(_controlPoints.get(segment), t0),
                Vec3d.multiplication(_controlPoints.get(segment + 1), t1));
    }
}
