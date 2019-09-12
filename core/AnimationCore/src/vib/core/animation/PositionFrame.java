/*
 * This file is part of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
 */

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
