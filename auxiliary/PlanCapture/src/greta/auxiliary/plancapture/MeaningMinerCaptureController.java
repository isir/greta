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
