/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common;

import vib.core.util.math.Quaternion;
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
