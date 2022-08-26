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
package greta.core.keyframes;

import greta.core.util.audio.Audio;
import greta.core.util.laugh.Laugh;
import greta.core.util.speech.Speech;
import javax.sound.sampled.AudioFormat;

/**
 *
 * @author Andre-Marie Pez
 */
public class AudioKeyFrame implements Keyframe {

    private String id;
    private double onset;
    private double offset;
    private final Audio audio;

    public AudioKeyFrame(Speech speech) {
        this(
                speech.getId() + "_audio",
                speech.getAudio(),
                speech.getStart().getValue());
    }

    public AudioKeyFrame(Laugh laugh) {
        this(
                laugh.getId() + "_audio",
                laugh.getAudio(),
                laugh.getStart().getValue());
    }

    public AudioKeyFrame(String identifier, Audio audio, double startTime) {
        id = identifier;
        this.audio = audio;
        offset = startTime;
        onset = 0;
    }

    public byte[] getBuffer() {
        return audio.getBuffer();
    }

    public AudioFormat getAudioFormat() {
        return audio.getFormat();
    }

    public Audio getAudio() {
        return audio;
    }

    @Override
    public double getOffset() {
        return offset;
    }

    @Override
    public void setOffset(double time) {
        offset = time;
    }

    @Override
    public double getOnset() {
        return onset;
    }

    @Override
    public void setOnset(double time) {
        onset = time;
    }

    @Override
    public String getModality() {
        return "speech";
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getPhaseType() {          //??????
        return "audio";
    }

    @Override
    public String getCategory() {           //??????
        return "audio";
    }

    public String getTrajectoryType() {     //??????
        return "audio";
    }

    public double getDuration() {
        if (audio == null) {
            return 0;
        }
        return audio.getDuration();
    }
}
