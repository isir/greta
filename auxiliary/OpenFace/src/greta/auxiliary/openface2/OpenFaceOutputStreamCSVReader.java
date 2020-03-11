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
        LOGGER.info(String.format("Thread: %s exiting", OpenFaceOutputStreamCSVReader.class.getName()));
    }

    public void processHeader(BufferedReader reader) {
        try {
            String line;
            if ((line = reader.readLine()) != null) {
                boolean changed = OpenFaceFrame.readHeader(line);
                if (changed) {
                    LOGGER.info("Header headerChanged");
                    headerChanged(OpenFaceFrame.headers);
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
