/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package vib.auxiliary.environmentmanager.core.io.fap;

import vib.auxiliary.environmentmanager.core.IEnvironmentServer;
import vib.core.animation.mpeg4.MPEG4Animatable;
import vib.core.animation.mpeg4.fap.FAPFrameEmitter;

/**
 *
 * @author Brice Donval
 */
public interface IFAPReceiver extends FAPFrameEmitter {

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
