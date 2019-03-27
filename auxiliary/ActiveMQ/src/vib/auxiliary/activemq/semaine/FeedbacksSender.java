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
import vib.core.feedbacks.FeedbackPerformer;
import vib.core.util.id.ID;
import vib.core.util.time.Temporizable;
import vib.core.util.time.Timer;
import java.util.HashMap;
import java.util.List;
import vib.core.signals.SpeechSignal;
import vib.core.util.time.TimeMarker;


/**
 *
 * @author Ken Prepin
 */

public class FeedbacksSender extends TextSender implements FeedbackPerformer{

    private HashMap<String,Object> semaineMap;
    private boolean detailedFeedbacks;
    private boolean detailsOnFace;
    private boolean detailsOnGestures;

    public FeedbacksSender(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "semaine.callback.output.feedback");
    }
    public FeedbacksSender(String host, String port, String topic){
        super(host, port, topic);
        semaineMap = new HashMap<String,Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("datatype", "feedback");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
        semaineMap.put("xml", true);
        detailedFeedbacks = false; // TODO implement the possibility to use detailedFeedbacks
    }

    @Override
    public void performFeedback(ID AnimId, String Type, SpeechSignal speechsignal, TimeMarker tm) {
        String content = "{\"fml_id\": \"" + AnimId.getFmlID() + "\"," +" \"TimeMarker_id\": \"" + tm.getName() + "\"," + " \"type\": \"" + Type + "\"" + ", \"time\": " + tm.getValue() + "}\n";
  
        semaineMap.put("AnimId", AnimId.toString());
        semaineMap.put("type", "ongoing");
        semaineMap.put("current-time", Timer.getTimeMillis());
        semaineMap.put("content-id", AnimId.toString());
        semaineMap.put("usertime", Timer.getTimeMillis());
        semaineMap.put("content-creation-time", Timer.getTimeMillis());
        
        this.send(content, semaineMap);
    }
    
    @Override
    public void performFeedback(ID AnimId, String Type, List<Temporizable> listTmp) {
        
        // information about start and end of each gesture, facial expression
        // if you want this you should be sure that the format of the string sent as feedback will be in the json format
        // How it is implemented now it sent more id in the same sting and this it is not good so it need to be changed
        
        /*String content = "{\"type\": \"" +Type + "\",\n";
        for(Temporizable tmp : listTmp){
            content += "\"id\": \"" + tmp.getId() + "\",\n" + "\"time\": " + tmp.toString() + "}\n";
        }
        System.out.println(content);
        semaineMap.put("AnimId", AnimId.toString());
        semaineMap.put("type", "ongoing");
        semaineMap.put("current-time", Timer.getTimeMillis());
        semaineMap.put("content-id", AnimId.toString());
        semaineMap.put("usertime", Timer.getTimeMillis());
        semaineMap.put("content-creation-time", Timer.getTimeMillis());
        
        this.send(content, semaineMap);*/
     }

    @Override
    public void performFeedback(Callback callback) {
        
       String content = "{\"fml_id\": \"" + callback.animId().getFmlID() +"\", "+"\"type\": \""+callback.type()+"\", "+ "\"time\": " + String.valueOf(callback.time()) + "}";
        semaineMap.put("AnimId", callback.animId().toString());
        semaineMap.put("type", callback.type());
        semaineMap.put("current-time", Timer.getTimeMillis());
        semaineMap.put("content-id", callback.animId().toString());
        semaineMap.put("usertime", Timer.getTimeMillis());
        semaineMap.put("content-creation-time", Timer.getTimeMillis());
        this.send(content, semaineMap);
    }

    @Override
    public void setDetailsOption(boolean detailed){
        detailedFeedbacks = detailed;
    }
    @Override
    public boolean areDetailedFeedbacks(){
        return detailedFeedbacks;
    }

    @Override
    public void setDetailsOnFace(boolean detailsOnFace) {
        this.detailsOnFace = detailsOnFace;
    }

    @Override
    public boolean areDetailsOnFace() {
        return this.detailsOnFace;
    }

    @Override
    public void setDetailsOnGestures(boolean detailsOnGestures) {
        this.detailsOnGestures = detailsOnGestures;
    }

    @Override
    public boolean areDetailsOnGestures() {
        return this.detailsOnGestures;
    }
 }
