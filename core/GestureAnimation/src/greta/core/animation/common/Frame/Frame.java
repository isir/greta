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
package greta.core.animation.common.Frame;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Jing Huang
 */
public class Frame {

    protected HashMap<String, JointFrame> _results = new HashMap<String, JointFrame>();

    public void addJointFrame(String name, JointFrame q) {
        _results.put(name, new JointFrame(q));
    }

    public void setJointFrames(HashMap<String, JointFrame> rotations) {
        _results = rotations;
    }

    public void addJointFrames(HashMap<String, JointFrame> rotations) {

        Set entries = rotations.entrySet();
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            addJointFrame((String) entry.getKey(), new JointFrame((JointFrame) entry.getValue()));
        }
    }

    public HashMap<String, JointFrame> getJointFrames() {
        return _results;
    }

    public JointFrame getJointFrame(String name) {
        if (_results.containsKey(name)) {
            return _results.get(name);
        } else {
            return null;
        }
    }

    public void interpolation(Frame f0, Frame f1, double t) {
        for (String name : f1.getJointFrames().keySet()) {
            JointFrame jf0 = f0.getJointFrame(name);
            JointFrame jf1 = f1.getJointFrame(name);
            if (jf0 == null || jf1 == null) {
                //System.err.println(name + "jf0" + jf0 + "jf1" + jf1);
                jf0 = new JointFrame();
                JointFrame jf = JointFrame.interpolate(jf0, jf1, t);
                _results.put(name, jf);
            } else {
                JointFrame jf = JointFrame.interpolate(jf0, jf1, t);
                _results.put(name, jf);
            }
        }
    }

    @Override
    public Frame clone() {
        Frame f = new Frame();
        for (String name : _results.keySet()) {
            JointFrame jf = new JointFrame();
            jf._localrotation = new Quaternion(_results.get(name)._localrotation);
            jf._translation = new Vec3d(_results.get(name)._translation);
            f.addJointFrame(name, jf);
        }

        return f;
    }
}
