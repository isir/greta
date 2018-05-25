/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.interruptions.reactions;

/**
 *
 * @author Angelo Cafaro
 */
public interface InterruptionReaction {

    public BehaviorType getBehaviorType();
    public void setBehaviorType(BehaviorType behaviorType);

    public InterruptionReactionParameters getParameters();
    public void setParameters(InterruptionReactionParameters params);

}
