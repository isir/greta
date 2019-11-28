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
