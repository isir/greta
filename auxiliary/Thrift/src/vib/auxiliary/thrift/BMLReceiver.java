/*
 * This file is part of the auxiliaries of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
 */

package vib.auxiliary.thrift;

import java.util.ArrayList;
import java.util.List;
import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.services.Receiver;
import vib.core.signals.BMLTranslator;
import vib.core.signals.Signal;
import vib.core.signals.SignalEmitter;
import vib.core.signals.SignalPerformer;
import vib.core.util.CharacterManager;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;

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
