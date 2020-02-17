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
