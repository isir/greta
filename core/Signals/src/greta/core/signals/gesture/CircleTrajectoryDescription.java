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
