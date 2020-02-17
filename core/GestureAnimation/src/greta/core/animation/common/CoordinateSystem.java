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

import greta.core.util.math.Quaternion;
import java.util.ArrayList;
import java.util.Iterator;

/**
 *
 * @author Jing Huang
 */
public abstract class CoordinateSystem {

    public CoordinateSystem() {
        init();
    }

    public void rotate(Quaternion rotation) {
        Iterator<Axis> itor = _axes.iterator();
        while (itor.hasNext()) {
            Axis axis = itor.next();
            axis._vect = Quaternion.multiplication(rotation, axis._vect);
            axis.normalize();
        }
    }

    public abstract void drawAxes(double length);

    public abstract void init();

    public abstract void reset();
    public ArrayList<Axis> _axes = new ArrayList<Axis>();
}
