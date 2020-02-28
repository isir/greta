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

import greta.core.intentions.PseudoIntentionSpeech;
import greta.core.util.CharacterManager;
import greta.tools.editors.SpeechUtil;
import greta.tools.editors.TemporizableContainer;
import greta.tools.editors.TimeLine;
import java.awt.FontMetrics;

/**
 *
 * @author Andre-Marie
 */
public class PseudoIntentionSpeechTimeLine extends TimeLine<PseudoIntentionSpeech> {
    private CharacterManager cm;

    public PseudoIntentionSpeechTimeLine(CharacterManager cm){
        this.cm = cm;
    }

    @Override
    protected TemporizableContainer<PseudoIntentionSpeech> instanciateTemporizable(double startTime, double endTime) {
        return new TemporizableContainer<PseudoIntentionSpeech>(
                            new PseudoIntentionSpeech(SpeechUtil.instanciateTemporizable(cm, startTime, endTime)),
                            manager.getLabel());
    }

    @Override
    protected String getDescription(TemporizableContainer<PseudoIntentionSpeech> temporizableContainer, FontMetrics metrics, int limitSize) {
        return SpeechUtil.getDescription(temporizableContainer.getTemporizable(), metrics, limitSize);
    }

}
