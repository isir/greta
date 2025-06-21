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
import java.util.HashMap;

/**
 *
 * @author Angelo Cafaro
 */
public class BMLCallbacksSender extends TextSender implements CallbackPerformer {

    private HashMap<String,Object> propertiesMap;

    public BMLCallbacksSender() {
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
                WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
                "greta.output.feedback.BML");
    }

    public BMLCallbacksSender(String host, String port, String topic) {
        super(host, port, topic);
        propertiesMap = new HashMap<String,Object>();
    }

    @Override
    public void performCallback(Callback clbck) {
        propertiesMap.put("feedback-type", clbck.type());
        propertiesMap.put("feedback-id", clbck.animId().getSource());
        propertiesMap.put("feedback-time", ((long)(clbck.time()*1000.0)));
        this.send("", propertiesMap);
    }
}
