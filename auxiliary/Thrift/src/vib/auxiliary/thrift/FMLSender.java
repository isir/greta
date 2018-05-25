/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import java.util.List;
import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.services.Sender;
import vib.core.intentions.FMLTranslator;
import vib.core.intentions.Intention;
import vib.core.intentions.IntentionPerformer;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.time.Timer;

/**
 *
 * @author Brice Donval
 */
public class FMLSender extends Sender implements IntentionPerformer {

    public FMLSender() {
        this(Sender.DEFAULT_THRIFT_HOST, Sender.DEFAULT_THRIFT_PORT);
    }

    public FMLSender(String host, int port) {
        super(host, port);
    }

    public void sendFML(String FML, String requestId) {
        Message m = new Message();
        m.type = "FML";
        m.id = requestId + Timer.getTime();
        m.time = Timer.getTimeMillis();
        m.string_content = FML;
        send(m);
    }

    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode) {
        sendFML(FMLTranslator.IntentionsToFML(intentions, mode).toString(), requestId.toString());
    }

}
