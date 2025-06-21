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
