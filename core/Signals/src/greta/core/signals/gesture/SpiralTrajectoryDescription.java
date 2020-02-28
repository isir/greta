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
public class SpiralTrajectoryDescription extends TrajectoryDescription{
    public SpiralTrajectoryDescription(){}

    public SpiralTrajectoryDescription(SpiralTrajectoryDescription t){
        super(t);
    }


    public void makeSpiral(int A, int B, Variation space, Variation temps) {
        spatialVariation[A] = space;
        spatialVariation[B] = space;
        temporalVariation[A] = temps;
        temporalVariation[B] = temps;
    }

    public ArrayList<Vec3d> computeSpiral(Vec3d start, Vec3d end, int A, int B, Variation space, Variation temps, int slides, Variation timeVariation) {
        makeSpiral(A, B, space, temps);
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
