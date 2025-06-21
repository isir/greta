/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
