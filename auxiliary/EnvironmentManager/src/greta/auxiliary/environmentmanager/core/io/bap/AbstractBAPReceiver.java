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
package greta.auxiliary.environmentmanager.core.io.bap;

import greta.auxiliary.environmentmanager.core.IEnvironmentServer;
import greta.auxiliary.environmentmanager.core.mvc.IEnvironmentManagerCore;
import greta.core.animation.mpeg4.MPEG4Animatable;

/**
 *
 * @author Brice Donval
 */
public abstract class AbstractBAPReceiver implements IBAPReceiver {

    private final IEnvironmentManagerCore environmentServer;

    private final MPEG4Animatable mpegAnimatable;

    /* ---------------------------------------------------------------------- */

    public AbstractBAPReceiver(IEnvironmentServer environmentServer, MPEG4Animatable mpegAnimatable) {
        this.environmentServer = (IEnvironmentManagerCore) environmentServer;
        this.mpegAnimatable = mpegAnimatable;
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final IEnvironmentServer getEnvironmentServer() {
        return environmentServer;
    }

    @Override
    public final MPEG4Animatable getMPEG4Animatable() {
        return mpegAnimatable;
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final void hasConnected() {
        environmentServer.bapReceiverHasConnected(getPort());
    }

    @Override
    public final void hasDisconnected() {
        environmentServer.bapReceiverHasDisconnected(getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public final void destroy() {
        onDestroy();
        environmentServer.bapReceiverHasDisappeared();
    }

}
