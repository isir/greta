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
import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackPerformer;
import greta.core.util.time.Timer;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class CallbacksSender extends TextSender implements CallbackPerformer{

    private HashMap<String,Object> semaineMap;


    public CallbacksSender() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "semaine.callback.output.Animation");
    }

    public CallbacksSender(String host, String port, String topic) {
        super(host, port, topic);
        semaineMap = new HashMap<String,Object>();
        semaineMap.put("datatype", "callback");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
        semaineMap.put("xml", true);
    }

    @Override
    public void performCallback(Callback clbck) {

        XMLTree callback = XML.createTree("callback", "http://www.semaine-project.eu/semaineml");
        XMLTree event = callback.createChild("event", "http://www.semaine-project.eu/semaineml");
        event.setAttribute("type", clbck.type());
        event.setAttribute("data", "Animation");
        event.setAttribute("id", clbck.animId().toString());
        event.setAttribute("contentType", clbck.type());
        event.setAttribute("time", ""+((long)(clbck.time()*1000.0)));
        semaineMap.put("content-type", clbck.type());
        semaineMap.put("content-id", clbck.animId());
        this.send(callback.toString());
    }


    @Override
    protected void onSend(Map<String, Object> properties) {
        properties.put("usertime", Timer.getTimeMillis());
        properties.put("content-creation-time", Timer.getTimeMillis());
        properties.putAll(semaineMap);
        // Must be overrided to complete the map
    }

}
