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

import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class Tools {

    public static void normalizeFAPs(SubMesh sm, double ENS, double ES, double IRISD, double MNS, double MW) {
        addaptPose1D(sm.getPoseAssignments("FAP_3"), MNS);
        addaptPose1D(sm.getPoseAssignments("FAP_4"), MNS);
        addaptPose1D(sm.getPoseAssignments("FAP_5"), MNS);
        addaptPose1D(sm.getPoseAssignments("FAP_6"), MW);
        addaptPose1D(sm.getPoseAssignments("FAP_7"), MW);
        addaptPose1D(sm.getPoseAssignments("FAP_8"), MNS);
        addaptPose1D(sm.getPoseAssignments("FAP_9"), MNS);
        addaptPose1D(sm.getPoseAssignments("FAP_10"), MNS);
        addaptPose1D(sm.getPoseAssignments("FAP_11"), MNS);
    }

    private static double addaptPose1D(List<PoseAssignment> pose, double targetSize) {
        double scaleFactor = 0;

        for (PoseAssignment p : pose) {
            scaleFactor = Math.max(scaleFactor, p.offset.length());
        }
        scaleFactor = targetSize / scaleFactor;
        for (PoseAssignment p : pose) {
            p.offset.multiply(scaleFactor);
        }

        return scaleFactor;
    }
}
