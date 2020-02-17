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
