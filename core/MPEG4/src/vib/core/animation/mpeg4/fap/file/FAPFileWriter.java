/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.animation.mpeg4.fap.file;

import vib.core.animation.mpeg4.fap.FAPFrame;
import vib.core.util.Constants;
import vib.core.util.id.ID;
import vib.core.util.log.Logs;
import java.util.List;

/**
 *
 * @author Radoslaw Niewiadomski
 */
public class FAPFileWriter implements vib.core.animation.mpeg4.fap.FAPFramePerformer {

    private String path;

    public FAPFileWriter() {
        path = "";
    }

    public FAPFileWriter(String path) {
        this.path = path;
    }

    @Override
    public void performFAPFrames(List<FAPFrame> fapframes, ID requestId) {
        String name = (path + requestId + ".fap");
        try {

            java.io.FileWriter fos = new java.io.FileWriter(name);

            String first_line =  "2.1 "+ name + " "+Constants.FRAME_PER_SECOND+" "+ fapframes.size()+"\n";
            fos.write(first_line);

            int first = fapframes.get(0).getFrameNumber();
            for(FAPFrame frame : fapframes) {
                fos.write(frame.AnimationParametersFrame2String(frame.getFrameNumber() - first));
            }

            fos.close();
        } catch (Exception ignored2) {
            Logs.warning("Error saving file: " + ignored2);
        }

    }//end of perorm

    @Override
    public void performFAPFrame(FAPFrame fapf, ID string) {

    }
}
