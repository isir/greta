/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.core.feedbacks;

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
     * @see greta.core.feedbacks.FeedbackPerformer#performFeedback(greta.core.util.id.ID, java.lang.String, java.util.List)
     */
    public void addFeedbackPerformer(FeedbackPerformer performer);

    /**
     * Removes the first occurence of an {@code CallbackPerformer}.
     * @param performer the {@code CallbackPerformer} to remove
     */
    public void removeFeedbackPerformer(FeedbackPerformer performer);
}
