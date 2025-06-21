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
package greta.auxiliary.groupbehavior;

import java.util.Map;
import greta.auxiliary.activemq.Sender;
import javax.jms.JMSException;
import javax.jms.Message;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

public class GroupBehaviorSender extends Sender<MoveGranted> {
	
	private boolean active;
	
    public GroupBehaviorSender(String host, String port, String topic){
        super(host, port, topic);
        this.active = true;
    }

	@Override
	protected void onSend(Map<String, Object> properties) {
		if (!this.active) {
			return;
		}
		//else, also do nothing
	}

	@Override
	protected Message createMessage(MoveGranted content) throws JMSException {
		if (!this.active) {
			return null;
		}
		
		ObjectMapper mapper = new ObjectMapper();
		ObjectNode rootNode = mapper.createObjectNode();
		rootNode.put("cmd", "move_granted");
		ObjectNode paramsNode = rootNode.putObject("params");
		paramsNode.put("actorName", content.getActorName());
		paramsNode.put("moveID", content.getMoveId());
		paramsNode.put("moveUID", content.getMoveUid());
		paramsNode.put("BMLTemplate", content.getBmlTemplate());
		
		String json;
		try {
			json = mapper.writeValueAsString(rootNode);
		} catch (JsonProcessingException e) {
			throw new RuntimeException(e);
		}
		return this.session.createTextMessage(json);
	}
	
	public void deactivate() {
		//somehow stopping the connection or closing the producer, session, and consumer fail
		//so, as a hack solution, just "deactivate" this object
		this.active = false;
	}

}
