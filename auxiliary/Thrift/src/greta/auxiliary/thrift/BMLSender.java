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
import greta.auxiliary.thrift.services.Sender;
import greta.core.signals.BMLTranslator;
import greta.core.signals.Signal;
import greta.core.signals.SignalPerformer;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.time.Timer;
import java.util.List;

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
