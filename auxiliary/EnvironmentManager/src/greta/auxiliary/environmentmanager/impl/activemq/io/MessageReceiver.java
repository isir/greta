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
package greta.auxiliary.environmentmanager.impl.activemq.io;

import greta.auxiliary.activemq.ConnectionListener;
import greta.auxiliary.environmentmanager.core.IEnvironmentServer;
import greta.auxiliary.environmentmanager.core.io.message.AbstractMessageReceiver;
import greta.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import greta.auxiliary.environmentmanager.util.Toolbox;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Brice Donval
 */
public class MessageReceiver extends AbstractMessageReceiver implements ConnectionListener {

    private greta.auxiliary.activemq.TextReceiver textReceiver;

    /* ---------------------------------------------------------------------- */

    public MessageReceiver(IEnvironmentServer environmentServer) {
        super(environmentServer);
        textReceiver = new greta.auxiliary.activemq.TextReceiver(environmentServer.getHost(), environmentServer.getStartingPort(), getTopic()) {
            @Override
            protected void onMessage(String content, Map<String, Object> properties) {
                Map<String, String> details = new HashMap<String, String>();
                for (String key : properties.keySet()) {
                    details.put(key, properties.get(key).toString());
                }
                Toolbox.log(getEnvironmentServer(), "<- MessageReceiver is receiving the message below :\n  - Message = \"" + content + "\"\n  - Details = " + details);
                hasReceived(content, details);
            }
        };
        textReceiver.addConnectionListener(this);
        Toolbox.log(environmentServer, "++ MessageReceiver is initialized with the values below :\n  - Host = \"" + textReceiver.getHost() + "\"\n  - Port = \"" + textReceiver.getPort() + "\"\n  - Topic = \"" + textReceiver.getTopic());
    }

    private String getTopic() {
        return getEnvironmentServer().isPrimary() ? ActiveMQConstants.Topic_Message_from_Replica : ActiveMQConstants.Topic_Message_from_Primary;
    }

    /* ---------------------------------------------------------------------- */
    /*                            IMessageReceiver                            */
    /* ---------------------------------------------------------------------- */

    @Override
    public String getHost() {
        return textReceiver.getHost();
    }

    @Override
    public void setHost(String host) {
        textReceiver.setHost(host);
        Toolbox.log(getEnvironmentServer(), "** MessageReceiver Host has changed with the value below :\n  - Host = \"" + textReceiver.getHost());
    }

    /* ------------------------------ */

    @Override
    public String getPort() {
        return textReceiver.getPort();
    }

    @Override
    public void setPort(String port) {
        textReceiver.setPort(port);
        Toolbox.log(getEnvironmentServer(), "** MessageReceiver Port has changed with the value below :\n  - Port = \"" + textReceiver.getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public void onDestroy() {
        textReceiver.stopConnection();
        textReceiver = null;
        Toolbox.log(getEnvironmentServer(), "-- MessageReceiver is destroyed.");
    }

    /* ---------------------------------------------------------------------- */
    /*                           ConnectionListener                           */
    /* ---------------------------------------------------------------------- */

    @Override
    public void onConnection() {
        hasConnected();
    }

    @Override
    public void onDisconnection() {
        hasDisconnected();
    }

}
