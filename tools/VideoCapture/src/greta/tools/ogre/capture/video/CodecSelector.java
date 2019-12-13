/*
 * This file is part of Greta.
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
package greta.tools.ogre.capture.video;

import com.xuggle.xuggler.ICodec;
import com.xuggle.xuggler.IContainerFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Vector;
import javax.swing.ComboBoxModel;

/**
 *
 * @author Andre-Marie
 */
public class CodecSelector extends javax.swing.JFrame {

    private IContainerFormat selectedContainer = null;
    private ICodec selectedVideoCodec = null;
    private ICodec selectedAudioCodec = null;
    private XuggleVideoCapture xvc = null;

    /**
     * Creates new form CodecSelector
     */
    public CodecSelector() {
        initComponents();
        buildContainerComboBox();
        buildVideoCodecComboBox(((ContainerAdapt)(containerComboBox.getSelectedItem())).containerFormat);
        buildAudioCodecComboBox(((ContainerAdapt)(containerComboBox.getSelectedItem())).containerFormat);
        pack();
        fireFormatChanged();
    }
    public void setXuggleVideoCapture(XuggleVideoCapture xvc){
        this.xvc = xvc;
        if(xvc!=null){
            xvc.setWantedFormat(selectedContainer, selectedVideoCodec, selectedAudioCodec);
        }
    }
    private void fireFormatChanged(){
        selectedContainer = ((ContainerAdapt)(containerComboBox.getSelectedItem())).containerFormat;
        selectedVideoCodec = ((CodecAdapt)(videoCodecComboBox.getSelectedItem())).codec;
        selectedAudioCodec = ((CodecAdapt)(audioCodecComboBox.getSelectedItem())).codec;
        if(xvc!=null){
            xvc.setWantedFormat(selectedContainer, selectedVideoCodec, selectedAudioCodec);
        }
    }

    private Comparator compareByToString = new Comparator() {
        @Override
        public int compare(Object o1, Object o2) {
            return o1.toString().compareToIgnoreCase(o2.toString());
        }
    };

    private class ContainerAdapt {

        IContainerFormat containerFormat;

        ContainerAdapt(IContainerFormat containerFormat) {
            this.containerFormat = containerFormat;
        }

        @Override
        public String toString() {
            return containerFormat.getOutputFormatLongName();
        }
    }

    private class CodecAdapt {

        ICodec codec;

        CodecAdapt(ICodec codec) {
            this.codec = codec;
        }

        @Override
        public String toString() {
            return codec.getLongName();
        }
    }

    private boolean isContainerSupportsCodecType(IContainerFormat containerFormat, ICodec.Type type){
        for(ICodec.ID codecID : containerFormat.getOutputCodecsSupported()){
            ICodec c = ICodec.findEncodingCodec(codecID);
            if(c!=null && c.getType().equals(type) && c.canEncode()){
                return true;
            }
        }
        return false;
    }

    private void buildContainerComboBox() {
        Vector<ContainerAdapt> containers = new Vector<ContainerAdapt>();
        ContainerAdapt avi = null;
        for (IContainerFormat c : IContainerFormat.getInstalledOutputFormats()) {
            if (c.isOutput()) {
                if(c.getOutputExtensions()!=null && isContainerSupportsCodecType(c, ICodec.Type.CODEC_TYPE_VIDEO) && isContainerSupportsCodecType(c, ICodec.Type.CODEC_TYPE_AUDIO)){
                    ICodec codecVideo = ICodec.findEncodingCodec(c.getOutputDefaultVideoCodec());
                    if (codecVideo != null && codecVideo.canEncode() && codecVideo.getID() != ICodec.ID.CODEC_ID_PNG) { //to disable mp3 (or an other audio container)
                        ContainerAdapt ca = new ContainerAdapt(c);
                        if (c.getOutputFormatShortName().equalsIgnoreCase("AVI")) {
                            avi = ca;
                        }
                        containers.add(ca);
                    }
                }
            }
        }
        Collections.sort(containers, compareByToString);
        ComboBoxModel containerModel = new javax.swing.DefaultComboBoxModel(containers);
        containerModel.setSelectedItem(avi);
        containerComboBox.setModel(containerModel);
    }

