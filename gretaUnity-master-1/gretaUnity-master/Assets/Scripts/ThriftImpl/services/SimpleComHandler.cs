using System;
using UnityEngine;
/**
 *
 * @author Ken
 */


using thrift.gen_csharp;
using Thrift;
using Thrift.Protocol;
using Thrift.Transport;
using Thrift.Collections;


namespace thrift.services
{
	public class SimpleComHandler : SimpleCom.Iface
	{
		   private Message message;
    private Receiver receiver;
    
    public SimpleComHandler(Receiver receiver) {
        message = new Message();
        this.receiver = receiver;
    }
 
    
    public void send(Message m) {
        message=m;
        
     //   if(message.Type!=null)
     //       Debug.Log("message received by server:"+message.Type);
        
        receiver.perform(message);
    }

    
    public bool isStarted() {
       return receiver.isConnected();
    }
     
	}
}

