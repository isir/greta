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
