/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.SubjectPlanner;

import greta.core.util.parameter.EngineParameterSetOfSet;

/**
 *
 * @author Nadine
 */
public class CharacteristicMap extends EngineParameterSetOfSet{

        public static final String CHAR_MAP = "SubjectPlanner/Data/CharacteristicMaps.xml";


    public static CharacteristicMap global_characteristic;


    public CharacteristicMap(String fileName){
        //get the default Lexicon :
        super(fileName);

    }

  //  public double GetCharacteristicDistance(String Characteristic){
  //      double CharacteristicDistance;
  //      CharacteristicDistance = CurrentCharacteristic - NewCharacteristic();
   //     return CharacteristicDistance;


}
