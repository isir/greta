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

    protected static final Logger LOGGER = Logger.getLogger(OpenFaceOutputStreamAbstractReader.class.getName());

    protected Thread thread;
    protected final String threadName = OpenFaceOutputStreamAbstractReader.class.getSimpleName();

    protected List<StringArrayListener> headerListeners = new ArrayList<>();
    protected String[] selectedHeaders = null;

    /* ---------------------------------------------------------------------- */

    protected OpenFaceOutputStreamAbstractReader(OpenFaceOutputStreamReader loader) {
        this.loader = loader;
        addHeaderListener(loader);
    }

    /* ---------------------------------------------------------------------- */

    private void addHeaderListener(StringArrayListener headerListener) {
        if (headerListener != null && !headerListeners.contains(headerListener)) {
            headerListeners.add(headerListener);
        }
    }

    private void removeHeaderListene(StringArrayListener headerListener) {
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
            LOGGER.info(String.format("Setting selected headers to: %s", Arrays.toString(selected)));
        } else {
            LOGGER.warning("No header selected");
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
}
