/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.audio;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import vib.core.util.Mode;
import vib.core.util.enums.CompositionType;
import vib.core.util.id.ID;

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
