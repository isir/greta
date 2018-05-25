/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.keyframes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import vib.core.util.Mode;
import vib.core.util.audio.Audio;
import vib.core.util.audio.AudioEmitter;
import vib.core.util.audio.AudioPerformer;
import vib.core.util.enums.CompositionType;
import vib.core.util.id.ID;

/**
 *
 * @author Andre-Marie Pez
 */
public class AudioKeyFramePerformer implements KeyframePerformer, AudioEmitter {

    private ArrayList<AudioPerformer> audioPerformers = new ArrayList<AudioPerformer>();

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
                Audio audio = ((AudioKeyFrame) kf).getAudio();
                if (audio != null) {
                    audio.setTime(kf.getOffset());
                    audios.add(audio);
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
