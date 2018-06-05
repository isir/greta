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
import vib.core.feedbacks.Callback;
import vib.core.feedbacks.CallbackPerformer;
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
                "vib.output.feedback.BML");
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