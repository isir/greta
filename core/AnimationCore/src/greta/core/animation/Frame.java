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
package greta.core.animation;

import greta.core.util.math.Quaternion;
import greta.core.util.math.Vec3d;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

/**
 *
 * @author Jing Huang
 * @author Brian Ravenet
 */
public class Frame {

    //BRIAN : I added a boolean to keep track if this phase was the last phase of the gesture
    private boolean isStrokeEnd = false;
    protected HashMap<String, Quaternion> _rotations = new HashMap<String, Quaternion>();
    protected Vec3d _root_translation = new Vec3d();

    public void setRootTranslation(Vec3d translation) {
        _root_translation = translation.clone();
    }

    public Vec3d getRootTranslation() {
        return _root_translation.clone();
    }

    public void addRotation(String name, Quaternion q) {
        _rotations.put(name, new Quaternion(q));
    }

    public void accumulateRotation(String name, Quaternion q) {
        if (_rotations.get(name) != null) {
            _rotations.put(name, Quaternion.multiplication(_rotations.get(name), q).normalized());
        } else {
            _rotations.put(name, q);
        }
    }

    public void setRotations(HashMap<String, Quaternion> rotations) {
        _rotations = rotations;
    }

    public void addRotations(HashMap<String, Quaternion> rotations) {
        Set entries = rotations.entrySet();
        Iterator it = entries.iterator();
        while (it.hasNext()) {
            Map.Entry entry = (Map.Entry) it.next();
            addRotation((String) entry.getKey(), new Quaternion((Quaternion) entry.getValue()));
        }
    }

    public HashMap<String, Quaternion> getRotations() {
        return _rotations;
    }

    public Quaternion getRotation(String name) {
//        if (_rotations.containsKey(name)) {
            return _rotations.get(name);
//        } else {
//            return null;
//        }
    }

    public void interpolation(Frame f0, Frame f1, double t) {
        TreeSet<String> names = new TreeSet<String>();
        names.addAll(f0.getRotations().keySet());
        names.addAll(f1.getRotations().keySet());
        for (String name : names) {
            Quaternion jf0 = f0.getRotation(name);
            Quaternion jf1 = f1.getRotation(name);
            if (jf0 == null) {
                jf0 = new Quaternion();
            }
            if (jf1 == null) {
                jf1 = new Quaternion();
            } //else {
            Quaternion jf = Quaternion.slerp(jf0, jf1, t, true).normalized();
            _rotations.put(name, jf);
            //}
        }
        _root_translation = Vec3d.addition(Vec3d.multiplication(f0.getRootTranslation(), 1 - t) ,
                Vec3d.multiplication(f1.getRootTranslation(), t));
    }

    public void interpolationAccumulation(Frame f0, Frame f1, double t) {
        TreeSet<String> names = new TreeSet<String>();
        names.addAll(f0.getRotations().keySet());
        names.addAll(f1.getRotations().keySet());
        for (String name : names) {
            Quaternion jf0 = f0.getRotation(name);
            Quaternion jf1 = f1.getRotation(name);
            if (jf0 == null) {
                jf0 = new Quaternion();
            }
            if (jf1 == null) {
                jf1 = new Quaternion();
            } //else {
            Quaternion jf = Quaternion.slerp(jf0, jf1, t, true).normalized();
            this.accumulateRotation(name, jf);
        }
        _root_translation = Vec3d.addition(Vec3d.multiplication(f0.getRootTranslation(), 1 - t) ,
                Vec3d.multiplication(f1.getRootTranslation(), t));
    }

    public void mixAddition(Frame f0, Frame f1) {
        if(f0 == null && f1 == null){
            return;
        }
        if (f0 != null) {
            _rotations.putAll(f0.getRotations());
        }
        if (f1 != null) {
            _rotations.putAll(f1.getRotations());
        }
        for (String name : _rotations.keySet()) {
            Quaternion jf0 = null;
            if (f0 != null) {
                f0.getRotation(name);
            }
            Quaternion jf1 = null;
            if (f1 != null) {
                f1.getRotation(name);
            }
            if (jf0 == null && jf1 != null) {
                _rotations.put(name, jf1.clone());
            } else if (jf1 == null && jf0 != null) {
                _rotations.put(name, jf0.clone());
            } else if(jf1 != null && jf0 != null){
                Quaternion jf = Quaternion.addition(jf0, jf1).normalized(); // check if need quaternion multiplication
                _rotations.put(name, jf);
            }
        }
    }

    public void mixAddition(Frame f0, Frame f1, Frame f2) {
        _rotations.putAll(f0.getRotations());
        _rotations.putAll(f1.getRotations());
        _rotations.putAll(f2.getRotations());
        for (String name : _rotations.keySet()) {
            Quaternion jf0 = f0.getRotation(name);
            Quaternion jf1 = f1.getRotation(name);
            Quaternion jf2 = f1.getRotation(name);
            if (jf0 == null) {
                jf0 = new Quaternion();
            }
            if (jf1 == null) {
                jf1 = new Quaternion();
            }
            if (jf2 == null) {
                jf2 = new Quaternion();
            }

            Quaternion jf = Quaternion.addition(jf2, Quaternion.addition(jf0, jf1)).normalized(); // check if need quaternion multiplication
            _rotations.put(name, jf);
        }
    }

    public void extract(Frame frame) {
        for (String name : _rotations.keySet()) {
            if (frame.getRotations().containsValue(name)) {
                _rotations.put(name, frame.getRotation(name));
            }
        }
    }

    /**
     * @return the isStrokeEnd
     */
    public boolean isIsStrokeEnd() {
        return isStrokeEnd;
    }

    /**
     * @param isStrokeEnd the isStrokeEnd to set
     */
    public void setIsStrokeEnd(boolean isStrokeEnd) {
        this.isStrokeEnd = isStrokeEnd;
    }

    @Override
    public Frame clone() {
        Frame f = new Frame();
        for (String name : _rotations.keySet()) {
            Quaternion jf = _rotations.get(name).clone();
            f.addRotation(name, jf);
        }
        f.setRootTranslation(_root_translation.clone());
        f.setIsStrokeEnd(isStrokeEnd);
        return f;
    }
}
