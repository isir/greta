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

import vib.auxiliary.activemq.TextSender;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.intentions.FMLTranslator;
import vib.core.intentions.Intention;
import vib.core.intentions.IntentionPerformer;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.xml.XMLTree;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class FMLSender extends TextSender implements IntentionPerformer {

    private HashMap<String, Object> semaineMap;

    public FMLSender() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "vib.FML");
    }

    public FMLSender(String host, String port, String topic) {
        super(host, port, topic);
        semaineMap = new HashMap<String, Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("datatype", "FML");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
        semaineMap.put("xml", true);
    }

    @Override
    public void performIntentions(List<Intention> list, ID requestId, Mode mode) {
        XMLTree fml = FMLTranslator.IntentionsToFML(list, mode);
        semaineMap.put("content-id", requestId.getSource().toString());
        this.send(fml.toString());
    }

    @Override
    protected void onSend(Map<String, Object> properties) {
        properties.put("usertime", System.currentTimeMillis());
        properties.put("content-creation-time", System.currentTimeMillis());
        properties.putAll(semaineMap);
        // Must be overrided to complete the map
    }
}
