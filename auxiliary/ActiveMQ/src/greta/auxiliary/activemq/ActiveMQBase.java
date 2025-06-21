/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
            // System.out.println("greta.auxiliary.activemq.ActiveMQBase.ConnectionStarter()");
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
  
}
