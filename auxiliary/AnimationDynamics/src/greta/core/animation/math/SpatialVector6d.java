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
 * dynamics model from featherstone http://royfeatherstone.org/spatial/
 */
public class SpatialVector6d extends VectorNd<SpatialVector6d> {

    public SpatialVector6d() {
        super(6);
        this.set(0, 0, 0, 0, 0, 0);
    }

    public SpatialVector6d(double v0, double v1, double v2, double v3, double v4, double v5) {
        super(6);
        this.set(v0, v1, v2, v3, v4, v5);
    }

    public SpatialVector6d(RealVector v) {
        super(6);
        this.setEntry(0, v.getEntry(0));
        this.setEntry(1, v.getEntry(1));
        this.setEntry(2, v.getEntry(2));
        this.setEntry(3, v.getEntry(3));
        this.setEntry(4, v.getEntry(4));
        this.setEntry(5, v.getEntry(5));
    }

    public SpatialVector6d(Vector3d upper, Vector3d lower) {
        super(6);
        this.setEntry(0, upper.getEntry(0));
        this.setEntry(1, upper.getEntry(1));
        this.setEntry(2, upper.getEntry(2));
        this.setEntry(3, lower.getEntry(0));
        this.setEntry(4, lower.getEntry(1));
        this.setEntry(5, lower.getEntry(2));
    }

    public void set(double v0, double v1, double v2, double v3, double v4, double v5) {
        this.setEntry(0, v0);
        this.setEntry(1, v1);
        this.setEntry(2, v2);
        this.setEntry(3, v3);
        this.setEntry(4, v4);
        this.setEntry(5, v5);
    }


    public void setZero() {
        this.set(0, 0, 0, 0, 0, 0);
    }

    public static SpatialVector6d zero() {
        return new SpatialVector6d();
    }

    @Override
    public SpatialVector6d copyData(RealVector arv) {
        return new SpatialVector6d(arv);
    }

    public Vector3d getUpper() {
        return new Vector3d(getEntry(0), getEntry(1), getEntry(2));
    }

    public Vector3d getLower() {
        return new Vector3d(getEntry(3), getEntry(4), getEntry(5));
    }

    public SpatialMatrix6d crossM() {
        return new SpatialMatrix6d(
                0, -getEntry(2), getEntry(1), 0, 0, 0,
                getEntry(2), 0, -getEntry(0), 0, 0, 0,
                -getEntry(1), getEntry(0), 0, 0, 0, 0,
                0, -getEntry(5), getEntry(4), 0, -getEntry(2), getEntry(1),
                getEntry(5), 0, -getEntry(3), getEntry(2), 0, -getEntry(0),
                -getEntry(4), getEntry(3), 0, -getEntry(1), getEntry(0), 0
        );
    }

    public static SpatialVector6d crossM(SpatialVector6d v1, SpatialVector6d v2) {
        return new SpatialVector6d(
                -v1.getEntry(2) * v2.getEntry(1) + v1.getEntry(1) * v2.getEntry(2),
                v1.getEntry(2) * v2.getEntry(0) - v1.getEntry(0) * v2.getEntry(2),
                -v1.getEntry(1) * v2.getEntry(0) + v1.getEntry(0) * v2.getEntry(1),
                -v1.getEntry(5) * v2.getEntry(1) + v1.getEntry(4) * v2.getEntry(2) - v1.getEntry(2) * v2.getEntry(4) + v1.getEntry(1) * v2.getEntry(5),
                v1.getEntry(5) * v2.getEntry(0) - v1.getEntry(3) * v2.getEntry(2) + v1.getEntry(2) * v2.getEntry(3) - v1.getEntry(0) * v2.getEntry(5),
                -v1.getEntry(4) * v2.getEntry(0) + v1.getEntry(3) * v2.getEntry(1) - v1.getEntry(1) * v2.getEntry(3) + v1.getEntry(0) * v2.getEntry(4)
        );
    }

    public SpatialMatrix6d crossF() {
        return new SpatialMatrix6d(
                0, -getEntry(2), getEntry(1), 0, -getEntry(5), getEntry(4),
                getEntry(2), 0, -getEntry(0), getEntry(5), 0, -getEntry(3),
                -getEntry(1), getEntry(0), 0, -getEntry(4), getEntry(3), 0,
                0, 0, 0, 0, -getEntry(2), getEntry(1),
                0, 0, 0, getEntry(2), 0, -getEntry(0),
                0, 0, 0, -getEntry(1), getEntry(0), 0
        );
    }

    public static SpatialVector6d crossF(SpatialVector6d v1, SpatialVector6d v2) {
        return new SpatialVector6d(
                -v1.getEntry(2) * v2.getEntry(1) + v1.getEntry(1) * v2.getEntry(2) - v1.getEntry(5) * v2.getEntry(4) + v1.getEntry(4) * v2.getEntry(5),
                v1.getEntry(2) * v2.getEntry(0) - v1.getEntry(0) * v2.getEntry(2) + v1.getEntry(5) * v2.getEntry(3) - v1.getEntry(3) * v2.getEntry(5),
                -v1.getEntry(1) * v2.getEntry(0) + v1.getEntry(0) * v2.getEntry(1) - v1.getEntry(4) * v2.getEntry(3) + v1.getEntry(3) * v2.getEntry(4),
                -v1.getEntry(2) * v2.getEntry(4) + v1.getEntry(1) * v2.getEntry(5),
                +v1.getEntry(2) * v2.getEntry(3) - v1.getEntry(0) * v2.getEntry(5),
                -v1.getEntry(1) * v2.getEntry(3) + v1.getEntry(0) * v2.getEntry(4)
        );
    }

    public boolean isZero(){
        if(getEntry(0) == 0 && getEntry(1) == 0&& getEntry(2) == 0&& getEntry(3) == 0&& getEntry(4) == 0&& getEntry(5) == 0){
            return true;
        }
        return false;
    }
}
