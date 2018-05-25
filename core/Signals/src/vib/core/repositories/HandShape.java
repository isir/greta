/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.repositories;

import vib.core.util.math.Vec3d;
import vib.core.util.parameter.Parameter;
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
