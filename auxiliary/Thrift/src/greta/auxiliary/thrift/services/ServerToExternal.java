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
package greta.auxiliary.thrift.services;

import greta.auxiliary.thrift.gen_java.ExternalCom;
import greta.auxiliary.thrift.gen_java.Message;
import greta.core.util.log.Logs;
import greta.core.util.time.Timer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

/**
 *
 * @author Ken Prepin
 */
public abstract class ServerToExternal extends Connector<ServerToExternal>{

    private ExternalComHandler handler;
    private ExternalCom.Processor processor;
    TServerTransport serverTransport;
    TThreadPoolServer server;
    ServerToExternalThread serverToExternalThread;
    protected Message message;
    protected Message messageTempon;
    public static final int DELAY_IN_NUM_OF_FRAMES = 10;

    public final Object messageLock = new Object();


    public ServerToExternal() {
        this(DEFAULT_THRIFT_PORT);
    }

    public ServerToExternal(int port) {
        super(DEFAULT_THRIFT_HOST, port);
        message = new Message();
        message.setId(ExternalComHandler.messageInitId);
        messageTempon = new Message();

    }

    @Override
    public void startConnector(ServerToExternal serverToExternal) {
        try {
            handler = new ExternalComHandler(this);
            processor = new ExternalCom.Processor(handler);
            serverToExternalThread = new ServerToExternalThread(this);
            serverToExternalThread.start();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
    @Override
    public void stopConnector(ServerToExternal serverToExternal){
       synchronized(serverToExternal){
       if(serverToExternal.server.isServing()){
           serverToExternal.server.stop();
           serverToExternal.serverTransport.close();
       }
        if(serverToExternal.serverToExternalThread.isAlive()){
            serverToExternal.serverToExternalThread.interrupt();
        }
         serverToExternal.setConnected(false);
       }
    }
    public boolean isMessageOutdated(){
        return (message.getLastFrameNumber()<(Timer.getCurrentFrameNumber())-DELAY_IN_NUM_OF_FRAMES);
    }
    public boolean isNewMessage(String oldMessageId) {
      //  Logs.debug("is new message asked to server which answer "+(!oldMessageId.equals(message.getId())));
        if(isMessageOutdated()){

            return false;
        } else if (oldMessageId.equals(message.getId())){
            return false;
        } else {
            Logs.warning("New message id " + message.getId() + " type "+ message.getType() + " string_content " + message.getString_content() + " time " + message.getTime() + " apframelist size " + message.getAPFrameListSize());
            return true;
        }

      //  return(!isMessageOutdated()&&(!oldMessageId.equals(message.getId())));
    //    return(!oldMessageId.equals(message.getId()));
    }
    public  Message getMessage(String oldMessageId) {
        synchronized(messageLock){
        if(isNewMessage(oldMessageId)){
            return new Message(message);

        } else {
            Message m = new Message();
            m.setType("empty");
            return m;
        }
        }
    }
       public Message getMessage() {
            return new Message(message);
    }

   public void setMessage(Message message_) {
       synchronized(messageLock){
        this.message = message_;
       }
    }

    private class ServerToExternalThread extends Thread {

        final ServerToExternal serverToExternal;

        public ServerToExternalThread(ServerToExternal serverToExternalToBeStarted) {
            this.setDaemon(true);
            this.serverToExternal = serverToExternalToBeStarted;

        }
        @Override
        public void run() {
            Logs.debug("Try to start on " + serverToExternal.getHost() + " - " + serverToExternal.getPort());
            try {
                serverToExternal.serverTransport = new TServerSocket(getPort());
                serverToExternal.server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

                // Use this for a multithreaded server
                // TServer server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

                System.out.println("Starting the ServerToExternal...");
                setConnected(true);

                serverToExternal.server.serve();

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}
