/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.environmentmanager.impl.activemq.io;

import java.util.List;
import vib.auxiliary.activemq.ConnectionListener;
import vib.auxiliary.environmentmanager.core.IEnvironmentServer;
import vib.auxiliary.environmentmanager.core.io.fap.AbstractFAPSender;
import vib.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import vib.auxiliary.environmentmanager.util.Toolbox;
import vib.core.animation.mpeg4.MPEG4Animatable;
import vib.core.animation.mpeg4.fap.FAPFrame;
import vib.core.util.id.ID;

/**
 *
 * @author Brice Donval
 */
public class FAPSender extends AbstractFAPSender implements ConnectionListener {

    private vib.auxiliary.activemq.semaine.FAPSender fapSender;

    public FAPSender(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        super(environmentServer, mpegAnimatable);
        fapSender = new vib.auxiliary.activemq.semaine.FAPSender(environmentServer.getHost(), environmentServer.getStartingPort(), getTopic());
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
