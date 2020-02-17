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
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;

/**
 *
 * @author Ken Prepin
 */
public abstract class ExternalClient extends Connector<ExternalClient>{

    TSocket transport;
    ExternalCom.Client client;
    ExternalClientConnectionCheckThread extClientThread;

   Message message;
   boolean newMessage;

   public ExternalClient(){
        this(DEFAULT_THRIFT_HOST,DEFAULT_THRIFT_PORT);
    }
    public ExternalClient(String host, int port){
        super(host, port);
        message = new Message();
        message.setId(ExternalComHandler.messageInitId);
        newMessage =  false;
    }
    public void updateMessage() {
        if(this.isConnected()){
            try {
                synchronized(client){
                   Message m = new Message(client.update(message.getId()));
                   if(!"empty".equals(m.getType())){
                        message = client.update(message.getId());
                   }
                }

            } catch (Exception ex) {
                System.err.println("Can not send Message. " + ex.getMessage());
                System.err.println("Check that the right Receiver is connected on " + this.getHost() + " " + this.getPort());
                System.err.println("Check that there is no remaining java.exe running");
                if (isConnected()) {
                    startConnection();
                }
            }
        } else {
            Logs.info("Thrift Sender not connected");
        }
    }

    public boolean isNewMessage(){
        return newMessage;
    }

    public Message getMessage(){
        Logs.debug("new message " + message.id +" received on the client");
        return message;
    }

    @Override
    protected void finalize() throws Throwable {
        synchronized (this) {
            if (transport != null) {
                System.out.println("Closing the simple client...");
                transport.close();
            }
        }
        super.finalize();
    }

    @Override
    public void startConnector(ExternalClient extClient) {
        try {
            synchronized (extClient) {
                extClient.transport = new TSocket(extClient.getHost(), extClient.getPort());
                extClient.transport.open();
                extClient.transport.setTimeout(1000);
                TProtocol protocol = new TBinaryProtocol(extClient.transport);
                extClient.client = new ExternalCom.Client(protocol);
                System.out.println("Connected to " + extClient.getHost() + " - " + extClient.getPort());
                System.out.println("sender to String: " + extClient.client.toString());
                if (extClient.client != null) {
                    setConnected(true);
                }
                extClientThread = new ExternalClient.ExternalClientConnectionCheckThread(this);
                extClientThread.start();
            }
        } catch (Throwable ex) {
            System.err.println("Exception " + ex.getMessage());
        }
    }
    @Override
    public void stopConnector(ExternalClient externalClient){
        if(externalClient.transport.isOpen()){
            externalClient.transport.close();
        }
        externalClient.setConnected(false);
    }
    public void send(Message m) {
        if(this.isConnected()){
            try {
                synchronized(client){
                    client.send(m);
                }

            } catch (Exception ex) {
                System.err.println("Can not send Message. " + ex.getMessage());
                System.err.println("Check that the right Receiver is connected on " + this.getHost() + " " + this.getPort());
                System.err.println("Check that there is no remaining java.exe running");
                if (isConnected()) {
                    startConnection();
                }
            }
        } else {
            Logs.info("Thrift Sender not connected");
        }
    }


    @Override
    public void setPort(int port) {
        if (getPort() != port) {
            super.setPort(port);
            startConnection();
        }
    }

    @Override
    public void setPort(String port) {
        setPort(Integer.parseInt(port));
    }

    @Override
    public void setHost(String host) {
        if (!getHost().equals(host)) {
            super.setHost(host);
            startConnection();
        }
    }
    private class ExternalClientConnectionCheckThread extends Thread {
        final ExternalClient externalClient;

        public ExternalClientConnectionCheckThread(ExternalClient externalClientToConnect){
            Logs.debug("ExternalClientConnectionCheckThread created");
            this.setDaemon(true);
            this.externalClient=externalClientToConnect;

            this.setName("ThriftExternalClientConnectionCheckThread "+externalClient.getHost()+ " "+ externalClient.getPortString());
        }

        @Override
        public void run() {
            Logs.debug("ExternalClientConnectionCheck started");
            try {
                while (externalClient.isConnected()) {
                    boolean clientStarted = false;
                    synchronized(externalClient.client){
                        clientStarted = externalClient.client.isStarted();
                    }
                    if (clientStarted) {
                        try {
                            sleep(SLEEP_TIME);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(Sender.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
            } catch (Exception ex) {

                Logs.debug("ExternalClientConnectionCheckThread: sender.client.isStarted : exception = " + ex.getMessage());
                externalClient.startConnection();

            }
        }

    }
    public abstract void perform(Message m);
}
