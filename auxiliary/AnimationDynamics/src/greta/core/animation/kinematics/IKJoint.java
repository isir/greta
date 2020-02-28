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

import greta.core.animation.math.Matrix3d;
import greta.core.animation.math.Vector2d;
import greta.core.animation.math.Vector3d;
import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.ArrayList;

public class IKJoint {
    public IKJoint(IKChain sk, int parent, int dof, String name) {
        p_owner = sk;
        m_index = sk.m_joints.size();
        p_owner.m_joint_name_index. put(name, m_index);
        sk.m_joints.add(this);
        m_index_parent = parent;
        m_name = name;
        m_dof = dof;
        for (int i = 0; i < m_dof; ++i) {
            m_dims.add(new Dim());
            m_dims.get(i).m_idx = sk.m_localRotations.size();
            if (i == 0) {
                if (parent >= 0) {
                    m_dims.get(i).m_lastIdx = sk.m_joints.get(parent).m_dims.get(sk.m_joints.get(parent).m_dims.size() - 1).m_idx;
                } else {
                    m_dims.get(i).m_lastIdx = -1;
                }
            } else {
                m_dims.get(i).m_lastIdx = m_dims.get(i - 1).m_idx;
            }

            sk.m_axis.add(new Vector3d());
            sk.m_anglelimites.add(new Vector2d(-3.14, 3.14));
            sk.m_localRotations.add(Matrix3d.identity());
            sk.m_globalOrientations.add(Matrix3d.identity());
            sk.m_localTranslations.add(new Vector3d());
            sk.m_globalPositions.add(new Vector3d());
            sk.m_values.add(0.0);
        }
    }

    public void setAxis(int dim, Vector3d axis){
        p_owner.m_axis.get(m_dims.get(dim).m_idx).set(axis);
    }

    public void setLimites(int dim, Vector2d limit){
        p_owner.m_anglelimites.get(m_dims.get(dim).m_idx).set(limit);
    }

    public void setLocalTranslation(int dim, Vector3d tran){
        p_owner.m_localTranslations.get(m_dims.get(dim).m_idx).set(tran);
    }

    public void setLocalRotation(int dim, Matrix3d r){
        p_owner.m_localRotations.get(m_dims.get(dim).m_idx).set(r);
    }

    public Quaternion computeQuaternion(){
        Quaternion q = new Quaternion();
        for(int i = 0; i < m_dof; ++i){
            double value = p_owner.m_values.get(m_dims.get(i).m_idx);
            Vector3d axis = p_owner.m_axis.get(m_dims.get(i).m_idx);
            q = Quaternion.multiplication(q, (new Quaternion(new Vec3d(axis.getEntry(0),axis.getEntry(1),axis.getEntry(2)),value)));
        }
        return q;
    }

    public String m_name;
    public int m_index;
    public int m_index_parent;
    public IKChain p_owner;
    public int m_dof;
    public ArrayList<Dim> m_dims = new ArrayList<>();
}
