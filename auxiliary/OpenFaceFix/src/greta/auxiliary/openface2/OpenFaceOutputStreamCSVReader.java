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
import greta.core.util.time.Timer;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.logging.Logger;

/**
 *
 * @author Brice Donval
 */
public class OpenFaceOutputStreamCSVReader extends OpenFaceOutputStreamAbstractReader {

    protected static final Logger LOGGER = Logger.getLogger(OpenFaceOutputStreamCSVReader.class.getName());

    private String fileName = "";

    private boolean isConnected;

    /* ---------------------------------------------------------------------- */

    public OpenFaceOutputStreamCSVReader(OpenFaceOutputStreamReader loader) {
        super(loader);
    }

    /* ---------------------------------------------------------------------- */

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /* ---------------------------------------------------------------------- */

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        stopConnection();
        this.fileName = fileName;
    }

    /* ---------------------------------------------------------------------- */

    public void startConnection() {
        stopConnection();
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            reader.close();
            isConnected = true;
        } catch (IOException ex) {
            isConnected = false;
        }
        if (isConnected) {
            startThread();
        } else {
            LOGGER.warning(String.format("Failed to open: %s", getFileName()));
            stopConnection();
        }
    }

    public void stopConnection() {
        isConnected = false;
        Timer.sleep(50);
        stopThread();
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public void run() {
        LOGGER.info(String.format("Thread: %s running", OpenFaceOutputStreamCSVReader.class.getName()));
        try {
            LOGGER.fine(String.format("Thread: %s", OpenFaceOutputStreamCSVReader.class.getName()));
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            processHeader(reader);
            while (true) {
                while (isConnected) {
                    processData(reader);
                    Thread.sleep(30);
                }
                Thread.sleep(1000);
            }
        } catch (FileNotFoundException | InterruptedException e) {
            LOGGER.warning(String.format("Thread: %s interrupted", OpenFaceOutputStreamCSVReader.class.getName()));
        }

        cleanHeader();
        LOGGER.info(String.format("Thread: %s exiting", OpenFaceOutputStreamCSVReader.class.getName()));
    }

    public void processHeader(BufferedReader reader) {
        try {
            String line;
            if ((line = reader.readLine()) != null) {
                boolean changed = OpenFaceFrame.readHeader(line);
                if (changed) {
                    LOGGER.info("Header headerChanged");
                    headerChanged(OpenFaceFrame.availableFeatures);
                }
            }
            while ((reader.readLine()) != null) {
            }
        } catch (IOException ex) {
            LOGGER.warning(String.format("Thread: %s interrupted", OpenFaceOutputStreamCSVReader.class.getName()));
        }
    }

    public void processData(BufferedReader reader) {
        try {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.split(OpenFaceFrame.separator).length == 714) {
                    processFrame(line);
                }
            }
        } catch (IOException ex) {
            LOGGER.warning(String.format("Thread: %s interrupted", OpenFaceOutputStreamCSVReader.class.getName()));
        }
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns a {@code javax.swing.filechooser.FileFilter} corresponding to CSV Files.
     *
     * @return a {@code javax.swing.filechooser.FileFilter} corresponding to CSV Files
     */
    public javax.swing.filechooser.FileFilter getFileFilter() {
        return new javax.swing.filechooser.FileFilter() {
            @Override
            public boolean accept(File pathName) {
                String fileName = pathName.getName().toLowerCase();
                return (pathName.isDirectory() || fileName.endsWith(".csv"));
            }

            @Override
            public String getDescription() {
                return "OpenFace CSV File";
            }
        };
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public void finalize() throws Throwable {
        stopConnection();
        super.finalize();
    }
}
