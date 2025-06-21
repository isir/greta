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
import greta.auxiliary.thrift.services.Receiver;
import greta.core.signals.BMLTranslator;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Brice Donval
 */
public class BMLReceiver extends Receiver implements SignalEmitter {

    private CharacterManager cm;

    public BMLReceiver(CharacterManager cm) {
        super();
        this.cm = cm;
        performers = new ArrayList<SignalPerformer>();
    }

    public BMLReceiver(CharacterManager cm,int port) {
        super(port);
        this.cm = cm;
        performers = new ArrayList<SignalPerformer>();
    }

    private final ArrayList<SignalPerformer> performers;
    private final XMLParser bmlparser = XML.createParser();

    @Override
    public void perform(Message m) {
        XMLTree bml = bmlparser.parseBuffer(m.getString_content());
        List<Signal> signals = BMLTranslator.BMLToSignals(bml,cm);
        Mode mode = BMLTranslator.getDefaultBMLMode();
        CommandReceiver.setModeParametersForAnimationSignal(bml, mode);
        //send to all SignalPerformer added
        ID id = IDProvider.createID(m.getId());
        for (SignalPerformer performer : performers) {
            performer.performSignals(signals, id, mode);
        }
    }

    @Override
    public void addSignalPerformer(SignalPerformer performer) {
        performers.add(performer);
    }

    @Override
    public void removeSignalPerformer(SignalPerformer performer) {
        performers.remove(performer);
    }

}
