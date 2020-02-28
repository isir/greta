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

import greta.core.repositories.AUItem;
import greta.core.signals.FaceSignal;
import greta.core.signals.SpeechSignal;
import greta.core.signals.gesture.GesturePose;
import greta.core.signals.gesture.GestureSignal;
import greta.core.util.CharacterManager;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class Feedbacks2Logs implements FeedbackPerformer {

    private boolean detailedFeedbacks;
    private boolean detailsOnFace;
    private boolean detailsOnGestures;
    private CharacterManager charactermanager;

    public Feedbacks2Logs(CharacterManager cm) {
        this.charactermanager = cm;
        detailedFeedbacks = true;
    }

    @Override
    public void performFeedback(ID AnimId, String type, List<Temporizable> listTmp) {
        double currentTime = greta.core.util.time.Timer.getTime();
        String content = "[Feedbacks2Logs] " + AnimId + "\n" + type + " " + currentTime +" ";
        if (detailedFeedbacks) {
            Logs.debug("[Feedbacks2Logs] " + "detailedFeebacks");
            if (detailsOnFace) {
                for (Temporizable tmp : listTmp) {
                    content += "\n" + tmp.getId() + " ";
                    if (tmp instanceof greta.core.signals.FaceSignal) {
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
                    if (tmp instanceof greta.core.signals.gesture.GestureSignal) {
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
        String content = "fml_id: " + callback.animId().getFmlID() + ", type: " + callback.type() + ", time: " + callback.time() + ", agent: " + this.charactermanager.getCurrentCharacterName();
        //String content = callback.animId() + " " + callback.type() + " " + String.valueOf(callback.time());
        //String content = "{id: \"" + callback.animId() +"\", "+"type: \""+callback.type()+"\", "+ "time: " + String.valueOf(callback.time()) + "}";
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

    @Override
    public void performFeedback(ID AnimId, String type, SpeechSignal speechsignal, TimeMarker tm) {
        String content = "{\"type\": \"" +type + "\",\n";

        content += "\"TimeMarker_id\": " + tm.getName() +", \"time\": " + tm.getValue() + "}\n";
        Logs.info(content);
    }
}
