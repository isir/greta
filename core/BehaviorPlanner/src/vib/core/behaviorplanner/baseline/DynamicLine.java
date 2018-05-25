/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.behaviorplanner.baseline;

import vib.core.util.parameter.EngineParameter;
import vib.core.util.parameter.EngineParameterSet;
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
 * @has - - - vib.core.util.parameter.EngineParameter
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
