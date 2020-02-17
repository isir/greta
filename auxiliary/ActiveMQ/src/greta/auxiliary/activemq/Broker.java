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
