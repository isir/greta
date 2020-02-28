/*
 * This file is part of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
 */
package greta.core.feedbacks;

import greta.core.signals.SpeechSignal;
import greta.core.util.id.ID;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public interface FeedbackPerformer {

   public void performFeedback(ID AnimId, String type, SpeechSignal speechSignal, TimeMarker tm);

   public void performFeedback(ID AnimId, String type, List<Temporizable> listTmp);

   public void performFeedback(Callback callback);

   public void setDetailsOption(boolean detailed);

   public boolean areDetailedFeedbacks();

   public void setDetailsOnFace(boolean detailsOnFace);

   public boolean areDetailsOnFace();

   public void setDetailsOnGestures(boolean detailsOnGestures);

   public boolean areDetailsOnGestures();

}
