/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.feedbacks;

import java.util.ArrayList;
import java.util.List;
import vib.core.repositories.AUItem;
import vib.core.signals.FaceSignal;
import vib.core.signals.gesture.GesturePose;
import vib.core.signals.gesture.GestureSignal;
import vib.core.util.id.ID;
import vib.core.util.log.Logs;
import vib.core.util.time.Temporizable;

/**
 *
 * @author Ken Prepin
 */
public class Feedbacks2Logs implements FeedbackPerformer {

    private boolean detailedFeedbacks;
    private boolean detailsOnFace;
    private boolean detailsOnGestures;

    public Feedbacks2Logs() {
        detailedFeedbacks = true;
    }

    @Override
    public void performFeedback(ID AnimId, String type, List<Temporizable> listTmp) {
        double currentTime = vib.core.util.time.Timer.getTime();
        String content = "[Feedbacks2Logs] " + AnimId + "\n" + type + " " + currentTime +" ";
        if (detailedFeedbacks) {
            Logs.debug("[Feedbacks2Logs] " + "detailedFeebacks");
            if (detailsOnFace) {
                for (Temporizable tmp : listTmp) {
                    content += "\n" + tmp.getId() + " ";
                    if (tmp instanceof vib.core.signals.FaceSignal) {
                        FaceSignal tmpFaceSignal = (FaceSignal) tmp;
                        content += tmpFaceSignal.getModality() + " " + tmpFaceSignal.getCategory();
                        ArrayList<AUItem> actionUnits = tmpFaceSignal.getActionUnits();
                        for (AUItem au : actionUnits) {
                            content += "\n";
                            content += au.getAU() + " = " + au.getIntensity();
                        }
                    }
                }
            }
            if (detailsOnGestures) {
                for (Temporizable tmp : listTmp) {
                    content += "\n" + tmp.getId() + " ";
                    if (tmp instanceof vib.core.signals.gesture.GestureSignal) {
                        GestureSignal tmpGestureSignal = (GestureSignal) tmp;
                        content += tmpGestureSignal.getModality() + " " + tmpGestureSignal.getCategory();
                        List<GesturePose> gesturePhases = tmpGestureSignal.getPhases();
                        for (GesturePose phase : gesturePhases) {
                            content += "\n";
                            content += phase.getRelativeTime();
                        }
                    }
                }
            }
        } else {
            for (Temporizable tmp : listTmp) {
                content += "\n" + tmp.getId();
            }
        }
        Logs.info(content);
    }

    @Override
    public void performFeedback(Callback callback) {
        String content = callback.animId() + " " + callback.type() + " " + String.valueOf(callback.time());
        Logs.info(content);
    }

    @Override
    public void setDetailsOption(boolean detailed) {
        detailedFeedbacks = detailed;
    }

    @Override
    public boolean areDetailedFeedbacks() {
        return detailedFeedbacks;
    }

    @Override
    public void setDetailsOnFace(boolean detailsOnFace) {
       this.detailsOnFace = detailsOnFace;
    }

    @Override
    public boolean areDetailsOnFace() {
        return this.detailsOnFace;
    }

    @Override
    public void setDetailsOnGestures(boolean detailsOnGestures) {
        this.detailsOnGestures = detailsOnGestures;
    }

    @Override
    public boolean areDetailsOnGestures() {
        return this.detailsOnGestures;
    }
}
