/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.feedbacks;

import vib.core.util.id.ID;
import vib.core.util.time.Temporizable;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public interface FeedbackPerformer {

   public void performFeedback(ID AnimId, String type, List<Temporizable> listTmp);

   public void performFeedback(Callback callback);

   public void setDetailsOption(boolean detailed);

   public boolean areDetailedFeedbacks();

   public void setDetailsOnFace(boolean detailsOnFace);

   public boolean areDetailsOnFace();

   public void setDetailsOnGestures(boolean detailsOnGestures);

   public boolean areDetailsOnGestures();

}
