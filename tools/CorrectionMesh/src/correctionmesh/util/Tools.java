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
