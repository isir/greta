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

import greta.core.util.math.Quaternion;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class QuaternionSphericalLinearInterpolation extends QuaternionInterpolation{
    public QuaternionSphericalLinearInterpolation() {
    }

    public void setControlPoints(ArrayList<Quaternion> controlPoints)
    {
        super.setControlPoints(controlPoints);
    }
    /**
     * using spherical linear interpolation by sin(angle) defined parameters
     */
    @Override
    public Quaternion getPosition(double t) {
        //System.out.println(t);
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
        double t_ = (t - _normalizedPointPositions.get(segment)) / _normalizedSegmentLengths.get(segment);

        double angle = _segmentLengths.get(segment);
        double sinAngle = (double) java.lang.Math.sin(angle);
        double c0 = (double) java.lang.Math.sin(angle * (1 - t_)) / sinAngle;
        double c1 = (double) java.lang.Math.sin(angle * t_) / sinAngle;
        Quaternion s1 = _controlPoints.get(segment);
        Quaternion s2 = _controlPoints.get(segment + 1);


        //return Quaternion.slerp(s1, s2, t, true);
//        System.out.println("s1" + s1.x() +" " + s1.y() + " " + s1.z() + " " + s1.w());
//        System.out.println("s2" + s2.x() +" " + s2.y() + " " + s2.z() + " " + s2.w());
//        System.out.println("c0" + c0 +" c1" + c1);
        return new Quaternion(c0 * s1.get(0) + c1 * s2.get(0), c0 * s1.get(1) + c1 * s2.get(1), c0 * s1.get(2) + c1 * s2.get(2), c0 * s1.get(3) + c1 * s2.get(3));
        //return s1;
    }

}
