/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.socialparameters;

import greta.core.util.CharacterDependentAdapter;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import java.util.List;

/**
 *
 * @author Angelo
 */
public class SocialParameterBaselineInfluence extends CharacterDependentAdapter implements SocialParameterPerformer {

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
                    getCharacterManager().setValueString("BASELINE", "./BehaviorPlanner/Baseline/baseline_angry.xml");
                }
                else if (newLikingValue >= 0.5)
                {
                    getCharacterManager().setValueString("BASELINE", "./BehaviorPlanner/Baseline/baseline_happy_medium.xml");
                }
                else {
                    getCharacterManager().setValueString("BASELINE", "./BehaviorPlanner/Baseline/baseline_neutral.xml");
                }
            }
            currentLikingValue = newLikingValue;
        }
    }

    @Override
    public void onCharacterChanged() {
        Logs.info("SocialParameterBaselineInfluence.onCharacterChanged(): nothing to do");
    }

}
