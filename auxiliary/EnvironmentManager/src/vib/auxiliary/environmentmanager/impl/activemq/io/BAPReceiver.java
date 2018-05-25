/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.environmentmanager.impl.activemq.io;

import vib.auxiliary.activemq.ConnectionListener;
import vib.auxiliary.environmentmanager.core.IEnvironmentServer;
import vib.auxiliary.environmentmanager.core.io.bap.AbstractBAPReceiver;
import vib.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import vib.auxiliary.environmentmanager.util.Toolbox;
import vib.core.animation.mpeg4.MPEG4Animatable;
import vib.core.animation.mpeg4.bap.BAPFramesPerformer;

/**
 *
 * @author Brice Donval
 */
public class BAPReceiver extends AbstractBAPReceiver implements ConnectionListener {

    private vib.auxiliary.activemq.semaine.BAPReceiver bapReceiver;

    public BAPReceiver(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        super(environmentServer, mpegAnimatable);
        bapReceiver = new vib.auxiliary.activemq.semaine.BAPReceiver(environmentServer.getHost(), environmentServer.getStartingPort(), getTopic());
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
    /*                            BAPFramesEmitter                            */
    /* ---------------------------------------------------------------------- */

    @Override
    public void addBAPFramesPerformer(BAPFramesPerformer bapfp) {
        bapReceiver.addBAPFramesPerformer(bapfp);
    }

    @Override
    public void removeBAPFramesPerformer(BAPFramesPerformer bapfp) {
        bapReceiver.removeBAPFramesPerformer(bapfp);
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
