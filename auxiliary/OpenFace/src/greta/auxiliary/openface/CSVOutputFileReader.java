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
package greta.auxiliary.openface;

import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFramesEmitter;
import greta.core.animation.mpeg4.bap.BAPFramesEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramesPerformer;
import greta.core.animation.mpeg4.fap.FAPFrameEmitterImpl;
import greta.core.keyframes.face.AUEmitter;
import greta.core.keyframes.face.AUPerformer;
import greta.core.repositories.AUAPFrame;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

/**
 *
 * @author Brice Donval
 */
public class CSVOutputFileReader extends FAPFrameEmitterImpl implements AUEmitter, BAPFramesEmitter {

    private static final Logger LOGGER = Logger.getLogger(CSVOutputFileReader.class.getName());

    private ArrayList<AUPerformer> auPerfomers = new ArrayList<>();
    private ArrayList<AUAPFrame> auFrames = new ArrayList<>();

    private ArrayList<BAPFramesPerformer> bapFramesPerfomers = new ArrayList<>();
    private ArrayList<BAPFrame> bapFrames = new ArrayList<>();

    BAPFramesEmitterImpl bapFramesEmitter = new BAPFramesEmitterImpl();

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
    public void addBAPFramesPerformer(BAPFramesPerformer bapFramesPerformer) {
        if (bapFramesPerformer != null) {
            bapFramesPerfomers.add(bapFramesPerformer);
            bapFramesEmitter.addBAPFramesPerformer(bapFramesPerformer);
        }
    }

    @Override
    public void removeBAPFramesPerformer(BAPFramesPerformer bapFramesPerformer) {
        if (bapFramesPerformer != null) {
            bapFramesPerfomers.remove(bapFramesPerformer);
            bapFramesEmitter.removeBAPFramesPerformer(bapFramesPerformer);
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
