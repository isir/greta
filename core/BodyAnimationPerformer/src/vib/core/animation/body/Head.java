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
public class Head extends ExpressiveFrame {
    Quaternion _r = new Quaternion();
    Vec3d _gazeTarget;

    public Head(){

    }

    public Head(Head head){
        _r = head._r.clone();
        _gazeTarget = head._gazeTarget.clone();
        _time = head._time;
    }

    public Vec3d getGazeTarget() {
        return _gazeTarget;
    }

    public void setGazeTarget(Vec3d _gazeTarget) {
        this._gazeTarget = _gazeTarget;
    }
    public Head(double time) {
        _time = time;
    }

    public void setRotation(Quaternion q) {
        _r = new Quaternion(q);
    }

    public Quaternion getRotation() {
        return _r;
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
    public Head clone() {
        return new Head(this);
    }

  
}
