/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util.laugh;

import java.util.List;
import vib.core.util.audio.Audio;

/**
 *
 * @author Andre-Marie Pez
 * @author Yu Ding
 */
public interface LaughSynthetizer {

    /**
     * Sets a {@code Laugh} to performe.
     * @param laugh the {@code Laugh} to performe
     * @see vib.core.util.laugh.Laugh Laugh
     */
    public void setLaugh(Laugh laugh);

    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonemes);

    public List<LaughPhoneme> getPhonemes();

    public Audio getAudio();
}
