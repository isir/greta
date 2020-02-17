/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.animation.common;

import greta.core.util.math.Vec3d;

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
