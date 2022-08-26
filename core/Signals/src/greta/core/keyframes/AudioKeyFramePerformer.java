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
                    audio.save("output.wav",true);
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
