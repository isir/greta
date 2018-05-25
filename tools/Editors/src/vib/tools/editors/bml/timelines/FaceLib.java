/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.bml.timelines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import vib.core.repositories.AUExpression;
import vib.core.repositories.FaceLibrary;
import vib.core.repositories.Gestuary;
import vib.core.repositories.SignalEntry;
import vib.core.repositories.SignalFiller;
import vib.core.signals.FaceSignal;
import vib.core.signals.gesture.GestureSignal;

/**
 *
 * @author Andre-Marie
 */
public class FaceLib extends SignalLib<FaceSignal>{

    @Override
    public String getClassNameOf(FaceSignal signalInstance) {
        if(signalInstance.getCategory()==null){
            int pos = signalInstance.getReference().indexOf("=");
            if(pos>=0) {
                return signalInstance.getReference().substring(0,pos);
            }
            else{
                return null;
            }
        }
        return signalInstance.getCategory();
    }

    @Override
    public String getInstanceNameOf(FaceSignal signalInstance) {
        return signalInstance.getReference().substring(signalInstance.getReference().indexOf("=")+1);
    }

    @Override
    public List<String> getAllInstanceNamesOf(String className) {
        
        LinkedList<String> instances = new LinkedList<>();
        for (AUExpression faceexp : FaceLibrary.global_facelibrary.getAll()) {
            if (faceexp.getType().equalsIgnoreCase("faceexp")) {//only faceexps
                instances.add(faceexp.getInstanceName().toLowerCase());
            }
        }
        Collections.sort(instances, String.CASE_INSENSITIVE_ORDER);
        return instances;
    }

    @Override
    public List<String> getAllClassNames() {
        ArrayList<String> classes = new ArrayList<>();
        for (AUExpression faceexp : FaceLibrary.global_facelibrary.getAll()) {
            if( ! classes.contains(faceexp.getType().toLowerCase())){
                classes.add(faceexp.getType().toLowerCase());
            }
        }
        
        Collections.sort(classes, String.CASE_INSENSITIVE_ORDER);
        return classes;
    }

    @Override
    public void applyOn(FaceSignal signal, String className, String instanceName) {
        String newRef = (className==null || className.isEmpty()) ? "" : className+"=";
        newRef += instanceName;
        if(! newRef.equalsIgnoreCase(signal.getReference())){
            signal.setCategory(className);
            signal.setReference(newRef);
            SignalFiller.fill(signal);
        }
    }

}
