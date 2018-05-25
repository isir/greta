/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift;

import java.util.List;
import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.services.Sender;
import vib.core.signals.BMLTranslator;
import vib.core.signals.Signal;
import vib.core.signals.SignalPerformer;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.time.Timer;

/**
 *
 * @author Ken Prepin
 */
public class BMLSender extends Sender implements SignalPerformer {

    public BMLSender() {
        this(Sender.DEFAULT_THRIFT_HOST, Sender.DEFAULT_THRIFT_PORT);
    }

    public BMLSender(String host, int port) {
        super(host, port);
    }

    public void sendBML(String BML, String requestId) {
        Message m = new Message();
        m.type = "BML";
        m.id = requestId + Timer.getTime();
        m.time = Timer.getTimeMillis();
        m.string_content = BML;
        send(m);
    }

    @Override
    public void performSignals(List<Signal> signals, ID requestId, Mode mode) {
        sendBML(BMLTranslator.SignalsToBML(signals, mode).toString(), requestId.toString());
    }

}
