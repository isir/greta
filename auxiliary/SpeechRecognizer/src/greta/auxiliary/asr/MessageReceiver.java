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
package greta.auxiliary.asr;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.TextMessage;


import greta.auxiliary.activemq.Receiver;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MessageReceiver extends Receiver<String> {
	
	
	private List<SpeechRecognizerListener> listeners;
	private boolean active;
	
    public MessageReceiver(String host, String port, String topic){
        super(host, port, topic);
        this.listeners = new ArrayList<SpeechRecognizerListener>();
        this.active = true;
    }
    
    @Override
    protected void onConnectionStarted() {
    	super.onConnectionStarted();
    	for (SpeechRecognizerListener listener : this.listeners) {
    		listener.onConnectionStarted();
    	}
    }

	@Override
	protected void onMessage(String request, Map<String, Object> properties) {
		if (!this.active) {
			return;
		}

	}

	@Override
	protected String getContent(Message message) {
		if (!this.active) {
			return null;
		}
		
		String msg =null;
            try {
                msg = ((TextMessage) message).getText();
            } catch (JMSException ex) {
                Logger.getLogger(MessageReceiver.class.getName()).log(Level.SEVERE, null, ex);
            }
		return msg;

	}

	public List<SpeechRecognizerListener> getListeners() {
		return this.listeners;
	}

	public void deactivate() {
		//somehow stopping the connection or closing the consumer, session, and consumer fail
		//so, as a hack solution, just "deactivate" this object
		this.active = false;
	}
}
