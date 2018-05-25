/*
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
