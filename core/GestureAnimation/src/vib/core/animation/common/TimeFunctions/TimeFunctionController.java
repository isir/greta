/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.common.TimeFunctions;
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
