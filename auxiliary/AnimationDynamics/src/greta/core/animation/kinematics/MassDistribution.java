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
package greta.core.animation.kinematics;

/**
 *
 * @author Jing Huang
 */
public class MassDistribution {

    //double _inertiaMatrix[] = new double[9];

    /**
     *  the center of mass in the middle
     * @param y   y
     * @param z     z
     * @param x     x
     * @param mass
     * @param return inertiaMatrix    //check chapter 6 manipulator dynamics 169 (Introduction to Robotics Mechanics and Control THIRD EDITION John J.Craig)
     */
    public void computeUniformBox(double x, double y, double z, double mass, double[] inertiaMatrix){
        //inertiaMatrix = new double[9];
        inertiaMatrix[0] = mass /12.0 * (y * y + z * z);
        inertiaMatrix[1] = 0;
        inertiaMatrix[2] = 0;
        inertiaMatrix[3] = 0;
        inertiaMatrix[4] = mass /12.0 * (z * z + x * x);
        inertiaMatrix[5] = 0;
        inertiaMatrix[6] = 0;
        inertiaMatrix[7] = 0;
        inertiaMatrix[8] = mass /12.0 * (x * x + y * y);
    }

    /**
     *
     * @param r the distance of mass particle to center of mass
     * @param return skewMatrix
     */
    public void getSkewMatrix(double[] r, double[] skewMatrix){
        skewMatrix[0] = 0;
        skewMatrix[1] = -r[2];
        skewMatrix[2] = r[1];
        skewMatrix[3] = r[2];
        skewMatrix[4] = 0;
        skewMatrix[5] = -r[0];
        skewMatrix[6] = -r[1];
        skewMatrix[7] = r[0];
        skewMatrix[8] = 0;
    }

    /**
     *
     * @param inertia
     * @param mass
     * @param x
     * @param y
     * @param z
     * @param outputinertia
     */
    public void translationInertia(double[] inertia, double mass, double x, double y, double z, double[] outputinertia ){
        outputinertia[0] = inertia[0] + mass * (y * y + z * z);
        outputinertia[1] = inertia[1] + mass * x * y;
        outputinertia[2] = inertia[2] + mass * x * z;
        outputinertia[3] = inertia[3] + mass * y * x;
        outputinertia[4] = inertia[4] + mass * (x * x + z * z);
        outputinertia[5] = inertia[5] + mass * y * z;
        outputinertia[6] = inertia[6] + mass * z * x;
        outputinertia[7] = inertia[7] + mass * z * y;
        outputinertia[8] = inertia[8] + mass * (x * x + y * y);
    }

    /**
     *
     * @param inertia
     * @param matrixrotation
     * @param return outputinertia
     */
    public void rotateInertia(double[] inertia, double[] matrixrotation, double[] outputinertia){
        double[] middle = new double[9];
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int i = 0; i < 3; i++) {
                    middle[y * 3 + x] += matrixrotation[y * 3 + i] * inertia[i * 3 + x];
                }
            }
        }

        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                for (int i = 0; i < 3; i++) {
                    outputinertia[y * 3 + x] += middle[y * 3 + i] * matrixrotation[y * 3 + i];
                }
            }
        }


    }
}
