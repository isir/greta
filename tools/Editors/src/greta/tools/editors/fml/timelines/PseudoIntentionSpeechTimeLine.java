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
