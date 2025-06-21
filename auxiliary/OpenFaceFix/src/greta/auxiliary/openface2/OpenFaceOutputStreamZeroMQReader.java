/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.auxiliary.openface2;

import greta.auxiliary.openface2.gui.OpenFaceOutputStreamReader;
import greta.auxiliary.openface2.util.OpenFaceFrame;
import greta.core.util.CharacterManager;
import greta.core.util.time.Timer;
import java.io.IOException;
import java.util.logging.Level;
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
    private int lineNullCount;
    private static final int MAX_LINENULLCOUNT = 8;
    private CharacterManager cm;

    /* ---------------------------------------------------------------------- */

    public OpenFaceOutputStreamZeroMQReader(OpenFaceOutputStreamReader loader) {
        super(loader);
    }
    
    public OpenFaceOutputStreamZeroMQReader(OpenFaceOutputStreamReader loader, CharacterManager cm) {
        super(loader);
        this.cm=cm;
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
                    Thread.sleep(10);
                }
                Thread.sleep(1000);
            }
        } catch (InterruptedException ex) {
            LOGGER.warning(String.format("Thread: %s interrupted", OpenFaceOutputStreamZeroMQReader.class.getName()));
        } catch (IOException ex) {
            Logger.getLogger(OpenFaceOutputStreamZeroMQReader.class.getName()).log(Level.SEVERE, null, ex);
        }

        cleanHeader();
        LOGGER.info(String.format("Thread: %s exiting", OpenFaceOutputStreamZeroMQReader.class.getName()));
    }

    private void processLine() throws IOException {
        String line = null;
        try {
            line = zSubscriber.recvStr(ZMQ.DONTWAIT);
        } catch (org.zeromq.ZMQException ex) {
            LOGGER.warning(String.format("Line is undefined"));
        }
        if (line != null) {
            if (line.startsWith("DATA:")) {  
                lineNullCount = 0;
                processData(line);
            } else if (line.startsWith("HEADER:")) {
                System.out.println("HEADERS HAS BEEN CHANGED");
                processHeader(line);
            } else {
                LOGGER.warning(String.format("Line not recognized: %s", line));
            }
				
														  
        }
        else
            lineNullCount++;
        // Send null data when missing too many frames
        if(lineNullCount > MAX_LINENULLCOUNT)
            processNullData();
    }

    private void processHeader(String line) {
        boolean changed = OpenFaceFrame.readHeader(line.substring(7));
        if (changed) {
            LOGGER.info("Header headerChanged");
            headerChanged(OpenFaceFrame.availableFeatures);
        }
    }

    private void processData(String line) throws IOException {
        //System.out.println("PROCESSDATA has STARTED");
        processFrame(line.substring(5));
    }
    
    private void processNullData() throws IOException{
        processFrame(null);
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public void finalize() throws Throwable {
        stopConnection();
        super.finalize();
    }
}
