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

import greta.core.repositories.AUExpression;
import greta.core.repositories.FaceLibrary;
import greta.core.repositories.SignalFiller;
import greta.core.signals.FaceSignal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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
