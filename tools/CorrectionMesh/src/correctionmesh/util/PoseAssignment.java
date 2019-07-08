/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package correctionmesh.util;

import vib.core.util.math.Vec3d;

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
