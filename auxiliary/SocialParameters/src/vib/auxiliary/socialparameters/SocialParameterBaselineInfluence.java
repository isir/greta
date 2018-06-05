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
*/ /*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.socialparameters;

import java.util.List;
import vib.core.util.CharacterManager;
import vib.core.util.id.ID;
import vib.core.util.log.Logs;

/**
 *
 * @author Angelo
 */


public class SocialParameterBaselineInfluence implements SocialParameterPerformer {
    
    private double currentLikingValue = -2;
    @Override
    public void performSocialParameter(List<SocialParameterFrame> frames, ID requestId) {
        
        // Check if the list is empty
        if (!frames.isEmpty())
        {
            // Get the first for the moment
            SocialParameterFrame spf = frames.get(0);
            
            double newLikingValue = spf.getDoubleValue(SocialDimension.Liking);
            if (newLikingValue <= -0.5) {
                newLikingValue = -1;
            }
            else if (newLikingValue >= 0.5) 
            {
                newLikingValue = 1.0;
            } 
            else {
                newLikingValue = 0.0;
            } 
            
            if (newLikingValue != currentLikingValue) {
                
                Logs.info("New Liking Value received [" + newLikingValue + "] changing baseline");
                
                if (newLikingValue < -0.5) {
                    CharacterManager.setValueString("BASELINE", "./BehaviorPlanner/Baseline/baseline_angry.xml");
                }
                else if (newLikingValue >= 0.5) 
                {
                    CharacterManager.setValueString("BASELINE", "./BehaviorPlanner/Baseline/baseline_happy_medium.xml");
                } 
                else {
                    CharacterManager.setValueString("BASELINE", "./BehaviorPlanner/Baseline/baseline_neutral.xml");
                }
            }
            currentLikingValue = newLikingValue;
        }
    }
    
}
