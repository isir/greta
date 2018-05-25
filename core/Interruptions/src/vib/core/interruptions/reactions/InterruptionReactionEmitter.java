/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.interruptions.reactions;

/**
 *
 * @author Angelo Cafaro
 */
public interface InterruptionReactionEmitter {

    public void addInterruptionReactionPerformer(InterruptionReactionPerformer performer);

    public void removeInterruptionReactionPerformer(InterruptionReactionPerformer performer);

}
