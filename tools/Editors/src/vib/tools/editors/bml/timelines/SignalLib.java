/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.bml.timelines;

import vib.core.signals.Signal;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie
 */
public abstract class SignalLib<P extends Signal> {

    public abstract void applyOn(P signal, String className, String instanceName);
    //public abstract P get(String className, String instanceName); // Angelo: commented because never called by the classes that extend SignalLib
    public abstract String getClassNameOf(P signal);
    public abstract String getInstanceNameOf(P signal);
    public abstract List<String> getAllInstanceNamesOf(String className);
    public abstract List<String> getAllClassNames();

    public Map<String, List<String>> buildLib(){
        HashMap<String, List<String>> lib = new HashMap<String, List<String>>();
        List<String> keys = this.getAllClassNames();
        for(String key : keys){
            List<String> instances = this.getAllInstanceNamesOf(key);
            Collections.sort(instances, String.CASE_INSENSITIVE_ORDER);
            lib.put(key, instances);
        }
        return lib;
    }

}
