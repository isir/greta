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

import greta.auxiliary.environmentmanager.core.AbstractPrimaryEnvironmentServer;
import greta.auxiliary.environmentmanager.core.AbstractReplicaEnvironmentServer;
import greta.auxiliary.environmentmanager.core.IEnvironmentServer;
import greta.auxiliary.environmentmanager.core.mvc.IEnvironmentManagerCore;

/**
 *
 * @author Brice Donval
 */
public abstract class AbstractMessageSender implements IMessageSender {

    private final IEnvironmentManagerCore environmentServer;

    /* ---------------------------------------------------------------------- */

    public AbstractMessageSender(IEnvironmentServer environmentServer) {
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
        environmentServer.messageSenderHasConnected(getPort());
        if (!environmentServer.isPrimary()) {
            ((AbstractReplicaEnvironmentServer) environmentServer).sendRequestRegisterReplica();
        }
    }

    @Override
    public final void hasDisconnected() {
        environmentServer.messageSenderHasDisconnected(getPort());
    }

    /* -------------------------------------------------- */

    @Override
    public final void destroy() {
        if (environmentServer.isPrimary()) {
            ((AbstractPrimaryEnvironmentServer) environmentServer).sendRequestUnregisterPrimary();
        } else {
            ((AbstractReplicaEnvironmentServer) environmentServer).sendRequestUnregisterReplica();
        }
        onDestroy();
        environmentServer.messageSenderHasDisappeared();
    }

}
