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
import java.util.HashMap;

/**
 *
 * @author Jing Huang
 */
public class BapCommander extends TextSender{

    private HashMap<String,Object> semaineMap;

    public BapCommander(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "semaine.data.synthesis.lowlevel.command");
    }
    public BapCommander(String host, String port, String topic){
        super(host, port, topic);
        semaineMap = new HashMap<String,Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
    }

    public void sendDataInfo(String requestId) {
        semaineMap.put("datatype", "dataInfo");
        semaineMap.put("content-id", requestId);
        semaineMap.put("usertime", System.currentTimeMillis());
        semaineMap.put("content-creation-time", System.currentTimeMillis());
        this.send("HASAUDIO " + 0 + "\nHASFAP " + 0 + "\nHASBAP " + 1 + "\n", semaineMap);
    }

    public void sendPlayCommand(String requestId) {
        semaineMap.put("datatype", "playCommand");
        semaineMap.put("content-id", requestId);
        semaineMap.put("usertime", System.currentTimeMillis());
        semaineMap.put("content-creation-time", System.currentTimeMillis());
        this.send("STARTAT " + 0 + "\nLIFETIME " + 10000 + "\nPRIORITY " + 1 + "\n", semaineMap);
    }
}
