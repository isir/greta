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
package greta.core.animation.body;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;

/**
 *
 * @author Jing Huang
 */
public class Torso extends ExpressiveFrame {

    Quaternion _r = new Quaternion(); // torso rotation

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

    public void setRotation(Quaternion q) {
        _r = new Quaternion(q);
    }

    public Quaternion getRotation() {
        return _r;
    }


}
