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
import greta.auxiliary.environmentmanager.core.io.fap.AbstractFAPSender;
import greta.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import greta.auxiliary.environmentmanager.util.Toolbox;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.util.id.ID;
import java.util.List;

/**
 *
 * @author Brice Donval
 */
public class FAPSender extends AbstractFAPSender implements ConnectionListener {

    private greta.auxiliary.activemq.semaine.FAPSender fapSender;

    public FAPSender(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        super(environmentServer, mpegAnimatable);
        fapSender = new greta.auxiliary.activemq.semaine.FAPSender(environmentServer.getHost(), environmentServer.getStartingPort(), getTopic());
        fapSender.addConnectionListener(this);
        Toolbox.log(environmentServer, mpegAnimatable, "++ FAPSender is initialized with the values below :\n  - Host = \"" + fapSender.getHost() + "\"\n  - Port = \"" + fapSender.getPort() + "\"\n  - Topic = \"" + fapSender.getTopic());
    }

    private String getTopic() {
        return ActiveMQConstants.Topic_FAP_of_ + getMPEG4Animatable().getIdentifier();
    }

    /* ---------------------------------------------------------------------- */
    /*                               IFAPSender                               */
    /* ---------------------------------------------------------------------- */

    @Override
    public String getHost() {
        return fapSender.getHost();
    }

    @Override
    public void setHost(String host) {
        fapSender.setHost(host);
        Toolbox.log(getEnvironmentServer(), "** FAPSender Host has changed with the value below :\n  - Host = \"" + fapSender.getHost());
    }

    /* ------------------------------ */

    @Override
    public String getPort() {
        return fapSender.getPort();
    }

    @Override
    public void setPort(String port) {
        fapSender.setPort(port);
        Toolbox.log(getEnvironmentServer(), getMPEG4Animatable(), "** FAPSender Port has changed with the value below :\n  - Port = \"" + fapSender.getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public void onDestroy() {
        fapSender.stopConnection();
        fapSender = null;
        Toolbox.log(getEnvironmentServer(), getMPEG4Animatable(), "-- FAPSender is destroyed.");
    }

    /* ---------------------------------------------------------------------- */
    /*                           FAPFramePerformer                            */
    /* ---------------------------------------------------------------------- */

    @Override
    public void performFAPFrames(List<FAPFrame> list, ID id) {
        fapSender.performFAPFrames(list, id);
    }

    @Override
    public void performFAPFrame(FAPFrame fapf, ID id) {
        fapSender.performFAPFrame(fapf, id);
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
