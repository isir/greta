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
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Jing Huang
 */
public class IKChain {

    public IKChain(String name) {
        m_name = name;
    }

    public void updateJoint(int idx) {
        IKJoint current = m_joints.get(idx);
        for (int i = 0; i < current.m_dims.size(); ++i) {
            Dim dim = current.m_dims.get(i);
            if (dim.m_lastIdx >= 0) {
                m_globalPositions.set(dim.m_idx, m_globalPositions.get(dim.m_lastIdx).add(m_globalOrientations.get(dim.m_lastIdx).multiple(m_localTranslations.get(dim.m_idx))));
                m_globalOrientations.set(dim.m_idx, m_globalOrientations.get(dim.m_lastIdx).multiple(m_localRotations.get(dim.m_idx)));
            } else {
                m_globalPositions.set(dim.m_idx, m_localTranslations.get(dim.m_idx));
                m_globalOrientations.set(dim.m_idx, m_localRotations.get(dim.m_idx));
            }
        }
    }

    public void update() {
        for (int i = 0; i < m_joints.size(); ++i) {
            updateJoint(i);
        }
    }

    public IKJoint getJoint(String name) {
        if (m_joint_name_index.containsKey(name)) {
            return m_joints.get(m_joint_name_index.get(name));
        }
        return null;
    }

    public HashMap<String, Integer> m_joint_name_index = new HashMap<>();
    public ArrayList<IKJoint> m_joints = new ArrayList<>();
    public ArrayList<Matrix3d> m_localRotations = new ArrayList<>();
    public ArrayList<Matrix3d> m_globalOrientations = new ArrayList<>();
    public ArrayList<Vector3d> m_localTranslations = new ArrayList<>();
    public ArrayList<Vector3d> m_globalPositions = new ArrayList<>();
    public String m_name;

    public ArrayList<Vector3d> m_axis = new ArrayList<>();
    public ArrayList<Vector2d> m_anglelimites = new ArrayList<>();
    public ArrayList<Double> m_values = new ArrayList<>();

    public boolean loadFromIKFile(String fileName) {
        String line;
        InputStream fis = null;
        InputStreamReader isr = null;
        BufferedReader br = null;
        try {
            fis = new FileInputStream(fileName);
            isr = new InputStreamReader(fis);
            br = new BufferedReader(isr);
            while ((line = br.readLine()) != null) {
                String[] words = line.split("\\s+");
                if (words.length > 1) {
                    String name = words[0];
                    int parentIdx = Integer.parseInt(words[1]);
                    int dof = Integer.parseInt(words[2]);
                    float x = Float.parseFloat(words[3]);
                    float y = Float.parseFloat(words[4]);
                    float z = Float.parseFloat(words[5]);
                    IKJoint joint = new IKJoint(this, parentIdx, dof, name);
                    for (int i = 0; i < dof; ++i) {
                        line = br.readLine();
                        String[] axes = line.split("\\s+");
                        float xA = Float.parseFloat(axes[0]);
                        float yA = Float.parseFloat(axes[1]);
                        float zA = Float.parseFloat(axes[2]);
                        joint.setAxis(i, new Vector3d(xA, yA, zA));
                        if (axes.length > 3) {
                            float mi = Float.parseFloat(axes[3]);
                            float ma = Float.parseFloat(axes[4]);
                            joint.setLimites(i, new Vector2d(mi, ma));
                        }
                    }
                }
            }
        } catch (IOException ex) {
            Logger.getLogger(IKChain.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (Throwable t) { /* ensure close happens */ }
            }
            if (fis != null) {
                try {
                    fis.close();
                } catch (Throwable t) { /* ensure close happens */ }
            }
            if (isr != null) {
                try {
                    isr.close();
                } catch (Throwable t) { /* ensure close happens */ }
            }
        }
        return true;
    }
}
