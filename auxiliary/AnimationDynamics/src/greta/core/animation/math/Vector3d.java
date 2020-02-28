/*
 * This file is part of the auxiliaries of Greta.
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
package greta.core.animation.math;

import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)  jhuang@telecom-paristech.fr
 */
public class Vector3d extends VectorNd<Vector3d> {

    public Vector3d() {
        super(3);
        this.setEntry(0, 0);
        this.setEntry(1, 0);
        this.setEntry(2, 0);
    }

    public Vector3d(RealVector v) {
        super(3);
        this.setEntry(0, v.getEntry(0));
        this.setEntry(1, v.getEntry(1));
        this.setEntry(2, v.getEntry(2));
    }

    public Vector3d(double x, double y, double z) {
        super(3);
        this.set(x, y, z);
    }

    public Vector3d(double[] v) {
        super(3);
        this.set(v[0], v[1], v[2]);
    }

    public void set(double x, double y, double z) {
        this.setEntry(0, x);
        this.setEntry(1, y);
        this.setEntry(2, z);
    }

    public Matrix3d toCrossMatrix() {
        return new Matrix3d(
                0., -getEntry(2), getEntry(1),
                getEntry(2), 0., -getEntry(0),
                -getEntry(1), getEntry(0), 0.
        );
    }

    public Vector3d cross(Vector3d v){
        Vector3d c = new Vector3d(getEntry(1) * v.getEntry(2) - getEntry(2) * v.getEntry(1),
                                  getEntry(2) * v.getEntry(0) - getEntry(0) * v.getEntry(2),
                getEntry(0) * v.getEntry(1) - getEntry(1) * v.getEntry(0)
        );
        return c;
    }

    @Override
    public Vector3d copyData(RealVector arv) {
        return new Vector3d(arv);
    }

    public void normalize(){
        double norm = this.getNorm();
        this.setEntry(0, getEntry(0) / norm);
        this.setEntry(1, getEntry(1) / norm);
        this.setEntry(2, getEntry(2) / norm);
    }

    public static Vector3d zero(){
        return new Vector3d();
    }


    public static Vector3d add(Vector3d a, Vector3d b){
        return new Vector3d(a.getEntry(0) + b.getEntry(0), a.getEntry(1) + b.getEntry(1),a.getEntry(2) + b.getEntry(2));
    }

}
