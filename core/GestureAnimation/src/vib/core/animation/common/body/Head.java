/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.body;

import vib.core.animation.common.Frame.ExtendedKeyFrame;
import vib.core.animation.common.Skeleton;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;
import vib.core.util.math.easefunctions.EaseInOutSine;

/**
 *
 * @author Jing Huang
 */
public class Head extends ExtendedKeyFrame {
    Skeleton _skeleton;
    Quaternion r = new Quaternion();
    Vec3d _gazeTarget;

    public Skeleton getSkeleton() {
        return _skeleton;
    }

    public void setSkeleton(Skeleton _skeleton) {
        this._skeleton = _skeleton;
    }
    public Vec3d getGazeTarget() {
        return _gazeTarget;
    }

    public void setGazeTarget(Vec3d _gazeTarget) {
        this._gazeTarget = _gazeTarget;
    }
    public Head(double time) {
        super(time);
        setFunction(new EaseInOutSine());
    }

    public void setRotation(Quaternion q) {
        r = new Quaternion(q);
    }

    public Quaternion getRotation() {
        return r;
    }

    public void pointToDirection(Vec3d direction) {
        Vec3d original = new Vec3d(0, 0, 1);
        Vec3d n = Vec3d.cross3(original, direction);
        double cosTheta = original.dot3(direction);
        cosTheta = 1 < cosTheta ? 1 : cosTheta;
        double angle = (double) java.lang.Math.acos(cosTheta);
        Quaternion global = new Quaternion(n, angle);
    }

    @Override
    public Head clone() {
        return (Head) super.clone();
    }
}
