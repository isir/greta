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

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Jing Huang (http://perso.telecom-paristech.fr/~jhuang/)  jhuang@telecom-paristech.fr
 */
public abstract class MatrixNd<M extends MatrixNd> extends Array2DRowRealMatrix {

    public abstract M copyData(RealMatrix arv);

    public MatrixNd(int rowDimension, int columnDimension) {
        super(rowDimension, columnDimension);
    }

    public M add(M a) {
        return copyData(super.add(a));
    }

    public void addToSelf(M a) {
        for (int i = 0; i < a.getColumnDimension(); ++i) {
            for (int j = 0; j < a.getRowDimension(); ++j) {
                this.addToEntry(j, i, a.getEntry(j, i));
            }
        }
    }

    public void set(double[] v) {
        int n = 0;
        for (int j = 0; j < this.getRowDimension(); ++j) {
            for (int i = 0; i < this.getColumnDimension(); ++i) {
                this.addToEntry(j, i, v[n]);
                ++n;
            }
        }
    }

    public void set(M v){
        for (int j = 0; j < this.getRowDimension(); ++j) {
            for (int i = 0; i < this.getColumnDimension(); ++i) {
                this.setEntry(j, i, v.getEntry(j, i));
            }
        }
    }

    public M substract(M a) {
        return copyData(super.subtract(a));
    }

    public M multiple(double v) {
        return copyData(super.scalarMultiply(v));
    }

    public M multiple(M v) {
        return copyData(super.multiply(v));
    }

    public M divide(double v) {
        return copyData(super.scalarMultiply(1.0 / v));
    }

    @Override
    public RealMatrix transpose() {
        return super.transpose();
    }

    public void negative() {
        this.multiple(-1);
    }

    public static MatrixNd add(MatrixNd v1, MatrixNd v2, MatrixNd v3) {
        return v1.add(v2).add(v3);
    }

    public static MatrixNd add(MatrixNd v1, MatrixNd v2) {
        return v1.add(v2);
    }

    public static MatrixNd multiple(MatrixNd v1, MatrixNd v2, MatrixNd v3) {
        assert (v1.getRowDimension() == v1.getColumnDimension());
        assert (v2.getRowDimension() == v2.getColumnDimension());
        assert (v3.getRowDimension() == v3.getColumnDimension());
        return v1.multiple(v2).multiple(v3);
    }

    public static MatrixNd multiple(MatrixNd v1, MatrixNd v2) {
        assert (v1.getRowDimension() == v1.getColumnDimension());
        assert (v2.getRowDimension() == v2.getColumnDimension());
        return v1.multiple(v2);
    }
}
