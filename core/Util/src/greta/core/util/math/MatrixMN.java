/*
 * This file is part of Greta.
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
package greta.core.util.math;

/**
 * Encapsulates a 4x4 matrix
 *
 *
 */
public class MatrixMN {
  protected int numRows;
  protected int numCols;

  public double[][] elements;

  /**
   * Constructs new 4x4 matrix, initializes to it to zeros
   */
  public MatrixMN() {
    numRows= 4; numCols= 4;
    elements= new double[numRows][numCols];
    for(int r=0; r<numRows; r++)
      for(int c=0; c<numCols; c++) {
        elements[r][c]= 0.0;
      }
  }

  public MatrixMN(int rows, int cols) {
    numRows= rows; numCols= cols;
    elements= new double[numRows][numCols];
    for(int r=0; r<numRows; r++)
      for(int c=0; c<numCols; c++) {
        elements[r][c]= 0.0;
      }
  }

  /**
   * Copy constructor
   */
  public MatrixMN(MatrixMN m) {
    numRows= m.numRows;
    numCols= m.numCols;
    elements= new double[numRows][numCols];
    for(int r=0; r<numRows; r++)
      for(int c=0; c<numCols; c++)
        this.elements[r][c]= m.elements[r][c];
  }

  public int getNumRows() {
    return numRows;
  }

  public int getNumCols() {
    return numCols;
  }

  /**
   * @param tx ty tz translations in the x,y, and z directions
   * @returns the associated translation transformation matrix
   */
  public static MatrixMN translation(double tx, double ty, double tz) {
    MatrixMN T= new MatrixMN();
    T.elements[0][0]= 1;
    T.elements[1][1]= 1;
    T.elements[2][2]= 1;
    T.elements[3][3]= 1;
    T.elements[3][0]= tx;
    T.elements[3][1]= ty;
    T.elements[3][2]= tz;
    return T;
  }

  /**
   * @param sx sy sz translations in the x,y, and z directions
   * @returns the associated scaling transformation matrix
   */
  public static MatrixMN scaling(double sx, double sy, double sz) {
    MatrixMN S= new MatrixMN();
    S.elements[0][0]= sx;
    S.elements[1][1]= sy;
    S.elements[2][2]= sz;
    S.elements[3][3]= 1;
    return S;
  }

  /**
   * @param theta an angle in radians
   * @return the associated x-axis rotation transformation matrix
   */
  public static MatrixMN xRotation(double theta) {
    MatrixMN R= new MatrixMN();
    double c= Math.cos(theta);
    double s= Math.sin(theta);
    R.elements[0][0]= 1;
    R.elements[1][1]= c;
    R.elements[2][2]= c;
    R.elements[3][3]= 1;
    R.elements[1][2]= s;
    R.elements[2][1]= -s;
    return R;
  }

  /**
   * @param theta an angle in radians
   * @return the associated y-axis rotation transformation matrix
   */
  public static MatrixMN yRotation(double theta) {
    MatrixMN R= new MatrixMN();
    double c= Math.cos(theta);
    double s= Math.sin(theta);
    R.elements[0][0]= c;
    R.elements[1][1]= 1;
    R.elements[2][2]= c;
    R.elements[3][3]= 1;
    R.elements[2][0]= s;
    R.elements[0][2]= -s;
    return R;
  }

  /**
   * @param theta an angle in radians
   * @return the associated z-axis rotation transformation matrix
   */
  public static MatrixMN zRotation(double theta) {
    MatrixMN R= new MatrixMN();
    double c= Math.cos(theta);
    double s= Math.sin(theta);
    R.elements[0][0]= c;
    R.elements[1][1]= c;
    R.elements[2][2]= 1;
    R.elements[3][3]= 1;
    R.elements[0][1]= s;
    R.elements[1][0]= -s;
    return R;
  }

  /**
   * @param m a Matrix
   * @return a new matrix which is equal to the sum of this + m
   */
  public MatrixMN addTo (MatrixMN m) {
    if(numRows!=m.numRows || numCols!=m.numCols) {
      System.out.println("dimensions bad in addTo()");
      System.exit(1);
    }
    MatrixMN ret= new MatrixMN(numRows, numCols);

    for(int r=0; r<numRows; r++)
      for(int c=0; c<numCols; c++)
        ret.elements[r][c]= this.elements[r][c] + m.elements[r][c];

    return ret;
  }

  /**
   * @param m a Matrix
   * @return a new matrix which is equal to the difference of this - m
   */
  public MatrixMN subtractFrom (MatrixMN m) {
    if(numRows!=m.numRows || numCols!=m.numCols) {
      System.out.println("dimensions bad in addTo()");
      System.exit(1);
    }
    MatrixMN ret= new MatrixMN(numRows, numCols);

    for(int r=0; r<numRows; r++)
      for(int c=0; c<numCols; c++)
        ret.elements[r][c]= this.elements[r][c] - m.elements[r][c];

    return ret;
  }

