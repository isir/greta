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
package greta.auxiliary.thrift;

import greta.auxiliary.thrift.gen_java.Message;
import greta.auxiliary.thrift.services.ServerToExternal;
import greta.core.util.Constants;
import greta.core.util.Mode;
import greta.core.util.audio.Audio;
import greta.core.util.audio.AudioPerformer;
import greta.core.util.id.ID;
import greta.core.util.log.Logs;
import greta.core.util.time.Timer;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class AudioServerToExternal extends ServerToExternal implements AudioPerformer {

    private boolean sendAudioBuffer;

    public AudioServerToExternal() {
        super();
        sendAudioBuffer = false;
    }
    public AudioServerToExternal(int port) {
        super(port);
        sendAudioBuffer = false;
    }

    @Override
    public void performAudios(List<Audio> listAudio, ID id, Mode mode) { // "mode" not used after the AudioKFramePerformer (except for replace)
        for (Audio audio : listAudio) {
            updateAudioMessage(audio, id);
        }
    }

    private void updateAudioMessage(Audio audio, ID id) {
        Message messageAudio = new Message();
        messageAudio.setType("Audio");
        messageAudio.setId(id.getSource() + Timer.getTimeMillis());
        messageAudio.setTime((long) (audio.getTimeMillis() / Constants.FRAME_DURATION_MILLIS));
        messageAudio.setFirstFrameNumber((long) (audio.getTimeMillis() / Constants.FRAME_DURATION_MILLIS));
        messageAudio.setLastFrameNumber((long) (audio.getEndMillis() / Constants.FRAME_DURATION_MILLIS));
        messageAudio.setString_content(id.getSource());
        HashMap<String,String> properties = new HashMap<String, String>();
        properties.put("sampleRate", Float.toString(audio.getFormat().getSampleRate()));
        messageAudio.setProperties(properties);
        Logs.debug("messageAudio: " + id.getSource() + " firstFrame: " + messageAudio.getFirstFrameNumber());
        if (sendAudioBuffer) {
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            audio.save(outStream);
            messageAudio.setBinary_content(outStream.toByteArray());
        }
        setMessage(messageAudio);
     //   Logs.debug("Message " + messageAudio.type + " " + messageAudio.id + " " + messageAudio.firstFrameNumber + " on the server");
    }

    public void setSendAudioBuffer(boolean sendBuffer) {
        sendAudioBuffer = sendBuffer;
    }

    public boolean getSendAudioBuffer() {
        return sendAudioBuffer;
    }
}
