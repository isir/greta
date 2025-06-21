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

import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.parameter.EngineParameterSetOfSet;

/**
 * This class contains informations about base line of the agent.<br/>
 * It may have one or more definition, that is you can add/set the Baseline you want to use,
 * it is not usefull to add/set a complete base line, the missing parameter are found in the default
 * base line. (make sure that the default one is complete !)
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @has - - * greta.core.util.parameter.EngineParameter
 */
public class BaseLine extends EngineParameterSetOfSet implements CharacterDependent{
    public static final String CHARACTER_PARAMETER_BASELINE = "BASELINE";
//public methods :
    /**
     * Construct a base line with the default base line and all additionnal base line found in the {@code CharacterManager}.
     */
    public BaseLine(CharacterManager cm){
        //get the default baseline :
        super();
        setCharacterManager(cm);
        set(cm.getDefaultValueString(CHARACTER_PARAMETER_BASELINE));
        //load additionnal baseLines :
        for(String fileName : cm.getAllValuesString(CHARACTER_PARAMETER_BASELINE)) {
            add(fileName);
        }

        //set the current baseLines to use :
        set(getCharacterManager().getValueString(CHARACTER_PARAMETER_BASELINE));
    }

    @Override
    public void onCharacterChanged() {
        set(getCharacterManager().getValueString(CHARACTER_PARAMETER_BASELINE));
    }

    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        if(this.characterManager!=null)
            this.characterManager.remove(this);
        this.characterManager = characterManager;
        characterManager.add(this);
    }
}
