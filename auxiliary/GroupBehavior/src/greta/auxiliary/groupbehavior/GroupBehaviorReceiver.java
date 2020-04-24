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
package greta.auxiliary.groupbehavior;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import greta.auxiliary.activemq.Receiver;

public class GroupBehaviorReceiver extends Receiver<GbmRequest> {
	
	private final MovementProcessor movementProcessor;
	private List<GroupBehaviorReceiverListener> listeners;
	private boolean active;
	
    public GroupBehaviorReceiver(MovementProcessor movementProcessor, String host, String port, String topic){
        super(host, port, topic);
        this.movementProcessor = movementProcessor;
        this.listeners = new ArrayList<GroupBehaviorReceiverListener>();
        this.active = true;
    }
    
    @Override
    protected void onConnectionStarted() {
    	super.onConnectionStarted();
    	for (GroupBehaviorReceiverListener listener : this.listeners) {
    		listener.onConnectionStarted();
    	}
    }

	@Override
	protected void onMessage(GbmRequest gbmRequest, Map<String, Object> properties) {
		if (!this.active) {
			return;
		}
		
		if (gbmRequest instanceof MovesAvailable) {
			this.movementProcessor.setMovesAvailable((MovesAvailable) gbmRequest);
		} else if (gbmRequest instanceof MoveSelected) {
			this.movementProcessor.addMoveSelected((MoveSelected) gbmRequest);
		} else {
			throw new RuntimeException(gbmRequest.getClass().getCanonicalName()+" has no handler");
		}
	}

	@Override
	protected GbmRequest getContent(Message message) throws JMSException {
		if (!this.active) {
			return null;
		}
		
		String jsonStr = ((TextMessage) message).getText();
		
		ObjectMapper mapper = new ObjectMapper();
		JsonNode messageJson = null;
		try {
			messageJson = mapper.readTree(jsonStr);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
		String cmd = messageJson.get("cmd").textValue();
		if ("moves_available".equals(cmd)) {
			MovesAvailable movesAvailable = new MovesAvailable();
			JsonNode actorsNode = messageJson.get("params").get("actors");
			
			Iterator<String> actorIterator = actorsNode.fieldNames();
			while(actorIterator.hasNext()) {
				String actor = actorIterator.next();
				movesAvailable.getActors().add(actor);
				movesAvailable.getAgentBmlIdMap().put(actor, actorsNode.get(actor).get("agentBMLID").textValue());
				movesAvailable.getAgentTypeMap().put(actor, actorsNode.get(actor).get("agentType").textValue());
				movesAvailable.getMoves().put(actor, new ArrayList<MovesAvailable.Move>());
				for (JsonNode moveNode : actorsNode.get(actor).get("moves")) {
					String target = moveNode.get("target").textValue();
					String moveId = moveNode.get("moveID").textValue();
					String moveUid = moveNode.get("moveUID").textValue();
					String opener = moveNode.get("opener").textValue();
					
					movesAvailable.getMoves().get(actor).add(new MovesAvailable.Move(target, moveId, moveUid, opener));
				}
			}
			
			return movesAvailable;
			
		} else if ("move_selected".equals(cmd)) {
			JsonNode paramsNode = messageJson.get("params");
			String actorName = paramsNode.get("actorName").textValue();
			String targetId = paramsNode.get("targetID").textValue();
			String moveUid = paramsNode.get("moveUID").textValue();
			String moveId = paramsNode.get("moveID").textValue();
			return new MoveSelected(actorName, targetId, moveUid, moveId);
		} else {
			throw new RuntimeException("Unknown cmd: "+cmd);
		}
	}

	public List<GroupBehaviorReceiverListener> getListeners() {
		return this.listeners;
	}

	public void deactivate() {
		//somehow stopping the connection or closing the consumer, session, and consumer fail
		//so, as a hack solution, just "deactivate" this object
		this.active = false;
	}
}
