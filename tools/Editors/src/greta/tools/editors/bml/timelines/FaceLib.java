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
