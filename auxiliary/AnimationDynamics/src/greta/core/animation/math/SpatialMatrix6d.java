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
 * dynamics model from featherstone http://royfeatherstone.org/spatial/
 */
public class SpatialMatrix6d extends MatrixNd<SpatialMatrix6d> {

    public SpatialMatrix6d() {
        super(6,6);
        this.setColumn(0, new double[]{0, 0, 0, 0, 0, 0});
        this.setColumn(1, new double[]{0, 0, 0, 0, 0, 0});
        this.setColumn(2, new double[]{0, 0, 0, 0, 0, 0});
        this.setColumn(3, new double[]{0, 0, 0, 0, 0, 0});
        this.setColumn(4, new double[]{0, 0, 0, 0, 0, 0});
        this.setColumn(5, new double[]{0, 0, 0, 0, 0, 0});
    }

    public SpatialMatrix6d(double v1, double v2, double v3, double v4, double v5, double v6,
            double v7, double v8, double v9, double v10, double v11, double v12,
            double v13, double v14, double v15, double v16, double v17, double v18,
            double v19, double v20, double v21, double v22, double v23, double v24,
            double v25, double v26, double v27, double v28, double v29, double v30,
            double v31, double v32, double v33, double v34, double v35, double v36) {
        super(6,6);
        this.set(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16, v17, v18, v19, v20, v21, v22, v23, v24, v25, v26, v27, v28, v29, v30, v31, v32, v33, v34, v35, v36);
    }

    public SpatialMatrix6d(RealMatrix m) {
        super(6,6);
        this.setRow(0, m.getRow(0));
        this.setRow(1, m.getRow(1));
        this.setRow(2, m.getRow(2));
        this.setRow(3, m.getRow(3));
        this.setRow(4, m.getRow(4));
        this.setRow(5, m.getRow(5));
    }

    public void set(double v1, double v2, double v3, double v4, double v5, double v6,
            double v7, double v8, double v9, double v10, double v11, double v12,
            double v13, double v14, double v15, double v16, double v17, double v18,
            double v19, double v20, double v21, double v22, double v23, double v24,
            double v25, double v26, double v27, double v28, double v29, double v30,
            double v31, double v32, double v33, double v34, double v35, double v36) {
        this.setRow(0, new double[]{v1, v2, v3, v4, v5, v6});
        this.setRow(1, new double[]{v7, v8, v9, v10, v11, v12});
        this.setRow(2, new double[]{v13, v14, v15, v16, v17, v18});
        this.setRow(3, new double[]{v19, v20, v21, v22, v23, v24});
        this.setRow(4, new double[]{v25, v26, v27, v28, v29, v30});
        this.setRow(5, new double[]{v31, v32, v33, v34, v35, v36});
    }

    public static SpatialMatrix6d zero(){
        return new SpatialMatrix6d();
    }

    public static SpatialMatrix6d identity(){
        SpatialMatrix6d sm = new SpatialMatrix6d();
        sm.setEntry(0, 0, 1);
        sm.setEntry(1, 1, 1);
        sm.setEntry(2, 2, 1);
        sm.setEntry(3, 3, 1);
        sm.setEntry(4, 4, 1);
        sm.setEntry(5, 5, 1);
        return sm;
    }

    @Override
    public SpatialMatrix6d copyData(RealMatrix arv) {
        return new SpatialMatrix6d(arv);
    }

    public Matrix3d getRotation() {
        return new Matrix3d(this.getSubMatrix(0, 2, 0, 2));
    }

    public Vector3d getTranslation() {
        return new Vector3d(this.getEntry(4, 2), this.getEntry(3, 2), -this.getEntry(3, 1));
    }

    public SpatialMatrix6d spatial_adjoint(SpatialMatrix6d m) {
        SpatialMatrix6d result = new SpatialMatrix6d(m);
        result.setSubMatrix(m.getSubMatrix(0, 2, 3, 5).transpose().getData(), 3, 0);
        result.setSubMatrix(m.getSubMatrix(3, 5, 0, 2).transpose().getData(), 0, 3);
        return result;
    }

    public SpatialMatrix6d sptial_inverse() {
        SpatialMatrix6d result = new SpatialMatrix6d();
        result.setSubMatrix(this.getSubMatrix(0, 2, 0, 2).transpose().getData(), 0, 0);
        result.setSubMatrix(this.getSubMatrix(0, 2, 3, 5).transpose().getData(), 0, 3);
        result.setSubMatrix(this.getSubMatrix(3, 5, 0, 2).transpose().getData(), 3, 0);
        result.setSubMatrix(this.getSubMatrix(3, 5, 3, 5).transpose().getData(), 3, 3);
        return result;
    }

    public SpatialVector6d multiple(SpatialVector6d v){
        return new SpatialVector6d(super.operate(v));
    }

    public Matrix63d multiple(Matrix63d v){
        return new Matrix63d(super.multiply(v));
    }
}
