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
package greta.mgik.core.animation;

import greta.core.animation.math.Vector3d;
import java.util.ArrayList;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class JacobianDLSSolver extends IKSolver {

    double m_dampling = 0.01;

    public JacobianDLSSolver(int maxTries, double targetThreshold, double dampling) {
        super(maxTries, targetThreshold);
        m_dampling = dampling;
    }

    String getIKSolverName() {
        return "JacobianDLSSolver";
    }

    @Override
    public void solveOneStep(Skeleton chain, ArrayList<Vector3d> targets) {
        chain.update();
        int tries = 0;
        RealMatrix jacobian = chain.m_jacobian;
        ArrayRealVector distance = new ArrayRealVector(jacobian.getRowDimension());

        for (int ei = 0; ei < chain.m_endeffectors.size(); ++ei) {
            int endeffectorIdx = chain.m_endeffectors.get(ei);

            Vector3d endpos = chain.m_joint_globalPositions.get(endeffectorIdx);
            Vector3d dis = (targets.get(ei).substract(endpos));
            distance.setEntry(ei * 3 + 0, dis.getEntry(0));
            distance.setEntry(ei * 3 + 1, dis.getEntry(1));
            distance.setEntry(ei * 3 + 2, dis.getEntry(2));
            if (m_activeRoot) {
                for (int j = 0; j < 3; ++j) {
                    Skeleton.Dim dim = chain.m_dims.get(j);
                    Vector3d axis = chain.m_dim_axis.get(dim.m_idx);
                    int lastDim = dim.m_lastIdx;
                    if (lastDim >= 0) {
                        axis = chain.m_dim_globalOrientations.get(lastDim).multiple(axis);
                    }
                    Vector3d axisXYZgradient = axis;
                    jacobian.setEntry(ei * 3 + 0, j, axisXYZgradient.getEntry(0));
                    jacobian.setEntry(ei * 3 + 1, j, axisXYZgradient.getEntry(1));
                    jacobian.setEntry(ei * 3 + 2, j, axisXYZgradient.getEntry(2));
                }
            }

            for (int j = chain.m_startDim4IK; j < chain.m_dims.size(); ++j) {
                if (chain.m_jacobian.getEntry(ei * 3 + 0, j) < 0.1) {
                    continue;
                }

                Skeleton.Dim dim = chain.m_dims.get(j);
                Vector3d jointPos = chain.m_dim_globalPositions.get(dim.m_idx);
                Vector3d boneVector = endpos.substract(jointPos);
                if (boneVector.getNorm() == 0) {
                    continue;
                }
                //boneVector.normalize();
                Vector3d axis = chain.m_dim_axis.get(dim.m_idx);
                int lastDim = dim.m_lastIdx;
                if (lastDim >= 0) {
                    axis = chain.m_dim_globalOrientations.get(lastDim).multiple(axis);
                }
                Vector3d axisXYZgradient = axis.cross(boneVector);

                jacobian.setEntry(ei * 3 + 0, j, axisXYZgradient.getEntry(0));
                jacobian.setEntry(ei * 3 + 1, j, axisXYZgradient.getEntry(1));
                jacobian.setEntry(ei * 3 + 2, j, axisXYZgradient.getEntry(2));
            }
        }

        RealMatrix jacobianTranspose = jacobian.transpose();
        RealMatrix jtj = jacobian.multiply(jacobianTranspose);

        RealMatrix lamdaI = MatrixUtils.createRealIdentityMatrix(jtj.getRowDimension());
        RealMatrix inv = MatrixUtils.inverse(jtj.add(lamdaI.scalarMultiply(m_dampling)));
        RealVector dR = jacobianTranspose.operate(inv.operate(distance));
        if (m_activeRoot) {
            for (int i = 0; i < 3; ++i) {
                chain.m_dim_values.set(i, castPiRange(chain.m_dim_values.get(i) + dR.getEntry(i)));
            }
        }
        for (int i = chain.m_startDim4IK; i < chain.m_dims.size(); ++i) {
            chain.m_dim_values.set(i, castPiRange(chain.m_dim_values.get(i) + dR.getEntry(i)));
        }
        chain.update();
    }

}
