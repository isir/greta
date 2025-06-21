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
package greta.core.intentplanner;

import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.parameter.EngineParameterSetOfSet;

/**
 * This class provides a representation of the agent mental state.<br/>
 * It contains the list of the agent's communicative intentions and the importance the agent attributs to each one of them
 * At present it is used only by the Listener Intent Planner to generate "response" backchannels
 *
 * @author Elisabetta Bevacqua
 */
public class AgentState extends EngineParameterSetOfSet implements CharacterDependent {

    private static final String AGENT_STATE_PARAMETER_NAME = "AGENTSTATE";
    private static final AgentState globalState;
    static{
        globalState = new AgentState(CharacterManager.getStaticInstance());
    }

    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    public void setCharacterManager(CharacterManager characterManager) {
        if(this.characterManager!=null)
            this.characterManager.remove(this);
        this.characterManager = characterManager;
        characterManager.add(this);
    }

    public static AgentState getGlobalState(){
        return globalState;
    }

    /**
     * Construct an agent state with the default values found in the {@code CharacterManager}.
     */
    public AgentState(CharacterManager cm){
        //get the default agent state :
        super();
        setCharacterManager(cm);
        set(getCharacterManager().getDefaultValueString(AGENT_STATE_PARAMETER_NAME));
        //load additionnal agent state :
        for(String fileName : getCharacterManager().getAllValuesString(AGENT_STATE_PARAMETER_NAME)) {
            add(fileName);
        }
        //set the current agent state to use :
        set( getCharacterManager().getValueString(AGENT_STATE_PARAMETER_NAME));
    }

    @Override
    public void onCharacterChanged() {
        set( getCharacterManager().getValueString(AGENT_STATE_PARAMETER_NAME));
    }

    public void modifyState(String setName, String paramName, double newValue){
        find(setName).get(paramName).setValue(newValue);
    }
}
