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

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.animation.mpeg4.fap.FAPFrame;
import greta.core.animation.mpeg4.fap.FAPFrameEmitter;
import greta.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import greta.core.animation.mpeg4.fap.FAPFramePerformer;
import greta.core.keyframes.face.AUEmitter;
import greta.core.keyframes.face.AUPerformer;
import greta.core.repositories.AUAPFrame;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author Brice Donval
 */
public class OpenFaceOutputStreamFileReader extends Thread implements AUEmitter, FAPFrameEmitter, BAPFrameEmitter {

    private static final Logger LOGGER = Logger.getLogger(OpenFaceOutputStreamFileReader.class.getName());

    private ArrayList<AUPerformer> auPerfomers = new ArrayList<>();
    private ArrayList<AUAPFrame> auFrames = new ArrayList<>();

    private ArrayList<FAPFramePerformer> fapFramePerfomers = new ArrayList<>();
    private ArrayList<FAPFrame> fapFrames = new ArrayList<>();

    private ArrayList<BAPFramePerformer> bapFramePerfomers = new ArrayList<>();
    private ArrayList<BAPFrame> bapFrames = new ArrayList<>();

    private FAPFrameEmitterImpl fapFrameEmitter = new FAPFrameEmitterImpl();
    private BAPFrameEmitterImpl bapFrameEmitter = new BAPFrameEmitterImpl();

    private String csvFileName;

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
                    sleep(5000);
                    System.out.println("---------- Begining! ----------");
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    @Override
    public void addAUPerformer(AUPerformer auPerfomer) {
        if (auPerfomer != null) {
            auPerfomers.add(auPerfomer);
        }
    }

    @Override
    public void removeAUPerformer(AUPerformer auPerfomer) {
        if (auPerfomer != null) {
            auPerfomers.remove(auPerfomer);
        }
    }

    @Override
    public void addFAPFramePerformer(FAPFramePerformer fapFramePerformer) {
        if (fapFramePerformer != null) {
            fapFramePerfomers.add(fapFramePerformer);
            fapFrameEmitter.addFAPFramePerformer(fapFramePerformer);
        }
    }

    @Override
    public void removeFAPFramePerformer(FAPFramePerformer fapFramePerformer) {
        if (fapFramePerformer != null) {
            fapFramePerfomers.remove(fapFramePerformer);
            fapFrameEmitter.removeFAPFramePerformer(fapFramePerformer);
        }
    }

    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapFramesPerformer) {
        if (bapFramesPerformer != null) {
            bapFramePerfomers.add(bapFramesPerformer);
            bapFrameEmitter.addBAPFramePerformer(bapFramesPerformer);
        }
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapFramesPerformer) {
        if (bapFramesPerformer != null) {
            bapFramePerfomers.remove(bapFramesPerformer);
            bapFrameEmitter.removeBAPFramePerformer(bapFramesPerformer);
        }
    }

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
            public boolean accept(File pathname) {
                String filename = pathname.getName().toLowerCase();
                if (filename.endsWith(".csv")) {
                    return true;
                }
                return false;
            }
        };
    }
}
