/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.feedbacks;

import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public interface FeedbackEmitter {
    /**
     * Adds an {@code FeedbackEmitter}.<br/>
     * The function {@code performeFeedbacks} of all {@code FeedbackPerformer}
     * added will be called when this emmits a list of {@code Feedbacks}.
     * @param performer the {@code FeedbackPerformer} to add
     * @see vib.core.feedbacks.FeedbackPerformer#performFeedback(vib.core.util.id.ID, java.lang.String, java.util.List)
     */
    public void addFeedbackPerformer(FeedbackPerformer performer);

    /**
     * Removes the first occurence of an {@code CallbackPerformer}.
     * @param performer the {@code CallbackPerformer} to remove
     */
    public void removeFeedbackPerformer(FeedbackPerformer performer);
}
