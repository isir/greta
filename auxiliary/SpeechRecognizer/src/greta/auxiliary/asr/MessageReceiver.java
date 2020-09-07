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
