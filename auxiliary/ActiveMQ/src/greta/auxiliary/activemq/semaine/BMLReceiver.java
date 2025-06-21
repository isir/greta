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
package greta.auxiliary.activemq.semaine;

import greta.auxiliary.activemq.TextReceiver;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.signals.BMLTranslator;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class BMLReceiver extends TextReceiver implements SignalEmitter, CharacterDependent {

    private ArrayList<SignalPerformer> performers;
    private XMLParser bmlParser;
    private CharacterManager cm;

    public BMLReceiver(CharacterManager cm) {
        this(cm,WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "greta.BML");
    }

    public BMLReceiver(CharacterManager cm,String host, String port, String topic) {
        super(host, port, topic);
        setCharacterManager(cm);
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

    @Override
    public void onCharacterChanged() {
        //
    }

    @Override
    public CharacterManager getCharacterManager() {
        return cm;
    }

    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        this.cm = characterManager;
    }
}
