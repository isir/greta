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

import greta.auxiliary.activemq.TextSender;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.intentions.FMLTranslator;
import greta.core.intentions.Intention;
import greta.core.intentions.IntentionPerformer;
import greta.core.signals.Signal;
import greta.core.util.Mode;
import greta.core.util.id.ID;
import greta.core.util.xml.XMLTree;
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
                "greta.FML");
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
    public void performIntentions(List<Intention> intentions, ID requestId, Mode mode, List<Signal> inputSignals){
        
    };
    
    @Override
    protected void onSend(Map<String, Object> properties) {
        properties.put("usertime", System.currentTimeMillis());
        properties.put("content-creation-time", System.currentTimeMillis());
        properties.putAll(semaineMap);
        // Must be overrided to complete the map
    }
}
