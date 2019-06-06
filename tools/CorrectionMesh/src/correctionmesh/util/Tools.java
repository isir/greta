/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
