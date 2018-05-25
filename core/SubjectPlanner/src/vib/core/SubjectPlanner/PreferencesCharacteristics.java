package vib.core.SubjectPlanner;


import vib.core.util.parameter.EngineParameterSetOfSet;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Nadine
 */
public class PreferencesCharacteristics extends EngineParameterSetOfSet{
    
    

    public static final String PREF_CHARACTERISTICS = "SubjectPlanner/Data/PreferencesCharacteristics.xml";//CHARACTER_OPINION
    
    public static PreferencesCharacteristics global_preferences_characteristics;
    

    public PreferencesCharacteristics(String filename){
        //get the default Lexicon :
        super(filename);
    }  

    


}
