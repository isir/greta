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
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.util.id.ID;
import greta.core.util.time.Timer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class BAPSender extends TextSender implements BAPFramePerformer{

    private HashMap<String,Object> semaineMap;
    private BapCommander commander = new BapCommander();//semaine tricks

    public BAPSender(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "greta.BAP");
    }
    public BAPSender(String host, String port, String topic){
        super(host, port, topic);
        semaineMap = new HashMap<String,Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("datatype", "BAP");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
    }

    @Override
    public void performBAPFrames(List<BAPFrame> bapframes, ID requestId) {
        String bapString = getFrames(bapframes);
        semaineMap.put("content-id", requestId.toString() + System.currentTimeMillis());
          this.send(bapString, semaineMap);
    }

   @Override
   public void send(String text, Map<String, Object> properties) {
      String requestId=(String) properties.get("content-id");
        if(commander!=null) {
           commander.sendDataInfo(requestId);
       }
        super.send("\n"+text, properties);
        if(commander!=null) {
           commander.sendPlayCommand(requestId);
       }
    }


   public String getFrames(List<BAPFrame> frames) {
        StringBuilder bap = new StringBuilder();
        for (BAPFrame bapframe : frames) {
            bap.append(bapframe.AnimationParametersFrame2String());
        }
        return bap.toString();
    }
      @Override
    protected void onSend(Map<String, Object> properties) {
        properties.put("usertime", Timer.getTimeMillis());
        properties.put("content-creation-time", System.currentTimeMillis());
        properties.putAll(semaineMap);
        // Must be overrided to complete the map
    }

}
