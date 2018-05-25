/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes;

import vib.core.util.math.Function;
import vib.core.util.math.easefunctions.*;
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
