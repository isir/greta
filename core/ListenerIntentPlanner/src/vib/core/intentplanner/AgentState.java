/*
 * This file is part of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
 */

package vib.core.intentplanner;

import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.parameter.EngineParameterSetOfSet;

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
        for(String filename : getCharacterManager().getAllValuesString(AGENT_STATE_PARAMETER_NAME)) {
            add(filename);
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
