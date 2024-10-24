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
package greta.auxiliary.asr;

public class ActiveMQConnector {
	
	private String host = null;
	private String port = null;
	private String requestTopic = null;
	private String responseTopic = null;
	
	private TranscriptSender sender = null;
	private MessageReceiver receiver = null;
	
	
	public ActiveMQConnector() {
		
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
		
		this.sender = new TranscriptSender(host, port, responseTopic);
		this.receiver = new MessageReceiver( host, port, requestTopic);

	}
	
	public void registerAsSpeechRecognizerListener(SpeechRecognizerListener arg) {
		if (this.receiver != null) {
			this.receiver.getListeners().add(arg);
		}
	}

	public TranscriptSender getSender() {
		return sender;
	}

	public MessageReceiver getReceiver() {
		return receiver;
	}
}
