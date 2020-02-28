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
public class Matrix4d extends MatrixNd<Matrix4d>{
    public Matrix4d(){
        super(4,4);
        this.setColumn(0, new double[]{0,0,0,0});
        this.setColumn(1, new double[]{0,0,0,0});
        this.setColumn(2, new double[]{0,0,0,0});
        this.setColumn(3, new double[]{0,0,0,0});
    }

    public Matrix4d(double v1, double v2, double v3, double v4,
            double v5, double v6, double v7, double v8,
            double v9, double v10, double v11, double v12,
            double v13, double v14, double v15, double v16){
        super(4,4);
        this.set(v1, v2, v3, v4, v5, v6, v7, v8, v9, v10, v11, v12, v13, v14, v15, v16);
    }

    public Matrix4d(RealMatrix m){
        super(4,4);
        this.setRow(0, m.getRow(0));
        this.setRow(1, m.getRow(1));
        this.setRow(2, m.getRow(2));
        this.setRow(3, m.getRow(3));
    }

    public void set(double v1, double v2, double v3, double v4,
            double v5, double v6, double v7, double v8,
            double v9, double v10, double v11, double v12,
            double v13, double v14, double v15, double v16){
        this.setRow(0, new double[]{v1,v2,v3,v4});
        this.setRow(1, new double[]{v5,v6,v7,v8});
        this.setRow(2, new double[]{v9,v10,v11,v12});
        this.setRow(2, new double[]{v13,v14,v15,v16});
    }

    public void toIdentity(){
        this.setRow(0, new double[]{1,0,0,0});
        this.setRow(1, new double[]{0,1,0,0});
        this.setRow(2, new double[]{0,0,1,0});
        this.setRow(2, new double[]{0,0,0,1});
    }

    public void toMIdentity(double m){
        this.setRow(0, new double[]{m,0,0,0});
        this.setRow(1, new double[]{0,m,0,0});
        this.setRow(2, new double[]{0,0,m,0});
        this.setRow(2, new double[]{0,0,0,m});
    }

    public void toZero(){
        this.setColumn(0, new double[]{0,0,0,0});
        this.setColumn(1, new double[]{0,0,0,0});
        this.setColumn(2, new double[]{0,0,0,0});
        this.setColumn(3, new double[]{0,0,0,0});
    }


    @Override
    public Matrix4d copyData(RealMatrix arv) {
        return new Matrix4d(arv);
    }


    public Vector4d multiple(Vector4d v){
        return new Vector4d(this.operate(v));
    }

    public static Matrix4d zero(){
        return new Matrix4d();
    }

    public static Matrix4d identity(){
        Matrix4d sm = new Matrix4d();
        sm.setEntry(0, 0, 1);
        sm.setEntry(1, 1, 1);
        sm.setEntry(2, 2, 1);
        sm.setEntry(3, 3, 1);
        return sm;
    }

    @Override
    public Matrix4d transpose(){
        return copyData(super.transpose());
    }
}
