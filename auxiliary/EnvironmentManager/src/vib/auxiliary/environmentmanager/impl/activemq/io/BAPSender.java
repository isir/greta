/*
 * This file is part of the auxiliaries of Greta.
 * 
 * Greta is free software: you can redistribute it and / or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with Greta.If not, see <http://www.gnu.org/licenses/>.
 */

package vib.auxiliary.environmentmanager.impl.activemq.io;

import java.util.List;
import vib.auxiliary.activemq.ConnectionListener;
import vib.auxiliary.environmentmanager.core.IEnvironmentServer;
import vib.auxiliary.environmentmanager.core.io.bap.AbstractBAPSender;
import vib.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import vib.auxiliary.environmentmanager.util.Toolbox;
import vib.core.animation.mpeg4.MPEG4Animatable;
import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.util.id.ID;

/**
 *
 * @author Brice Donval
 */
public class BAPSender extends AbstractBAPSender implements ConnectionListener {

    private vib.auxiliary.activemq.semaine.BAPSender bapSender;

    public BAPSender(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        super(environmentServer, mpegAnimatable);
        bapSender = new vib.auxiliary.activemq.semaine.BAPSender(environmentServer.getHost(), environmentServer.getStartingPort(), getTopic());
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
    /*                           BAPFramesPerformer                           */
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
