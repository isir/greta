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
package greta.core.animation.common;

import greta.core.util.math.Quaternion;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class Skeleton {

    String _name = "no";
    ArrayList<Joint> _joints = new ArrayList<Joint>();
    HashMap<String, Integer> _nameIdMap = new HashMap<String, Integer>();
/**
 *
 * @param name
 */
    public Skeleton(String name) {
        _name = name;
    }

    public String getName(){
        return _name;
    }
    /**
     *
     * @param name
     * @param parent
     * @return index in joint list
     */
    public int createJoint(String name, int parent) {
        int id = _joints.size();
        Joint joint = new Joint(name, this);
        joint.setId(id);
        _joints.add(joint);
        _nameIdMap.put(name, id);
        if (parent != -1) {
            joint.setParentById(parent);
            Joint p = getJoint(parent);
            p.addChild(id);
        }
        return id;
    }
/**
 *
 * @param joint
 * @param parent defines parent id
 * @return index in joint list
 */
    public int addJoint(Joint joint, int parent) {
        int id = _joints.size();
        joint.setId(id);
        joint.setParentById(parent);
        _joints.add(joint);
        _nameIdMap.put(joint.getName(), id);
        return id;

    }

    /**
     * Removes the specified joint
     * @param joint the joint to remove
     * @return {@code true} if the remove succeed, {@code false} otherwise
     */
    public boolean removeJoint(Joint joint) {
        return removeJoint(joint.getId());
    }

    /**
     * Removes the specified joint
     * @param id the identifier of the joint
     * @return {@code true} if the remove succeed, {@code false} otherwise
     */
    public boolean removeJoint(int id) {
        if (0 < id && id < _joints.size()) {
            Joint j = _joints.get(id);
            _nameIdMap.remove(j.getName());
            _joints.remove(id);
            return true;
        }
        return false;
    }

    /**
     * Removes the specified joint
     * @param name the name of the joint
     * @return {@code true} if the remove succeed, {@code false} otherwise
     */
    public boolean removeJoint(String name) {
        return removeJoint(_nameIdMap.get(name));
    }

    /**
     *
     * @param id
     * @return joint
     */
    public Joint getJoint(int id) {
        if (id < _joints.size() && id > -1) {
            Joint j = _joints.get(id);
            return j;
        }
        return null;
    }

    /**
     *
     * @param name
     * @return joint
     */
    public Joint getJoint(String name) {

        if (_nameIdMap.containsKey(name)) {
            int id = _nameIdMap.get(name);
            return _joints.get(id);
        }
        return null;
    }

    /**
     *
     * @param name
     * @return index
     */
    public int getJointIndex(String name) {
        if (_nameIdMap.containsKey(name)) {
            int id = _nameIdMap.get(name);
            return id;
        }
        return -1;
    }

    /**
     *
     * @return joint list
     */
    public ArrayList<Joint> getJoints() {
        return _joints;
    }

    /**
     * clear joint
     */
    public void clear() {
        _joints.clear();
        _nameIdMap.clear();
    }

    public void resetPriority() {
    }

    /**
     * set to initial situation
     */
    public void reset() {
//        for (Joint joint : _joints) {
//            joint.reset();
//        }
        if(_joints.size() > 0)
            _joints.get(0).reset();
    }

    /**
     * update all joints
     */
    public void update(){
         if(_joints.size() > 0)
            _joints.get(0).update();
    }

    public void loadRotations(HashMap<String, Quaternion> current){
        for(String name : current.keySet()){
            Joint j = getJoint(name);
            if(j != null){
                j.setLocalRotation(current.get(name));
            }
        }
    }
}
