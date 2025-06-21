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

import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Florian Pecune
 */
public class SocialParameterEmitterImpl implements SocialParameterEmitter{

    private ArrayList<SocialParameterPerformer> performers = new ArrayList<SocialParameterPerformer>();

    @Override
    public void addSocialParameterPerformer(SocialParameterPerformer performer) {
        if (performer != null) {
            performers.add(performer);
        }
    }

    @Override
    public void removeSocialParameterPerformer(SocialParameterPerformer performer) {
        if (performer != null) {
            performers.remove(performer);
        }
    }

    public void sendSocialParameters(ID requestId, SocialParameterFrame... frames){
        sendSocialParameters(requestId, Arrays.asList(frames));
    }


    public void sendSocialParameters(ID requestId, List<SocialParameterFrame> frames){
        for(SocialParameterPerformer performer : performers){
            performer.performSocialParameter(frames, requestId);
        }
    }

    public void sendSocialParameter(ID requestId, SocialParameterFrame frame){
        sendSocialParameters(requestId, frame);
    }

}
