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
