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
public class UserPreferenceObject extends EngineParameterSetOfSet{

    public static final String OBJECT_PREF_USER_LIB = "SubjectPlanner/Data/ObjectPreferencesUser.xml";
    
    public static Sentences global_obj_pref_user_library;
     
    public UserPreferenceObject(String filename){
      super(filename);
    }
    
}