  /**
   * @param m a Matrix
   * @return a new matrix which is equal to the product of this*m
   */
  public MatrixMN multiply (MatrixMN m) {
    if(numCols!=m.numRows) {
      System.out.println("dimensions bad in multiply()");
      System.exit(1);
    }

    MatrixMN ret= new MatrixMN(numRows, m.numCols);

    for(int r=0; r<numRows; r++)
      for(int c=0; c<m.numCols; c++) {
	for(int k=0;k<numCols; k++) {
          ret.elements[r][c] += this.elements[r][k] * m.elements[k][c];
	}
      }

    return ret;
  }

  /**
   * Scalar multiplication- multiplies each element by a scalar
   * @param s a scalar
   * @return a new matrix which is equal to the product of s*this
   */
  public MatrixMN multiply (double s) {
    MatrixMN ret= new MatrixMN(numRows, numCols);
    for(int i=0; i<numRows; i++)
      for(int j=0; j<numCols; j++)
	ret.elements[i][j]= elements[i][j]*s;
    return ret;
  }

  /**
   * @return the transposed matrix with dimensions numCols x numRows
   */
  public MatrixMN transpose() {
    MatrixMN ret= new MatrixMN(numCols, numRows);

    for(int r=0; r<numRows; r++)
      for(int c=0;c<numCols; c++)
	ret.elements[c][r]= elements[r][c];
    return ret;
  }

  /**
   * @param val a scalar
   * @returns true if and only if all elements of the matrix equal val
   */
  public boolean equals(double val) {
    for(int r=0; r<numRows; r++)
      for(int c=0; c<numCols; c++)
	if(Math.abs(elements[r][c]-val) > .0001) return false;
    return true;
  }

  /**
   * Computes the dot product (or scalar product) of two matrices by
   *  multiplying corresponding elements and summing all the products.
   * @param m A Matrix with the same dimensions
   * @returns the dot product (scalar product)
   */
  public double dot(MatrixMN m) {
    if(numRows!=m.numRows || numCols!=m.numCols) {
      System.out.println("dimensions bad in dot()");
      System.exit(1);
    }
    double sum= 0;

    for(int r=0; r<numRows; r++)
      for(int c=0; c<numCols; c++)
        sum += this.elements[r][c] * m.elements[r][c];

    return sum;
  }

  /**
   * Calculates the matrix's Moore-Penrose pseudoinverse
   * @return an MxN matrix which is the matrix's pseudoinverse.
   */
  public MatrixMN pseudoInverse() {

    int r,c;

    int k=1;
    MatrixMN ak= new MatrixMN(numRows, 1);
        MatrixMN dk;
    MatrixMN ck, bk;

    MatrixMN R_plus;

    for(r=0; r<numRows; r++)
      ak.elements[r][0]= this.elements[r][0];

    if(!ak.equals(0.0)) {
      R_plus= ak.transpose().multiply( 1.0/( ak.dot(ak) ) );
    }
    else {
      //R_plus= new MatrixMN(1, numCols);
        R_plus= new MatrixMN(1, numRows);  //I modified make
    }

    while(k< this.numCols) {

      for(r=0; r<numRows; r++)
	ak.elements[r][0]= this.elements[r][k];

      dk= R_plus.multiply(ak);
      MatrixMN T= new MatrixMN(numRows, k);
      for(r=0; r<numRows; r++)
	for(c=0; c<k; c++)
	  T.elements[r][c]= this.elements[r][c];

      ck= ak.subtractFrom( T.multiply(dk) );

      if( !ck.equals(0.0) ) {
	bk= ck.transpose().multiply( 1.0/(ck.dot(ck)) );
      }
      else {
	bk= dk.transpose().multiply( 1.0/( 1.0 + dk.dot(dk) ) ).multiply(R_plus);
      }

      MatrixMN N= R_plus.subtractFrom( dk.multiply(bk) );
      R_plus= new MatrixMN(N.numRows+1, N.numCols);

      for(r=0; r< N.numRows; r++)
	for(c=0; c< N.numCols; c++)
	  R_plus.elements[r][c]= N.elements[r][c];
      for(c=0; c<N.numCols; c++)
	R_plus.elements[R_plus.numRows-1][c]= bk.elements[0][c];

      k++;
    }
    return R_plus;
  }

  /**
   * @return a String representation of the matrix
   */
  public String toString() {
    StringBuffer buf= new StringBuffer();
    buf.append("[ ");
    for(int r=0; r<numRows; r++) {
      buf.append("[ ");
      for(int c=0; c<numCols; c++) {
        buf.append(elements[r][c]);
        buf.append(" ");
      }
      buf.append("] ");
    }
    buf.append("]");
    return buf.toString();
  }
}
