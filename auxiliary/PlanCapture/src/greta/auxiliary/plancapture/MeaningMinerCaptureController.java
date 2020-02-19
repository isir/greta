/*
 * This file is part of the auxiliaries of Greta.
 *
 * Greta is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Greta is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Greta.  If not, see <http://www.gnu.org/licenses/>.
 *
 */
package greta.auxiliary.plancapture;

import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackPerformer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Brian Ravenet
 */
public class MeaningMinerCaptureController extends greta.auxiliary.player.ogre.capture.AUCapturecontroller implements CallbackPerformer {

    private volatile boolean iscapturing = false;
    private boolean mustcapture = false;
    private File[] listFiles;
    //private ImageSchemaExtractor imageSchemaExtractor;

    public MeaningMinerCaptureController() {
        File dir = new File("./Examples/MeaningMiner/Corpus/");
        listFiles = dir.listFiles();
    }

    @Override
    public void screenShot() {
        mustcapture = true;
        File dir = new File("./Examples/MeaningMiner/Corpus/");
        listFiles = dir.listFiles();
        for (File bmlfile : listFiles) {
            if (bmlfile.isFile() && bmlfile.getAbsolutePath().endsWith(".bml")) {

                recordBML(bmlfile);
            }
        }
    }

    @Override
    public void performCallback(Callback clbck) {

        if (mustcapture) {
            if ((clbck.type().equalsIgnoreCase("dead") || clbck.type().equalsIgnoreCase("end"))) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(FMLAttitudeCaptureController.class.getName()).log(Level.SEVERE, null, ex);
                }
                stopVideoCapture();

                iscapturing = false;

            }
        }
    }

    /*public void setImageSchemaExtractor(ImageSchemaExtractor ise) {
        this.imageSchemaExtractor = ise;
    }*/

    private void recordBML(File bmlfile) {
        String videoName = bmlfile.getAbsolutePath().substring(0, bmlfile.getAbsolutePath().length() - 4);
        File v = new File(videoName + ".avi");
        if (!v.exists()) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
            }
            setBaseFileName(videoName);
            startVideoCapture();
            try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
            }
            iscapturing = true;
            String content = "";

            try {
                content = new String(Files.readAllBytes(bmlfile.toPath()));
            } catch (IOException ex) {
                Logger.getLogger(MeaningMinerCaptureController.class.getName()).log(Level.SEVERE, null, ex);
            }
            /*imageSchemaExtractor.processText(content);
            while (iscapturing) {
                
            }*/
        }
    }

}
