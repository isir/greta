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
package greta.core.util.parameter;

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains {@code EngineParameters}, and manages a multi-definition of this parameters.
 * @author Andre-Marie Pez
 * @see greta.core.util.parameter.EngineParameter EngineParameter
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @has - - * greta.core.util.parameter.EngineParameter
 */
public class EngineParameterSet extends ParameterSet<EngineParameter>{

    /** The name of this {@code EngineParameterSet} */
    private String name;

    /**
     * Construct an {@code EngineParameterSet}.
     * @param nameOfTheSet the name of this {@code EngineParameterSet}
     * @param defaultDefinitionName The name of the default definition
     */
    public EngineParameterSet(String nameOfTheSet, String defaultDefinitionName){
        super(defaultDefinitionName);
        name = nameOfTheSet;
    }

    /**
     * Returns only an empty list of {@code EngineParameter}<br/>
     * The {@code EngineParameters} can not be loaded only by the name of the definition.
     * It is the {@code EngineParameterSetOfSet} that loads them.
     * @param definition the name of the definition
     * @return an empty list of {@code EngineParameter}
     */
    @Override
    protected List<EngineParameter> load(String definition) {
        return new ArrayList<EngineParameter>();
    }

    /**
     * Returns the name of this {@code EngineParameterSet}
     * @return the name of this {@code EngineParameterSet}
     */
    public String getName(){
        return name;
    }

    /**
     * Adds one {@code EngineParameter} in a specific definition.<br/>
     * (Only if the target definition already exists)
     * @param definitionName the name of the target definition
     * @param parameter the parameter to add
     */
    public void addParameter(String definitionName, EngineParameter parameter){
        Definition<EngineParameter> def = findDefinition(definitionName);
        if(def != null) {
            def.addParameter(parameter);
        }
    }

    /**
     * Adds a list of {@code EngineParameter} in a specific definition.<br/>
     * (Only if the target definition already exists)
     * @param definitionName the name of the target definition
     * @param parameters the list of parameters to add
     */
    public void addParameters(String definitionName, List<EngineParameter> parameters){
        Definition<EngineParameter> def = findDefinition(definitionName);
        if(def != null) {
            def.addParameters(parameters);
        }
    }

    @Override
    protected void save(String definition, List<EngineParameter> paramToSave) {
        //hox to save it
    }
}
