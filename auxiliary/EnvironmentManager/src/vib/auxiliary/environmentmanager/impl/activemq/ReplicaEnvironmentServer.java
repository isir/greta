/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.environmentmanager.impl.activemq;

import vib.auxiliary.environmentmanager.core.AbstractReplicaEnvironmentServer;
import vib.auxiliary.environmentmanager.impl.activemq.io.AudioReceiver;
import vib.auxiliary.environmentmanager.impl.activemq.io.AudioSender;
import vib.auxiliary.environmentmanager.impl.activemq.io.BAPReceiver;
import vib.auxiliary.environmentmanager.impl.activemq.io.BAPSender;
import vib.auxiliary.environmentmanager.impl.activemq.io.FAPReceiver;
import vib.auxiliary.environmentmanager.impl.activemq.io.FAPSender;
import vib.auxiliary.environmentmanager.impl.activemq.io.MessageReceiver;
import vib.auxiliary.environmentmanager.impl.activemq.io.MessageSender;

/**
 *
 * @author Brice Donval
 */
public class ReplicaEnvironmentServer extends AbstractReplicaEnvironmentServer {

    public ReplicaEnvironmentServer() {

        super(
                "ActiveMQ",
                MessageSender.class,
                MessageReceiver.class,
                FAPSender.class,
                FAPReceiver.class,
                BAPSender.class,
                BAPReceiver.class,
                AudioSender.class,
                AudioReceiver.class
        );
    }

    @Override
    public void onDestroy() {

    }

    /* ---------------------------------------------------------------------- */
    /*                           IEnvironmentServer                           */
    /* ---------------------------------------------------------------------- */

    @Override
    public void onHostChange(String host) {

        getMessageSender().setHost(host);
        getMessageReceiver().setHost(host);

        for (String mpeg4AnimatableId : getLocalMPEG4Animatables().keySet()) {
            getFAPSenderOf(mpeg4AnimatableId).setHost(host);
            getBAPSenderOf(mpeg4AnimatableId).setHost(host);
            getAudioSenderOf(mpeg4AnimatableId).setHost(host);
        }

        for (String mpeg4AnimatableId : getDistantMPEG4Animatables().keySet()) {
            getFAPReceiverOf(mpeg4AnimatableId).setHost(host);
            getBAPReceiverOf(mpeg4AnimatableId).setHost(host);
            getAudioReceiverOf(mpeg4AnimatableId).setHost(host);
        }
    }

    @Override
    public void onPortRangeChange(String startingPort, String endingPort) {

        getMessageSender().setPort(startingPort);
        getMessageReceiver().setPort(startingPort);

        for (String mpeg4AnimatableId : getLocalMPEG4Animatables().keySet()) {
            getFAPSenderOf(mpeg4AnimatableId).setPort(startingPort);
            getBAPSenderOf(mpeg4AnimatableId).setPort(startingPort);
            getAudioSenderOf(mpeg4AnimatableId).setPort(startingPort);
        }

        for (String mpeg4AnimatableId : getDistantMPEG4Animatables().keySet()) {
            getFAPReceiverOf(mpeg4AnimatableId).setPort(startingPort);
            getBAPReceiverOf(mpeg4AnimatableId).setPort(startingPort);
            getAudioReceiverOf(mpeg4AnimatableId).setPort(startingPort);
        }
    }

}
