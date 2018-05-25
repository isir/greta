/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.environmentmanager.core.io.message;

import java.util.Map;
import vib.auxiliary.environmentmanager.core.IEnvironmentServer;

/**
 *
 * @author Brice Donval
 */
public interface IMessageReceiver {

    public IEnvironmentServer getEnvironmentServer();

    /* ---------------------------------------------------------------------- */

    public String getHost();

    public void setHost(String host);

    /* ------------------------------ */

    public String getPort();

    public void setPort(String port);

    /* -------------------------------------------------- */

    public void hasConnected();

    public void hasDisconnected();

    /* -------------------------------------------------- */

    public void hasReceived(String message, Map<String, String> details);

    /* -------------------------------------------------- */

    public void destroy();

    public void onDestroy();

}
