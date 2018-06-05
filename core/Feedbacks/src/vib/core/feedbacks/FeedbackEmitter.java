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
