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
