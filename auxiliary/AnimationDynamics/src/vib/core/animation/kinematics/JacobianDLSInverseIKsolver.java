/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.animation.kinematics;

import java.util.ArrayList;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.ArrayRealVector;
import org.apache.commons.math3.linear.MatrixUtils;
import org.apache.commons.math3.linear.RealMatrix;
import org.apache.commons.math3.linear.RealVector;
import org.apache.commons.math3.linear.SingularValueDecomposition;
import vib.core.animation.math.Vector3d;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class JacobianDLSInverseIKsolver extends IKSolver {

    double damping_constant_Max = 0.9;
    double epsilon = 1.2;

    public JacobianDLSInverseIKsolver(IKChain chain) {
        super(chain);
    }

    public void setDampingConstant(double damping) {
        damping_constant_Max = damping;
    }

    @Override
    public boolean solve(Vector3d target) {
        long start_time = System.nanoTime();
        int tries = 0;
        int columnDim = m_chain.m_localRotations.size();
        Array2DRowRealMatrix jacobian = new Array2DRowRealMatrix(3, columnDim);
        m_chain.update();
        Vector3d endpos = m_chain.m_globalPositions.get(columnDim - 1);
        Vector3d distance = target.substract(endpos);
        //double beta = 0.5;
        while (++tries < m_maxTries
                && distance.getNorm() > m_targetThreshold) {
            Vector3d dT = distance;//.multiple(beta);
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
                    jacobian.setEntry(0, dim.m_idx, (0 == axisXYZgradient.getEntry(0) ? 0.000001 : axisXYZgradient.getEntry(0)) /* factors[dimensions]*/);// * m_stepweight;
                    jacobian.setEntry(1, dim.m_idx, (0 == axisXYZgradient.getEntry(1) ? 0.000001 : axisXYZgradient.getEntry(1)) /* factors[dimensions]*/);// * m_stepweight;
                    jacobian.setEntry(2, dim.m_idx, (0 == axisXYZgradient.getEntry(2) ? 0.000001 : axisXYZgradient.getEntry(2)) /* factors[dimensions]*/);// * m_stepweight;
                }
            }

            //RealMatrix jacobianTranspose = jacobian.transpose();

            SingularValueDecomposition svd = new SingularValueDecomposition(jacobian);
            RealMatrix uT = svd.getUT();
            RealMatrix s = svd.getS();
            RealMatrix v = svd.getV();
            double lamda2 = 0;
            RealMatrix e_s_inverse = new Array2DRowRealMatrix(s.getRowDimension(), s.getColumnDimension());
            double minvalue = 1000;
            for (int i = 0; i < s.getRowDimension(); ++i) {
                double value = s.getEntry(i, i);
                if (value < minvalue) {
                    minvalue = value;
                }
            }
            if (minvalue >= epsilon) {
                lamda2 = 0;
            } else {
                lamda2 = (1 - (minvalue / epsilon)) * damping_constant_Max;
            }
            for (int i = 0; i < s.getRowDimension(); ++i) {
                double value = s.getEntry(i, i);
                e_s_inverse.setEntry(i, i, value / (value * value + lamda2));
            }
            
            RealMatrix dlsInverse = v.multiply(e_s_inverse).multiply(uT);

//            RealMatrix nullspace = MatrixUtils.createRealIdentityMatrix(dls.getRowDimension()).subtract(dls.multiply(jacobian)) ;
//            ArrayRealVector alfa = new ArrayRealVector(dls.getRowDimension());
//            RealVector nullspaceV = nullspace.operate(alfa);
            RealVector dR = dlsInverse.operate(dT);// .add(nullspaceV);

            for (int i = 0; i < columnDim; ++i) {
                m_chain.m_values.set(i, castPiRange(m_chain.m_values.get(i) + dR.getEntry(i)));
                m_chain.m_values.set(i, clamp(m_chain.m_values.get(i), m_chain.m_anglelimites.get(i).getEntry(0), m_chain.m_anglelimites.get(i).getEntry(1)));
                m_chain.m_localRotations.get(i).fromAxisAngle(m_chain.m_axis.get(i), m_chain.m_values.get(i));
            }

            m_chain.update();
            endpos = m_chain.m_globalPositions.get(columnDim - 1);
            distance = target.substract(endpos);
            //System.out.println(distance);
        }
        long end_time = System.nanoTime();
        double difference = (end_time - start_time)/1e6;
        System.out.println(getIKSolverName() + " time: "+difference +" try: " + tries + "  " + distance.getNorm());
        if (tries == m_maxTries) {

            return false;
        }
        return true;
    }

    public String getIKSolverName() {
        return "JacobianDLSSVDIKSolver";
    }
}
