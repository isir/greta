package vib.core.animation;

import vib.core.util.math.Vec3d;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */


public class PositionFrame {
    HashMap<String, Vec3d> _points = new HashMap<String, Vec3d>();
    
    public PositionFrame(){}
    
    public void setPoint(String name, Vec3d value){
        _points.put(name, value);
    }
    
    public Vec3d getValue(String name){
        return _points.get(name);
    }
    
    public HashMap<String, Vec3d> getValues(){
        return _points;
    }
}
