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
package greta.auxiliary.activemq;

import greta.core.util.log.Logs;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class ActiveMQBase {

    public static final String DEFAULT_ACTIVEMQ_PROTOCOL = "tcp";
    public static final String DEFAULT_ACTIVEMQ_HOST = "localhost";
    public static final String DEFAULT_ACTIVEMQ_PORT = "61616";
    public static final int SLEEP_TIME = 700;

    private BufferedWriter p_stdin;

    private ArrayList<ConnectionListener> connectionListeners;
    private ConnectionStarter starter;

    public ActiveMQBase(){
        connectionListeners = new ArrayList<ConnectionListener>();
    }

    public void addConnectionListener(ConnectionListener connectionListener){
        if(connectionListener != null){
            connectionListeners.add(connectionListener);
        }
    }

    public void removeConnectionListener(ConnectionListener connectionListener){
        if(connectionListener != null){
            connectionListeners.remove(connectionListener);
        }
    }

    protected void fireConnection() {
        for(ConnectionListener connectionListener : connectionListeners){
            connectionListener.onConnection();
        }
    }

    protected void fireDisconnection() {
        for(ConnectionListener connectionListener : connectionListeners){
            connectionListener.onDisconnection();
        }
    }

    public final String getURL() {
        return getURL(getHost(), getPort());
    }


    public final String getURL(String host, String port) {
        return DEFAULT_ACTIVEMQ_PROTOCOL + "://" + host + (port==null || port.isEmpty() ? "" : ":" + port);
    }
    public void startConnection(){

        starter = new ConnectionStarter(this);
        starter.start();

//        System.out.println("greta.auxiliary.activemq.ActiveMQBase.startConnection(): trying to launch ActiveMQ server");
//        
//        // init shell
//        ProcessBuilder builder = new ProcessBuilder("C:/Windows/System32/cmd.exe");
//        Process p = null;
//        try {
//            p = builder.start();
//        } catch (IOException e) {
//            System.out.println(e);
//        }
//        
//        // get stdin of shell
//        p_stdin = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
//        executeCommand("cd Common\\Lib\\External\\apache-activemq-5.15.14\\bin");
//        executeCommand("activemq start");
//        
//        System.out.println("greta.auxiliary.activemq.ActiveMQBase.startConnection(): ActiveMQ srver should be launched now... I hope...");

    }

    public void stopConnection(){
        starter = null;
    }

    public abstract boolean isConnected();
    public abstract String getHost();
    public abstract String getPort();
    protected abstract void setupConnection() throws  Throwable;

    @Override
    protected void finalize() throws Throwable {
        stopConnection();
        super.finalize();
    }

    private static class ConnectionStarter extends Thread {

        final ActiveMQBase amq;

        ConnectionStarter(ActiveMQBase wbtoStart) {
            this.setDaemon(true);//this thread will be killed when no non-daemon thread runs
            this.amq = wbtoStart;
        }
        @Override
        public void run() {
            System.out.println("greta.auxiliary.activemq.ActiveMQBase.ConnectionStarter()");
            Logs.debug(amq.getClass().getSimpleName()+" trys to connect to " + amq.getURL());
            while ( ! amq.isConnected() && amq.starter == this) {
                try {
                    synchronized (amq) {
                        if (amq.starter == this) {
                            amq.setupConnection();
                            Logs.debug(amq.getClass().getSimpleName()+" connected to " + amq.getURL());
                        }
                    }
                } catch (Throwable ex) {
                    // ex.printStackTrace();
                    try {
                        sleep(SLEEP_TIME);
                    } 
                    catch(Exception ex1) {
                        
                    }
                }
            }
        }
    }
    private void executeCommand(String command) {
        try {
            // single execution
            p_stdin.write(command);
            p_stdin.newLine();
            p_stdin.flush();
        } catch (IOException e) {
            System.out.println("greta.auxiliary.activemq.BrokerFrame.executeCommand(): "+e);
        }
    }   
}
