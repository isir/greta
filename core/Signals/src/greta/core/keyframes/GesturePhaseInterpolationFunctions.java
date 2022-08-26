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
