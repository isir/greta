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
package greta.core.util.speech;

import greta.core.util.audio.Audio;
import java.util.List;

/**
 * This interface describe how a TTS (Text To Speech) must operate
 * @author Andre-Marie Pez
 */
public interface TTS {

    /**
     * Sets a {@code Speech} to performe.
     * @param speech the {@code Speech} to performe
     * @see greta.core.util.speech.Speech Speech
     */
    public void setSpeech(Speech speech);

    /**
     * This function computes needed datas.<br/>
     * A {@code Speech} must be set before.<br/>
     * If {@code doTemporize == true} it computes the values of the {@code TimeMarkers} in the {@code Speech} set.<br/>
     * If {@code doAudio == true} it computes the audio buffer and format corresponding to the text of the {@code Speech} set.<br/>
     * The audio buffer and format can be claimed later by calling {@code getAudioBuffer()} and {@code getAudioFormat()}.<br/>
     * If {@code doPhonemes == true} it computes the list of {@code Phonems} corresponding to the {@code Speech} set.<br/>
     * This list can be claimed later by calling {@code getPhonems()}.
     * @param doTemporize {@code true} to compute the values of the {@code TimeMarkers}
     * @param doAudio {@code true} to compute the audio buffer and format
     * @param doPhonemes {@code true} to compute the list of {@code Phonemes}
     * @see #setSpeech(greta.core.util.speech.Speech) setSpeech(Speech)
     */
    public void compute(boolean doTemporize, boolean doAudio, boolean doPhonemes);

    /**
     * Returns the list of {@code Phonemes} computed by {@code compute()}.
     * @return the list of {@code Phonemes}
     */
    public List<Phoneme> getPhonemes();

    /**
     * Returns the audio computed by {@code compute()}.
     * @return the audio
     */
    public Audio getAudio();

    /**
     * Returns a boolean value indicating whether interruption reactions are supported by the TTS in use
     * @return true if interruption reactions are supported by {@code compute()}, false otherwise.
     */
    public boolean isInterruptionReactionSupported();
}
