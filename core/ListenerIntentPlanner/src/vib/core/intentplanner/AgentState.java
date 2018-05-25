/*
 * This file is part of VIB (Virtual Interactive Behaviour).
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
public class AgentState extends EngineParameterSetOfSet implements CharacterDependent{

    private static final String AGENT_STATE_PARAMETER_NAME = "AGENTSTATE";
    private static final AgentState globalState;
    static{
        globalState = new AgentState();
        CharacterManager.add(globalState);
    }

    public static AgentState getGlobalState(){
        return globalState;
    }

    /**
     * Construct an agent state with the default values found in the {@code CharacterManager}.
     */
    public AgentState(){
        //get the default agent state :
        super(CharacterManager.getDefaultValueString(AGENT_STATE_PARAMETER_NAME));
        //load additionnal agent state :
        for(String filename : CharacterManager.getAllValuesString(AGENT_STATE_PARAMETER_NAME)) {
            add(filename);
        }
        //set the current agent state to use :
        set(CharacterManager.getValueString(AGENT_STATE_PARAMETER_NAME));
    }

    @Override
    public void onCharacterChanged() {
        set(CharacterManager.getValueString(AGENT_STATE_PARAMETER_NAME));
    }

    public void modifyState(String setName, String paramName, double newValue){
        find(setName).get(paramName).setValue(newValue);
    }
}
