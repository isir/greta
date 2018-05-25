/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.socialparameters;

/**
 *
 * @author Florian Pecune
 */
public interface SocialParameterEmitter {
    
    public void addSocialParameterPerformer(SocialParameterPerformer performer);

    public void removeSocialParameterPerformer(SocialParameterPerformer performer);
    
}
