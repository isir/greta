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

import greta.auxiliary.openface1.AUParserFilesReader;
import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackPerformer;
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

    private static final Logger LOGGER = Logger.getLogger(ParserCaptureController_AU.class.getName());

    private volatile boolean isCapturing = false;
    private boolean mustCapture = false;
    private File[] files;
    private AUParserFilesReader filesReader;

    public ParserCaptureController_AU(){
        videoButton.setEnabled(false);
        screenShotButton.setText("Record all files");

    }

    @Override
    public void screenShot() {
        mustCapture = true;

        if(filesReader==null || filesReader.getDirPath()==null){
            Logs.error("Please set a directory in linked AUParserFileReader component first.");
        }
        else{
            File dir = filesReader.getDirPath();
            files = dir.listFiles((File file, String name1) -> name1.contains(".csv"));
            Logs.debug("Nb Files to process: "+files.length);
            for(File aufile : files){
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

        if (mustCapture) {
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

    public void setAUParserFilesReader(AUParserFilesReader fr){
        this.filesReader = fr;
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
            int length = filesReader.loadFile(aufile.getAbsolutePath());
            while (!isCapturing) {
                Logs.debug("---- isCapturing : false");
                isCapturing =  filesReader.isPerforming();
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
