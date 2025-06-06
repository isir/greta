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

import greta.auxiliary.player.ogre.OgreRenderTexture;
import greta.core.feedbacks.CallbackPerformer;
import greta.core.intentions.FMLFileReader;
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.jms.JMSException;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import org.xml.sax.SAXException;

/**
 *
 * @author Andre-Marie Pez
 */
public class Capturecontroller extends javax.swing.JFrame implements CallbackPerformer, CaptureListener {

    private AWTImageCaptureOutput imageCaptureOutput;
    private OneShotCapturer screenShotCapturer;
    private RealTimeVideoCapturer realTimeVideoCapturer;
    private OffLineVideoCapturer offLineVideoCapturer;
    private String fileName = null;
    private Capturable currentCapturable;
    private OgreRenderTexture textureCapturable;
    protected Capturer currentVideoCapturer;

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
    private FMLFileReader filereader;
    private JFileChooser jFileChooser1;


    /**
     * Creates new form Capturecontroller
     */
    public Capturecontroller() {
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
        
        fileName = baseNameTextBox.getText();        

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
        Capturecontroller.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ensureCapturable(currentVideoCapturer);
        currentVideoCapturer.startCapture(fileName, useFixedIndexCheckBox.isSelected());
        //System.out.println(fileName);
    }

    public void stopVideoCapture() {
        Capturecontroller.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
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
        screenShotCapturer.startCapture(fileName, useFixedIndexCheckBox.isSelected());
    }

    public void FMLVideoRecord() throws IOException{

        //take the files name from the directory selected
        if (!FolderName.getText().isEmpty()){
            File dir = new File(FolderName.getText());
            listFiles = dir.listFiles();

            mustcapture = true;
            // for each file create a file .avi
            for(File f : listFiles){
                String videoNameH = f.getAbsolutePath().substring(0,f.getAbsolutePath().length()-4);//constructVideoName(f,""); //"H"
                //String videoNameN = constructVideoName(f,"N");
                //String videoNameF = constructVideoName(f,"F");
                File vf = new File(videoNameH + ".avi");

                if (!vf.exists()) {
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException ex) {
                        //Logger.getLogger(PlanCapturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    setBaseFileName(videoNameH);

                    /*sap = new SocialParameterFrame();
                    sap.setDoubleValue(SocialDimension.Dominance, 1);
                    sap.setDoubleValue(SocialDimension.Liking, -1);*/

                    iscapturing = true;

                    try {
                        filereader.load(f.getAbsolutePath());
                    } catch (TransformerException ex) {
                        Logger.getLogger(Capturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (SAXException ex) {
                        Logger.getLogger(Capturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (ParserConfigurationException ex) {
                        Logger.getLogger(Capturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (JMSException ex) {
                        Logger.getLogger(Capturecontroller.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    startVideoCapture();

                    while (iscapturing) {

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

    public void setFMLFileReader(FMLFileReader ffr){
        this.filereader = ffr;
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
        FMLVideo = new javax.swing.JButton();
        FolderName = new javax.swing.JTextField();
        SelectFolder = new javax.swing.JButton();
        baseNameTextBox = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        useFixedIndexCheckBox = new javax.swing.JCheckBox();

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

        FMLVideo.setText("FML Videos");
        FMLVideo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                FMLVideoActionPerformed(evt);
            }
        });

        FolderName.setToolTipText("");

        SelectFolder.setText("Select Folder");
        SelectFolder.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                SelectFolderActionPerformed(evt);
            }
        });

        baseNameTextBox.setText("Capture_");
        baseNameTextBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                baseNameTextBoxActionPerformed(evt);
            }
        });

        jLabel1.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        jLabel1.setText("Base name");

        useFixedIndexCheckBox.setText("fixed index (0)");
        useFixedIndexCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                useFixedIndexCheckBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(screenShotButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(videoButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                        .addComponent(jLabel1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(FMLVideo, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 91, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(39, 39, 39)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(realTimeCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(textureCheckBox))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                            .addComponent(baseNameTextBox, javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(FolderName, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.DEFAULT_SIZE, 326, Short.MAX_VALUE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(SelectFolder)
                            .addComponent(useFixedIndexCheckBox))))
                .addContainerGap(26, Short.MAX_VALUE))
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
                    .addComponent(FMLVideo)
                    .addComponent(FolderName, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(SelectFolder))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(baseNameTextBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1)
                    .addComponent(useFixedIndexCheckBox))
                .addContainerGap(50, Short.MAX_VALUE))
        );

        baseNameTextBox.getAccessibleContext().setAccessibleName("baseNameTextBox");
        useFixedIndexCheckBox.getAccessibleContext().setAccessibleName("fixed index (0)");

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

    private void FMLVideoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_FMLVideoActionPerformed
        try {
            FMLVideoRecord();
        } catch (IOException ex) {
            Logger.getLogger(Capturecontroller.class.getName()).log(Level.SEVERE, null, ex);
        }
    }//GEN-LAST:event_FMLVideoActionPerformed

    private void SelectFolderActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_SelectFolderActionPerformed
        jFileChooser1.setLocale(Locale.getDefault());
        jFileChooser1.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        jFileChooser1.updateUI();
        if(jFileChooser1.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION){
            File file = jFileChooser1.getSelectedFile();
            this.FolderName.setText(file.getAbsolutePath());
        }
    }//GEN-LAST:event_SelectFolderActionPerformed

    private void useFixedIndexCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_useFixedIndexCheckBoxActionPerformed

    }//GEN-LAST:event_useFixedIndexCheckBoxActionPerformed

    private void baseNameTextBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_baseNameTextBoxActionPerformed
        fileName = baseNameTextBox.getText();
//        imageCaptureOutput = new AWTImageCaptureOutput();
    }//GEN-LAST:event_baseNameTextBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton FMLVideo;
    private javax.swing.JTextField FolderName;
    private javax.swing.JButton SelectFolder;
    private javax.swing.JTextField baseNameTextBox;
    private javax.swing.JLabel jLabel1;
    protected javax.swing.JCheckBox realTimeCheckBox;
    protected javax.swing.JButton screenShotButton;
    protected javax.swing.JCheckBox textureCheckBox;
    private javax.swing.JCheckBox useFixedIndexCheckBox;
    protected javax.swing.JButton videoButton;
    // End of variables declaration//GEN-END:variables

    @Override
    public void performCallback(greta.core.feedbacks.Callback clbck) {
        if (mustcapture) {
            if ((clbck.type().equalsIgnoreCase("dead") || clbck.type().equalsIgnoreCase("end"))) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException ex) {
                    //Logger.getLogger(FMLAttitudeCaptureController.class.getName()).log(Level.SEVERE, null, ex);
                }
                stopVideoCapture();

                iscapturing = false;
            }
        }
    }
}
