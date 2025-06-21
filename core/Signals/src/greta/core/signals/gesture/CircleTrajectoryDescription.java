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
package greta.core.signals.gesture;

import greta.core.util.math.Vec3d;
import java.util.ArrayList;

/**
 *
 * @author Jing Huang
 */
public class CircleTrajectoryDescription extends TrajectoryDescription{
    public CircleTrajectoryDescription(){}

    public CircleTrajectoryDescription(CircleTrajectoryDescription t){
        super(t);
    }

     public void makeCircle(int A, int B, float radius) {
        amplitude[A] = radius;
        amplitude[B] = radius;
        temporalVariation[0] = Variation.NONE;
        temporalVariation[1] = Variation.NONE;
        temporalVariation[2] = Variation.NONE;
        if (startDirection == Direction.UP) {
            shift[A] = (float) ((3 - 2 * rotation.ordinal()) * PIon2);
        }
        if (startDirection == Direction.EXTERN) {
            shift[B] = (float) ((1 + 2 * rotation.ordinal()) * PIon2);
        }
        if (startDirection == Direction.DOWN) {
            shift[A] = (float) ((1 + 2 * rotation.ordinal()) * PIon2);
            shift[B] = (float) java.lang.Math.PI;
        }
        if (startDirection == Direction.INTERN) {
            shift[A] = (float) java.lang.Math.PI;
            shift[B] = (float) ((3 - 2 * rotation.ordinal()) * PIon2);
        }
    }


      public ArrayList<Vec3d> computeCircle(Vec3d start, Vec3d end, int A, int B, float amplitude, int slides, Variation timeVariation) {
        makeCircle(A, B, amplitude);
        setStartPosition(start);
        setEndPosition(end);
        ArrayList<Vec3d> list = new ArrayList<Vec3d>();
        if (timeVariation == Variation.GREATER) {
            for (int i = 0; i < slides + 1; i++) {
                float time = easeInQuad((float) i / (float) slides);
                Vec3d pos = getPosition(time, SideType.l);
                list.add(pos);
            }

        } else if (timeVariation == Variation.SMALLER) {
            for (int i = 0; i < slides + 1; i++) {
                float time = easeOutQuad((float) i / (float) slides);
                Vec3d pos = getPosition(time, SideType.l);
                list.add(pos);
            }
        } else {
            for (int i = 0; i < slides + 1; i++) {
                Vec3d pos = getPosition((float) i / (float) slides, SideType.l);
                list.add(pos);
            }
        }
        return list;
    }
}
