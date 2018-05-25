/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.bml.timelines;

import java.util.ArrayList;
import java.util.List;
import vib.core.repositories.HeadLibrary;
import vib.core.repositories.SignalEntry;
import vib.core.repositories.SignalFiller;
import vib.core.signals.HeadSignal;

/**
 *
 * @author Andre-Marie
 */
public class HeadLib extends SignalLib<HeadSignal> {

    private static final String misc = "MISCELANOUS";

    @Override
    public void applyOn(HeadSignal signal, String className, String instanceName) {
        if(className!=null && !className.equalsIgnoreCase(signal.getLexeme())){
            signal.setLexeme(className==misc? null : className);
        }
        if(instanceName!=null && !instanceName.equalsIgnoreCase(signal.getReference())){
            signal.setReference(instanceName);
        }
        signal.reset();
        SignalFiller.fill(signal);
    }

//    @Override
    public HeadSignal get(String className, String instanceName) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public String getClassNameOf(HeadSignal signal) {
       return signal.getLexeme()==null? misc : signal.getLexeme();
    }

    @Override
    public String getInstanceNameOf(HeadSignal signal) {
        return signal.getReference();
    }

    @Override
    public List<String> getAllInstanceNamesOf(String className) {
        ArrayList<String> instances = new ArrayList<String>();
        for(SignalEntry<HeadSignal> entry : HeadLibrary.getGlobalLibrary().getAll()){
            if(className==misc){
                if(entry.getSignal().getLexeme()==null){
                    instances.add(entry.getSignal().getId().toUpperCase());
                }
            }
            else{
                if(className.equalsIgnoreCase(entry.getSignal().getLexeme())) {
                    instances.add(entry.getSignal().getId().toUpperCase());
                }
            }
        }
        return instances;
    }

    @Override
    public List<String> getAllClassNames() {
        ArrayList<String> classes = new ArrayList<String>();
        for(SignalEntry<HeadSignal> entry : HeadLibrary.getGlobalLibrary().getAll()){
            String ref = entry.getSignal().getLexeme()==null ? misc : entry.getSignal().getLexeme();
            if( ! classes.contains(ref)){
                classes.add(ref);
            }
        }
        return classes;
    }

}
