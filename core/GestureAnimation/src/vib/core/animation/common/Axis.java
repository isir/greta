/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common;

import vib.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */


public class Axis {
    public static final int X_AXIS = 0;
    public static final int Y_AXIS = 1;
    public static final int Z_AXIS = 2;

    public Vec3d _vect = new Vec3d();

    public void normalize() {
        _vect.normalize();
    }
}
