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
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class FMLReceiver extends TextReceiver implements IntentionEmitter {

    private ArrayList<IntentionPerformer> performers;
    private XMLParser fmlParser;
    private CharacterManager cm;

    public FMLReceiver(CharacterManager cm) {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "greta.FML",cm);

    }

    public FMLReceiver(String host, String port, String topic, CharacterManager cm) {
        super(host, port, topic);
        this.cm = cm;
        performers = new ArrayList<IntentionPerformer>();
        fmlParser = XML.createParser();
        fmlParser.setValidating(false);
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        XMLTree fml = fmlParser.parseBuffer(content.toString());

        String fml_id = "";

        if (fml == null) {
            return;
        }

        if(fml.hasAttribute("id")){
            fml_id = fml.getAttribute("id");
        }else{
            fml_id = "fml_1";
        }
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
        for (XMLTree fmlchild : fml.getChildrenElement()) {
            // store the bml id in the mode class
            if (fmlchild.isNamed("bml")) {
                //System.out.println(fmlchild.getName());
                if(fmlchild.hasAttribute("id")){
                    mode.setBml_id(fmlchild.getAttribute("id"));
                }
            }
        }

        List<Intention> intentions = FMLTranslator.FMLToIntentions(fml, cm);

        Object contentId = null;
        if (fml.hasAttribute("id")) {
            contentId = fml.getAttribute("id");
        }
        else {
            contentId = properties.get("content-id");
        }

        ID id = IDProvider.createID(contentId == null ? "FMLReceiver" : contentId.toString());
        id.setFmlID(fml_id);
        for (IntentionPerformer performer : performers) {
            performer.performIntentions(intentions, id, mode);
        }
    }

    @Override
    public void addIntentionPerformer(IntentionPerformer ip) {
        performers.add(ip);
    }

    @Override
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }
}
