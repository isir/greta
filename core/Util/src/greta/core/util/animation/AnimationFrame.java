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
package greta.core.util.animation;

import java.util.ArrayList;

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
