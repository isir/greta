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
package greta.core.behaviorplanner.baseline;

import greta.core.util.parameter.Parameter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains informations on a BehaviorQualifier.
 * A Modulation corresponds to the set of modulations (mathematical operations)
 * that have to be performed over the parameters of
 * the Baseline when a certain CommunicativeAct is encountered. So this object contains a search map
 * of operations.
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @has - - * greta.core.behaviorplanner.baseline.Modulation
 */
public class Qualifier implements Parameter<Qualifier>{

    /**
     * Name of the CommunicativeAct associated to this Modulation.
     */
    private String name;
    /**
     * This is the search map of the modulations contained in the Modulation.
     * The map is made by lists of modulations, that is, each map element is a list
     * of modulations. The operations associated to the same attribute of the Baseline
     * are linked in a list and only the head of the list is kept in the search map.
     */
    private Map<String,List<Modulation>> modulations;

    /**
     * Class contructor.
     */
    Qualifier(String name){
        this.name = name;
        this.modulations = new HashMap<String,List<Modulation>>();
    }

    @Override
    public String getParamName() {
        return name;
    }

    @Override
    public void setParamName(String name) {
        this.name = name;
    }

    /**
     * Adds a modulation to the search map of operations.
     * @param mod the modulation to add
     */
    public void addModulation(Modulation mod){
        List<Modulation> target = modulations.get(mod.getDestinationModality());
        if(target == null){ //if the target list does not exist, we create it.
            target = new ArrayList<Modulation>();
            modulations.put(mod.getDestinationModality(), target);
        }
        target.add(mod);
    }

    /**
     * Accessor to the map of {@code Modulations}.
     * @return the map of {@code Modulations}
     */
    public Map<String,List<Modulation>> getModulations(){
        return modulations;
    }

    /**
     * Returns the list of {@code Modulations} to a specific modality.
     * @param modality the name of the modality
     * @return the list of the corresponding {@code Modulation}
     */
    public List<Modulation> getModulation(String modality){
        return modulations.get(modality);
    }

    @Override
    public boolean equals(Qualifier other) {
        return this == other;
        //TODO make a better comparison
    }

}
