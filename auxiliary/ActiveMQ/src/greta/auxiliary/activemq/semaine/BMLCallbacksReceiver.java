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
import java.util.ArrayList;
import java.util.Map;

/**
 *
 * @author Angelo Cafaro
 */
public class BMLCallbacksReceiver extends TextReceiver implements CallbackEmitter {

    private ArrayList<CallbackPerformer> callbackPerformersfList;

    public BMLCallbacksReceiver() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "greta.input.feedback.BML");
    }

    public BMLCallbacksReceiver(String host, String port, String topic) {
        super(host, port, topic);
        callbackPerformersfList = new ArrayList<CallbackPerformer>();
    }

    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        String callbackType = properties.get("feedback-type").toString();
        double callbackTime = Double.parseDouble(properties.get("feedback-time").toString()) / 1000;
        String requestId = properties.get("feedback-id").toString();

        Callback callback = new Callback(callbackType, callbackTime, IDProvider.createID(requestId));
        for (CallbackPerformer performer : callbackPerformersfList) {
            performer.performCallback(callback);
        }
    }

    @Override
    public void addCallbackPerformer(CallbackPerformer performer) {
        callbackPerformersfList.add(performer);
    }

    @Override
    public void removeCallbackPerformer(CallbackPerformer performer) {
        callbackPerformersfList.remove(performer);
    }
}
