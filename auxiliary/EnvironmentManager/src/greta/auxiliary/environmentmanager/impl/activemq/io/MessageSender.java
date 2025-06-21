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
