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
