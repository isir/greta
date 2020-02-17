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
import greta.auxiliary.environmentmanager.core.io.audio.AbstractAudioSender;
import greta.auxiliary.environmentmanager.impl.activemq.util.ActiveMQConstants;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.util.Mode;
import greta.core.util.audio.Audio;
import greta.core.util.id.ID;
import java.util.List;

/**
 *
 * @author Brice Donval
 */
public class AudioSender extends AbstractAudioSender implements ConnectionListener {

    public AudioSender(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        super(environmentServer, mpegAnimatable);
    }

    private String getTopic() {
        return ActiveMQConstants.Topic_Audio_of_ + getMPEG4Animatable().getIdentifier();
    }

    /* ---------------------------------------------------------------------- */
    /*                              IAudioSender                              */
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
    /*                             AudioPerformer                             */
    /* ---------------------------------------------------------------------- */

    @Override
    public void performAudios(List<Audio> list, ID id, Mode mode) {
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
