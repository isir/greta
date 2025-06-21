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

public class GroupBehaviorMain {
	
	private String host = null;
	private String port = null;
	private String requestTopic = null;
	private String responseTopic = null;
	
	private GroupBehaviorSender sender = null;
	private GroupBehaviorReceiver receiver = null;
	
	private final MovementProcessor movementProcessor;
	
	public GroupBehaviorMain() {
		this.movementProcessor = new MovementProcessor();
	}
	
	public String getHost() {
		return this.host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return this.port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getRequestTopic() {
		return this.requestTopic;
	}
	
	public String getResponseTopic() {
		return this.responseTopic;
	}

	public void setRequestTopic(String requestTopic) {
		this.requestTopic = requestTopic;
	}
	
	public void setResponseTopic(String responseTopic) {
		this.responseTopic = responseTopic;
	}
	
	public void initializeSenderAndReceiver() {
		if (this.sender != null) {
			this.sender.deactivate();
		}
		if (this.receiver != null) {
			this.receiver.deactivate();
		}
		
		this.sender = new GroupBehaviorSender(host, port, responseTopic);
		this.receiver = new GroupBehaviorReceiver(this.movementProcessor, host, port, requestTopic);
		this.movementProcessor.setGroupBehaviourSender(this.sender);
	}
	
	public void registerAsGroupBehaviorReceiverListener(GroupBehaviorReceiverListener arg) {
		if (this.receiver != null) {
			this.receiver.getListeners().add(arg);
		}
	}

	public GroupBehaviorSender getSender() {
		return sender;
	}

	public GroupBehaviorReceiver getReceiver() {
		return receiver;
	}
}
