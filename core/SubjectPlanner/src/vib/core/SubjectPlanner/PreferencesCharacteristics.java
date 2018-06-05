/* This file is part of Greta.
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
*/package vib.core.SubjectPlanner;


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
