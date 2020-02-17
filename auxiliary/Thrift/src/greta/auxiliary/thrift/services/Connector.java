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

import greta.core.util.log.Logs;

/*
 * Sender imports
 *
 * import greta.core.util.log.Logs; import org.apache.thrift.TException; import
 * org.apache.thrift.protocol.TBinaryProtocol; import
 * org.apache.thrift.protocol.TProtocol; import
 * org.apache.thrift.transport.TSocket; import
 * org.apache.thrift.transport.TTransport;
 *
 */

/*
 * Receiver imports * import org.apache.thrift.server.TServer; import
 * org.apache.thrift.server.TServer.Args; import
 * org.apache.thrift.server.TSimpleServer; import
 * org.apache.thrift.transport.TServerSocket; import
 * org.apache.thrift.transport.TServerTransport;
 *
 */
/**
 *
 * @author Ken Prepin
 */
public abstract class Connector<C extends Connector> {

    public static final String DEFAULT_THRIFT_HOST = "localhost";
    public static final int DEFAULT_THRIFT_PORT = 9090;
    public static final int SLEEP_TIME = 700;
    private String host;
    private int port;
    private ConnectionListener connectionListener;
    ConnectionStarter starter;

    /*
     * = isStarted for Servers
     */
    private Boolean isConnected;

    public Connector() {
        this(DEFAULT_THRIFT_HOST, DEFAULT_THRIFT_PORT);
    }

    public Connector(String host, int port) {
        this.host = host;
        this.port = port;
        this.isConnected = false;
        this.startConnection();
    }

    @Override
    protected void finalize() throws Throwable {
        starter = null;
        super.finalize();
    }

    public String getHost() {
        return host;
    }

    public String getPortString() {
        return String.valueOf(port);
    }

    public int getPort() {
        return port;
    }

    public void setHost(String host) {
        this.host = host;
        startConnection();
    }

    public void setPort(int port) {
        this.port = port;
        startConnection();
    }

    public void setPort(String port) {
        this.port = Integer.parseInt(port);
        startConnection();
    }

    public void setURL(String host, String port) {
        setHost(host);
        setPort(port);
        startConnection();
    }


    public void setConnected(boolean isConnected) {
        this.isConnected = isConnected;
        if (connectionListener != null) {
            if (isConnected) {
                connectionListener.onConnection();
            } else {
                connectionListener.onDisconnection();
            }
        }
    }

    public void startConnection() {
        if (this.isConnected()) {
            this.starter.stopConnector(this);
        }
        setConnected(false);
        starter = new ConnectionStarter(this);
        starter.start();

    }

    private static class ConnectionStarter extends Thread {

        final Connector connector;

        public ConnectionStarter(Connector connectorToBeStarted) {
            this.setDaemon(true);
            this.connector = connectorToBeStarted;
        }

        @Override
        public void run() {
            Logs.debug("Try to start on " + connector.getHost() + " - " + connector.getPort());
            while (!connector.isConnected() && connector.starter == this) {
                connector.startConnector(connector);
                if (!connector.isConnected()) {
                    try {
                        sleep(SLEEP_TIME);
                    } catch (Exception ex1) {
                    }
                }
            }
        }

         public void stopConnector(Connector connector){
             this.connector.stopConnector(connector);
         };
    }

    public abstract void startConnector(C connector);

    public abstract void stopConnector(C connector);

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnectionListener(ConnectionListener connectionListener) {
        this.connectionListener = connectionListener;
    }
}
