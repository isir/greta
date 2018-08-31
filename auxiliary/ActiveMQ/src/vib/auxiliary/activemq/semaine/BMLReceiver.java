/* This file is part of Greta.
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
*//*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextReceiver;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.signals.BMLTranslator;
import vib.core.signals.Signal;
import vib.core.signals.SignalEmitter;
import vib.core.signals.SignalPerformer;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import vib.core.util.CharacterManager;

/**
 *
 * @author Andre-Marie Pez
 */
public class BMLReceiver extends TextReceiver implements SignalEmitter {

    private ArrayList<SignalPerformer> performers;
    private XMLParser bmlParser;
    private CharacterManager cm;

    public BMLReceiver(CharacterManager cm) {
        this(cm,WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "vib.BML");
    }

    public BMLReceiver(CharacterManager cm,String host, String port, String topic) {
        super(host, port, topic);
        this.cm = cm;
        performers = new ArrayList<SignalPerformer>();
        bmlParser = XML.createParser();
        bmlParser.setValidating(false);
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        XMLTree bml = bmlParser.parseBuffer(content.toString());

        if (bml == null) {
            return;
        }

        Mode mode = BMLTranslator.getDefaultBMLMode();
        if (bml.hasAttribute("composition")) {
            mode.setCompositionType(bml.getAttribute("composition"));
        }
        if (bml.hasAttribute("reaction_type")) {
            mode.setReactionType(bml.getAttribute("reaction_type"));
        }
        if (bml.hasAttribute("reaction_duration")) {
            mode.setReactionDuration(bml.getAttribute("reaction_duration"));
        }
        if (bml.hasAttribute("social_attitude")) {
            mode.setSocialAttitude(bml.getAttribute("social_attitude"));
        }

        List<Signal> signals = BMLTranslator.BMLToSignals(bml,cm);
        
        Object contentId = null;
        if (bml.hasAttribute("id")) {
            contentId = bml.getAttribute("id");
        }
        else {
            contentId = properties.get("content-id");
        }
        
        ID id = IDProvider.createID(contentId == null ? "BMLReceiver" : contentId.toString());
        for (SignalPerformer performer : performers) {
            performer.performSignals(signals, id, mode);
        }
    }

    @Override
    public void addSignalPerformer(SignalPerformer performer) {
        if (performer != null) {
            performers.add(performer);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer performer) {
        performers.remove(performer);
    }
}