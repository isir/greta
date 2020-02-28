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
public class ParserCaptureController_AU extends greta.auxiliary.player.ogre.capture.AUCapturecontroller implements CallbackPerformer {
    private static final Logger LOGGER = Logger.getLogger(ParserCaptureController_AU.class.getName() );

    private volatile boolean isCapturing = false;
    private boolean mustcapture = false;
    private File[] listFiles;
    private AUParserFileReader filereader;


    public ParserCaptureController_AU(){
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
