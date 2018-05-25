/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation;

import vib.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public abstract class MassConstraint {

    public String _name = "";
    boolean _active = true;

    public boolean isActive() {
        return _active;
    }

    public void setActive(boolean active) {
        this._active = active;
    }

    public abstract Vec3d getForce();

    public abstract Vec3d getForceDirection();

    public abstract double getForceEnergy();
}
