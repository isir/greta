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
