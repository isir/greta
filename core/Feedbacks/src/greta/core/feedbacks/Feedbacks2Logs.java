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
