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

import greta.core.animation.math.Matrix3d;
import greta.core.animation.math.Vector3d;
import java.util.ArrayList;
import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.RealMatrix;

/**
 *
 * @author Jing Huang
 * <gabriel.jing.huang@gmail.com or jing.huang@telecom-paristech.fr>
 */
public class Skeleton {

    public Skeleton(){
        init();
    }

    public void init(){
        BVHLoader bl = new BVHLoader();
        bl.sk = this;
        bl.load("Soccer_Toes_zyx.bvh");
        buildJacobian(this.m_endeffectors, 6);
    }

    enum DimType {

        Zposition,
        Yposition,
        Xposition,
        Zrotation,
        Yrotation,
        Xrotation,
    }

    class Dim {

        int m_lastIdx;
        int m_idx;
        String m_name;
        DimType m_type;
    }

    class Joint {

        String m_name;
        int m_index;
        int m_index_parent;
        int m_dof;
        int m_level;
        ArrayList<Integer> m_dims = new ArrayList<Integer>();
    }

    Joint addJoint(int parent, int dof, Vector3d offset, String name) {
        int size = m_joints.size();
        m_joints.add(new Joint());
        Joint joint = m_joints.get(size);
        joint.m_index = size;

        joint.m_index_parent = parent;
        joint.m_name = name;
        joint.m_dof = dof;

        //joint.m_dims.resize(joint.m_dof);
        for (int i = 0; i < joint.m_dof; ++i) {
            int currentDimIndex = m_dims.size();
            joint.m_dims.add(currentDimIndex);
            m_dims.add(new Dim());
            Dim currentDim = m_dims.get(currentDimIndex);
            currentDim.m_idx = currentDimIndex;
            if (i == 0) {
                if (parent >= 0) {
                    currentDim.m_lastIdx = m_joints.get(parent).m_dims.get(m_joints.get(parent).m_dims.size() - 1);
                } else {
                    currentDim.m_lastIdx = -1;
                }
            } else {
                currentDim.m_lastIdx = joint.m_dims.get(i - 1);
            }

            m_dim_axis.add(Vector3d.zero());
            m_dim_localRotations.add(Matrix3d.identity());
            m_dim_globalOrientations.add(Matrix3d.identity());
            m_dim_localTranslations.add(Vector3d.zero());
            m_dim_globalPositions.add(Vector3d.zero());
            m_dim_values.add(0.0);
            if (i == 0) {
                m_dim_localTranslations.set(m_dim_localTranslations.size() - 1, offset);
            }
        }

        m_joint_localRotations.add(Matrix3d.identity());
        m_joint_globalOrientations.add(Matrix3d.identity());
        m_joint_offsets.add(offset);
        m_joint_localTranslations.add(Vector3d.zero());
        m_joint_globalPositions.add(Vector3d.zero());
        return joint;
    }

    void reset() {
        for (int i = 0; i < m_dims.size(); ++i) {
            m_dim_localRotations.get(i).toIdentity();
            m_dim_globalOrientations.get(i).toIdentity();
            m_dim_values.set(i, 0.);
        }
        update();
    }

    void resetValues() {
        for (int i = 0; i < m_dims.size(); ++i) {
            m_dim_values.set(i, 0.);
        }
        update();
    }

    void resetRotationValues() {
        for (int i = 3; i < m_dims.size(); ++i) {
            m_dim_values.set(i, 0.);
        }
        update();
    }

    void resetNotRootRotationValues() {
        for (int i = 6; i < m_dims.size(); ++i) {
            m_dim_values.set(i, 0.0);
        }
        update();
    }

    void updateDimLocalRotation(int idx) {
        m_dim_localRotations.get(idx).fromAxisAngle(m_dim_axis.get(idx), m_dim_values.get(idx));
    }

    void updateDim(int idx) {
        if (idx < 3) {
            if (idx == 0) {
                m_dim_globalPositions.set(0, m_dim_localTranslations.get(0).add(m_dim_axis.get(0).multiple(m_dim_values.get(0))));
                return;
            }

            Dim dim = m_dims.get(idx);
            m_dim_globalPositions.set(dim.m_idx, m_dim_globalPositions.get(dim.m_lastIdx).add(m_dim_localTranslations.get(dim.m_idx).add(m_dim_axis.get(idx).multiple(m_dim_values.get(idx)))));
            return;
        }

        updateDimLocalRotation(idx);
        Dim dim = m_dims.get(idx);

        m_dim_globalPositions.set(dim.m_idx, m_dim_globalPositions.get(dim.m_lastIdx).add(m_dim_globalOrientations.get(dim.m_lastIdx).multiple(m_dim_localTranslations.get(dim.m_idx))));
        m_dim_globalOrientations.set(dim.m_idx, m_dim_globalOrientations.get(dim.m_lastIdx).multiple(m_dim_localRotations.get(dim.m_idx)));

    }

