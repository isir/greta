/*
 * Model-driven Turn Management Frame for Greta.
 *
 * Minimal UI that shows:
 *   - Connection status (Python model connected or not)
 *   - Live action log (last action, counters for nods/BCs/HALTs)
 *
 * This Frame is the entry point registered in Modular.xml.
 * It creates the TurnManagementModel backend which handles all socket
 * communication with RealTimeGreta.py.
 */
package greta.auxiliary.TurnManagement;

import greta.core.intentions.IntentionPerformer;
import greta.core.feedbacks.Callback;
import greta.core.feedbacks.FeedbackPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.CharacterManager;
import greta.core.util.id.ID;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;
import java.awt.Color;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.swing.SwingUtilities;

public class TurnManagementFrameModel extends javax.swing.JFrame
        implements FeedbackPerformer, TurnManagementModel.StatusListener {

    private ArrayList<IntentionPerformer> performers = new ArrayList<>();
    private TurnManagementModel turnManagementModel;
    private CharacterManager cm;
    
    
    //SELECT MODE: AUDIO_ONLY VAP or FULL Configuration
    private String batch_main_path = "Common\\Data\\TurnManagement\\run_RealTimeGreta.bat";
    //private String batch_main_path = "Common\\Data\\TurnManagement\\run_RealTimeGreta_audio_only.bat";
    
    //Offline Vap configuration 
    //private String batch_main_path = "Common\\Data\\TurnManagement\\run_HybridRealTime.bat";

    public TurnManagementFrameModel(CharacterManager cm) throws IOException {
        this.cm = cm;
        initComponents();
        System.out.println("[TurnManagementFrameModel] Initializing...");
        turnManagementModel = new TurnManagementModel(this.cm, batch_main_path);
        turnManagementModel.setStatusListener(this);
    }

    @SuppressWarnings("unchecked")
    private void initComponents() {

        statusPanel = new javax.swing.JPanel();
        statusTitleLabel = new javax.swing.JLabel();
        connectionStatusLabel = new javax.swing.JLabel();

        countersPanel = new javax.swing.JPanel();
        lastActionTitleLabel = new javax.swing.JLabel();
        lastActionValueLabel = new javax.swing.JLabel();
        totalTitleLabel = new javax.swing.JLabel();
        totalValueLabel = new javax.swing.JLabel();
        nodsTitleLabel = new javax.swing.JLabel();
        nodsValueLabel = new javax.swing.JLabel();
        bcTitleLabel = new javax.swing.JLabel();
        bcValueLabel = new javax.swing.JLabel();
        haltsTitleLabel = new javax.swing.JLabel();
        haltsValueLabel = new javax.swing.JLabel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("TurnManagement - Model");
        setMinimumSize(new java.awt.Dimension(380, 260));

        // Status panel
        statusPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Connection"));
        statusTitleLabel.setText("Python Model:");
        statusTitleLabel.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));
        connectionStatusLabel.setText("Waiting...");
        connectionStatusLabel.setForeground(new Color(200, 120, 0));

        javax.swing.GroupLayout statusLayout = new javax.swing.GroupLayout(statusPanel);
        statusPanel.setLayout(statusLayout);
        statusLayout.setHorizontalGroup(
            statusLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusTitleLabel)
                .addGap(10)
                .addComponent(connectionStatusLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        statusLayout.setVerticalGroup(
            statusLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(statusTitleLabel)
                .addComponent(connectionStatusLabel)
        );

        // Counters panel
        countersPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Model Actions"));
        lastActionTitleLabel.setText("Last action:");
        lastActionValueLabel.setText("none");
        lastActionValueLabel.setFont(new java.awt.Font("Tahoma", java.awt.Font.BOLD, 11));
        totalTitleLabel.setText("Total received:");
        totalValueLabel.setText("0");
        nodsTitleLabel.setText("Head nods:");
        nodsValueLabel.setText("0");
        bcTitleLabel.setText("Verbal BCs:");
        bcValueLabel.setText("0");
        haltsTitleLabel.setText("HALT interruptions:");
        haltsValueLabel.setText("0");

        javax.swing.GroupLayout countersLayout = new javax.swing.GroupLayout(countersPanel);
        countersPanel.setLayout(countersLayout);
        countersLayout.setHorizontalGroup(
            countersLayout.createSequentialGroup()
                .addContainerGap()
                .addGroup(countersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(countersLayout.createSequentialGroup()
                        .addComponent(lastActionTitleLabel, 130, 130, 130)
                        .addComponent(lastActionValueLabel))
                    .addGroup(countersLayout.createSequentialGroup()
                        .addComponent(totalTitleLabel, 130, 130, 130)
                        .addComponent(totalValueLabel))
                    .addGroup(countersLayout.createSequentialGroup()
                        .addComponent(nodsTitleLabel, 130, 130, 130)
                        .addComponent(nodsValueLabel))
                    .addGroup(countersLayout.createSequentialGroup()
                        .addComponent(bcTitleLabel, 130, 130, 130)
                        .addComponent(bcValueLabel))
                    .addGroup(countersLayout.createSequentialGroup()
                        .addComponent(haltsTitleLabel, 130, 130, 130)
                        .addComponent(haltsValueLabel)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        countersLayout.setVerticalGroup(
            countersLayout.createSequentialGroup()
                .addGap(5)
                .addGroup(countersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(lastActionTitleLabel)
                    .addComponent(lastActionValueLabel))
                .addGap(8)
                .addGroup(countersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(totalTitleLabel)
                    .addComponent(totalValueLabel))
                .addGap(5)
                .addGroup(countersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nodsTitleLabel)
                    .addComponent(nodsValueLabel))
                .addGap(5)
                .addGroup(countersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(bcTitleLabel)
                    .addComponent(bcValueLabel))
                .addGap(5)
                .addGroup(countersLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(haltsTitleLabel)
                    .addComponent(haltsValueLabel))
                .addGap(5)
        );

        // Main layout
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                    .addContainerGap()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                        .addComponent(statusPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(countersPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(statusPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(10)
                .addComponent(countersPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );

        pack();
    }

    // ===== StatusListener callback from TurnManagementModel =====

    @Override
    public void onStatusChanged(String lastAction, int totalActions, int nods, int bcs, int halts, boolean connected) {
        SwingUtilities.invokeLater(() -> {
            if (connected) {
                connectionStatusLabel.setText("Connected");
                connectionStatusLabel.setForeground(new Color(0, 150, 0));
            } else {
                connectionStatusLabel.setText("Disconnected");
                connectionStatusLabel.setForeground(Color.RED);
            }
            lastActionValueLabel.setText(lastAction);
            totalValueLabel.setText(String.valueOf(totalActions));
            nodsValueLabel.setText(String.valueOf(nods));
            bcValueLabel.setText(String.valueOf(bcs));
            haltsValueLabel.setText(String.valueOf(halts));
        });
    }

    // ===== FeedbackPerformer (agent speaking state) =====

    @Override
    public void performFeedback(Callback clbck) {
        if (turnManagementModel != null) {
            turnManagementModel.performFeedback(clbck.type());
        }
    }

    public void performFeedback(List<Callback> list) {
        for (Callback c : list) {
            performFeedback(c);
        }
    }

    // ===== IntentionPerformer management (for Modular connector) =====

    public void addIntentionPerformer(IntentionPerformer performer) {
        performers.add(performer);
        if (turnManagementModel != null) {
            turnManagementModel.getPerformers().add(performer);
        }
    }

    public void removeIntentionPerformer(IntentionPerformer performer) {
        performers.remove(performer);
        if (turnManagementModel != null) {
            turnManagementModel.getPerformers().remove(performer);
        }
    }

    // ===== Getters/setters for Modular parameters =====

    public String getHost() { return "localhost"; }
    public void setHost(String host) { }
    public String getPort() { return "5961"; }
    public void setPort(String port) { }

    // ===== UI variables =====

    private javax.swing.JPanel statusPanel;
    private javax.swing.JLabel statusTitleLabel;
    private javax.swing.JLabel connectionStatusLabel;

    private javax.swing.JPanel countersPanel;
    private javax.swing.JLabel lastActionTitleLabel;
    private javax.swing.JLabel lastActionValueLabel;
    private javax.swing.JLabel totalTitleLabel;
    private javax.swing.JLabel totalValueLabel;
    private javax.swing.JLabel nodsTitleLabel;
    private javax.swing.JLabel nodsValueLabel;
    private javax.swing.JLabel bcTitleLabel;
    private javax.swing.JLabel bcValueLabel;
    private javax.swing.JLabel haltsTitleLabel;
    private javax.swing.JLabel haltsValueLabel;

    @Override
    public void performFeedback(ID id, String string, SpeechSignal ss, TimeMarker tm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void performFeedback(ID id, String string, List<Temporizable> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDetailsOption(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean areDetailedFeedbacks() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDetailsOnFace(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean areDetailsOnFace() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setDetailsOnGestures(boolean bln) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public boolean areDetailsOnGestures() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
