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
package greta.auxiliary.environmentmanager.core.io.message;

import greta.auxiliary.environmentmanager.core.IEnvironmentServer;
import greta.auxiliary.environmentmanager.core.mvc.IEnvironmentManagerCore;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Brice Donval
 */
public abstract class AbstractMessageReceiver implements IMessageReceiver {

    private final IEnvironmentManagerCore environmentServer;

    /* ---------------------------------------------------------------------- */

    public AbstractMessageReceiver(IEnvironmentServer environmentServer) {
        this.environmentServer = (IEnvironmentManagerCore) environmentServer;
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final IEnvironmentServer getEnvironmentServer() {
        return environmentServer;
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public final void hasConnected() {
        environmentServer.messageReceiverHasConnected(getPort());
    }

    @Override
    public final void hasDisconnected() {
        environmentServer.messageReceiverHasDisconnected(getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public final void hasReceived(String message, Map<String, String> details) {
        environmentServer.receiveMessage(message, new HashMap<String, String>(details));
    }

    /* -------------------------------------------------- */

    @Override
    public final void destroy() {
        onDestroy();
        environmentServer.messageReceiverHasDisappeared();
    }

}
