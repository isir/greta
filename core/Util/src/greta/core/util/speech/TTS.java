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
