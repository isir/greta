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
import greta.auxiliary.environmentmanager.core.io.message.AbstractMessageSender;
import greta.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import greta.auxiliary.environmentmanager.util.Toolbox;
import java.util.Map;

/**
 *
 * @author Brice Donval
 */
public class MessageSender extends AbstractMessageSender implements ConnectionListener {

    private greta.auxiliary.activemq.TextSender textSender;

    /* ---------------------------------------------------------------------- */

    public MessageSender(IEnvironmentServer environmentServer) {
        super(environmentServer);
        textSender = new greta.auxiliary.activemq.TextSender(environmentServer.getHost(), environmentServer.getStartingPort(), getTopic());
        textSender.addConnectionListener(this);
        Toolbox.log(environmentServer, "++ MessageSender is initialized with the values below :\n  - Host = \"" + textSender.getHost() + "\"\n  - Port = \"" + textSender.getPort() + "\"\n  - Topic = \"" + textSender.getTopic());
    }

    private String getTopic() {
        return getEnvironmentServer().isPrimary() ? ActiveMQConstants.Topic_Message_from_Primary : ActiveMQConstants.Topic_Message_from_Replica;
    }

    /* ---------------------------------------------------------------------- */
    /*                             IMessageSender                             */
    /* ---------------------------------------------------------------------- */

    @Override
    public String getHost() {
        return textSender.getHost();
    }

    @Override
    public void setHost(String host) {
        textSender.setHost(host);
        Toolbox.log(getEnvironmentServer(), "** MessageSender Host has changed with the value below :\n  - Host = \"" + textSender.getHost());
    }

    /* ------------------------------ */

    @Override
    public String getPort() {
        return textSender.getPort();
    }

    @Override
    public void setPort(String port) {
        textSender.setPort(port);
        Toolbox.log(getEnvironmentServer(), "** MessageSender Port has changed with the value below :\n  - Port = \"" + textSender.getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public void send(String message, Map<String, String> details) {
        Map<String, Object> properties = (Map) details;
        textSender.send(message, properties);
        Toolbox.log(getEnvironmentServer(), "<- MessageSender is sending the message below :\n  - Message = \"" + message + "\"\n  - Details = " + properties);
    }

    /* -------------------------------------------------- */

    @Override
    public void onDestroy() {
        textSender.stopConnection();
        textSender = null;
        Toolbox.log(getEnvironmentServer(), "-- MessageSender is destroyed.");
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
