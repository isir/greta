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
import greta.auxiliary.environmentmanager.core.io.fap.AbstractFAPReceiver;
import greta.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import greta.auxiliary.environmentmanager.util.Toolbox;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.animation.mpeg4.fap.FAPFramePerformer;

/**
 *
 * @author Brice Donval
 */
public class FAPReceiver extends AbstractFAPReceiver implements ConnectionListener {

    private greta.auxiliary.activemq.semaine.FAPReceiver fapReceiver;

    public FAPReceiver(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        super(environmentServer, mpegAnimatable);
        fapReceiver = new greta.auxiliary.activemq.semaine.FAPReceiver(environmentServer.getHost(), environmentServer.getStartingPort(), getTopic());
        fapReceiver.addConnectionListener(this);
        Toolbox.log(environmentServer, mpegAnimatable, "++ FAPReceiver is initialized with the values below :\n  - Host = \"" + fapReceiver.getHost() + "\"\n  - Port = \"" + fapReceiver.getPort() + "\"\n  - Topic = \"" + fapReceiver.getTopic());
    }

    private String getTopic() {
        return ActiveMQConstants.Topic_FAP_of_ + getMPEG4Animatable().getIdentifier();
    }

    /* ---------------------------------------------------------------------- */
    /*                              IFAPReceiver                              */
    /* ---------------------------------------------------------------------- */

    @Override
    public String getHost() {
        return fapReceiver.getHost();
    }

    @Override
    public void setHost(String host) {
        fapReceiver.setHost(host);
        Toolbox.log(getEnvironmentServer(), "** FAPReceiver Host has changed with the value below :\n  - Host = \"" + fapReceiver.getHost());
    }

    /* ------------------------------ */

    @Override
    public String getPort() {
        return fapReceiver.getPort();
    }

    @Override
    public void setPort(String port) {
        fapReceiver.setPort(port);
        Toolbox.log(getEnvironmentServer(), getMPEG4Animatable(), "** FAPReceiver Port has changed with the value below :\n  - Port = \"" + fapReceiver.getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public void onDestroy() {
        fapReceiver.stopConnection();
        fapReceiver = null;
        Toolbox.log(getEnvironmentServer(), getMPEG4Animatable(), "-- FAPReceiver is destroyed.");
    }

    /* ---------------------------------------------------------------------- */
    /*                            FAPFrameEmitter                             */
    /* ---------------------------------------------------------------------- */

    @Override
    public void addFAPFramePerformer(FAPFramePerformer fapfp) {
        fapReceiver.addFAPFramePerformer(fapfp);
    }

    @Override
    public void removeFAPFramePerformer(FAPFramePerformer fapfp) {
        fapReceiver.removeFAPFramePerformer(fapfp);
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
