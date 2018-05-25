/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift.services;

import vib.auxiliary.thrift.gen_java.Message;
import vib.auxiliary.thrift.gen_java.SimpleCom;
import org.apache.thrift.TException;

// Generated code

/**
 *
 * @author Ken Prepin
 */
public class SimpleComHandler implements SimpleCom.Iface{

    private Message message;
    private Receiver receiver;

    public SimpleComHandler(Receiver receiver) {
        message = new Message();
        this.receiver = receiver;
    }

    @Override
    public void send(Message m) throws TException {
        message=m;
        
        if(message.id!=null)
            System.out.println("message received by server:"+message.id+" "+message.type);
         
        receiver.perform(message);
    }

    @Override
    public boolean isStarted() {
       return receiver.isConnected();
    }

}
