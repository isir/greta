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
package greta.furhat.activemq;

import greta.core.util.log.Logs;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.apache.activemq.broker.BrokerService;
import org.apache.activemq.broker.TransportConnector;

/**
 *
 * @author Andre-Marie Pez
 */
public class Broker extends ActiveMQBase{
    private static BrokerService brokerService;

    private synchronized static BrokerService getBrokerService() throws Exception{
        if(brokerService==null){
            BrokerService tmpbrokerService = new BrokerService();
            tmpbrokerService.setUseShutdownHook(true);
            tmpbrokerService.setPersistent(false);
            tmpbrokerService.setSchedulerSupport(false);
            tmpbrokerService.setUseLocalHostBrokerName(true);
            tmpbrokerService.setUseVirtualTopics(true);
            tmpbrokerService.start();
            brokerService = tmpbrokerService;
        }
        return brokerService;
    }

    private String port;
    private TransportConnector currentTransportConnector;

    public Broker(){
        this(DEFAULT_ACTIVEMQ_PORT);
    }

    public Broker(String port){
        this.port = port;
        startConnection();
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
}
