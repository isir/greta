/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.plancapture;

import java.awt.Cursor;
import vib.core.feedbacks.Callback;
import vib.core.feedbacks.CallbackPerformer;
import vib.core.intentions.FMLFileReader;
import vib.core.signals.Signal;
import vib.core.signals.SignalPerformer;
import vib.core.util.Mode;
import vib.core.util.id.ID;
import vib.core.util.id.IDProvider;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import vib.auxiliary.player.ogre.OgreRenderTexture;
import vib.auxiliary.player.ogre.capture.Capturecontroller;
import vib.auxiliary.player.ogre.capture.Capturer;
import vib.auxiliary.socialparameters.SocialDimension;
import vib.auxiliary.socialparameters.SocialParameterFrame;
import vib.auxiliary.socialparameters.SocialParameterPerformer;

/**
 *
 * @author Brian Ravenet
 */
public class FMLAttitudeCaptureController extends vib.auxiliary.player.ogre.capture.Capturecontroller implements CallbackPerformer, SocialParameterPerformer{

    private SocialParameterFrame sap;
    private volatile boolean iscapturing = false;
    private boolean mustcapture = false;
    private File[] listFiles;
    private FMLFileReader filereader;
        public FMLAttitudeCaptureController(){
        File dir = new File("D:\\mergeMultiCharactersGaze\\bin\\Examples");
        listFiles = dir.listFiles();
    }
    
    @Override
    public void screenShot() {
        mustcapture = true;
        for(File f : listFiles){
            String videoNameH = constructVideoName(f,""); //"H"
            //String videoNameN = constructVideoName(f,"N");
            //String videoNameF = constructVideoName(f,"F");
            File vf = new File(videoNameH + ".avi");
            
            if (!vf.exists()) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                }
                setBaseFileName(videoNameH);
                /*sap = new SocialParameterFrame();
                sap.setDoubleValue(SocialDimension.Dominance, 1);
                sap.setDoubleValue(SocialDimension.Liking, -1);*/
                startVideoCapture();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                }
                iscapturing = true;
                filereader.load(f.getAbsolutePath());
                while (iscapturing) {


                }
            }
            /*vf = new File(videoNameN + ".avi");
            if (!vf.exists()) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                }
                setBaseFileName(videoNameN);
                sap = new SocialParameterFrame();
                sap.setDoubleValue(SocialDimension.Dominance, 1);
                sap.setDoubleValue(SocialDimension.Liking, 0);
                startVideoCapture();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                }
                iscapturing = true;
                filereader.load(f.getAbsolutePath());
                while (iscapturing) {


                }
            }
            vf = new File(videoNameF + ".avi");
            if (!vf.exists()) {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                }
                setBaseFileName(videoNameF);
                sap = new SocialParameterFrame();
                sap.setDoubleValue(SocialDimension.Dominance, 1);
                sap.setDoubleValue(SocialDimension.Liking, 1);
                startVideoCapture();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                }
                iscapturing = true;
                filereader.load(f.getAbsolutePath());
                while (iscapturing) {


                }
            }*/
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

    
    private String constructVideoName(File f,String appendix) {
        return f.getName().substring(0,f.getName().length()-4) + "-"+appendix;
    }
    
    public void setFMLFileReader(FMLFileReader ffr){
        this.filereader = ffr;
    }

    @Override
    public void performSocialParameter(List<SocialParameterFrame> frames, ID requestId) {
        
        if (frames.size() > 0) {
            this.sap = (frames.get(frames.size() - 1));
        }
    }
}
