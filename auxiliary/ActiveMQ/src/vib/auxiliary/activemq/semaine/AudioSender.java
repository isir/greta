/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.jms.BytesMessage;
import javax.jms.JMSException;
import javax.jms.Message;
import vib.auxiliary.activemq.Sender;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.keyframes.AudioKeyFrame;
import vib.core.keyframes.Keyframe;
import vib.core.keyframes.KeyframePerformer;
import vib.core.util.id.ID;
import vib.core.util.Mode;

/**
 * This class sends an audio buffer onto an ActiveMQ white board.
 * @author Andre-Marie Pez
 */
public class AudioSender extends Sender<Object> implements KeyframePerformer{

    public AudioSender(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "vib.Audio");
    }

    public AudioSender(String host, String port, String topic){
        super(host, port, topic);
    }


    @Override
    protected void onSend(Map<String, Object> properties) {
    }

    @Override
    protected Message createMessage(Object content) throws JMSException {
        if(content instanceof byte[]){
            BytesMessage msg = session.createBytesMessage();
            msg.writeBytes(((byte[])content));
            //msg.getStringProperty("format");
            System.out.println("Send audio");
            return msg;
        }
        else{
            throw new JMSException("Wrong content type");
        }
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId) {
        for(Keyframe kf : keyframes){
            if(kf instanceof AudioKeyFrame){
                AudioKeyFrame akf = (AudioKeyFrame)kf;

                HashMap<String,Object> map = new HashMap<String,Object>();
                map.put("content-id", requestId.toString());
                map.put("datatype", "AUDIO");
                map.put("start at", akf.getOffset());
                System.out.println("Audio Start at: " + akf.getOffset()/40);
                //map.put("format", akf.getAudioFormat());
                map.put("channels", akf.getAudioFormat().getChannels());
                map.put("encoding", akf.getAudioFormat().getEncoding().toString());
                map.put("frame rate", akf.getAudioFormat().getFrameRate());
                map.put("frame size", akf.getAudioFormat().getFrameSize());
                map.put("sample rate", akf.getAudioFormat().getSampleRate());
                map.put("sample size in bits", akf.getAudioFormat().getSampleSizeInBits());
                map.put("is big endian", akf.getAudioFormat().isBigEndian());

                send(akf.getBuffer(), map);
            }
        }
    }

    @Override
    public void performKeyframes(List<Keyframe> keyframes, ID requestId, Mode mode) {
        // TODO : Mode management in progress
        performKeyframes(keyframes, requestId);
    }

}
