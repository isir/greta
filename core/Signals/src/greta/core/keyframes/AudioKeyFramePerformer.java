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

import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.audio.Audio;
import greta.core.util.audio.AudioEmitter;
import greta.core.util.audio.AudioPerformer;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Andre-Marie Pez
 */
public class AudioKeyFramePerformer implements KeyframePerformer, AudioEmitter {

    private CharacterManager charactermanager;
    private ArrayList<AudioPerformer> audioPerformers;

    public AudioKeyFramePerformer(CharacterManager cm){
        this.charactermanager = cm;
        this.audioPerformers = new ArrayList<AudioPerformer>();
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId) {
        performKeyframes(keyframes, requestId, new Mode(CompositionType.blend));
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId, Mode mode) {
        // TODO : Mode management in progress
        ArrayList<Audio> audios = new ArrayList<Audio>();
        for (Keyframe kf : keyframes) {
            if (kf instanceof AudioKeyFrame) {
                try {
                    // Here you can chnage the Audio source for the speech (or before if you want GRETA to not compute the audio from the fml/bml
                    Audio audio = ((AudioKeyFrame) kf).getAudio();
                    System.out.println("greta.core.keyframes.AudioKeyFramePerformer.performKeyframes() "+audio.toString());
                    audio.save("output.wav",this.charactermanager.isAsap_enabled());
                    if (audio != null) {
                        audio.setTime(kf.getOffset());
                        audios.add(audio);
                    }
                } catch (IOException ex) {
                    Logger.getLogger(AudioKeyFramePerformer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        if (!audios.isEmpty()) {
            Collections.sort(audios, Audio.audioComparator);
            for (AudioPerformer performer : audioPerformers) {
                performer.performAudios(audios, requestId, mode);
            }
        }
    }

    @Override
    public void addAudioPerformer(AudioPerformer ap) {
        if (ap != null) {
            audioPerformers.add(ap);
        }
    }

    @Override
    public void removeAudioPerformer(AudioPerformer ap) {
        audioPerformers.remove(ap);
    }
}
