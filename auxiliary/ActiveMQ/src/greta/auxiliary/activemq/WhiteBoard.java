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
import java.io.IOException;
import javax.jms.Destination;
import javax.jms.Session;
import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.transport.TransportListener;

/**
 *
 * @author Andre-Marie Pez
 */
public class WhiteBoard extends ActiveMQBase{

    public static final String NULL_DESTINATION = "null";
    private String host;
    private String port;
    private String destinationName;
    private boolean isQueue;
    protected ActiveMQConnection connection;
    private ActiveMQConnectionFactory factory;
    protected Session session;
    protected Destination destination;

    WhiteBoard() {
        this(DEFAULT_ACTIVEMQ_HOST, DEFAULT_ACTIVEMQ_PORT, NULL_DESTINATION);
    }

    WhiteBoard(String host, String port, String topic) {
        this(host, port, topic, false);
    }

    WhiteBoard(String host, String port, String topic, boolean isQueue) {
        createFactory(host, port);
        destinationName = topic;
        this.isQueue = isQueue;
        startConnection();
    }

    private void createFactory(String host, String port) {
        this.host = host;
        this.port = port;
        factory = new ActiveMQConnectionFactory(null, null, getURL());
        factory.setCheckForDuplicates(true);
    }

    public synchronized void setURL(String host, String port) {
        createFactory(host, port);
        rebootConnection();
    }

    public void setTopic(String topic) {
        setDestination(topic, isQueue);
    }

    public synchronized void setDestination(String topic, boolean isQueue){
        if (!destinationName.equals(topic) || isQueue!=this.isQueue) {
            try {
                session.unsubscribe(destinationName);
            } catch (Exception ex) {
                Logs.error("at changing destination (unsubscribe) : " + ex.getLocalizedMessage());
            }

            destinationName = topic == null || topic.isEmpty() ? NULL_DESTINATION : topic;
            this.isQueue = isQueue;

            try {
                this.destination = this.isQueue ? session.createQueue(destinationName) : session.createTopic(destinationName);
            } catch (Exception ex) {
                Logs.error("at changing destination (create"+(this.isQueue?"Queue":"Topic")+") : " + ex.getLocalizedMessage());
            }

            onDestinationChanged();
        }
    }

    public void setIsQueue(boolean isQueue){
        setDestination(destinationName, isQueue);
    }

    public void setAsQueue(boolean isQueue){
        setIsQueue(true);
    }

    public void setAsTopic(boolean isQueue){
        setIsQueue(false);
    }

    public boolean isQueue(){
        return isQueue;
    }

    public boolean isTopic(){
        return !isQueue;
    }

    public String getTopic() {
        return destinationName;
    }

    public void setHost(String host) {
        if (!this.host.equals(host)) {
            setURL(host, port);
        }
    }

    @Override
    public String getHost() {
        return host;
    }

    public void setPort(String port) {
        if (!this.port.equals(port)) {
            setURL(host, port);
        }
    }

    @Override
    public String getPort() {
        return port;
    }

    /**
     * callback method.
     */
    protected void onDestinationChanged() {
    }

    @Override
    public synchronized void startConnection() {
        connection = null;
        super.startConnection();
    }

    @Override
    public boolean isConnected(){
        return connection!=null;
    }
    /**
     * callback method.
     */
    protected void onConnectionStarted() {
        fireConnection();
    }

    /**
     * callback method.
     */
    protected void onReboot() {
    }

    private synchronized void rebootConnection() {
        //Logs.debug("reboot connection");
        onReboot();
        stopConnection();
        startConnection();
    }

    @Override
    public synchronized void stopConnection() {
        super.stopConnection();
        try {
            session.close();
        } catch (Exception ex) {
            //Logs.error("at session closing : "+ex.getLocalizedMessage());
        }
        session = null;
        try {
            connection.close();
        } catch (Exception ex) {
            //Logs.error("at connection closing : "+ex.getLocalizedMessage());
        }
        fireDisconnection();
    }

    @Override
    protected void setupConnection() throws  Throwable{
        connection = (ActiveMQConnection) factory.createConnection();
        connection.getTransport().setTransportListener(new WBTransporterListener(this));// passthrow
        connection.setUseAsyncSend(true);
        connection.setCopyMessageOnSend(false);
        connection.setOptimizeAcknowledge(true);
        connection.start();
        session = connection.createSession(false, Session.DUPS_OK_ACKNOWLEDGE);
        destination = this.isQueue ? session.createQueue(destinationName) : session.createTopic(destinationName);
        onConnectionStarted();
    }

    private static class WBTransporterListener implements TransportListener {

        private TransportListener original;
        private WhiteBoard wb;

        public WBTransporterListener(WhiteBoard wb) {
            this.wb = wb;
            this.original = this.wb.connection.getTransport().getTransportListener();
        }

        @Override
        public void onCommand(Object o) {
            if (original != null) {
                original.onCommand(o);
            }
        }

        @Override
        public void onException(IOException e) {
            //Logs.error("myTransportListener onException : "+e.getLocalizedMessage());
            //the connection will be different with an other WBTransporterListener
            original = null;
            wb.rebootConnection();
            wb = null;
        }

        @Override
        public void transportInterupted() {
            if (original != null) {
                original.transportInterupted();
            }
        }

        @Override
        public void transportResumed() {
            if (original != null) {
                original.transportResumed();
            }
        }
    }
}
