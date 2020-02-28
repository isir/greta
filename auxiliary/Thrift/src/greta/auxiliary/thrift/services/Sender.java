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

// Generated code
import greta.auxiliary.thrift.gen_java.Message;
import greta.auxiliary.thrift.gen_java.SimpleCom;
import greta.core.util.log.Logs;
import java.net.ConnectException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;

/**
 *
 * @author Ken Prepin
 */
public abstract class Sender extends Connector<Sender> {

    TSocket transport;
    SimpleCom.Client client;
    SenderThread senderThread;

    public Sender(){
        this(DEFAULT_THRIFT_HOST,DEFAULT_THRIFT_PORT);
    }
    public Sender(String host, int port){
        super(host, port);
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
    public void startConnector(Sender sender) {
        try {
            synchronized (sender) {
                sender.transport = new TSocket(sender.getHost(), sender.getPort());
                sender.transport.open();
                sender.transport.setTimeout(1000);
                TProtocol protocol = new TBinaryProtocol(sender.transport);
                sender.client = new SimpleCom.Client(protocol);
                System.out.println("Connected to " + sender.getHost() + " - " + sender.getPort());
                System.out.println("sender to String: " + sender.client.toString());
                if (sender.client != null) {
                    setConnected(true);
                }
                senderThread = new SenderThread(this);
                senderThread.start();
            }
        } catch (Throwable ex) {
            if (ex.getCause() instanceof ConnectException) {
                System.err.println("ConnectException : could not connect, have you tried launching Unity ?");
            } else {
                ex.printStackTrace();
            }
        }
    }
    @Override
    public void stopConnector(Sender sender){
        if(sender.transport.isOpen()){
            sender.transport.close();
        }
        sender.setConnected(false);
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
    private class SenderThread extends Thread {
        final Sender sender;

        public SenderThread(Sender senderToConnect){
            Logs.debug("senderThread created");
            this.setDaemon(true);
            this.sender=senderToConnect;

            this.setName("ThriftSender "+sender.getHost()+ " "+ sender.getPortString());
        }

        @Override
        public void run() {
            Logs.debug("senderThread started");
            try {
                while (sender.isConnected()) {
                    boolean clientStarted = false;
                    synchronized(sender.client){
                        clientStarted = sender.client.isStarted();
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

                Logs.debug("senderThread: sender.client.isStarted : exception = " + ex.getMessage());
                sender.startConnection();

            }
        }

    }

}
