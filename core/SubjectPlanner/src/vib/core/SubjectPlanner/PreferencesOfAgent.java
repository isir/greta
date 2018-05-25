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
public class PreferencesOfAgent extends EngineParameterSetOfSet{

    public static final String OBJECT_PREF_AGENT_LIB = "SubjectPlanner/Data/ObjectPreferenceAgent.xml";
    
    public static Sentences global_obj_pref_library;
     
    public PreferencesOfAgent(String filename){
      super(filename);
    }
    
}
