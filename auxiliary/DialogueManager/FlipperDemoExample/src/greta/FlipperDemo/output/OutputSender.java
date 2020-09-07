/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.FlipperDemo.output;

import greta.auxiliary.activemq.Sender;
import java.util.Map;
import javax.jms.JMSException;
import javax.jms.Message;
/**
 *
 * @author admin
 */
public class OutputSender extends Sender<String>{
    private boolean active;

    public OutputSender(){
        super();
        this.active = true;
    }
    public OutputSender(String host, String port, String topic){
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
    protected Message createMessage(String content) throws JMSException{
        if (!this.active) {
			return null;
	}
        return session.createTextMessage(content.toString());
    }
    
    public void deactivate() {

	this.active = false;
    }
}
