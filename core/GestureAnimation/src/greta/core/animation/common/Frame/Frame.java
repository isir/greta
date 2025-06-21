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
