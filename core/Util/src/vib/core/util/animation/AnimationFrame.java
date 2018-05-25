/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.animation;

import java.util.ArrayList;
import java.util.List;
import vib.core.util.math.Quaternion;
import vib.core.util.math.Vec3d;

/**
 *
 * @author Andre-Marie Pez, jing huang
 */
public class AnimationFrame {

    int frameNumber;
    private ArrayList<Double> _values;
    String _animationType;

    public AnimationFrame(int size){
        _values = new ArrayList<Double>(size);
    }
    
    public void setValue(int index, double value){
        _values.set(index, value);
    }

    public void setFrameNumber(int num){
        frameNumber = num;
    }

    public int getFrameNumber(){
        return frameNumber;
    }

    public ArrayList<Double> getValues() {
        return _values;
    }
    
    public double getValue(int index){
        return _values.get(index);
    }

    public String getAnimationDataType() {
        return _animationType;
    }

    public void setAnimationDataType(String animationType) {
        this._animationType = animationType;
    }
   
}
