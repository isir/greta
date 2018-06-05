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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import vib.core.util.parameter.EngineParameterSetOfSet;
import vib.core.util.log.Logs;
/**
 *
 * @author Nadine
 */
public class CharacteristicMap extends EngineParameterSetOfSet{
    
        public static final String CHAR_MAP = "SubjectPlanner/Data/CharacteristicMaps.xml";
    

    public static CharacteristicMap global_characteristic;
    
    
    public CharacteristicMap(String filename){
        //get the default Lexicon :
        super(filename);
  
    }
    
  //  public double GetCharacteristicDistance(String Characteristic){
  //      double CharacteristicDistance;
  //      CharacteristicDistance = CurrentCharacteristic - NewCharacteristic();
   //     return CharacteristicDistance;
    
   
    
}
