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

import greta.core.util.math.Quaternion;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public abstract class QuaternionInterpolation extends Interpolation <Quaternion>{

    public QuaternionInterpolation() {

    }

    public void setControlPoints(ArrayList<Quaternion> controlPoints)
    {
        super.setControlPoints(controlPoints);
        calculateLength();
    }


    @Override
    public abstract Quaternion getPosition(double t);

    @Override
    protected double calculateDistance(Quaternion p1, Quaternion p2) {
         double cosAngle = Quaternion.dot(p1, p2);
         if(cosAngle < -1) cosAngle = -1;
         if(cosAngle > 1) cosAngle = 1;
         double angle = (double) java.lang.Math.acos(java.lang.Math.abs(cosAngle));
         //System.out.println("angle"+angle);
         return angle;
    }

}
