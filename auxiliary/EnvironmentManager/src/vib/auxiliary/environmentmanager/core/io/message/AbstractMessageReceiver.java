/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.environmentmanager.core.io.message;

import java.util.HashMap;
import java.util.Map;
import vib.auxiliary.environmentmanager.core.IEnvironmentServer;
import vib.auxiliary.environmentmanager.core.mvc.IEnvironmentManagerCore;

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
