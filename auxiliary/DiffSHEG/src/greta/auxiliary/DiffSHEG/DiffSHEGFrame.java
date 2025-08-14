package greta.auxiliary.DiffSHEG;

import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.util.CharacterManager;
import greta.core.util.CharacterDependent;

import java.io.IOException;

/**
 *
 * @author Leroux Paul
 */

public class DiffSHEGFrame extends javax.swing.JFrame implements BAPFrameEmitter, CharacterDependent {
    private DiffSHEG diffSHEG;

    private CharacterManager cm;

    public DiffSHEGFrame (CharacterManager cm) throws IOException {
        initComponents();
        this.cm = cm;
        cm.add(this);
        diffSHEG = new DiffSHEG(cm);
        System.out.println("[Greta DiffSHEG]greta.auxiliary.DiffSHEGFrame()");
    }

    public void addBAPFramePerformer(BAPFramePerformer performer) {
        System.out.println("[Greta DiffSHEG] adding BAP Frame Performer");
        if (diffSHEG != null) {
            diffSHEG.addBAPFramePerformer(performer);
        }
    }

    public void removeBAPFramePerformer(BAPFramePerformer perfomer) {
        if (diffSHEG != null) {
            diffSHEG.removeBAPFramePerformer(perfomer);
        }
    }

    private void initComponents() {
        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("DiffSHEG Controller");
        pack();
    }

    @Override
    public void setCharacterManager(CharacterManager cm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }   
    
    @Override
    public CharacterManager getCharacterManager() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onCharacterChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JTextField jTextField1;
    // End of variables declaration//GEN-END:variables
}
