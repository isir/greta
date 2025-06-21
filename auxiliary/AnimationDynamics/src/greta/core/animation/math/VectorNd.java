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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)  jhuang@telecom-paristech.fr
 */
public abstract class VectorNd<V extends VectorNd> extends ArrayRealVector {

    abstract V copyData(RealVector arv);

    public VectorNd(int dimension){
        super(dimension);
    }

    public V add(V a) {
        return copyData(super.add(a));
    }

    public V substract(V a) {
        return copyData(super.subtract(a));
    }

    public V multiple(double v) {
        return copyData(super.mapMultiply(v));
    }

    public V divide(double v) {
        return copyData(super.mapMultiply(1.0 / v));
    }

    public void set(double[] v) {
        for (int j = 0; j < this.getDimension(); ++j) {
            this.setEntry(j, v[j]);
        }
    }

    public void set(V v) {
        for (int j = 0; j < this.getDimension(); ++j) {
            this.setEntry(j, v.getEntry(j));
        }
    }

    public Array2DRowRealMatrix transpose() {
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(1, this.getDimension());
        m.setRowVector(0, this);
        return m;
    }

    public Array2DRowRealMatrix toMatrix(){
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(this.getDimension(),1);
        m.setColumnVector(0, this);
        return m;
    }

    public Array2DRowRealMatrix toTransposeMatrix(){
        Array2DRowRealMatrix m = new Array2DRowRealMatrix(1, this.getDimension());
        m.setRowVector(0, this);
        return m;
    }

    public void negative() {
        this.multiple(-1);
    }

    public void addToSelf(V a) {
        for (int i = 0; i < a.getDimension(); ++i) {
            this.addToEntry(i, a.getEntry(i));
        }
    }

    public static VectorNd add(VectorNd v1, VectorNd v2, VectorNd v3){
        return v1.add(v2).add(v3);
    }
}
