package greta.auxiliary.Microsaccade;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
import greta.core.intentions.FMLTranslator;
import greta.core.signals.GazeSignal;
import greta.core.signals.Signal;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.CharacterManager;
import greta.core.util.Mode;
import greta.core.util.enums.CompositionType;
import greta.core.util.enums.GazeDirection;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Admin
 */
public class MicrosaccadeFrame extends javax.swing.JFrame implements SignalEmitter {
    
    private List<SignalPerformer> signalPerformers;
    private CharacterManager characterManager;
    private Thread microsaccadeThread;
    
    private volatile boolean isEnabled = false;
    
    private List<GazeDirection> gazeDirections;
    
    private Random random;

    /**
     * Creates new form GazeNoiseMicrosaccadFrame
     */
    public MicrosaccadeFrame(CharacterManager cm) {
        
        initComponents();
        
        characterManager = cm;
        signalPerformers = new ArrayList<SignalPerformer>();

        gazeDirections = new ArrayList<GazeDirection>();
        gazeDirections.add(GazeDirection.UP);
        gazeDirections.add(GazeDirection.DOWN);
        gazeDirections.add(GazeDirection.RIGHT);
        gazeDirections.add(GazeDirection.LEFT);
        gazeDirections.add(GazeDirection.UPRIGHT);
        gazeDirections.add(GazeDirection.UPLEFT);
        gazeDirections.add(GazeDirection.DOWNRIGHT);
        gazeDirections.add(GazeDirection.DOWNLEFT);
        
        random = new Random();
        
        startMicrosaccadeThread();
        
    }
    
    private void startMicrosaccadeThread() {
        
        microsaccadeThread = new Thread() {
            
            @Override
            public void run() {
                
                double angle = 0.0;
                boolean increment = true;
                
                while (true) {
                    if (isEnabled) {

                        if (increment) {
                            angle += 1.0;
                        } else {
                            angle -= 1.0;
                        }

                        if (angle >= 3.0) {
                            increment = false;
                            continue;
                        }
                        if (angle <= 0.0) {
                            increment = true;
                            continue;
                        }

                        GazeSignal gaze = new GazeSignal("Microsaccard");
                        
                        //gaze.setOffsetDirection(GazeDirection.RIGHT);
                        gaze.setOffsetDirection(getRandomDirection());
                        
                        gaze.setOffsetAngle(angle);
                        gaze.setGazeShift(true);
                        
                        List<Signal> signals = new ArrayList<Signal> ();
                        signals.add((Signal) gaze);
                        
                        ID id = IDProvider.createID("Microsaccard");
                        
                        Mode mode = FMLTranslator.getDefaultFMLMode();
                        mode.setCompositionType(CompositionType.blend);

                        System.out.format("greta.auxiliary.Microsaccade.MicrosaccadeFrame.startMicrosaccardThread(): direction - %s, angle - %.2f %n",
                                gaze.getOffsetDirection().toString(), gaze.getOffsetAngle());
                        
                        for (SignalPerformer performer:signalPerformers) {
                            performer.performSignals(signals, id, mode);
                        }
                        
                        try {
//                            int duration = random.nextInt(1000 - 300) + 300;
                            int duration = random.nextInt(500 - 300) + 300;
                            Thread.sleep(duration);
                        } catch (InterruptedException ex) {
                            Logger.getLogger(MicrosaccadeFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                        
                    }
                }
                
            }
            
        };
        microsaccadeThread.start();
        
    }
    
    private GazeDirection getRandomDirection() {
        GazeDirection direction = gazeDirections.get(random.nextInt(gazeDirections.size()));
        return direction;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jCheckBox1 = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jCheckBox1.setText("enable");
        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addContainerGap(337, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jCheckBox1)
                .addContainerGap(270, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        // TODO add your handling code here:
        System.out.format("greta.auxiliary.Microsaccade.MicrosaccadeFrame: isEnabled from " + isEnabled);
        if (isEnabled) {
            isEnabled = false;
        } else {
            isEnabled = true;            
        }
        System.out.format(" to " + isEnabled + "%n");
        
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JCheckBox jCheckBox1;
    // End of variables declaration//GEN-END:variables

    @Override
    public void addSignalPerformer(SignalPerformer performer) {
        if (performer != null) {
            signalPerformers.add(performer);
        }
    }

    @Override
    public void removeSignalPerformer(SignalPerformer performer) {
        if (performer != null) {
            signalPerformers.remove(performer);
        }
    }
}
