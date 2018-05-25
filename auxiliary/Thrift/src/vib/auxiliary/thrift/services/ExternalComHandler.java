/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.thrift.services;

import vib.auxiliary.thrift.gen_java.ExternalCom;
import vib.auxiliary.thrift.gen_java.Message;

/**
 *
 * @author Ken Prepin
 */
public class ExternalComHandler implements ExternalCom.Iface{

    public static final String messageInitId = "initMessage";
    private ServerToExternal serverToExt;
    private Message message;


    public ExternalComHandler(ServerToExternal serverToExt) {
        this.message = new Message();
        this.serverToExt = serverToExt;
    }

    @Override
    public void send(Message m) {
        message=m;

        if(message.type!=null) {
            System.out.println("message received by ServerToExt:"+message.type);
        }

        //serverToExt.perform(message);
    }

    @Override
    public boolean isStarted() {
       return serverToExt.isConnected();
    }

    @Override
    public Message update(String oldMessageId) {
        message = serverToExt.getMessage(oldMessageId);
        return message;
    }

    @Override
    public boolean isNewMessage(String oldMessageId) {
        return serverToExt.isNewMessage(oldMessageId);
    }

}
