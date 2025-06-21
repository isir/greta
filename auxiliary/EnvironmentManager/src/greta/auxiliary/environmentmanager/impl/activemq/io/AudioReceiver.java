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
import greta.auxiliary.environmentmanager.core.io.audio.AbstractAudioReceiver;
import greta.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.util.audio.AudioPerformer;

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
