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