    private void buildVideoCodecComboBox(IContainerFormat containerFormat) {
        Vector<CodecAdapt> videoCodecs = new Vector<CodecAdapt>();
        CodecAdapt preferedCodec = null;
        for (ICodec.ID codecID : containerFormat.getOutputCodecsSupported()) {
            ICodec codec = ICodec.findEncodingCodec(codecID);
            if (codec != null && codec.getType().equals(ICodec.Type.CODEC_TYPE_VIDEO) && codec.canEncode()) {
                CodecAdapt ca = new CodecAdapt(codec);
                if (codecID.equals(containerFormat.getOutputDefaultVideoCodec())) {
                    preferedCodec = ca;
                }
                videoCodecs.add(ca);
            }
        }
        Collections.sort(videoCodecs, compareByToString);
        ComboBoxModel codecModel = new javax.swing.DefaultComboBoxModel(videoCodecs);
        codecModel.setSelectedItem(preferedCodec);
        videoCodecComboBox.setModel(codecModel);
    }


    private void buildAudioCodecComboBox(IContainerFormat containerFormat) {
        Vector<CodecAdapt> audioCodecs = new Vector<CodecAdapt>();
        CodecAdapt preferedCodec = null;
        for (ICodec.ID codecID : containerFormat.getOutputCodecsSupported()) {
            ICodec codec = ICodec.findEncodingCodec(codecID);
            if (codec != null && codec.getType().equals(ICodec.Type.CODEC_TYPE_AUDIO) && codec.canEncode()) {
                CodecAdapt ca = new CodecAdapt(codec);
                if (codecID.equals(containerFormat.getOutputDefaultAudioCodec())) {
                    preferedCodec = ca;
                }
                audioCodecs.add(ca);
            }
        }
        Collections.sort(audioCodecs, compareByToString);
        ComboBoxModel codecModel = new javax.swing.DefaultComboBoxModel(audioCodecs);
        codecModel.setSelectedItem(preferedCodec);
        audioCodecComboBox.setModel(codecModel);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        videoCodecComboBox = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jLabel3 = new javax.swing.JLabel();
        audioCodecComboBox = new javax.swing.JComboBox();
        containerComboBox = new javax.swing.JComboBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jLabel1.setText("Container");

        videoCodecComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                videoCodecComboBoxActionPerformed(evt);
            }
        });

        jLabel2.setText("Video codec");

        jLabel3.setText("Audio codec");

        audioCodecComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                audioCodecComboBoxActionPerformed(evt);
            }
        });

        containerComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                containerComboBoxActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jLabel3)
                    .addComponent(jLabel2)
                    .addComponent(jLabel1))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(videoCodecComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(containerComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(audioCodecComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(containerComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel2)
                    .addComponent(videoCodecComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel3)
                    .addComponent(audioCodecComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void containerComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_containerComboBoxActionPerformed
        buildVideoCodecComboBox(((ContainerAdapt)(containerComboBox.getSelectedItem())).containerFormat);
        buildAudioCodecComboBox(((ContainerAdapt)(containerComboBox.getSelectedItem())).containerFormat);
        fireFormatChanged();
    }//GEN-LAST:event_containerComboBoxActionPerformed

    private void videoCodecComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_videoCodecComboBoxActionPerformed
        fireFormatChanged();
    }//GEN-LAST:event_videoCodecComboBoxActionPerformed

    private void audioCodecComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_audioCodecComboBoxActionPerformed
        fireFormatChanged();
    }//GEN-LAST:event_audioCodecComboBoxActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox audioCodecComboBox;
    private javax.swing.JComboBox containerComboBox;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JComboBox videoCodecComboBox;
    // End of variables declaration//GEN-END:variables
}
