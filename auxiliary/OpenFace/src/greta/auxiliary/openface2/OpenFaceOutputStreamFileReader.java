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
public class OpenFaceOutputStreamFileReader extends OpenFaceOutputStreamAbstractReader {

    protected static final Logger LOGGER = Logger.getLogger(OpenFaceOutputStreamFileReader.class.getName());

    private String csvFileName = "";
    private boolean isConnected;

    /* ---------------------------------------------------------------------- */

    public OpenFaceOutputStreamFileReader(OpenFaceOutputStreamReader loader) {
        super(loader);
    }

    /* ---------------------------------------------------------------------- */

    @Override
    protected Logger getLogger() {
        return LOGGER;
    }

    /* ---------------------------------------------------------------------- */

    public String getFileName() {
        return csvFileName;
    }

    public void setFileName(String fileName) {
        stopConnection();
        this.csvFileName = fileName;
    }

    /* ---------------------------------------------------------------------- */

    public void startConnection() {
        isConnected = true;
        startThread();
    }

    public void stopConnection() {
        stopThread();
        isConnected = false;
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public void run() {
        LOGGER.info(String.format("Thread: %s running", OpenFaceOutputStreamFileReader.class.getName()));
        try {
            LOGGER.fine(String.format("Thread: %s", OpenFaceOutputStreamFileReader.class.getName()));
            BufferedReader reader = new BufferedReader(new FileReader(csvFileName));
            while (true) {
                while (loaderIsPerforming()) {
                    while (isConnected) {
                        processLine(reader);
                        Thread.sleep(30);
                    }
                    Thread.sleep(1000);
                }
            }
        } catch (FileNotFoundException | InterruptedException ex) {
            LOGGER.warning(String.format("Thread: %s interrupted", OpenFaceOutputStreamFileReader.class.getName()));
        }
        LOGGER.info(String.format("Thread: %s exiting", OpenFaceOutputStreamFileReader.class.getName()));
    }

    public void processLine(BufferedReader reader) {
        String line = null;
        do {
            try {
                if ((line = reader.readLine()) != null) {
                    System.out.println(line);
                }
            } catch (IOException ex) {
                LOGGER.warning(String.format("Thread: %s interrupted", OpenFaceOutputStreamFileReader.class.getName()));
            }
        } while (line != null);
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Returns a {@code java.io.FileFilter} corresponding to CSV Files.
     *
     * @return a {@code java.io.FileFilter} corresponding to CSV Files
     */
    public java.io.FileFilter getFileFilter() {
        return new java.io.FileFilter() {
            @Override
            public boolean accept(File pathName) {
                String fileName = pathName.getName().toLowerCase();
                if (fileName.endsWith(".csv")) {
                    return true;
                }
                return false;
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
