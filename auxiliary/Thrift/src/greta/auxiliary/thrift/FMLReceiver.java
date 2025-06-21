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
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionEmitter;
import greta.core.intentions.IntentionPerformer;
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
 * @author Ken Prepin
 * @author Brian Ravenet
 */
public class FMLReceiver extends Receiver implements IntentionEmitter {
    private CharacterManager cm;

    public FMLReceiver(CharacterManager cm) {
        super();
        this.cm = cm;
        performers = new ArrayList<IntentionPerformer>();
    }

    public FMLReceiver(CharacterManager cm,int port) {
        super(port);
        this.cm = cm;
        performers = new ArrayList<IntentionPerformer>();
    }

    private final ArrayList<IntentionPerformer> performers;
    private final XMLParser fmlparser = XML.createParser();

    @Override
    public void perform(Message m) {
        XMLTree fml = fmlparser.parseBuffer(m.getString_content());
        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml,cm);
        Mode mode = FMLTranslator.getDefaultFMLMode();
        if (fml.hasAttribute("composition")) {
            mode.setCompositionType(fml.getAttribute("composition"));
        }
        if (fml.hasAttribute("reaction_type")) {
            mode.setReactionType(fml.getAttribute("reaction_type"));
        }
        if (fml.hasAttribute("reaction_duration")) {
            mode.setReactionDuration(fml.getAttribute("reaction_duration"));
        }
        if (fml.hasAttribute("social_attitude")) {
            mode.setSocialAttitude(fml.getAttribute("social_attitude"));
        }
        //send to all IntentionPerformer added
        ID id = IDProvider.createID(m.getId());
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode);
        }
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer performer) {
        performers.add(performer);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }

}
