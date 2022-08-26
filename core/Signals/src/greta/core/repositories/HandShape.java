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
