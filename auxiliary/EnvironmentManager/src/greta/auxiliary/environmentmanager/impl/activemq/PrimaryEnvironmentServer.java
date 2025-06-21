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
package greta.auxiliary.environmentmanager.impl.activemq;

import greta.auxiliary.activemq.Broker;
import greta.auxiliary.activemq.ConnectionListener;
import greta.auxiliary.environmentmanager.core.AbstractPrimaryEnvironmentServer;
import greta.auxiliary.environmentmanager.impl.activemq.io.AudioReceiver;
import greta.auxiliary.environmentmanager.impl.activemq.io.AudioSender;
import greta.auxiliary.environmentmanager.impl.activemq.io.BAPReceiver;
import greta.auxiliary.environmentmanager.impl.activemq.io.BAPSender;
import greta.auxiliary.environmentmanager.impl.activemq.io.FAPReceiver;
import greta.auxiliary.environmentmanager.impl.activemq.io.FAPSender;
import greta.auxiliary.environmentmanager.impl.activemq.io.MessageReceiver;
import greta.auxiliary.environmentmanager.impl.activemq.io.MessageSender;

/**
 *
 * @author Brice Donval
 */
public class PrimaryEnvironmentServer extends AbstractPrimaryEnvironmentServer implements ConnectionListener {

    private Broker broker;

    public PrimaryEnvironmentServer() throws Exception {

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

        broker = new Broker(getStartingPort());
        broker.addConnectionListener(this);
    }

    @Override
    public void onDestroy() {
        broker.stopConnection();
        broker = null;
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

        broker.setPort(startingPort);

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

    /* ---------------------------------------------------------------------- */
    /*                           ConnectionListener                           */
    /* ---------------------------------------------------------------------- */

    @Override
    public void onDisconnection() {
        // TODO add your handling code here:
    }

    @Override
    public void onConnection() {
        // TODO add your handling code here:
    }

}
