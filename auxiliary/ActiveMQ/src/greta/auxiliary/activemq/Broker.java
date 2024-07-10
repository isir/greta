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
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;
import org.apache.activemq.broker.BrokerFactory;

/**
 *
 * @author Andre-Marie Pez
 */
public class Broker extends ActiveMQBase{
    private static BrokerService broker;
    private String port;
    private TransportConnector currentTransportConnector;

    private BufferedWriter p_stdin;
    
    public Broker() throws Exception{
        this(DEFAULT_ACTIVEMQ_PORT);
    }

    public Broker(String port) throws Exception{
//        String connector_config = "tcp://localhost:"+DEFAULT_ACTIVEMQ_PORT;
//        try{
//            System.out.println("greta.auxiliary.activemq.Broker()");
//            this.port = port;
//            
//            BrokerService tmpbroker = new BrokerService();
//            tmpbroker.setUseShutdownHook(true);
//            tmpbroker.setPersistent(false);
//            tmpbroker.setSchedulerSupport(false);
//            tmpbroker.setUseLocalHostBrokerName(true);
//            tmpbroker.setUseVirtualTopics(true);
//            tmpbroker.start();
//            broker = tmpbroker;
//            System.out.println("ActiveMQ Broker started at " + connector_config);
//            // startConnection();
//        }
//        catch(IOException ex){
//            System.out.println("Failed to launch ActiveMQ Broker at " + connector_config);
//            System.out.println("Broker is already running?");
//        }
        

            // init shell
            ProcessBuilder builder = new ProcessBuilder("C:/Windows/System32/cmd.exe");
            Process p = null;
            try {
                p = builder.start();
            } catch (IOException e) {
                System.out.println(e);
            }
            // get stdin of shell
            p_stdin = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

            // execute commands
            executeCommand("cd Common\\Lib\\External\\apache-activemq-5.15.14\\bin");
            executeCommand("activemq start");
            System.out.println("ActiveMQ Broker started");
    }

    private synchronized static BrokerService getBrokerService() throws Exception{
        if(broker==null){
            BrokerService tmpbroker = new BrokerService();
            tmpbroker.setUseShutdownHook(true);
            tmpbroker.setPersistent(false);
            tmpbroker.setSchedulerSupport(false);
            tmpbroker.setUseLocalHostBrokerName(true);
            tmpbroker.setUseVirtualTopics(true);
            tmpbroker.start(true);
            broker = tmpbroker;
        }
        return broker;
    }    
    
    @Override
    public String getHost(){
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException ex) {
            return getLocalHost();
        }
    }

    public String getLocalHost(){
        return "0.0.0.0";
    }

    @Override
    public String getPort(){
        return port;
    }

    public void setPort(String port){
        String oldport = this.port;
        this.port = port;
        if(port==null){
            stopConnection();
        }
        else{
            if( ! port.equals(oldport)){
                stopConnection();
                startConnection();
            }
        }
    }

    @Override
    protected void setupConnection() throws Throwable {
        TransportConnector tempTransportConnector = getBrokerService().addConnector(getURL(getLocalHost(), getPort()));
        tempTransportConnector.setBrokerService(getBrokerService());
        tempTransportConnector.start();
        currentTransportConnector = tempTransportConnector;
        fireConnection();
    }

    @Override
    public boolean isConnected() {
        return currentTransportConnector!=null;
    }

    @Override
    public synchronized void stopConnection() {
        super.stopConnection();
        try {
            if(currentTransportConnector!=null){
                getBrokerService().removeConnector(currentTransportConnector);
                currentTransportConnector.stop();
                currentTransportConnector = null;
            }
            fireDisconnection();
        } catch (Exception ex) {
            Logs.error(this.getClass().getName()+": can not stop connection. "+ex.getMessage());
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
