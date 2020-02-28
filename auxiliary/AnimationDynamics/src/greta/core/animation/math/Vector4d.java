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
public class Vector4d  extends VectorNd<Vector4d>{
    public Vector4d(){
        super(4);
        this.set(0, 0, 0, 0);
    }

    public Vector4d(RealVector v) {
        super(4);
        this.setEntry(0,v.getEntry(0));
        this.setEntry(1,v.getEntry(1));
        this.setEntry(2,v.getEntry(2));
        this.setEntry(3,v.getEntry(3));
    }

    public Vector4d(double x, double y, double z, double w){
        super(4);
        this.set(x, y, z, w);
    }

    public void set(double x, double y, double z, double w) {
        this.setEntry(0, x);
        this.setEntry(1, y);
        this.setEntry(2, z);
        this.setEntry(3, w);
    }


    @Override
    public Vector4d copyData(RealVector arv) {
        return new Vector4d(arv);
    }

    public static Vector4d zero(){
        return new Vector4d();
    }
}
