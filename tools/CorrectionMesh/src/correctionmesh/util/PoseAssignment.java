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
package correctionmesh.util;

import greta.core.util.math.Vec3d;

/**
 *
 * @author Andre-Marie Pez
 */
public class PoseAssignment {

    public Vec3d offset;
    public String poseName;

    public PoseAssignment(String pose, double x, double y, double z) {
        offset = new Vec3d(x, y, z);
        poseName = pose;
    }

    public PoseAssignment(PoseAssignment toCopy) {
        offset = new Vec3d(toCopy.offset);
        poseName = toCopy.poseName;
    }
}
