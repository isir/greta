/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.body;

import vib.core.animation.Frame;
import vib.core.keyframes.ExpressivityParameters;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class Torso extends ExpressiveFrame {


    public Torso() {
    }

    public Torso(Torso torso) {
       // _r = torso._r.clone();
        _time = torso._time;
    }


    public Quaternion getOrientationByPointToDirection(Vec3d direction) {
        Vec3d original = new Vec3d(0, 0, 1);
        Vec3d n = Vec3d.cross3(original, direction);
        double cosTheta = original.dot3(direction);
        cosTheta = 1 < cosTheta ? 1 : cosTheta;
        double angle = (double) java.lang.Math.acos(cosTheta);
        Quaternion global = new Quaternion(n, angle);
        return global;
    }

    @Override
    public Torso clone() {
        return new Torso(this);
    }

   
}
