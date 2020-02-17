/*
 * This file is part of the auxiliaries of Greta.
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
package greta.auxiliary.thrift;

import greta.auxiliary.thrift.gen_java.Message;
import greta.auxiliary.thrift.services.Sender;
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
public class AudioSender extends Sender implements AudioPerformer{

    private boolean sendAudioBuffer;

    public AudioSender(){
        this(Sender.DEFAULT_THRIFT_HOST,Sender.DEFAULT_THRIFT_PORT);
    }

    public AudioSender(String host, int port){
        super(host,port);
        sendAudioBuffer = false;
    }

    @Override
    public void performAudios(List<Audio> listAudio, ID id, Mode mode) { // "mode" not used after the AudioKFramePerformer (except for replace)
        for(Audio audio : listAudio){
            sendAudio(audio, id);
        }
    }

    private void sendAudio(Audio audio, ID id) {
        Message messageAudio =  new Message();
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
        if(sendAudioBuffer){
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            audio.save(outStream);
            messageAudio.setBinary_content(outStream.toByteArray());
        }

        this.send(messageAudio);
    }

    public void setSendAudioBuffer(boolean sendBuffer){
        sendAudioBuffer = sendBuffer;
    }
    public boolean getSendAudioBuffer(){
        return sendAudioBuffer;
    }

}
