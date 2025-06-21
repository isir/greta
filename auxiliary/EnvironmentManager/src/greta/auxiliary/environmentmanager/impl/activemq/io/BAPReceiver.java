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
import greta.auxiliary.environmentmanager.core.io.bap.AbstractBAPReceiver;
import greta.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import greta.auxiliary.environmentmanager.util.Toolbox;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;

/**
 *
 * @author Brice Donval
 */
public class BAPReceiver extends AbstractBAPReceiver implements ConnectionListener {

    private greta.auxiliary.activemq.semaine.BAPReceiver bapReceiver;

    public BAPReceiver(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        super(environmentServer, mpegAnimatable);
        bapReceiver = new greta.auxiliary.activemq.semaine.BAPReceiver(environmentServer.getHost(), environmentServer.getStartingPort(), getTopic());
        bapReceiver.addConnectionListener(this);
        Toolbox.log(environmentServer, mpegAnimatable, "++ BAPReceiver is initialized with the values below :\n  - Host = \"" + bapReceiver.getHost() + "\"\n  - Port = \"" + bapReceiver.getPort() + "\"\n  - Topic = \"" + bapReceiver.getTopic());
    }

    private String getTopic() {
        return ActiveMQConstants.Topic_BAP_of_ + getMPEG4Animatable().getIdentifier();
    }

    /* ---------------------------------------------------------------------- */
    /*                              IBAPReceiver                              */
    /* ---------------------------------------------------------------------- */

    @Override
    public String getHost() {
        return bapReceiver.getHost();
    }

    @Override
    public void setHost(String host) {
        bapReceiver.setHost(host);
        Toolbox.log(getEnvironmentServer(), "** BAPReceiver Host has changed with the value below :\n  - Host = \"" + bapReceiver.getHost());
    }

    /* ------------------------------ */

    @Override
    public String getPort() {
        return bapReceiver.getPort();
    }

    @Override
    public void setPort(String port) {
        bapReceiver.setPort(port);
        Toolbox.log(getEnvironmentServer(), getMPEG4Animatable(), "** BAPReceiver Port has changed with the value below :\n  - Port = \"" + bapReceiver.getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public void onDestroy() {
        bapReceiver.stopConnection();
        bapReceiver = null;
        Toolbox.log(getEnvironmentServer(), getMPEG4Animatable(), "-- BAPReceiver is destroyed.");
    }

    /* ---------------------------------------------------------------------- */
    /*                            BAPFrameEmitter                            */
    /* ---------------------------------------------------------------------- */

    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapfp) {
        bapReceiver.addBAPFramePerformer(bapfp);
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapfp) {
        bapReceiver.removeBAPFramePerformer(bapfp);
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
