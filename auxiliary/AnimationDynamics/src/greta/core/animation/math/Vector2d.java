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
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class Vector2d extends VectorNd<Vector2d> {
    public Vector2d() {
        super(2);
        this.setEntry(0, 0);
        this.setEntry(1, 0);
    }

    public Vector2d(RealVector v) {
        super(2);
        this.setEntry(0, v.getEntry(0));
        this.setEntry(1, v.getEntry(1));
    }

    public Vector2d(double x, double y) {
        super(2);
        this.set(x, y);
    }

    public Vector2d(double[] v) {
        super(2);
        this.set(v[0], v[1]);
    }

    public void set(double x, double y) {
        this.setEntry(0, x);
        this.setEntry(1, y);
    }


    public Vector2d cross(Vector3d v){
        Vector2d c = new Vector2d(getEntry(1) * v.getEntry(2) - getEntry(2) * v.getEntry(1),
                                  getEntry(2) * v.getEntry(0) - getEntry(0) * v.getEntry(2)
        );
        return c;
    }

    @Override
    public Vector2d copyData(RealVector arv) {
        return new Vector2d(arv);
    }

    public void normalize(){
        double norm = this.getNorm();
        this.setEntry(0, getEntry(0) / norm);
        this.setEntry(1, getEntry(1) / norm);
    }

    public static Vector2d zero(){
        return new Vector2d();
    }

}
