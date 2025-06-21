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
package greta.core.util.audio;

import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.id.ID;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class AudioEmitterImpl implements AudioEmitter{

    private ArrayList<AudioPerformer> performers = new ArrayList<AudioPerformer>();

    @Override
    public void addAudioPerformer(AudioPerformer audioPerformer) {
        if(audioPerformer != null){
            performers.add(audioPerformer);
        }
    }

    @Override
    public void removeAudioPerformer(AudioPerformer audioPerformer) {
        performers.remove(audioPerformer);
    }


    public void sendAudio(ID requestId, Audio audio){
        sendAudios(requestId, audio);
    }

    public void sendAudio(ID requestId, Mode mode, Audio audio){
        sendAudios(requestId, mode, audio);
    }

    public void sendAudios(ID requestId, Audio... audios){
        sendAudios(requestId, Arrays.asList(audios));
    }

    public void sendAudios(ID requestId, Mode mode, Audio... audios){
        sendAudios(requestId, mode, Arrays.asList(audios));
    }

    public void sendAudios(ID requestId, List<Audio> audios){
        sendAudios(requestId, new Mode(CompositionType.replace), audios);
    }

    public void sendAudios(ID requestId, Mode mode, List<Audio> audios){
        for(AudioPerformer performer : performers){
            performer.performAudios(audios, requestId, mode);
        }
    }
}
