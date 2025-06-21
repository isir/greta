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
