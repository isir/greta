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
import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackEmitter;
import greta.core.feedbacks.CallbackPerformer;
import greta.core.util.id.IDProvider;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Ken Prepin
 */
public class CallbacksReceiver extends TextReceiver implements CallbackEmitter {

    private ArrayList<CallbackPerformer> callbackPerfList;
    private XMLParser callbackParser;

    public CallbacksReceiver() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "semaine.callback.output.Animation");
    }

    public CallbacksReceiver(String host, String port, String topic) {
        super(host, port, topic);
        callbackPerfList = new ArrayList<CallbackPerformer>();
        callbackParser = XML.createParser();
        callbackParser.setValidating(false);
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        XMLTree callbacks = callbackParser.parseBuffer(content.toString());

        if (callbacks == null) {
            return;
        }

        String callbackType = callbacks.findNodeCalled("event").getAttribute("type");
        double callbackTime = callbacks.findNodeCalled("event").getAttributeNumber("time") / 1000.;
        String requestId = properties.get("content-id").toString();
        Callback callback = new Callback(callbackType, callbackTime, IDProvider.createID(requestId));
        for (CallbackPerformer performer : callbackPerfList) {
            performer.performCallback(callback);
        }
    }

    @Override
    public void addCallbackPerformer(CallbackPerformer performer) {
        callbackPerfList.add(performer);
    }

    @Override
    public void removeCallbackPerformer(CallbackPerformer performer) {
        callbackPerfList.remove(performer);
    }
}
