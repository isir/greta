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

import greta.core.util.parameter.EngineParameter;
import greta.core.util.parameter.EngineParameterSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

/**
 *
 * @author Nadine
 */
public class CharacteristicDistances {

    private final CharacteristicMap map;
    private final PreferencesCharacteristics characteristic_preferences;

    public CharacteristicDistances() {

        map = new CharacteristicMap(CharacteristicMap.CHAR_MAP);

        characteristic_preferences = new PreferencesCharacteristics(PreferencesCharacteristics.PREF_CHARACTERISTICS);


    }

    public double GetPositionXCharacteristic(String characteristic, String characteristic_instance) {
        EngineParameterSet instances = map.find(characteristic);
        for (EngineParameter instance : instances.getAll()) {
            if (instance.getParamName().equals(characteristic_instance)) {
                double instancePositionX = instance.getValue();
                //         System.out.println("postion X" + characteristic_instance + instancePositionX);
                return instancePositionX;
            }
        }
        return 0;
    }

    public double GetPositionYCharacteristic(String characteristic, String characteristic_instance) {
        EngineParameterSet instances = map.find(characteristic);
        for (EngineParameter instance : instances.getAll()) {
            //       System.out.println(instance.getParamName());
            if (instance.getParamName().equals(characteristic_instance)) {
                double instancePositionY = instance.getMax();
                //           System.out.println(instance.getMax());
                //          System.out.println("postion Y" + characteristic_instance + instancePositionY);
                return instancePositionY;
            }
        }
        return 0;
    }

    public String GetFavouriteCharacteristicInstance(String Characteristic) {
        EngineParameterSet instances = characteristic_preferences.find(Characteristic);
        ArrayList<Double> preferenceList = new ArrayList();
        for (EngineParameter instance : instances.getAll()) {
            preferenceList.add(instance.getValue());
        }
        double MaximumPreference = Collections.max(preferenceList);
        for (EngineParameter instance : instances.getAll()) {
            if (instance.getValue() == MaximumPreference) {
                String favouriteInstance = instance.getParamName();
                // return favouriteInstance;
                return favouriteInstance;
            }

        }
        //return null;

        return "nothing";
    }

    public double GetDistanceFromCurrentInstance(String characteristic, String currentInstance, String newInstance) {
        double PositionXCurrentInstance = GetPositionXCharacteristic(characteristic, currentInstance);
        double PositionYCurrentInstance = GetPositionYCharacteristic(characteristic, currentInstance);
        double PositionXInstance2 = GetPositionXCharacteristic(characteristic, newInstance);
        double PositionYInstance2 = GetPositionYCharacteristic(characteristic, newInstance);
        double DistanceX = 1000;
        double DistanceY = 1000;
        if (PositionXCurrentInstance >= PositionXInstance2) {
            DistanceX = PositionXCurrentInstance - PositionXInstance2;
        } else {
            DistanceX = PositionXInstance2 - PositionXCurrentInstance;
        }
        if (PositionYCurrentInstance >= PositionYInstance2) {
            DistanceY = PositionYCurrentInstance - PositionYInstance2;
        } else {
            DistanceY = PositionYInstance2 - PositionYCurrentInstance;
        }
        double distanceFromCurrentInstance = Math.sqrt(Math.pow(DistanceX, 2) + Math.pow(DistanceY, 2));
        return distanceFromCurrentInstance;
    }

    public HashMap<String, Double> GetListDistancesFromCurrentInstance(String characteristic, String currentInstance) {
        HashMap<String, Double> Distances = new HashMap();
        double PositionXCurrentInstance = GetPositionXCharacteristic(characteristic, currentInstance);
        double PositionYCurrentInstance = GetPositionYCharacteristic(characteristic, currentInstance);
        EngineParameterSet positions = map.find(characteristic);
        for (EngineParameter position : positions.getAll()) {
            {
                if (position.getParamName() != currentInstance) {
                    double PositionXInstance2 = GetPositionXCharacteristic(characteristic, position.getParamName());
                    double PositionYInstance2 = GetPositionYCharacteristic(characteristic, position.getParamName());
                    double DistanceX = 1000;
                    double DistanceY = 1000;
                    if (PositionXCurrentInstance >= PositionXInstance2) {
                        DistanceX = PositionXCurrentInstance - PositionXInstance2;
                    } else {
                        DistanceX = PositionXInstance2 - PositionXCurrentInstance;
                    }
                    if (PositionYCurrentInstance >= PositionYInstance2) {
                        DistanceY = PositionYCurrentInstance - PositionYInstance2;
                    } else {
                        DistanceY = PositionYInstance2 - PositionYCurrentInstance;
                    }
                    double distanceTwoCharachteristicInstances = Math.sqrt(Math.pow(DistanceX, 2) + Math.pow(DistanceY, 2));
                    Distances.put(position.getParamName(), distanceTwoCharachteristicInstances);
                }
            }
        }
        return Distances;
    }
}
