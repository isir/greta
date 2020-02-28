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

import greta.core.animation.math.Vector3d;
import java.util.ArrayList;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class JacobianIKSolver extends IKSolver {

    double damping_constant = 0.9;
    public JacobianIKSolver(IKChain chain) {
        super(chain);
    }

    public void setDampingConstant(double damping){
        damping_constant = damping;
    }

    public boolean solve(Vector3d target) {
        int tries = 0;
        int columnDim = m_chain.m_localRotations.size();
        Array2DRowRealMatrix jacobian = new Array2DRowRealMatrix(3, columnDim);
        m_chain.update();
        Vector3d endpos = m_chain.m_globalPositions.get(columnDim - 1);
        Vector3d distance = target.substract(endpos);
//        double factors[] = new double[columnDim];
//        for(int i = 0; i < columnDim; ++i){
//            factors[i] = (i + 1) / 2.0;
//        }

        double beta = 0.5;
m_maxTries = 1000;
        while (++tries < m_maxTries
                && distance.getNorm() > m_targetThreshold) {
            Vector3d dT = distance.multiple(beta);
            //int dimensions = 0;
            for (int i = 0; i < m_chain.m_joints.size(); ++i) {
                IKJoint joint = m_chain.m_joints.get(i);
                ArrayList<Dim> dims = joint.m_dims;
                for (int j = 0; j < dims.size(); ++j) {
                    Dim dim = dims.get(j);
                    Vector3d jointPos = m_chain.m_globalPositions.get(dim.m_idx);
                    Vector3d boneVector = endpos.substract(jointPos);

                    Vector3d axis = m_chain.m_axis.get(dim.m_idx);
                    int lastDim = dim.m_lastIdx;
                    if (lastDim >= 0) {
                        axis = m_chain.m_globalOrientations.get(lastDim).multiple(axis);
                    }
                    Vector3d axisXYZgradient = axis.cross(boneVector);
                    jacobian.setEntry(0, dim.m_idx, (0 == axisXYZgradient.getEntry(0) ? 0.000001 : axisXYZgradient.getEntry(0))  /* factors[dimensions]*/ );// * m_stepweight;
                    jacobian.setEntry(1, dim.m_idx, (0 == axisXYZgradient.getEntry(1) ? 0.000001 : axisXYZgradient.getEntry(1))  /* factors[dimensions]*/ );// * m_stepweight;
                    jacobian.setEntry(2, dim.m_idx, (0 == axisXYZgradient.getEntry(2) ? 0.000001 : axisXYZgradient.getEntry(2))  /* factors[dimensions]*/ );// * m_stepweight;
                }
            }

            RealMatrix jacobianTranspose = jacobian.transpose();
            RealMatrix a = jacobian.multiply(jacobianTranspose);

           // JT ( J JT + lamdalamdaI)-1

            RealMatrix mat = MatrixUtils.createRealIdentityMatrix(a.getColumnDimension()).scalarMultiply(damping_constant).add(a);
            RealMatrix dls = jacobianTranspose.multiply(MatrixUtils.inverse(mat));
            RealVector dR = dls.operate(dT);

            for (int i = 0; i < columnDim; ++i) {
                double w = 1;
                if(i == 0){
                    w = 1;
                }
                m_chain.m_values.set(i, castPiRange(m_chain.m_values.get(i) + dR.getEntry(i) * w));
                m_chain.m_values.set(i, clamp(m_chain.m_values.get(i), m_chain.m_anglelimites.get(i).getEntry(0), m_chain.m_anglelimites.get(i).getEntry(1)));
                m_chain.m_localRotations.get(i).fromAxisAngle(m_chain.m_axis.get(i), m_chain.m_values.get(i));
            }

            m_chain.update();
            endpos = m_chain.m_globalPositions.get(columnDim - 1);
            distance = target.substract(endpos);
            //System.out.println(distance);
        }
        //System.out.println(m_chain.m_values.get(0));
System.out.println(getIKSolverName()+" try: " + tries + "  " +distance.getNorm());
        if (tries == m_maxTries) {

            return false;
        }
        return true;
    }

    public String getIKSolverName() {
        return "JacobianIKSolver";
    }
}
