/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.environmentmanager.core.io.audio;

import vib.auxiliary.environmentmanager.core.IEnvironmentServer;
import vib.core.animation.mpeg4.MPEG4Animatable;
import vib.core.util.audio.AudioEmitter;

/**
 *
 * @author Brice Donval
 */
public interface IAudioReceiver extends AudioEmitter {

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
