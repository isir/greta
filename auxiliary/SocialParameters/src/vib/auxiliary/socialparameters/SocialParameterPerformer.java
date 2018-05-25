/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.socialparameters;

import java.util.List;
import vib.core.util.id.ID;

/**
 *
 * @author Florian Pecune
 */
public interface SocialParameterPerformer {

    public void performSocialParameter(List<SocialParameterFrame> frames, ID requestId);
    
    
}
