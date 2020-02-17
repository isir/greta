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

import greta.auxiliary.thrift.gen_java.Message;
import greta.auxiliary.thrift.gen_java.SimpleCom;
import greta.core.util.log.Logs;
import org.apache.thrift.server.TServer;
import org.apache.thrift.server.TThreadPoolServer;
import org.apache.thrift.transport.TServerSocket;
import org.apache.thrift.transport.TServerTransport;

/**
 *
 * @author Ken Prepin
 */
public abstract class Receiver extends Connector<Receiver> {

    private SimpleComHandler handler;
    private SimpleCom.Processor processor;
    TServerTransport serverTransport;
    TServer server;
    ReceiverThread receiverThread;
    Message message;

    public Receiver() {
        this(DEFAULT_THRIFT_PORT);
    }

    public Receiver(int port) {
        super(DEFAULT_THRIFT_HOST, port);
    }

    @Override
    public void startConnector(Receiver receiver) {
        try {
            handler = new SimpleComHandler(this);
            processor = new SimpleCom.Processor(handler);
            receiverThread = new ReceiverThread(this);
            receiverThread.start();
        } catch (Exception x) {
            x.printStackTrace();
        }
    }
    @Override
    public void stopConnector(Receiver receiver){
       if(receiver.server.isServing()){
           receiver.server.stop();
           receiver.serverTransport.close();
       }
        if(receiver.receiverThread.isAlive()){
            receiver.receiverThread.interrupt();
        }
         receiver.setConnected(false);
    }


    private class ReceiverThread extends Thread {

        final Receiver receiver;

        public ReceiverThread(Receiver receiverToBeStarted) {
            this.setDaemon(true);
            this.receiver = receiverToBeStarted;

        }
        @Override
        public void run() {
            Logs.debug("Try to start on " + receiver.getHost() + " - " + receiver.getPort());
            try {
                receiver.serverTransport = new TServerSocket(getPort());

                // Use this for a monothreaded server ...
                //receiver.server = new TSimpleServer(new Args(serverTransport).processor(processor));

                // ... or use this for a multithreaded server
                receiver.server = new TThreadPoolServer(new TThreadPoolServer.Args(serverTransport).processor(processor));

                System.out.println("Starting the server...");
                setConnected(true);
                server.serve();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public abstract void perform(Message m);
}
