/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common;

import vib.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class LocalCoordinateSystem extends CoordinateSystem{

    public LocalCoordinateSystem(){ init();}


    @Override
    public void drawAxes(double length) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init() {
        _axes.add(new Axis());
        _axes.add(new Axis());
        _axes.add(new Axis());
	_axes.get(Axis.X_AXIS)._vect = new Vec3d(1,0,0);
        _axes.get(Axis.Y_AXIS)._vect = new Vec3d(0,1,0);
	_axes.get(Axis.Z_AXIS)._vect = new Vec3d(0,0,1);
    }

    @Override
    public void reset() {
        _axes.get(Axis.X_AXIS)._vect = new Vec3d(1,0,0);
        _axes.get(Axis.Y_AXIS)._vect = new Vec3d(0,1,0);
	_axes.get(Axis.Z_AXIS)._vect = new Vec3d(0,0,1);
    }

}
