/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
