/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextReceiver;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.animation.mpeg4.bap.BAPFramesEmitter;
import vib.core.animation.mpeg4.bap.BAPFramesEmitterImpl;
import vib.core.animation.mpeg4.bap.BAPFramesPerformer;
import vib.core.animation.mpeg4.bap.BAPParser;
import vib.core.util.id.IDProvider;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class BAPReceiver extends TextReceiver implements BAPFramesEmitter {

    private BAPFramesEmitterImpl bapEmitter = new BAPFramesEmitterImpl();
    private BAPParser parser = new BAPParser();

    public BAPReceiver() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "vib.BAP");
    }

    public BAPReceiver(String host, String port, String topic) {
        super(host, port, topic);
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {

        List<BAPFrame> frames = parser.readFromString(content.toString(), false, false);

        if (!frames.isEmpty()) {
            String id = "ActiveMQ_BAP_Receiver";
            if (properties.containsKey("content-id")) {
                id = properties.get("content-id").toString();
            } else {
                if (properties.containsKey("id")) {
                    id = properties.get("id").toString();
                }
            }
            bapEmitter.sendBAPFrames(IDProvider.createID(id), frames);
        }
    }

    @Override
    public void addBAPFramesPerformer(BAPFramesPerformer bapFramesPerformer) {
        bapEmitter.addBAPFramesPerformer(bapFramesPerformer);
    }

    @Override
    public void removeBAPFramesPerformer(BAPFramesPerformer bapFramesPerformer) {
        bapEmitter.removeBAPFramesPerformer(bapFramesPerformer);
    }

}
