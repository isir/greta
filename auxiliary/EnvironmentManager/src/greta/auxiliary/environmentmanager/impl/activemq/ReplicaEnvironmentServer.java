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
package greta.auxiliary.environmentmanager.impl.activemq;

import greta.auxiliary.environmentmanager.core.AbstractReplicaEnvironmentServer;
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
