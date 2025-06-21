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
import greta.auxiliary.environmentmanager.core.io.bap.AbstractBAPSender;
import greta.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import greta.auxiliary.environmentmanager.util.Toolbox;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.util.id.ID;
import java.util.List;

/**
 *
 * @author Brice Donval
 */
public class BAPSender extends AbstractBAPSender implements ConnectionListener {

    private greta.auxiliary.activemq.semaine.BAPSender bapSender;

    public BAPSender(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        super(environmentServer, mpegAnimatable);
        bapSender = new greta.auxiliary.activemq.semaine.BAPSender(environmentServer.getHost(), environmentServer.getStartingPort(), getTopic());
        bapSender.addConnectionListener(this);
        Toolbox.log(environmentServer, mpegAnimatable, "++ BAPSender is initialized with the values below :\n  - Host = \"" + bapSender.getHost() + "\"\n  - Port = \"" + bapSender.getPort() + "\"\n  - Topic = \"" + bapSender.getTopic());
    }

    private String getTopic() {
        return ActiveMQConstants.Topic_BAP_of_ + getMPEG4Animatable().getIdentifier();
    }

    /* ---------------------------------------------------------------------- */
    /*                               IBAPSender                               */
    /* ---------------------------------------------------------------------- */

    @Override
    public String getHost() {
        return bapSender.getHost();
    }

    @Override
    public void setHost(String host) {
        bapSender.setHost(host);
        Toolbox.log(getEnvironmentServer(), getMPEG4Animatable(), "** BAPSender Host has changed with the value below :\n  - Host = \"" + bapSender.getHost());
    }

    /* ------------------------------ */

    @Override
    public String getPort() {
        return bapSender.getPort();
    }

    @Override
    public void setPort(String port) {
        bapSender.setPort(port);
        Toolbox.log(getEnvironmentServer(), getMPEG4Animatable(), "** BAPSender Port has changed with the value below :\n  - Port = \"" + bapSender.getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public void onDestroy() {
        bapSender.stopConnection();
        bapSender = null;
        Toolbox.log(getEnvironmentServer(), getMPEG4Animatable(), "-- BAPSender is destroyed.");
    }

    /* ---------------------------------------------------------------------- */
    /*                           BAPFramePerformer                           */
    /* ---------------------------------------------------------------------- */

    @Override
    public void performBAPFrames(List<BAPFrame> list, ID id) {
        bapSender.performBAPFrames(list, id);
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
