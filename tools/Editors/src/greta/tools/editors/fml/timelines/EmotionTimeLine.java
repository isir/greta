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
package greta.tools.editors.fml.timelines;

import greta.core.intentions.EmotionIntention;
import greta.core.util.time.TimeMarker;
import greta.tools.editors.TemporizableContainer;
import greta.tools.editors.TimeLine;
import java.awt.FontMetrics;

/**
 *
 * @author Andre-Marie
 */
public class EmotionTimeLine extends TimeLine<EmotionIntention>{

    @Override
    protected TemporizableContainer<EmotionIntention> instanciateTemporizable(double startTime, double endTime) {
        return new TemporizableContainer<EmotionIntention> (
                new EmotionIntention("emotion_"+System.currentTimeMillis(), IntentionsAviable.EMOTIONS.length==0? "NEUTRAL" : IntentionsAviable.EMOTIONS[0], new TimeMarker("start",startTime), new TimeMarker("end",endTime), EmotionIntention.FELT, 1),
                manager.getLabel());
    }

    @Override
    protected String getDescription(TemporizableContainer<EmotionIntention> temporizableContainer, FontMetrics metrics, int limitSize) {
        return isGoodSize(temporizableContainer.getTemporizable().getType(), metrics, limitSize) ? temporizableContainer.getTemporizable().getType() : null;
    }
}
