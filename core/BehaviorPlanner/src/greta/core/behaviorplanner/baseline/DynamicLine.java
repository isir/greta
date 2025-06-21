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

import greta.core.util.parameter.EngineParameter;
import greta.core.util.parameter.EngineParameterSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * This class contains informations about expressivity parameters by modality for a specific {@code Intention}.
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @has - - - greta.core.util.parameter.EngineParameter
 */
public class DynamicLine {
    private Map<String,List<EngineParameter>> params;

    /**
     * Constructs a {@code DynamicLine} from a specific {@code BaseLine}.
     * @param baseline the {@code BaseLine} reference
     */
    public DynamicLine(BaseLine baseline){
        params = new HashMap<String,List<EngineParameter>>();
        for(EngineParameterSet eps : baseline.getAllSets()){
            add(eps.getName(), eps.getAll());
        }
    }

    /**
     * Returns the names of all known modalities.
     * @return a set of names of all known modalities.
     */
    public Set<String> getModalitiesNames(){
        return params.keySet();
    }

    /**
     * Returns all {@code EngineParameters} of a specific modality.
     * @param modality the name of the modality
     * @return the list of {@code EngineParameter}
     */
    public List<EngineParameter> getParameters(String modality){
        return params.get(modality);
    }

    /**
     * Returns a specific {@code EngineParameter}.
     * @param modality the name of the modality containing the {@code EngineParameter}
     * @param attribute the name of the {@code EngineParameter}
     * @return the target {@code EngineParameter}
     */
    public EngineParameter getParameter(String modality, String attribute){
        List<EngineParameter> epl = params.get(modality);
        if(epl!=null) {
            for(EngineParameter ep : params.get(modality)) {
                if(ep.getParamName().equalsIgnoreCase(attribute)) {
                    return ep;
                }
            }
        }
        return null;
    }

    /**
     * Adds a list of {@code EngineParameter} to a specific modality in the map.
     * @param modality the name of the modality
     * @param params theList of {@code EngineParameter} to add
     */
    private void add(String modality, List<EngineParameter> params){
        List<EngineParameter> target = this.params.get(modality);
        if(target == null){ //if the target list does not exist, we create it.
            target = new ArrayList<EngineParameter>();
            this.params.put(modality, target);
        }
        for(EngineParameter ep : params){
            target.add(ep.clone());
        }
    }

}
