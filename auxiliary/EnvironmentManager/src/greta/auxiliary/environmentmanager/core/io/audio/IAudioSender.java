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
package greta.auxiliary.environmentmanager.core.io.audio;

import greta.auxiliary.environmentmanager.core.IEnvironmentServer;
import greta.core.animation.mpeg4.MPEG4Animatable;
import greta.core.util.audio.AudioPerformer;

/**
 *
 * @author Brice Donval
 */
public interface IAudioSender extends AudioPerformer {

    public IEnvironmentServer getEnvironmentServer();

    public MPEG4Animatable getMPEG4Animatable();

    /* ---------------------------------------------------------------------- */

    public String getHost();

    public void setHost(String host);

    /* -------------------------------------------------- */

    public String getPort();

    public void setPort(String port);

    /* -------------------------------------------------- */

    public void hasConnected();

    public void hasDisconnected();

    /* -------------------------------------------------- */

    public void destroy();

    public void onDestroy();

}
