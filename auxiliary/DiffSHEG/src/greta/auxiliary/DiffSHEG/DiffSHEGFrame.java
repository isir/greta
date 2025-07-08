package greta.auxiliary.DiffSHEG;

import greta.core.feedbacks.Callback;
import greta.core.feedbacks.FeedbackPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.id.ID;
import greta.core.util.time.TimeMarker;

import java.io.IOException;
import java.util.List;
import greta.core.util.time.Temporizable;

public class DiffSHEGFrame extends javax.swing.JFrame implements FeedbackPerformer {
    private DiffSHEG diffSHEG;

    public DiffSHEGFrame() throws IOException {
        initComponents();
        System.out.println("greta.auxiliary.DiffSHEGFrame()");
        diffSHEG = new DiffSHEG(null);
    }

    @Override
    public void performFeedback(String type) {
        System.out.println("DiffSHEGFrame received feedback: " + type);
        if (type.equals("start") || type.equals("end")) {
            diffSHEG.sendFeedbackToPython(type);
        }
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

    public void performFeedback(String str) {
        turnManager.performFeedback(str);
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

    public void addIntentionPerformer(IntentionPerformer performer) {
        performers = turnManager.getPerformers();
        performers.add(performer);
    }
    
    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
    }
    
}
