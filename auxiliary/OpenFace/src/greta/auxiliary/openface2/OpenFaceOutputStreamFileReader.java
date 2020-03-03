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
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 *
 * @author Brice Donval
 */
public class OpenFaceOutputStreamFileReader extends OpenFaceOutputStreamAbstractReader {

    private String csvFileName;

    /* ---------------------------------------------------------------------- */

    public OpenFaceOutputStreamFileReader(OpenFaceOutputStreamReader loader) {
        super(loader);
    }

    /* ---------------------------------------------------------------------- */

    @Override
    public void run() {

        try {
            BufferedReader reader = new BufferedReader(new FileReader(csvFileName));
            String line = "";

            System.out.println("---------- Begining! ----------");
            while (true) {
                if ((line = reader.readLine()) != null) {
                    System.out.println(line);
                } else {
                    System.out.println("---------- Waiting... ----------");
                    //sleep(5000);
                    System.out.println("---------- Begining! ----------");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /* ---------------------------------------------------------------------- */

    /**
     * Loads an CSV file.
     *
     * @param csvFileName the name of the file to load
     * @return The ID of the generated event
     */
    public ID load(String csvFileName) {
        //get the base file name to use it as requestId
        String base = (new File(csvFileName)).getName().replaceAll("\\.csv$", "");

        ID id = IDProvider.createID(base);
        return id;
    }

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
}
