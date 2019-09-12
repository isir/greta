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

package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextReceiver;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.intentions.FMLTranslator;
import vib.core.intentions.Intention;
import vib.core.intentions.IntentionEmitter;
import vib.core.intentions.IntentionPerformer;
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
public class FMLReceiver extends TextReceiver implements IntentionEmitter {

    private ArrayList<IntentionPerformer> performers;
    private XMLParser fmlParser;
    private CharacterManager cm;

    public FMLReceiver(CharacterManager cm) {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "vib.FML",cm);
        
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