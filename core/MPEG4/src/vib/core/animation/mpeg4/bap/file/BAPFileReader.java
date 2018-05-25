/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.bap.file;

import vib.core.animation.mpeg4.bap.BAPFrame;
import vib.core.animation.mpeg4.bap.BAPFramesEmitterImpl;
import vib.core.animation.mpeg4.bap.BAPParser;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import java.io.File;
import java.util.List;

/**
 *
 * @author Andre-Marie Pez
 */
public class BAPFileReader extends BAPFramesEmitterImpl{

    private BAPParser parser = new BAPParser();

    public void load(String bapfilename) {
        List<BAPFrame> bap_animation = parser.readFromFile(bapfilename, true);

        if (!bap_animation.isEmpty()) {
            String base = (new File(bapfilename)).getName().replaceAll("\\.bap$", "");
            ID id = IDProvider.createID(base);

            //send to all BAPPerformer added
            sendBAPFrames(id, bap_animation);
        }
    }

    public java.io.FileFilter getFileFilter() {
        return new java.io.FileFilter() {
            @Override
            public boolean accept(File pathname) {
                return pathname.getName().toLowerCase().endsWith(".bap");
            }
        };
    }

}
