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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import greta.auxiliary.activemq.TextSender;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.feedbacks.Callback;
import greta.core.feedbacks.FeedbackPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.CharacterManager;
import greta.core.util.id.ID;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import greta.core.util.time.Timer;
import java.util.HashMap;
import java.util.List;

/**
 *
 * @author Ken Prepin
 */
public class FeedbacksSender extends TextSender implements FeedbackPerformer{

    private CharacterManager charactermanager;
    private HashMap<String,Object> semaineMap;
    private boolean detailedFeedbacks;
    private boolean detailsOnFace;
    private boolean detailsOnGestures;

    public FeedbacksSender(CharacterManager cm){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "semaine.callback.output.feedback", cm);
    }
    public FeedbacksSender(String host, String port, String topic, CharacterManager cm){
        super(host, port, topic);
        semaineMap = new HashMap<String,Object>();
        semaineMap.put("content-type", "utterance");
        semaineMap.put("datatype", "feedback");
        semaineMap.put("source", "Greta");
        semaineMap.put("event", "single");
        semaineMap.put("xml", true);
        detailedFeedbacks = false; // TODO implement the possibility to use detailedFeedbacks
        this.charactermanager = cm;
    }

    @Override
    public void performFeedback(ID AnimId, String Type, SpeechSignal speechsignal, TimeMarker tm) {
    	@SuppressWarnings(value = "unused")
    	class FeedbackContent {
    		private String fml_id;
    		private String TimeMarker_id;
    		private String type;
    		private double time;
    		private String agent;

    		public FeedbackContent(String fml_id, String TimeMarker_id, String type, double time, String agent) {
    			this.fml_id = fml_id;
    			this.TimeMarker_id = TimeMarker_id;
    			this.type = type;
    			this.time = time;
    			this.agent = agent;
    		}

			public String getFml_id() {
				return fml_id;
			}

			public void setFml_id(String fml_id) {
				this.fml_id = fml_id;
			}

			public String getTimeMarker_id() {
				return TimeMarker_id;
			}

			public void setTimeMarker_id(String timeMarker_id) {
				TimeMarker_id = timeMarker_id;
			}

			public String getType() {
				return type;
			}

			public void setType(String type) {
				this.type = type;
			}

			public double getTime() {
				return time;
			}

			public void setTime(double time) {
				this.time = time;
			}

			public String getAgent() {
				return agent;
			}

			public void setAgent(String agent) {
				this.agent = agent;
			}
    	}

    	String content;
    	try {
    		content = (new ObjectMapper()).writeValueAsString(new FeedbackContent(AnimId.getFmlID(), tm.getName(), Type, tm.getValue(), this.charactermanager.getCurrentCharacterName()));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

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
    	@SuppressWarnings(value = "unused")
    	class FeedbackContent {
    		private String fml_id;
    		private String type;
    		private double time;
    		private String agent;

    		public FeedbackContent(String fml_id, String type, double time, String agent) {
    			this.fml_id = fml_id;
    			this.type = type;
    			this.time = time;
    			this.agent = agent;
    		}

			public String getFml_id() {
				return fml_id;
			}

			public void setFml_id(String fml_id) {
				this.fml_id = fml_id;
			}

			public String getType() {
				return type;
			}

			public void setType(String type) {
				this.type = type;
			}

			public double getTime() {
				return time;
			}

			public void setTime(double time) {
				this.time = time;
			}

			public String getAgent() {
				return agent;
			}

			public void setAgent(String agent) {
				this.agent = agent;
			}
    	}

    	String content;
    	try {
    		content = (new ObjectMapper()).writeValueAsString(new FeedbackContent(callback.animId().getFmlID(), callback.type(), callback.time(), this.charactermanager.getCurrentCharacterName()));
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}

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
