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
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.Signal;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.time.Timer;
import java.util.List;

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

    @Override
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode, List<Signal> inputSignals){
        
    };    
    
}