    void updateAllDims() {
        for (int i = 0; i < m_dims.size(); ++i) {
            updateDim(i);
        }
    }

    void updateJoint(int idx) {
        Joint current = m_joints.get(idx);
        Matrix3d temp = Matrix3d.identity();
        for (int i = 0; i < current.m_dims.size(); ++i) {
            Dim dim = m_dims.get(current.m_dims.get(i));
            updateDim(dim.m_idx);
            temp = new Matrix3d(Matrix3d.multiple(temp, m_dim_localRotations.get(dim.m_idx)));
        }
        m_joint_localRotations.set(idx, temp);
        int last = current.m_dof - 1;
        if (last > 0) {
            m_dim_globalOrientations.set(current.m_dims.get(last), m_dim_globalOrientations.get(current.m_dims.get(last)));
            m_joint_globalPositions.set(idx, m_dim_globalPositions.get(current.m_dims.get(last)));
        } else {
            m_joint_globalOrientations.set(idx, m_joint_globalOrientations.get(current.m_index_parent));
            m_joint_globalPositions.set(idx, Vector3d.add(m_joint_globalPositions.get(current.m_index_parent), m_joint_globalOrientations.get(idx).multiple(m_joint_offsets.get(idx))));
        }
    }

    void update() {
        for (int i = 0; i < m_joints.size(); ++i) {
            updateJoint(i);
        }
    }

    void buildJacobian(ArrayList<Integer> endeffectors, int startDim) {
        for (int i = 0; i < endeffectors.size(); ++i) {
            m_endeffectorsPositions.add(new Vector3d(this.m_joint_globalPositions.get(endeffectors.get(i))));
        }
        m_startDim4IK = startDim;
        int columnDim = m_dims.size();
        int rowDim = endeffectors.size() * 3;
        m_jacobian = new Array2DRowRealMatrix(rowDim, columnDim);

        m_jacobian = m_jacobian.scalarMultiply(0.);
        for (int i = 0; i < endeffectors.size(); ++i) {
            Joint currentEnd = m_joints.get(endeffectors.get(i));
            while (currentEnd.m_dof < 1) {
                currentEnd = m_joints.get(currentEnd.m_index_parent);
            }
            currentEnd = m_joints.get(currentEnd.m_index_parent);
            Dim currentDim = m_dims.get(currentEnd.m_dims.get(currentEnd.m_dims.size() - 1));
            while (currentDim.m_idx > m_startDim4IK - 1) {
                m_jacobian.setEntry(i * 3 + 0, currentDim.m_idx, 1);
                m_jacobian.setEntry(i * 3 + 1, currentDim.m_idx, 1);
                m_jacobian.setEntry(i * 3 + 2, currentDim.m_idx, 1);
                currentDim =  m_dims.get(currentDim.m_lastIdx);
            }
        }
    }
    int m_startDim4IK;

    ArrayList<Joint> m_joints = new ArrayList<Joint>();
    ArrayList<Vector3d> m_joint_offsets = new ArrayList<Vector3d>();
    ArrayList<Matrix3d> m_joint_localRotations = new ArrayList<Matrix3d>();
    ArrayList<Matrix3d> m_joint_globalOrientations = new ArrayList<Matrix3d>();
    ArrayList<Vector3d> m_joint_localTranslations = new ArrayList<Vector3d>();
    ArrayList<Vector3d> m_joint_globalPositions = new ArrayList<Vector3d>();

    ArrayList<Dim> m_dims = new ArrayList<Dim>();
    ArrayList<Matrix3d> m_dim_localRotations = new ArrayList<Matrix3d>();
    ArrayList<Matrix3d> m_dim_globalOrientations = new ArrayList<Matrix3d>();
    ArrayList<Vector3d> m_dim_localTranslations = new ArrayList<Vector3d>();
    ArrayList<Vector3d> m_dim_globalPositions = new ArrayList<Vector3d>();
    ArrayList<Double> m_dim_values = new ArrayList<Double>();
    ArrayList<Double> m_dim_last_values = new ArrayList<Double>();
    ArrayList<Vector3d> m_dim_axis = new ArrayList<Vector3d>();
    String m_name;

    //joint idx for endeffector
    ArrayList<Integer> m_endeffectors = new ArrayList<Integer>();
    ArrayList<Vector3d> m_endeffectorsPositions = new ArrayList<Vector3d>();
    RealMatrix m_jacobian;
}
