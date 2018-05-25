/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.intentions;

/**
 * This interface descibes an object that can send a list of {@code Signal} to all {@code SignalPerformer} added.
 *
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @composed - - * vib.core.intentions.IntentionPerformer
 * @navassoc - "emmits" * vib.core.intentions.Intention
 */
public interface IntentionEmitter {

    /**
     * Adds an {@code IntentionPerformer}.<br>
     * The function {@code performIntentions} of all {@code IntentionPerformer}
     * added will be called when this emmits a list of {@code Intention}.
     * @param performer the {@code IntentionPerformer} to add
     * @see vib.core.intentions.IntentionPerformer#performIntentions(java.util.List, vib.core.util.id.ID, vib.core.util.Mode) performIntentions
     */
    public void addIntentionPerformer(IntentionPerformer performer);

    /**
     * Removes the first occurence of an {@code IntentionPerformer}.
     * @param performer the {@code IntentionPerformer} to remove
     */
    public void removeIntentionPerformer(IntentionPerformer performer);
}
