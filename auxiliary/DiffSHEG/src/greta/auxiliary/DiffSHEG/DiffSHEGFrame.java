package greta.auxiliary.DiffSHEG;

import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.feedbacks.Callback;
import greta.core.feedbacks.FeedbackPerformer;
import greta.core.intentions.IntentionPerformer;

import java.io.IOException;
import java.util.List;

/**
 *
 * @author Leroux Paul
 */

public class DiffSHEGFrame extends javax.swing.JFrame implements FeedbackPerformer, BAPFrameEmitter {
    private DiffSHEG diffSHEG;

    public DiffSHEGFrame() throws IOException {
        initComponents();
        System.out.println("greta.auxiliary.DiffSHEGFrame()");
        diffSHEG = new DiffSHEG(null);
    }

    public void addBAPFramePerformer(BAPFramePerformer performer) {
        if (diffSHEG != null) {
            diffSHEG.addBAPFramePerformer(performer);
        }
    }

    public void removeBAPFramePerformer(BAPFramePerformer perfomer) {
        if (diffSHEG != null) {
            diffSHEG.removeBAPFramePerformer(perfomer);
        }
    }

    public void performFeedback(String type) {
        System.out.println("DiffSHEGFrame received feedback: " + type);
        if (type.equals("start") || type.equals("end")) {
            diffSHEG.sendFeedbackToPython(type);
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DiffSHEG Controller");
        pack();
    }
    
    @Override
    public void performFeedback(greta.core.util.id.ID id, String string, greta.core.signals.SpeechSignal ss, greta.core.util.time.TimeMarker tm) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void performFeedback(greta.core.util.id.ID id, String string, List<greta.core.util.time.Temporizable> list) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void performFeedback(Callback clbck) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    @Override
    public void setDetailsOption(boolean bln) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean areDetailedFeedbacks() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return false;
    }

    @Override
    public void setDetailsOnFace(boolean bln) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean areDetailsOnFace() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return false;
    }

    @Override
    public void setDetailsOnGestures(boolean bln) {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean areDetailsOnGestures() {
//        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        return false;
    }

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
