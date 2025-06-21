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
