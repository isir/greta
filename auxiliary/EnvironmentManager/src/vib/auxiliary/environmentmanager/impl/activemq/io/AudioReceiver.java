/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.environmentmanager.impl.activemq.io;

import vib.auxiliary.activemq.ConnectionListener;
import vib.auxiliary.environmentmanager.core.IEnvironmentServer;
import vib.auxiliary.environmentmanager.core.io.audio.AbstractAudioReceiver;
import vib.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import vib.core.animation.mpeg4.MPEG4Animatable;
import vib.core.util.audio.AudioPerformer;

/**
 *
 * @author Brice Donval
 */
public class AudioReceiver extends AbstractAudioReceiver implements ConnectionListener {

    public AudioReceiver(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        super(environmentServer, mpegAnimatable);
    }

    private String getTopic() {
        return ActiveMQConstants.Topic_Audio_of_ + getMPEG4Animatable().getIdentifier();
    }

    /* ---------------------------------------------------------------------- */
    /*                             IAudioReceiver                             */
    /* ---------------------------------------------------------------------- */

    @Override
    public String getHost() {
        // TODO add your code here:
        return "--";
    }

    @Override
    public void setHost(String host) {
        // TODO add your code here:
    }

    /* ------------------------------ */

    @Override
    public String getPort() {
        // TODO add your code here:
        return "--";
    }

    @Override
    public void setPort(String port) {
        // TODO add your code here:
    }

    /* -------------------------------------------------- */

    @Override
    public void onDestroy() {
        // TODO add your code here:
    }

    /* ---------------------------------------------------------------------- */
    /*                              AudioEmitter                              */
    /* ---------------------------------------------------------------------- */

    @Override
    public void addAudioPerformer(AudioPerformer ap) {
        // TODO add your code here:
    }

    @Override
    public void removeAudioPerformer(AudioPerformer ap) {
        // TODO add your code here:
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
