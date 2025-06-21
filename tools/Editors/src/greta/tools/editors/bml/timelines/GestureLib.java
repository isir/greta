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

import greta.core.repositories.Gestuary;
import greta.core.repositories.SignalEntry;
import greta.core.repositories.SignalFiller;
import greta.core.signals.gesture.GestureSignal;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

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
