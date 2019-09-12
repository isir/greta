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
 *//*
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
