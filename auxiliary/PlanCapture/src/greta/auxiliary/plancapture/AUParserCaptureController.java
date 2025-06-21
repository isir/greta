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
import greta.core.signals.AUParserFileReader;
import greta.core.util.log.Logs;
import java.io.File;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Thomas Janssoone
 */
public class AUParserCaptureController extends greta.auxiliary.player.ogre.capture.Capturecontroller implements CallbackPerformer {
    private static final Logger LOGGER = Logger.getLogger( AUParserCaptureController.class.getName() );

    private volatile boolean isCapturing = false;
    private boolean mustcapture = false;
    private File[] listFiles;
    private AUParserFileReader filereader;


    public AUParserCaptureController(){
        videoButton.setEnabled(false);
        screenShotButton.setText("Record all files");

    }


    @Override
    public void screenShot() {
        mustcapture = true;

        if(filereader==null || filereader.getDirPath()==null){
            Logs.error("Please set a directory in linked AUParserFileReader component first.");
        }
        else{
            File dir = filereader.getDirPath();
            listFiles = dir.listFiles((File file, String name1) -> name1.contains(".csv"));
            Logs.debug("Nb Files to process: "+listFiles.length);
            for(File aufile : listFiles){
                if(aufile.isFile()){
                    Logs.debug("Process: "+aufile.getAbsolutePath());

                    recordAU(aufile);
                }
            }
        }
    }

    @Override
    public void performCallback(Callback clbck) {
        Logs.debug("performCallback:");

        if (mustcapture) {
            Logs.debug("-- mustcapture");

            if ((clbck.type().equalsIgnoreCase("dead") || clbck.type().equalsIgnoreCase("end"))) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logs.error(ex.getLocalizedMessage());
                    LOGGER.log(Level.SEVERE, ex.toString(), ex);
                }
            }
            stopVideoCapture();

            isCapturing = false;
        }
        else{
            Logs.debug("-- mustcapture == FALSE");
        }
    }


    public void setAUParserFileReader(AUParserFileReader bfr){
        this.filereader = bfr;
    }

    private void recordAU(File aufile) {

        String videoName =aufile.getAbsolutePath().substring(0,aufile.getAbsolutePath().length()-4);
        File v = new File(videoName + ".avi");
        if (!v.exists()) {
            try {
                Thread.sleep(3000);
            } catch (InterruptedException ex) {
                Logs.error(ex.getLocalizedMessage());
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }
            setBaseFileName(videoName);


            isCapturing = false;
            int length = filereader.loadFile(aufile.getAbsolutePath());
            while (!isCapturing) {
                Logs.debug("---- isCapturing : false");
                isCapturing =  filereader.isPerforming();
            }
            Logs.debug("---- isCapturing : TRUE");

            startVideoCapture();

            try {
                TimeUnit.SECONDS.sleep(length+1);
            } catch (InterruptedException ex) {
                Logs.error(ex.getLocalizedMessage());
                LOGGER.log(Level.SEVERE, ex.toString(), ex);
            }

            stopVideoCapture();
            isCapturing = false;
            Logs.debug("---- isCapturing : False !!");
        }
        else
            Logs.info("Output file already exists, skipping");
    }
}
