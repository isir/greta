/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.bml.timelines;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import vib.core.repositories.Gestuary;
import vib.core.repositories.SignalEntry;
import vib.core.repositories.SignalFiller;
import vib.core.signals.gesture.GestureSignal;

/**
 *
 * @author Andre-Marie
 */
public class GestureLib extends SignalLib<GestureSignal>{

    @Override
    public String getClassNameOf(GestureSignal signalInstance) {
        if(signalInstance.getCategory()==null){
            int pos = signalInstance.getReference().indexOf("=");
            if(pos>=0) {
                return signalInstance.getReference().substring(0,pos);
            }
            else{
                return null;
            }
        }
        return signalInstance.getCategory().toUpperCase();
    }

    @Override
    public String getInstanceNameOf(GestureSignal signalInstance) {
        return signalInstance.getReference().substring(signalInstance.getReference().indexOf("=")+1);
    }

    @Override
    public List<String> getAllInstanceNamesOf(String className) {
        LinkedList<String> instances = new LinkedList<String>();
        for(SignalEntry<GestureSignal> entry : Gestuary.global_gestuary.getAll()){
            GestureSignal gesture = entry.getSignal();
            if(gesture.getCategory().equalsIgnoreCase(className)){
                instances.add(gesture.getId().toUpperCase());
            }
        }
        return instances;
    }

    @Override
    public List<String> getAllClassNames() {
        ArrayList<String> classes = new ArrayList<String>();
        for(SignalEntry<GestureSignal> entry : Gestuary.global_gestuary.getAll()){
            GestureSignal gesture = entry.getSignal();
            if( ! classes.contains(gesture.getCategory().toUpperCase())){
                classes.add(gesture.getCategory().toUpperCase());
            }
        }
        return classes;
    }

    @Override
    public void applyOn(GestureSignal signal, String className, String instanceName) {
        String newRef = (className==null || className.isEmpty()) ? "" : className+"=";
        newRef += instanceName;
        if(! newRef.equalsIgnoreCase(signal.getReference())){
            signal.setCategory(className);
            signal.setReference(newRef);
            SignalFiller.fill(signal);
        }
    }

}
