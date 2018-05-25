/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.environmentmanager.core.io.message;

import vib.auxiliary.environmentmanager.core.AbstractPrimaryEnvironmentServer;
import vib.auxiliary.environmentmanager.core.AbstractReplicaEnvironmentServer;
import vib.auxiliary.environmentmanager.core.IEnvironmentServer;
import vib.auxiliary.environmentmanager.core.mvc.IEnvironmentManagerCore;

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
