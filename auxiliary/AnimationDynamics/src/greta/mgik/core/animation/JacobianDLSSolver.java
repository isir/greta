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
