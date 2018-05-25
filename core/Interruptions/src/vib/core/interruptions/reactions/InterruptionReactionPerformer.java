/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.interruptions.reactions;

import java.util.List;
import vib.core.util.id.ID;

/**
 *
 * @author Angelo Cafaro
 */
public interface InterruptionReactionPerformer {

    public void performInterruptionReaction(InterruptionReaction interruptionReaction, ID requestId);
    public void performInterruptionReactions(List<InterruptionReaction> interruptionReactions, ID requestId);

}
