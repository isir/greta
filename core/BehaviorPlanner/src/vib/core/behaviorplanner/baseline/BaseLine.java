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
*//*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.behaviorplanner.baseline;

import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.parameter.EngineParameterSetOfSet;

/**
 * This class contains informations about base line of the agent.<br/>
 * It may have one or more definition, that is you can add/set the Baseline you want to use,
 * it is not usefull to add/set a complete base line, the missing parameter are found in the default
 * base line. (make sure that the default one is complete !)
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @has - - * vib.core.util.parameter.EngineParameter
 */
public class BaseLine extends EngineParameterSetOfSet implements CharacterDependent{
    public static final String CHARACTER_PARAMETER_BASELINE = "BASELINE";
//public methods :
    /**
     * Construct a base line with the default base line and all additionnal base line found in the {@code CharacterManager}.
     */
    public BaseLine(){
        //get the default baseline :
        super(CharacterManager.getDefaultValueString(CHARACTER_PARAMETER_BASELINE));

        //load additionnal baseLines :
        for(String filename : CharacterManager.getAllValuesString(CHARACTER_PARAMETER_BASELINE)) {
            add(filename);
        }

        //set the current baseLines to use :
        set(CharacterManager.getValueString(CHARACTER_PARAMETER_BASELINE));
    }

    @Override
    public void onCharacterChanged() {
        set(CharacterManager.getValueString(CHARACTER_PARAMETER_BASELINE));
    }
}
