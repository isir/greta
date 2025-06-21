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
