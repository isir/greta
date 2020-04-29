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
import greta.auxiliary.openface2.util.ArrayOfDoubleFilter;
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

    private String[] selectedFeatures;

    /* ---------------------------------------------------------------------- */

    private int startInputFrame = 0;
    private final int offsetFrame = 0;

    private double fps = 0.0;
    private double frameDuration = 0.0;
    private OpenFaceFrame curFrame = new OpenFaceFrame();
    private OpenFaceFrame prevFrame = new OpenFaceFrame();
    private ArrayOfDoubleFilter filter = new ArrayOfDoubleFilter(64,5);
    private boolean useFilter = true;

    // loop variables
    private double prev_rot_X = 0.0;
    private double prev_rot_Y = 0.0;
    private double prev_rot_Z = 0.0;

    private double min_time = Double.MAX_VALUE;
    private double max_time = 0.0;

    private double prev_gaze_x = 0.0;
    private double prev_gaze_y = 0.0;

    private double prev_blink = 0.0;

    private double alpha = 0.75; //1.0;
    
    
    public int getFilterMaxQueueSize(){
        return filter.getMaxSizePerQueue();
    }
    
    public void setFilterMaxQueueSize(int i){
        filter.setMaxSizePerQueue(i);
    }

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

    protected void cleanHeader() {
        OpenFaceFrame.readHeader(null);
        headerChanged(OpenFaceFrame.availableFeatures);
    }

    /* ---------------------------------------------------------------------- */

    protected void processFrame(String line) {
        if (loaderIsPerforming() && line != null) {
            int curGretaFrame = (int) (Timer.getTime() * Constants.FRAME_PER_SECOND);
            prevFrame.copy(curFrame);

            if (startInputFrame == 0) {
                startInputFrame = curFrame.frameNumber;
            }

            curFrame.readDataLine(line);
            //curFrame.frameNumber += offsetFrame-startInputFrame + curGretaFrame;
            curFrame.frameNumber = offsetFrame + curGretaFrame;
            int frameDiff = curFrame.frameNumber - prevFrame.frameNumber;
            if (frameDiff < 10 && frameDiff > 0) { // If less than 10 frame delay
                frameDuration = curFrame.timestamp - prevFrame.timestamp;
                fps = 1.0 / frameDuration;
                //LOGGER.info(String.format("frameNumber: %d, fps:%f, f dur:%f",curFrame.frameNumber, fps, frameDuration));
                processOpenFace();
            }
        }
    }

    private void processOpenFace() {

        // Format based on <https://github.com/TadasBaltrusaitis/OpenFace>:
        // frame,           face_id,        timestamp,      confidence,     success,
        // gaze_0_x,        gaze_0_y,       gaze_0_z,
        // gaze_1_x,        gaze_1_y,       gaze_1_z,
        // gaze_angle_x,    gaze_angle_y,
        // pose_Tx,         pose_Ty,        pose_Tz,
        // pose_Rx,         pose_Ry,        pose_Rz
        // AUs_r,           AUs_c

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
        au_frame.setFrameNumber(curFrame.frameNumber);

        for (int i = 0; i < OpenFaceFrame.getAUFeatureMasksCount(); ++i) {
            // we assume both tables have corresponding values. AU**_c acts as a mask
            if (curFrame.auMasks[i] > 0.0) {
                double value = Math.pow(curFrame.aus[i], 0.5); // non linear curve to get to 1.
                double intensity;
                if(isUseFilter()){
                    filter.push(i, value);                
                    intensity = filter.getFiltered(i);//alpha * value + (1 - alpha) * prevValue; // filter
                }
                else{
                    double prevValue = prevFrame.intensity[i];
                    intensity = alpha * value + (1 - alpha) * prevValue; // filter
                }
                
                au_frame.setAUAPboth(OpenFaceFrame.getAUFeatureMaskNumber(i), intensity);
            }
        }
        

        // gaze
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

        // blink
        //double blink = alpha*(Double.parseDouble(values[col_blink].replace(',', '.'))/5.0)+(1-alpha)*prev_blink;
        double blink = curFrame.blink;
        au_frame.setAUAPboth(43, blink);
        return au_frame;
    }

    private BAPFrame makeBAPFrame() {
        BAPFrame hmFrame = new BAPFrame();
        hmFrame.setFrameNumber(curFrame.frameNumber);

        double rot_X_rad = curFrame.headPoseR.x();
        double rot_Y_rad = -1.0 * curFrame.headPoseR.y();
        double rot_Z_rad = -1.0 * curFrame.headPoseR.z();

        double rot_X_deg = rot_X_rad * 180 / Math.PI;
        double rot_Y_deg = rot_Y_rad * 180 / Math.PI;
        double rot_Z_deg = rot_Z_rad * 180 / Math.PI;

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

    protected void headerChanged(String[] newFeatures) {
        headerListeners.forEach((headerListener) -> {
            headerListener.stringArrayChanged(newFeatures);
        });
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Set selected features
     *
     * @param selected features to use
     */
    public void setSelected(String[] selected) {
        if (selected != null) {
            if (!Arrays.equals(selected, selectedFeatures)) {
                selectedFeatures = selected;
                OpenFaceFrame.setSelectedFeatures(selectedFeatures);
            }
            getLogger().info(String.format("Setting selected features to: %s", Arrays.toString(selected)));
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

    /**
     * @return the useFilter
     */
    public boolean isUseFilter() {
        return useFilter;
    }

    /**
     * @param useFilter the useFilter to set
     */
    public void setUseFilter(boolean useFilter) {
        getLogger().info("setUseFilter: "+useFilter);
        this.useFilter = useFilter;
    }
}
