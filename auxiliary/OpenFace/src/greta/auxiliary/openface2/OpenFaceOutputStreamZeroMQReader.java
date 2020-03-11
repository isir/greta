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
import greta.core.util.time.Timer;
import java.util.logging.Logger;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMQ.Socket;

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

    private String protocol = DEFAULT_ZEROMQ_PROTOCOL;
    private String host = DEFAULT_ZEROMQ_HOST;
    private String port = DEFAULT_ZEROMQ_PORT;

    private ZContext zContext;
    private Socket zSubscriber;
    private final static String TOPIC = "";

    private boolean isConnected = false;

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
        } catch (InterruptedException ex) {
            LOGGER.warning(String.format("Thread: %s interrupted", OpenFaceOutputStreamZeroMQReader.class.getName()));
        }
        LOGGER.info(String.format("Thread: %s exiting", OpenFaceOutputStreamZeroMQReader.class.getName()));
    }

    private void processLine() {
        String line = null;
        try {
            line = zSubscriber.recvStr();
        } catch (org.zeromq.ZMQException ex) {
            LOGGER.warning(String.format("Line is undefined"));
        }
        if (line != null) {
            if (line.startsWith("DATA:")) {
                processData(line);
            } else if (line.startsWith("HEADER:")) {
                processHeader(line);
            } else {
                LOGGER.warning(String.format("Line not recognized: %s", line));
            }
        } else {
            LOGGER.warning(String.format("Line is null"));
        }
    }

    private void processHeader(String line) {
        boolean changed = OpenFaceFrame.readHeader(line.substring(7));
        if (changed) {
            LOGGER.info("Header headerChanged");
            headerChanged(OpenFaceFrame.headers);
        }
    }

    private void processData(String line) {
        processFrame(line.substring(5));
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public void finalize() throws Throwable {
        stopConnection();
        super.finalize();
    }
}
