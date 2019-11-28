/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package vib.auxiliary.thrift;

import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.services.ServerToExternal;
import vib.core.util.Constants;
import vib.core.util.Mode;
import vib.core.util.audio.Audio;
import vib.core.util.audio.AudioPerformer;
import vib.core.util.id.ID;
import vib.core.util.log.Logs;
import vib.core.util.time.Timer;
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
