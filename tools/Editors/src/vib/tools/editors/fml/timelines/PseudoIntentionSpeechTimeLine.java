/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.fml.timelines;

import vib.tools.editors.SpeechUtil;
import vib.tools.editors.TimeLine;
import vib.core.intentions.PseudoIntentionSpeech;
import java.awt.FontMetrics;
import vib.tools.editors.TemporizableContainer;

/**
 *
 * @author Andre-Marie
 */
public class PseudoIntentionSpeechTimeLine extends TimeLine<PseudoIntentionSpeech> {

    @Override
    protected TemporizableContainer<PseudoIntentionSpeech> instanciateTemporizable(double startTime, double endTime) {
        return new TemporizableContainer<PseudoIntentionSpeech>(
                            new PseudoIntentionSpeech(SpeechUtil.instanciateTemporizable(startTime, endTime)),
                            manager.getLabel());
    }

    @Override
    protected String getDescription(TemporizableContainer<PseudoIntentionSpeech> temporizableContainer, FontMetrics metrics, int limitSize) {
        return SpeechUtil.getDescription(temporizableContainer.getTemporizable(), metrics, limitSize);
    }

}
