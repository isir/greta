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

import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)  jhuang@telecom-paristech.fr
 */
public class Matrix63d  extends MatrixNd<Matrix63d>{

     public Matrix63d() {
        super(6,3);
        this.setRow(0, new double[]{0, 0, 0});
        this.setRow(1, new double[]{0, 0, 0});
        this.setRow(2, new double[]{0, 0, 0});
        this.setRow(3, new double[]{0, 0, 0});
        this.setRow(4, new double[]{0, 0, 0});
        this.setRow(5, new double[]{0, 0, 0});
    }

    public Matrix63d(double v1, double v2, double v3,
            double v4, double v5, double v6,
            double v7, double v8, double v9,
            double v10, double v11, double v12,
            double v13, double v14, double v15,
            double v16, double v17, double v18) {
        super(6,3);
        this.setRow(0, new double[]{v1, v2, v3});
        this.setRow(1, new double[]{v4, v5, v6});
        this.setRow(2, new double[]{v7, v8, v9});
        this.setRow(3, new double[]{v10, v11, v12});
        this.setRow(4, new double[]{v13, v14, v15});
        this.setRow(5, new double[]{v16, v17, v18});
    }

    public Matrix63d(RealMatrix m) {
        super(6,3);
        this.setRow(0, m.getRow(0));
        this.setRow(1, m.getRow(1));
        this.setRow(2, m.getRow(2));
        this.setRow(3, m.getRow(3));
        this.setRow(4, m.getRow(4));
        this.setRow(5, m.getRow(5));
    }

    public void set(double v1, double v2, double v3,
            double v4, double v5, double v6,
            double v7, double v8, double v9,
            double v10, double v11, double v12,
            double v13, double v14, double v15,
            double v16, double v17, double v18) {
        this.setRow(0, new double[]{v1, v2, v3});
        this.setRow(1, new double[]{v4, v5, v6});
        this.setRow(2, new double[]{v7, v8, v9});
        this.setRow(3, new double[]{v10, v11, v12});
        this.setRow(4, new double[]{v13, v14, v15});
        this.setRow(5, new double[]{v16, v17, v18});
    }


    public void toZero(){
        this.setRow(0, new double[]{0, 0, 0});
        this.setRow(1, new double[]{0, 0, 0});
        this.setRow(2, new double[]{0, 0, 0});
        this.setRow(3, new double[]{0, 0, 0});
        this.setRow(4, new double[]{0, 0, 0});
        this.setRow(5, new double[]{0, 0, 0});
    }
    @Override
    public Matrix63d copyData(RealMatrix arv) {
        return new Matrix63d(arv);
    }

    public static Matrix63d zero(){
        return new Matrix63d();
    }

    public Vector3d transposeMultiple(SpatialVector6d v){
        return new Vector3d(super.transpose().operate(v));
    }

    public Matrix3d transposeMultiple(Matrix63d v){
        return new Matrix3d(super.transpose().multiply(v));
    }

    public SpatialVector6d multiple(Vector3d v){
        return new SpatialVector6d(super.operate(v));
    }
}
