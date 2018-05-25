/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.services.Sender;
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
