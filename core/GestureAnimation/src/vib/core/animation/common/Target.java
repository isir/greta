/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common;

import vib.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class Target {

    public Target(Vec3d position, Vec3d upVector) {
        _position = position;
        _upDirectionVector = upVector;
    }

    public Target(Vec3d position, Vec3d upVector, String name) {
        _position = position;
        _upDirectionVector = upVector;
        _name = name;
    }

    public void setEffector(String name) {
        _name = name;
    }

    public String getEffector() {
        return _name;
    }

    public Target(Vec3d position) {
        _position = position;
    }

    public Target() {
    }

    public Vec3d getPosition() {
        return _position;
    }

    public void setPosition(Vec3d position) {
        _position = position;
    }

    public Vec3d getUpDirectionVector() {
        return _upDirectionVector;
    }
    /*
     ** define the orientation of bones
     */

    public void setUpDirectionVector(Vec3d v) {
        _upDirectionVector = v;
    }

    public double getEnergy() {
        return _energy;
    }

    public void setEnergy(double energy) {
        this._energy = energy;
    }


    public Target(Target target) {
        _position = target.getPosition().clone();
        _upDirectionVector = target.getUpDirectionVector().clone();
        _energy = target.getEnergy();
        _name = target.getEffector();
    }

    public static Target interpolate(Target t0, Target t1, double t) {
        Vec3d pos = Vec3d.addition(Vec3d.multiplication(t0._position, t), Vec3d.multiplication(t1._position, (1 - t)));
        Vec3d up = Vec3d.addition(Vec3d.multiplication(t0._upDirectionVector, t), Vec3d.multiplication(t1._upDirectionVector, (1 - t)));
        return new Target(pos, up);
    }

    @Override
    public Target clone() {
        return new Target(this);
    }

    @Override
    public String toString() {
        return new String("pos: " + _position + " upvecter: " + _upDirectionVector);
    }
    private Vec3d _position = new Vec3d();
    private Vec3d _upDirectionVector = new Vec3d(0, 1, 0);
    private double _energy = 0.1f;
    private String _name = "";
}
