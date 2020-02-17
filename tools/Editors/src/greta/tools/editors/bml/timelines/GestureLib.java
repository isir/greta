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
