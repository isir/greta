/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.feedbacks;

/**
 *
 * @author Ken Prepin
 */
public interface CallbackEmitter {
    /**
     * Adds an {@code CallbackEmitter}.<br/>
     * The function {@code performeCallbacks} of all {@code CallbackPerformer}
     * added will be called when this emmits a list of {@code Callbacks}.
     * @param performer the {@code CallbackPerformer} to add
     * @see vib.core.feedbacks.CallbackPerformer#performCallback(vib.core.feedbacks.Callback)
     */
    public void addCallbackPerformer(CallbackPerformer performer);

    /**
     * Removes the first occurence of an {@code CallbackPerformer}.
     * @param performer the {@code CallbackPerformer} to remove
     */
    public void removeCallbackPerformer(CallbackPerformer performer);
}
