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
