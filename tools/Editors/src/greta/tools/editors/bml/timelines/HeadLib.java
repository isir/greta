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
package greta.tools.editors.bml.timelines;

import greta.core.repositories.HeadLibrary;
import greta.core.repositories.SignalEntry;
import greta.core.repositories.SignalFiller;
import greta.core.signals.HeadSignal;
import java.util.ArrayList;
import java.util.List;

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
