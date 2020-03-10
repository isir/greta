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
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.repositories.AUAPFrame;
import greta.core.util.Constants;
import greta.core.util.time.Timer;
import java.util.logging.Logger;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;
import org.zeromq.ZMQException;

/**
 * This file represents an OpenFace2 frame
 *
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 * @author Brice Donval
 */
public class OpenFaceOutputStreamZeroMQReader extends OpenFaceOutputStreamAbstractReader {

    protected static final Logger LOGGER = Logger.getLogger(OpenFaceOutputStreamZeroMQReader.class.getName());

    private static final String DEFAULT_ZEROMQ_PROTOCOL = "tcp";
    private static final String DEFAULT_ZEROMQ_HOST = "localhost";
    private static final String DEFAULT_ZEROMQ_PORT = "5000";

    private int startInputFrame = 0;
    private final int offsetFrame = 0;

    private ZContext zContext;
    private Socket zSubscriber;
    private String protocol = DEFAULT_ZEROMQ_PROTOCOL;
    private String host = DEFAULT_ZEROMQ_HOST;
    private String port = DEFAULT_ZEROMQ_PORT;
    private boolean isConnected;
    private final static String TOPIC = "";

    private String lastDataStr;
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

    public OpenFaceOutputStreamZeroMQReader(OpenFaceOutputStreamReader loader) {
        super(loader);
    }

    /* ---------------------------------------------------------------------- */

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /* ---------------------------------------------------------------------- */

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        stopConnection();
        this.protocol = protocol;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        stopConnection();
        this.host = host;
    }

    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        stopConnection();
        this.port = port;
    }

    public String getURL() {
        return protocol + "://" + host + ":" + port;
    }

    public void setURL(String host, String port) {
        stopConnection();
        this.host = host;
        this.port = port;
    }

    /* ---------------------------------------------------------------------- */

    public void startConnection() {
        stopConnection();
        zContext = new ZContext();
        zSubscriber = zContext.createSocket(SocketType.SUB);
        isConnected = zSubscriber.connect(getURL());
        if (isConnected) {
            zSubscriber.subscribe(TOPIC.getBytes(ZMQ.CHARSET));
            LOGGER.info(String.format("Connected to: %s", getURL()));
            startThread();
        } else {
            LOGGER.warning(String.format("Failed to open: %s", getURL()));
            stopConnection();
        }
    }

    public void stopConnection() {
        isConnected = false;
        Timer.sleep(50);
        stopThread();
        if (zSubscriber != null) {
            zSubscriber.close();
            zSubscriber = null;
        }
        if (zContext != null) {
            zContext.close();
            zContext = null;
        }
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public void run() {
        LOGGER.info(String.format("Thread: %s running", OpenFaceOutputStreamZeroMQReader.class.getName()));
        try {
            LOGGER.fine(String.format("Thread: %s", OpenFaceOutputStreamZeroMQReader.class.getName()));
            while (true) {
                while (isConnected) {
                    processLine();
                    Thread.sleep(30);
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException e) {
            LOGGER.warning(String.format("Thread: %s interrupted", OpenFaceOutputStreamZeroMQReader.class.getName()));
        }
        LOGGER.info(String.format("Thread: %s exiting", OpenFaceOutputStreamZeroMQReader.class.getName()));
    }

    private void processLine() {
        if (isConnected) {
            String line = zSubscriber.recvStr();
            if (line != null) {
                if (line.startsWith("DATA:")) {
                    lastDataStr = line.substring(5);
                    processFrame(lastDataStr);
                } else if (line.startsWith("HEADER:")) {
                    boolean changed = OpenFaceFrame.readHeader(line.substring(7));
                    if (changed) {
                        LOGGER.info("Header headerChanged");
                        headerChanged(OpenFaceFrame.headers);
                    }
                } else {
                    LOGGER.warning(String.format("Line not recognized: %s", line));
                }
            } else {
                LOGGER.warning(String.format("Line is empty"));
            }
        }
    }

    private void processFrame(String line) {
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
        if (isConnected && loaderIsPerforming()) {
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

    @Override
    public void finalize() throws Throwable {
        stopConnection();
        super.finalize();
    }
}
