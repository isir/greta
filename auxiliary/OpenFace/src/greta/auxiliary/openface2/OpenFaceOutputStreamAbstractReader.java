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
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.repositories.AUAPFrame;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.time.Timer;
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

    private int startInputFrame = 0;
    private final int offsetFrame = 0;

    private int col_blink = 412;
    private double fps = 0;
    private double frameDuration = 0;
    private OpenFaceFrame curFrame = new OpenFaceFrame();
    private OpenFaceFrame prevFrame = new OpenFaceFrame();

    // loop variables
    private double prev_rot_X = 0.0;
    private double prev_rot_Y = 0.0;
    private double prev_rot_Z = 0.0;

    private double min_time = Double.MAX_VALUE;
    private double max_time = 0.0;

    private double prev_gaze_x = 0.0;
    private double prev_gaze_y = 0.0;

    private double prev_blink = 0.0;

    private double alpha = 0.75;//1.0;

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

    protected void processFrame(String line) {
        if (loaderIsPerforming() && line != null) {
            int curGretaFrame = (int) (Timer.getTime() * Constants.FRAME_PER_SECOND);
            prevFrame.copy(curFrame);

            if (startInputFrame == 0) {
                startInputFrame = curFrame.frameId;
            }

            curFrame.readDataLine(line);
            //curFrame.frameId += offsetFrame-startInputFrame + curGretaFrame;
            curFrame.frameId = offsetFrame + curGretaFrame;
            int frameDiff = curFrame.frameId - prevFrame.frameId;
            if (frameDiff < 10 && frameDiff > 0) { // If less than 10 frame delay
                frameDuration = curFrame.timestamp - prevFrame.timestamp;

                fps = 1. / frameDuration;
                //LOGGER.info(String.format("frameid: %d, fps:%f, f dur:%f",curFrame.frameId, fps, frameDuration));
                processOpenFace();
            }
        }
    }

    //Format based on https://github.com/TadasBaltrusaitis/OpenFace
    //timestamp, gaze_0_x, gaze_0_y, gaze_0_z, gaze_1_x, gaze_1_y, gaze_1_z, gaze_angle_x, gaze_angle_y, pose_Tx, pose_Ty, pose_Tz, pose_Rx, pose_Ry, pose_Rz, AU01_r, AU02_r, AU04_r, AU05_r, AU06_r, AU07_r, AU09_r, AU10_r, AU12_r, AU14_r, AU15_r, AU17_r, AU20_r, AU23_r, AU25_r, AU26_r, AU45_r, AU01_c, AU02_c, AU04_c, AU05_c, AU06_c, AU07_c, AU09_c, AU10_c, AU12_c, AU14_c, AU15_c, AU17_c, AU20_c, AU23_c, AU25_c, AU26_c, AU28_c, AU45_c
    private void processOpenFace() {
        if (loaderIsPerforming()) {
            if (frameDuration != 0) {
                if (frameDuration > max_time) {
                    max_time = frameDuration;
                }
                if (frameDuration < min_time) {
                    min_time = frameDuration;
                }
                sendAUFrame(makeAUFrame());
                sendBAPFrame(makeBAPFrame());
            }
        }
    }

    private AUAPFrame makeAUFrame() {
        AUAPFrame au_frame = new AUAPFrame();
        au_frame.setFrameNumber(curFrame.frameId);

        for (int i = 0; i < OpenFaceFrame.getAUCCount(); i++) {
            // we assume both tables have corresponding values. AU**_c acts as a mask
            if (curFrame.au_c[i] > 0.) {
                double value = Math.pow(curFrame.au_r[i], .5); // non linear curve to get to 1.
                double prevValue = prevFrame.intensity[i];
                double intensity = alpha * value + (1 - alpha) * prevValue;// filter
                au_frame.setAUAPboth(OpenFaceFrame.getAUCIndex(i), intensity);
            }
        }

        //gaze
        double gaze_x = alpha * (0.5 * (curFrame.gaze0.x() + curFrame.gaze1.x())) + (1 - alpha) * prev_gaze_x;
        double gaze_y = alpha * (0.5 * (curFrame.gaze0.y() + curFrame.gaze1.y())) + (1 - alpha) * prev_gaze_y;

        if (gaze_x < 0) {
            au_frame.setAUAPboth(62, -gaze_x);
        } else {
            au_frame.setAUAPboth(61, gaze_x);
        }

        if (gaze_y < 0) {
            au_frame.setAUAPboth(64, -gaze_y);
        } else {
            au_frame.setAUAPboth(63, gaze_y);
        }
        prev_gaze_x = gaze_x;
        prev_gaze_y = gaze_y;

        //blink
        // double blink = alpha*(Double.parseDouble(values[col_blink].replace(',', '.'))/5.0)+(1-alpha)*prev_blink;
        double blink = curFrame.blink;
        au_frame.setAUAPboth(43, blink);
        prev_blink = blink;
        return au_frame;
    }

    private BAPFrame makeBAPFrame() {
        BAPFrame hmFrame = new BAPFrame();
        hmFrame.setFrameNumber(curFrame.frameId);

        double rot_X_deg = curFrame.headRot.x();
        double rot_Y_deg = -1.0 * curFrame.headRot.y();
        double rot_Z_deg = -1.0 * curFrame.headRot.z();

        rot_X_deg = alpha * (rot_X_deg) + (1 - alpha) * prev_rot_X;
        rot_Y_deg = alpha * (rot_Y_deg) + (1 - alpha) * prev_rot_Y;
        rot_Z_deg = alpha * (rot_Z_deg) + (1 - alpha) * prev_rot_Z;

        hmFrame.setDegreeValue(BAPType.vc3_tilt, rot_X_deg);
        hmFrame.setDegreeValue(BAPType.vc3_torsion, rot_Y_deg);
        hmFrame.setDegreeValue(BAPType.vc3_roll, rot_Z_deg);

        prev_rot_X = rot_X_deg;
        prev_rot_Y = rot_Y_deg;
        prev_rot_Z = rot_Z_deg;

        return hmFrame;
    }

    /* ---------------------------------------------------------------------- */

    private void addConnectionListener(ConnectionListener connectionListener) {
        if (connectionListener != null && !connectionListeners.contains(connectionListener)) {
            connectionListeners.add(connectionListener);
        }
    }

    private void removeConnectionListener(ConnectionListener connectionListener) {
        if (connectionListener != null && connectionListeners.contains(connectionListener)) {
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
