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
package greta.auxiliary.openface2;

import greta.auxiliary.openface2.gui.OpenFaceOutputStreamReader;
import greta.auxiliary.openface2.util.OpenFaceFrame;
import greta.auxiliary.openface2.util.StringArrayListener;
import greta.auxiliary.zeromq.ConnectionListener;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.repositories.AUAPFrame;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

/**
 *
 * @author Brice Donval
 */
public abstract class OpenFaceOutputStreamAbstractReader implements Runnable {

    private final OpenFaceOutputStreamReader loader;

    /* ---------------------------------------------------------------------- */

    private Thread thread;
    private final String threadName = OpenFaceOutputStreamAbstractReader.class.getSimpleName();

    private List<ConnectionListener> connectionListeners = new ArrayList<>();
    private List<StringArrayListener> headerListeners = new ArrayList<>();
    private String[] selectedHeaders;
 
   /* ---------------------------------------------------------------------- */

    protected OpenFaceOutputStreamAbstractReader(OpenFaceOutputStreamReader loader) {
        this.loader = loader;
        addConnectionListener(loader);
        addHeaderListener(loader);
    }

    protected boolean loaderIsPerforming() {
        return loader.isPerforming();
    }

    /* ---------------------------------------------------------------------- */

    protected abstract Logger getLogger();

    /* ---------------------------------------------------------------------- */

    protected void startThread() {
        if (thread == null) {
            getLogger().fine(String.format("Starting %s..", threadName));
            thread = new Thread(this, threadName);
            thread.start();
            fireConnection();
        }
    }

    protected void stopThread() {
        if (thread != null) {
            getLogger().fine(String.format("Stopping %s..", threadName));
            thread.interrupt();
            thread = null;
            fireDisconnection();
        }
    }

    /* ---------------------------------------------------------------------- */

    private void addConnectionListener(ConnectionListener connectionListener){
        if(connectionListener != null && !connectionListeners.contains(connectionListener)) {
            connectionListeners.add(connectionListener);
        }
    }

    private void removeConnectionListener(ConnectionListener connectionListener){
        if(connectionListener != null && connectionListeners.contains(connectionListener)) {
            connectionListeners.remove(connectionListener);
        }
    }

    private void fireConnection() {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onConnection();
        }
    }

    private void fireDisconnection() {
        for (ConnectionListener connectionListener : connectionListeners) {
            connectionListener.onDisconnection();
        }
    }

    /* ---------------------------------------------------------------------- */

    private void addHeaderListener(StringArrayListener headerListener) {
        if (headerListener != null && !headerListeners.contains(headerListener)) {
            headerListeners.add(headerListener);
        }
    }

    private void removeHeaderListener(StringArrayListener headerListener) {
        if (headerListener != null && headerListeners.contains(headerListener)) {
            headerListeners.remove(headerListener);
        }
    }

    protected void headerChanged(String[] headers) {
        headerListeners.forEach((headerListener) -> {
            headerListener.stringArrayChanged(headers);
        });
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Set selected headers
     *
     * @param selected headers to use
     */
    public void setSelected(String[] selected) {
        if (selected != null) {
            if (!Arrays.equals(selected, selectedHeaders)) {
                selectedHeaders = selected;
                OpenFaceFrame.setSelectedHeaders(selectedHeaders);
            }
            getLogger().info(String.format("Setting selected headers to: %s", Arrays.toString(selected)));
        } else {
            getLogger().warning("No header selected");
        }
    }

    /* ---------------------------------------------------------------------- */

    protected void sendAUFrame(AUAPFrame auFrame) {
        ID id = IDProvider.createID(threadName + "_sendAUFrame");
        loader.sendAUFrame(auFrame, id);
    }

    protected void sendBAPFrame(BAPFrame bapFrame) {
        ID id = IDProvider.createID(threadName + "_sendBAPFrame");
        loader.sendBAPFrame(bapFrame, id);
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public void finalize() throws Throwable {
        stopThread();
        removeConnectionListener(loader);
        removeHeaderListener(loader);
        super.finalize();
    }
}
