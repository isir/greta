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
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.socialparameters;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import vib.core.util.id.ID;

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
