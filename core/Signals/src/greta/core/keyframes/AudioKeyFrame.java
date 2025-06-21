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
    
    private String parentId;

    public AudioKeyFrame(Speech speech) {
        this(
                speech.getId() + "_audio",
                speech.getAudio(),
                speech.getStart().getValue(),
                speech.getId() //equals parent signal ID
            );
    }

    public AudioKeyFrame(Laugh laugh) {
        this(
                laugh.getId() + "_audio",
                laugh.getAudio(),
                laugh.getStart().getValue(),
                laugh.getId() //equals parent signal ID
            );
    }

    public AudioKeyFrame(String identifier, Audio audio, double startTime) {
        id = identifier; //format ex: s1_Audio
        this.audio = audio;
        offset = startTime;
        onset = 0;
    }
    
    public AudioKeyFrame(String identifier, Audio audio, double startTime, String parParentId) {
        id = identifier; //format ex: s1_Audio
        this.audio = audio;
        offset = startTime;
        onset = 0;
        this.parentId = parParentId;
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

    @Override
    public String getParentId() {
        return parentId;
    }

    @Override
    public void setParentId(String parParentId) {
        this.parentId = parParentId;
    }
}
