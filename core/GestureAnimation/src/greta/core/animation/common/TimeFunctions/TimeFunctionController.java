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
package greta.core.animation.common.TimeFunctions;

import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class TimeFunctionController {
    HashMap<String, TimeFunction> _timeFunctions = new HashMap<String, TimeFunction>();

    public HashMap<String, TimeFunction> getTimeFunctions() {
        return _timeFunctions;
    }
    LinearTimeFunction _linearTimeFunction = new LinearTimeFunction();
    RebounceTimeFunction _rebounceTimeFunction = new RebounceTimeFunction();
    CurveTimeFunction _curveTimeFunction = new CurveTimeFunction();
    SmoothCurveTimeFunction _smoothCurveTimeFunction= new SmoothCurveTimeFunction();
    OutBackTimeFunction _outBackTimeFunction = new OutBackTimeFunction();
    public TimeFunctionController() {
        init();
    }

    void init(){
        addTimeFunction("LinearTimeFunction",_linearTimeFunction);
        addTimeFunction("RebounceTimeFunction",_rebounceTimeFunction);
        addTimeFunction("CurveTimeFunction",_curveTimeFunction);
        addTimeFunction("SmoothCurveTimeFunction",_smoothCurveTimeFunction);
        addTimeFunction("OutBackTimeFunction",_outBackTimeFunction);
    }


    public void addTimeFunction(String jointName, TimeFunction timefunction){
        _timeFunctions.put(jointName, timefunction);
    }
    public TimeFunction getTimeFunction(String jointName){
        if(_timeFunctions.containsKey(jointName)){
            return _timeFunctions.get(jointName);
        }else
            return _timeFunctions.get("LinearTimeFunction");
    }
}
