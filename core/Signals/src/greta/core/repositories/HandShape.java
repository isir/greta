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
package greta.core.repositories;

import greta.core.util.math.Vec3d;
import greta.core.util.parameter.Parameter;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Set;

/**
 *
 * @author Andre-Marie Pez
 */
public class HandShape implements Parameter<HandShape> {

    private String shapeName;

    private HashMap<String, Vec3d> joints = new HashMap<String, Vec3d>();

    public HandShape(String shapeName){
        this.shapeName = shapeName;
    }

    public HandShape(HandShape handShape){
        this.shapeName = handShape.shapeName;
        for(Entry<String, Vec3d> joint : handShape.joints.entrySet()){
            joints.put(joint.getKey(), new Vec3d(joint.getValue()));
        }
    }

    @Override
    public String getParamName() {
        return shapeName;
    }

    @Override
    public void setParamName(String string) {
        shapeName = string;
    }

    public Vec3d getJoint(String jointName) {
        return joints.get(jointName);
    }

    public Set<String> getJointNames(){
        return joints.keySet();
    }

    public void setJoint(String jointName, double x, double y, double z) {
        Vec3d vec = joints.get(jointName);
        if(vec == null){
            vec = new Vec3d();
            joints.put(jointName, vec);
        }
        vec.set((float) x, (float) y, (float) z);
    }


    @Override
    public boolean equals(HandShape shape) {
        if(this==shape){
            return true;
        }
        if(shape==null){
            return false;
        }
        if(! shapeName.equalsIgnoreCase(shape.shapeName)){
            return false;
        }

        if(joints.size() != shape.joints.size()){
            return false;
        }

        for(String key : joints.keySet()){
            if( ! shape.joints.containsKey(key)){
                return false;
            }
            if( ! getJoint(key).equals(shape.getJoint(key))){
                return false;
            }
        }
        return true;
    }


}
