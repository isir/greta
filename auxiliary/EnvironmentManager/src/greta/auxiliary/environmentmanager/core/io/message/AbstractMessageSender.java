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
