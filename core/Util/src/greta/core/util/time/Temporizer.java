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
package greta.core.util.time;

import greta.core.util.log.Logs;
import java.util.ArrayList;
import java.util.List;

/**
 * This class temporizes (as possible) {@code Temporizables} and there {@code TimeMarkers}.<br/>
 * After create an instance of this class, you must add one or more {@code Temporizable}  using {@code add} function.<br/>
 * Then, call {@code temporize()} function to compute and deduce all {@code TimeMarker} of {@code Temporizables} added.
 * @see greta.core.util.time.Temporizable Temporizable
 * @see greta.core.util.time.TimeMarker TimeMarker
 * @author Andre-Marie Pez
 *
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @navassoc - "temporize and\nsynchronize" - greta.core.util.time.Temporizable
 */
public class Temporizer{

    private ArrayList<Temporizable> temporizables = new ArrayList<Temporizable>();

    /**
     * This function temporize all {@code Temporizable} added.<br/>
     * The {@code TimeMarkers} of all {@code Temporizable} added with {@code add} function will be deducted or computed.
     * @see #add(greta.core.util.time.Temporizable) add(Temporizable)
     * @see #add(java.util.List) add(List)
     * @return {@code true} if successfully temporized, {@code false} otherwise
     */
    public boolean temporize(){

        //temporize one by one :
        int i = 0;
        boolean recursive = false;
        int recursiveCount = 0;
        while(i<temporizables.size() && !recursive){
            Temporizable t = temporizables.get(i);

            // State of this Temporizable
            boolean toQueue = false;
            boolean toTemporize = false;
            boolean resetLoopControl = false;
            
            System.out.format("greta.core.util.time.Temporizer.temporize(): before: %s, %s, %s%n", t.getId(), t.getStart(), t.getEnd());

            for(TimeMarker tm :t.getTimeMarkers()){
                if(!tm.isConcretized()){
                    // Try to concretize using SynchPoints
                    List<SynchPoint> refs = tm.getReferences();
                    for(int j=0; j<refs.size() && !tm.isConcretized();++j){
                        SynchPoint ref = refs.get(j);
                        //try to link SynchPoint with timeMarkers
                        if(ref.hasTargetTimeMarker()){
                            if(ref.getTarget()==null){
                                String[] targetNameSplitted = ref.getTargetName().split(":");
                                if(targetNameSplitted.length<1){
                                    Logs.warning(this.getClass().getName() + " : the target of the synch point \""+ref.getTargetName()+"\" is missing.");
                                }
                                else{
                                    if(targetNameSplitted.length<2){
                                        Logs.debug(this.getClass().getName() + " : the target name of the synch point \""+ref.getTargetName()+"\" is incomplete. The system will use "+t.getId()+":"+ref.getTargetName());
                                        targetNameSplitted = new String[]{t.getId(),ref.getTargetName()};
                                    }
                                    Temporizable targetTemporizable = findSource(targetNameSplitted[0]);
                                    if(targetTemporizable==null){
                                        Logs.warning(this.getClass().getName() + " : can not find the target source id \""+targetNameSplitted[0]+"\".");
                                    }
                                    else{
                                        TimeMarker targetTimeMarker = targetTemporizable.getTimeMarker(targetNameSplitted[1]);
                                        if(targetTimeMarker==null){
                                            Logs.warning(this.getClass().getName() + " : can not find the target synch name \""+targetNameSplitted[1]+"\".");
                                        }
                                        else{
                                            ref.setTimeMarker(targetTimeMarker);
                                        }
                                    }
                                }
                            }
                        }
                        // check if the SynchPoint is concret (already temporized)
                        if(ref.isConcretized()){
                            tm.setValue(ref.getValue());
                            resetLoopControl = true;
                        }
                    }
                    // Update of the state of this Temporizable
                    if(!tm.isConcretized()){
                        // Can not concretize one TimeMarker so we need to temporize
                        toTemporize = true;
                        if(!refs.isEmpty()){
                            // We have at least one SynchPoint and it is not already concret
                            toQueue = true;
                        }
                    }
                }
                
            }

            // Following the states :

            if(toQueue){
                //move the current Temporizable to the end of the list
                temporizables.add(temporizables.remove(i));
            }
            else{
                if(toTemporize)
                {
                    t.schedule();
                }
                resetLoopControl = true;
                ++i; // go to the next
            }


            if(resetLoopControl) {
                recursiveCount = 0;
            }
            else {
                ++recursiveCount;
            }

            recursive = recursiveCount > temporizables.size()-i;
            
            // Treatment for rest GestureSignal (necessary to avoid error in sorting keyframes in greta.core.behaviorrealizer.Realizer.performSignals())
            if (Double.isNaN(t.getStart().getValue()) || Double.isInfinite(t.getStart().getValue()) || t.getStart().getValue() <= 0){
                t.getStart().setValue(0);
            }
            if (Double.isNaN(t.getEnd().getValue()) || Double.isInfinite(t.getEnd().getValue()) || t.getEnd().getValue() <= 0){
                t.getEnd().setValue(t.getStart().getValue());
            }

            System.out.format("greta.core.util.time.Temporizer.temporize(): after: %s, %s, %s%n", t.getId(), t.getStart(), t.getEnd());
            
        }

        if(recursive){
            String temporizableNotConcretized = "";
            for(int j = i; j<temporizables.size(); ++j) {
                temporizableNotConcretized += temporizables.get(j).getId() +" ";
            }
            Logs.error(this.getClass().getName() + " : can not temporize :\n" +temporizableNotConcretized +"\n(recursive refences found or incomplete information). They may be skipped.");
            return false;
        }

        return true;
    }

    /**
     * Adds a {@code Temporizable} that will be synchronized and
     * synchronised with other {@code Temporizables} added
     * when {@code temporize()} function will be called.
     * @see #temporize() temporize()
     * @param temporizable the {@code Temporizable} to add
     */
    public void add(Temporizable temporizable){
        temporizables.add(temporizable);
    }

    /**
     * Adds a list of {@code Temporizables} that will be synchronized and
     * synchronised with other {@code Temporizables} added
     * when {@code temporize()} function will be called.
     * @see #temporize() temporize()
     * @param temporizables the list of {@code Temporizables} to add
     */
    public void add(List<? extends Temporizable> temporizables){
        this.temporizables.addAll(temporizables);
    }

    //private methods
    private Temporizable findSource(String temporizableName){
        for(Temporizable t : temporizables) {
            if(t.getId().equals(temporizableName)) {
                return t;
            }
        }
        return null;
    }

}
