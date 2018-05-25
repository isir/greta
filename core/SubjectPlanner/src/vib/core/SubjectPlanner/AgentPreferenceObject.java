/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package vib.core.SubjectPlanner;

import vib.core.util.parameter.EngineParameterSetOfSet;

/**
 *
 * @author Nadine
 */
public class AgentPreferenceObject extends EngineParameterSetOfSet{

    public static final String AGENT_OBJ_PREF = "SubjectPlanner/Data/ObjectPreferences.xml";
    
    public static Sentences global_agent_obj_pref;
     
    public AgentPreferenceObject(String filename){
      super(filename);
    }
    
}
