/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Greta.  If not, see <https://www.gnu.org/licenses/>.
 *
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
