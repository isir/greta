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
package greta.core.keyframes;

import greta.core.util.math.Function;
import greta.core.util.math.easefunctions.EaseInOutSine;
import greta.core.util.math.easefunctions.EaseOutBack;
import greta.core.util.math.easefunctions.EaseOutBounce;
import greta.core.util.math.easefunctions.EaseOutQuad;
import greta.core.util.math.easefunctions.Linear;
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class GesturePhaseInterpolationFunctions {
    HashMap<String, Function> _phaseFunctions = new HashMap<String, Function>();
    HashMap<String, String> _phaseNameMapFunctions = new HashMap<String, String>();

    static private GesturePhaseInterpolationFunctions functions = new GesturePhaseInterpolationFunctions();
    private GesturePhaseInterpolationFunctions(){
        init();
    }

    public static GesturePhaseInterpolationFunctions getInstance(){
        return functions;
    }

    void init(){
        _phaseFunctions.put("Linear", new Linear());
        _phaseFunctions.put("EaseInOutSine", new EaseInOutSine());
        _phaseFunctions.put("EaseOutBack", new EaseOutBack());
        _phaseFunctions.put("EaseOutBounce", new EaseOutBounce());
        _phaseFunctions.put("EaseOutQuad", new EaseOutQuad());
    }

    public Function getFunction(String name){
        if(_phaseFunctions.containsKey(name)){
            return _phaseFunctions.get(name);
        }
        return null;
    }

    public HashMap<String, Function> getFunctions(){
        return _phaseFunctions;
    }

    public void setPhaseMappedFunctionName(String phaseType, String functionName){
        _phaseNameMapFunctions.put(phaseType, functionName);
    }

    public String getPhaseMappedFunctionName(String name){
        if(_phaseNameMapFunctions.containsKey(name)){
            return _phaseNameMapFunctions.get(name);
        }
        return "NULL";
    }

    //START, READY, STROKE-START, STROKE, STROKE-END, RELAX, END
    public Function getPhaseFunction(String typename){
       if(_phaseNameMapFunctions.containsKey(typename)){
           return getFunction(_phaseNameMapFunctions.get(typename));
       }else{
           return null;
       }
    }
}
