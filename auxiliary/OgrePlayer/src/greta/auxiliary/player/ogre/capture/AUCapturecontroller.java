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
import java.awt.Cursor;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *
 * @author Andre-Marie Pez
 */
public class AUCapturecontroller extends javax.swing.JFrame implements CaptureListener {

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

    /**
     * Creates new form Capturecontroller
     */
    public AUCapturecontroller() {
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
        AUCapturecontroller.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        ensureCapturable(currentVideoCapturer);
        currentVideoCapturer.startCapture(fileName);
    }

    public void stopVideoCapture() {
        AUCapturecontroller.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
        currentVideoCapturer.stopCapture();
    }

    private void ensureCapturable(Capturer capturer) {
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

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(screenShotButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(videoButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(realTimeCheckBox)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(textureCheckBox)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(screenShotButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(videoButton)
                    .addComponent(realTimeCheckBox)
                    .addComponent(textureCheckBox))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
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
    // Variables declaration - do not modify//GEN-BEGIN:variables
    protected javax.swing.JCheckBox realTimeCheckBox;
    protected javax.swing.JButton screenShotButton;
    protected javax.swing.JCheckBox textureCheckBox;
    protected javax.swing.JButton videoButton;
    // End of variables declaration//GEN-END:variables
}
