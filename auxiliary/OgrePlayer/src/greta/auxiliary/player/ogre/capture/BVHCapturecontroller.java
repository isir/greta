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
package greta.auxiliary.player.ogre.capture;

import greta.auxiliary.BVHMocap.BVHReaderGUI;
import greta.auxiliary.player.ogre.OgreRenderTexture;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 *
 * @author Donatella Simonetti
 */
public class BVHCapturecontroller extends javax.swing.JFrame implements CaptureListener, BAPFrameEmitter { // CallbackPerformer,

    private AWTImageCaptureOutput imageCaptureOutput;
    private OneShotCapturer screenShotCapturer;
    private RealTimeVideoCapturer realTimeVideoCapturer;
    private OffLineVideoCapturer offLineVideoCapturer;
    private String fileName = null;
    private Capturable currentCapturable;
    private OgreRenderTexture textureCapturable;
    protected Capturer currentVideoCapturer;

    private ArrayList<BAPFramePerformer> _bapFramePerformer = new ArrayList<BAPFramePerformer>();

    private ActionListener startCaptureAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            startVideoCapture();
        }
    };
    private ActionListener stopCaptureAction = new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            stopVideoCapture();
        }
    };


    private volatile boolean iscapturing = false;
    private boolean mustcapture = false;
    private File[] listFiles;
    private BVHReaderGUI bvhfilereader;
    private JFileChooser jFileChooser1;


    /**
     * Creates new form Capturecontroller
     */
    public BVHCapturecontroller() {
        initComponents();
        imageCaptureOutput = new AWTImageCaptureOutput();

        //image
        screenShotCapturer = new OneShotCapturer(null, imageCaptureOutput);

        //video
        realTimeVideoCapturer = new RealTimeVideoCapturer(null, imageCaptureOutput);
        offLineVideoCapturer = new OffLineVideoCapturer(null, imageCaptureOutput);
        currentVideoCapturer = realTimeVideoCapturer;
        realTimeVideoCapturer.addCaptureListener(this);
        offLineVideoCapturer.addCaptureListener(this);
        videoButton.addActionListener(startCaptureAction);
        jFileChooser1 = new JFileChooser();
    }

    public void setCapturable(Capturable capturable) {
        currentCapturable = capturable;
    }

    public void setCaptureOutput(CaptureOutput captureOutput) {
        if (captureOutput == null) {
            realTimeVideoCapturer.setCaptureOutput(imageCaptureOutput);
            offLineVideoCapturer.setCaptureOutput(imageCaptureOutput);
        } else {
            realTimeVideoCapturer.setCaptureOutput(captureOutput);
            offLineVideoCapturer.setCaptureOutput(captureOutput);
        }
    }

    @Override
    public void captureStarted(Capturer source, long time) {
        if (source == currentVideoCapturer) {
            screenShotButton.setEnabled(false);
            realTimeCheckBox.setEnabled(false);
            textureCheckBox.setEnabled(false);
            videoButton.removeActionListener(startCaptureAction);
            videoButton.addActionListener(stopCaptureAction);
            videoButton.setText("Stop");
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    @Override
    public void captureNewFrame(Capturer source, long time) {
    }

    @Override
    public void captureEnded(Capturer source, long time) {
        if (source == currentVideoCapturer) {
            screenShotButton.setEnabled(true);
            realTimeCheckBox.setEnabled(true);
            textureCheckBox.setEnabled(true);
            videoButton.removeActionListener(stopCaptureAction);
            videoButton.addActionListener(startCaptureAction);
            videoButton.setText("Video");
            setCursor(Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR));
        }
    }

    public void setBaseFileName(String baseFileName) {
        fileName = baseFileName;
    }

    public void startVideoCapture() {
        BVHCapturecontroller.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ensureCapturable(currentVideoCapturer);
        currentVideoCapturer.startCapture(fileName);

        //System.out.println(fileName);
    }

    public void run() {

    }

    public void stopVideoCapture() {
        BVHCapturecontroller.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        currentVideoCapturer.stopCapture();
    }

    public void ensureCapturable(Capturer capturer) {
        if (textureCheckBox.isSelected() && !(currentCapturable instanceof OgreRenderTexture)) {
            if (textureCapturable == null) {
                textureCapturable = new OgreRenderTexture(currentCapturable.getCamera());
            }
            else{
                textureCapturable.setCamera(currentCapturable.getCamera());
            }
            currentCapturable.prepareCapture();
            textureCapturable.setSize(currentCapturable.getCaptureWidth(), currentCapturable.getCaptureHeight());
            capturer.setCapturable(textureCapturable);
        } else {
            capturer.setCapturable(currentCapturable);
        }
    }

    public void setRealTimeCapture(boolean isRealTime){
        realTimeCheckBox.setSelected(isRealTime);
        realTimeCheckBoxActionPerformed(null);
    }

    public boolean isRealTimeCapture(){
        return realTimeCheckBox.isSelected();
    }

    public void setUseTexture(boolean useTexture){
        textureCheckBox.setSelected(useTexture);
    }

    public boolean isUseTexture(){
        return textureCheckBox.isSelected();
    }

    public void screenShot() {
        ensureCapturable(screenShotCapturer);
        screenShotCapturer.startCapture(fileName);
    }

    public void FMLVideoRecord() {

        //take the files name from the directory selected
        if (!FolderName.getText().isEmpty()){
            File dir = new File(FolderName.getText());
            listFiles = dir.listFiles();

            mustcapture = true;
            // for each file create a file .avi
            for(File f : listFiles){
                String videoName = f.getAbsolutePath().substring(0,f.getAbsolutePath().length()-4);//constructVideoName(f,""); //"H"

                File vf = new File(videoName + ".avi");

                if (!vf.exists()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        //Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    setBaseFileName(videoName);

                    startVideoCapture();
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ex) {
                        //Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    bvhfilereader.load(f.getAbsolutePath());

                    // take the frametime and number of frame to compute the mength of the video
                    List<Object> frametimeandnum = bvhfilereader.fileAndFrame.get(f.getAbsolutePath());

                    int length = Math.round((int) frametimeandnum.get(0)* (float) frametimeandnum.get(1));

                    //wait until the end of the video + 1 sec
                    try {
                    TimeUnit.SECONDS.sleep(length+1);
                    } catch (InterruptedException ex) {
                        Logs.error(ex.getLocalizedMessage());
                        //LOGGER.log(Level.SEVERE, ex.toString(), ex);
                    }

                    // stop video
                    stopVideoCapture();
                    iscapturing = false;
                    Logs.debug("---- isCapturing : False !!");

                    // once ended a video send to the agent the BAp values for the rest position
                    ArrayList<BAPFrame> bap_animation = new ArrayList<BAPFrame>();
                    BAPFrame restpose = new BAPFrame();

                    bap_animation.add(restpose);
                    ID id = IDProvider.createID("restpose");//today
                    for (int i = 0; i < _bapFramePerformer.size(); ++i) {
                        BAPFramePerformer performer = _bapFramePerformer.get(i);
                        performer.performBAPFrames(bap_animation, id);
                    }
                }
            }
        }else{
            JPanel panel = new JPanel();
            JOptionPane.showMessageDialog(panel, "Select a folder");
        }

    }

    private String constructVideoName(File f,String appendix) {
        return f.getName().substring(0,f.getName().length()-4) + "-"+appendix;
    }

    public void setBVHFileReader(BVHReaderGUI ffr){
        this.bvhfilereader = ffr;
    }

     public void setFileName(String fileName){
        this.FolderName.setText(fileName);
    }

    public String getFileName(){
        return this.FolderName.getText();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        screenShotButton = new javax.swing.JButton();
        videoButton = new javax.swing.JButton();
        realTimeCheckBox = new javax.swing.JCheckBox();
        textureCheckBox = new javax.swing.JCheckBox();
        BVHVideo = new javax.swing.JButton();
        FolderName = new javax.swing.JTextField();
        SelectFolder = new javax.swing.JButton();

        screenShotButton.setText("Screen Shot");
        screenShotButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                screenShotButtonActionPerformed(evt);
            }
        });

        videoButton.setText("Video");

        realTimeCheckBox.setSelected(true);
        realTimeCheckBox.setText("Real Time");
        realTimeCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                realTimeCheckBoxActionPerformed(evt);
            }
        });

        textureCheckBox.setText("Use texture");

        BVHVideo.setText("BVH Videos");
        BVHVideo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                BVHVideoActionPerformed(evt);
            }
        });

        FolderName.setToolTipText("");

        SelectFolder.setText("Select Folder");
        SelectFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectFolderActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(BVHVideo, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(screenShotButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(videoButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(realTimeCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textureCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(FolderName, javax.swing.GroupLayout.PREFERRED_SIZE, 326, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(SelectFolder)))
                .addContainerGap(30, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(18, 18, 18)
                .addComponent(screenShotButton)
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(realTimeCheckBox)
                        .addComponent(textureCheckBox))
                    .addComponent(videoButton))
                .addGap(24, 24, 24)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(BVHVideo)
                    .addComponent(FolderName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SelectFolder))
                .addContainerGap(24, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void screenShotButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_screenShotButtonActionPerformed
        screenShot();
    }//GEN-LAST:event_screenShotButtonActionPerformed

    private void realTimeCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_realTimeCheckBoxActionPerformed
        currentVideoCapturer.stopCapture();
        if (realTimeCheckBox.isSelected()) {
            currentVideoCapturer = realTimeVideoCapturer;
        } else {
            currentVideoCapturer = offLineVideoCapturer;
        }
    }//GEN-LAST:event_realTimeCheckBoxActionPerformed

    private void BVHVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_BVHVideoActionPerformed
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                FMLVideoRecord();
            }
        });
        //t.setDaemon(true);
        t.start();

    }//GEN-LAST:event_BVHVideoActionPerformed

    private void SelectFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectFolderActionPerformed
        jFileChooser1.setLocale(Locale.getDefault());
        jFileChooser1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser1.updateUI();
        if(jFileChooser1.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION){
            File file = jFileChooser1.getSelectedFile();
            this.FolderName.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_SelectFolderActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton BVHVideo;
    private javax.swing.JTextField FolderName;
    private javax.swing.JButton SelectFolder;
    protected javax.swing.JCheckBox realTimeCheckBox;
    protected javax.swing.JButton screenShotButton;
    protected javax.swing.JCheckBox textureCheckBox;
    protected javax.swing.JButton videoButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapfp) {
        if (bapfp != null) {
            _bapFramePerformer.add(bapfp);
        }
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapfp) {
         _bapFramePerformer.remove(bapfp);
    }

}
