/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package greta.auxiliary.asap;

import static greta.auxiliary.asap.ASAP.LOGGER;
import greta.core.animation.mpeg4.bap.BAPFrame;
import greta.core.animation.mpeg4.bap.BAPFrameEmitter;
import greta.core.animation.mpeg4.bap.BAPFrameEmitterImpl;
import greta.core.animation.mpeg4.bap.BAPFramePerformer;
import greta.core.keyframes.KeyframePerformer;
import greta.core.animation.mpeg4.bap.BAPType;
import greta.core.keyframes.face.AUEmitter;
import greta.core.keyframes.face.AUEmitterImpl;
import greta.core.keyframes.face.AUPerformer;
import greta.core.repositories.AUAPFrame;
import greta.core.signals.SignalEmitter;
import greta.core.signals.SignalPerformer;
import greta.core.util.Constants;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.log.Logs;
import greta.core.util.math.Vec3d;
import greta.core.util.time.Timer;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import greta.core.feedbacks.Callback;
import greta.core.feedbacks.CallbackEmitter;
import greta.core.feedbacks.CallbackPerformer;
import greta.core.behaviorrealizer.Realizer;
import greta.auxiliary.incrementality.IncrementalRealizerV2;
import greta.core.feedbacks.FeedbackPerformer;
import greta.core.signals.SpeechSignal;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.time.Temporizable;
import greta.core.util.time.TimeMarker;


/**
 *
 * @author Michele
 */
public class ASAPFrame extends javax.swing.JFrame implements AUEmitter, BAPFrameEmitter, CharacterDependent, FeedbackPerformer {

    /**
     * Creates new form ASAPFrame
     */
    public CharacterManager cm;
    private Server server;
    private ArrayList<BAPFramePerformer> bap_perfomers = new ArrayList<>();
    private ArrayList<AUPerformer> au_perfomers = new ArrayList<>();
    private static List<String> auFeatureKeys = new ArrayList<>();
    private static List<String> auFeatureMaskKeys = new ArrayList<>();
    BAPFrameEmitterImpl bapFrameEmitterImpl = new BAPFrameEmitterImpl();
    private static List<String> selectedFeatures = null;
    private ArrayList<AUAPFrame> au_frames = new ArrayList<>();
    private ArrayList<BAPFrame> bap_frames = new ArrayList<>();
    
    public static String separator = ", *";

    public int frameNumber = 0;
    public int faceId = 0;

    public double timestamp = 0.0;
    public double confidence = 0.0;

    public boolean success = false;

    public Vec3d gaze0 = new Vec3d();
    public Vec3d gaze1 = new Vec3d();

    public double gazeAngleX;
    public double gazeAngleY;

    public Vec3d headPoseT = new Vec3d();
    public Vec3d headPoseR = new Vec3d();

    public double[] aus = new double[MAX_AUS];
    public double[] auMasks = new double[MAX_AUS];
    public double[] intensity = new double[MAX_AUS];
    public double blink = 0.0;
    public boolean isNull = false;
    public ASAP asap_il;
    boolean isPerforming = false;
    double timeConstantFrame = 0.04;
    //private IncrementalRealizerV2 parent;
    private Realizer parent;
    private Boolean IsRunning = Boolean.FALSE;
    
        private final static int MAX_AUS = 18;
    private final static List<String> expectedPreAUFeatures
            = Arrays.asList(("frame,face_id,timestamp,confidence,success,"
                    + "gaze_0_x,gaze_0_y,gaze_0_z,"
                    + "gaze_1_x,gaze_1_y,gaze_1_z,"
                    + "gaze_angle_x,gaze_angle_y,"
                    + "pose_Tx,pose_Ty,pose_Tz,pose_Rx,pose_Ry,pose_Rz").split(","));

    
        @Override
    public void addAUPerformer(AUPerformer aup) {
        if (aup != null) {
            au_perfomers.add(aup);
        }
    }

    @Override
    public void removeAUPerformer(AUPerformer aup) {
        if (aup != null) {
            au_perfomers.remove(aup);
        }
    }

    @Override
    public void addBAPFramePerformer(BAPFramePerformer bapfp) {
        //System.out.println("addBAPFramePerformer");
        if (bapfp != null) {
            bap_perfomers.add(bapfp);
            bapFrameEmitterImpl.addBAPFramePerformer(bapfp);
        }
    }

    @Override
    public void removeBAPFramePerformer(BAPFramePerformer bapfp) {
        if (bapfp != null) {
            bap_perfomers.remove(bapfp);
            bapFrameEmitterImpl.removeBAPFramePerformer(bapfp);
        }
    }

    @Override
    public void onCharacterChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public CharacterManager getCharacterManager() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void setCharacterManager(CharacterManager cm) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    
        public static class AUFeature {

        public int index;   // integer representing the column index where it's read from
        public int num;     // number representing the AU (ex: 1, for key "AU01_r")

        public AUFeature(int index, int num) {
            this.index = index;
            this.num = num;
            
        }

        public AUFeature(int index, String num) {
            this.index = index;
            this.num = Integer.parseInt(num);
        }
		public String getKey(){
            return String.format("AU%02d", num);
        }										
    }
        
    	public static String getAUFeatureKey(int index){
        return auFeatureKeys.get(index);
    }
    
    public static String getAUFeatureMaskKey(int index){
        return auFeatureMaskKeys.get(index);
    }

    public static int getAUFeatureNumber(int index) {
        String key = getAUFeatureKey(index);
        return auFeaturesMap.get(key).num;
    }

    public static int getAUFeatureMaskNumber(int index) {
        String key = auFeatureMaskKeys.get(index);
        return auFeatureMasksMap.get(key).num;
    }

    
    public static String[] availableFeatures = new String[0];

    public static Map<String, Integer> preAUFeatureKeysMap = new TreeMap<>();
    public static Map<String, AUFeature> auFeaturesMap = new TreeMap<>();
    public static Map<String, AUFeature> auFeatureMasksMap = new TreeMap<>();
    public AUParserFilesReader aup;

    public final static String BLINK_AU = "AU45_r";

    public static int getAUFeaturesCount() {
        return auFeaturesMap.size()>MAX_AUS?MAX_AUS:auFeaturesMap.size();
    }

    public static int getAUFeatureMasksCount() {
        return auFeatureMasksMap.size()>MAX_AUS?MAX_AUS:auFeatureMasksMap.size();
    }

    public ASAPFrame(CharacterManager cm) {
        initComponents();
        server = new Server();
        this.cm = cm;
        cm.add(this);
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jPanel3 = new javax.swing.JPanel();
        jPanel1 = new javax.swing.JPanel();
        head_x = new java.awt.Checkbox();
        head_y = new java.awt.Checkbox();
        head_z = new java.awt.Checkbox();
        jPanel2 = new javax.swing.JPanel();
        au1 = new java.awt.Checkbox();
        au2 = new java.awt.Checkbox();
        au4 = new java.awt.Checkbox();
        au5 = new java.awt.Checkbox();
        au45 = new java.awt.Checkbox();
        bouche = new java.awt.Checkbox();
        gaze = new java.awt.Checkbox();
        au6 = new java.awt.Checkbox();
        au7 = new java.awt.Checkbox();
        au12 = new java.awt.Checkbox();
        jPanel4 = new javax.swing.JPanel();
        jLabel1 = new javax.swing.JLabel();
        port = new java.awt.TextField();
        jLabel2 = new javax.swing.JLabel();
        address = new java.awt.TextField();
        connexion = new javax.swing.JCheckBox();
        perform = new javax.swing.JCheckBox();

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Interactive Loop", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 3, 24))); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Head Rotations", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        head_x.setLabel("X");

        head_y.setLabel("Y");

        head_z.setLabel("Z");

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addGap(21, 21, 21)
                .addGroup(jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(head_x, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(head_z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(head_y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(139, Short.MAX_VALUE))
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(head_x, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(head_y, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(head_z, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "AU", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        au1.setLabel("AU1");

        au2.setLabel("AU2");

        au4.setLabel("AU4");

        au5.setLabel("AU5");

        au45.setLabel("AU45");

        bouche.setLabel("Mouth");

        gaze.setLabel("Gaze");

        au6.setLabel("AU6");

        au7.setLabel("AU7");

        au12.setLabel("AU12");

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(au1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addGap(45, 45, 45))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(au2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(au4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(au6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(66, 66, 66))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addComponent(au5, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)))
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(bouche, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(au45, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addContainerGap(75, Short.MAX_VALUE))
                    .addGroup(jPanel2Layout.createSequentialGroup()
                        .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(au7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(au12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(gaze, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(au1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(au45, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(au2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(bouche, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addComponent(au4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(gaze, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(au5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(au7, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(au6, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(au12, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(39, Short.MAX_VALUE))
        );

        jPanel4.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Socket parameters", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, new java.awt.Font("Tahoma", 1, 18))); // NOI18N

        jLabel1.setText("Port");

        port.setText("50151");
        port.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                portActionPerformed(evt);
            }
        });

        jLabel2.setText("Address");

        address.setText("localhost");
        address.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                addressActionPerformed(evt);
            }
        });

        connexion.setText("Enable");
        connexion.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connexionActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel4Layout = new javax.swing.GroupLayout(jPanel4);
        jPanel4.setLayout(jPanel4Layout);
        jPanel4Layout.setHorizontalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(jLabel1))
                        .addGap(43, 43, 43)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addContainerGap(82, Short.MAX_VALUE))
                            .addGroup(jPanel4Layout.createSequentialGroup()
                                .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(connexion)
                        .addGap(0, 0, Short.MAX_VALUE))))
        );
        jPanel4Layout.setVerticalGroup(
            jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel4Layout.createSequentialGroup()
                .addGap(15, 15, 15)
                .addComponent(connexion)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(jPanel4Layout.createSequentialGroup()
                        .addComponent(jLabel1)
                        .addGap(31, 31, 31)
                        .addGroup(jPanel4Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel2)
                            .addComponent(address, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addComponent(port, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        perform.setText("Perform");
        perform.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                performActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(20, 20, 20)
                .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(18, 18, 18)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(perform)
                    .addGroup(jPanel3Layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(35, 35, 35)
                        .addComponent(jPanel4, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(49, Short.MAX_VALUE))
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addGap(35, 35, 35)
                .addGroup(jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(jPanel4, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(perform))
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(49, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jPanel3, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(26, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void portActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_portActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_portActionPerformed

    private void addressActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_addressActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_addressActionPerformed

    private void connexionActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connexionActionPerformed
        // TODO add your handling code here:

        if (connexion.isSelected()) {
            
            //parent = (IncrementalRealizerV2) cm.getCharacterDependentObject(IncrementalRealizerV2.class);
            parent = (Realizer) cm.getCharacterDependentObject(Realizer.class);

            try {
                server.setAddress(address.getText());
                server.setPort(port.getText());
                System.out.println("greta.auxiliary.asap.ASAPFrame:" + server.port + "   " + server.address);
                server.startConnection();
                Thread r1 = new Thread() {
                    @Override
                    public void run() {
                        try {
                            String line;
                            while(true)
                            {
                                String line2=server.receiveMessage();
                                if(line2!=null && line2.length()>0)
                                //System.out.println("CLIENT:"+line2);
                                loadASAP(line2);
                                }
                               
                        } catch (IOException ex) {
                            Logger.getLogger(ASAPFrame.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                };
                Thread r2 = new Thread() {
                    @Override
                    public void run() {
                        String tmp = null;
                        server.getOut().print(tmp);
                    }
                };
                
                r1.start();
                r2.start();
            } catch (IOException ex) {
                Logger.getLogger(ASAPFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        } else {
            System.out.println("greta.auxiliary.asap.ASAPFrame.connexionActionPerformed() UNCHECKED");
            try {
                // r2.join();
                server.setStop(true);
                server.stopConnection();
            } catch (IOException ex) {
                Logger.getLogger(ASAPFrame.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }//GEN-LAST:event_connexionActionPerformed

    private void performActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_performActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_performActionPerformed

    /**
     * @param args the command line arguments
     */

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private java.awt.TextField address;
    private java.awt.Checkbox au1;
    private java.awt.Checkbox au12;
    private java.awt.Checkbox au2;
    private java.awt.Checkbox au4;
    private java.awt.Checkbox au45;
    private java.awt.Checkbox au5;
    private java.awt.Checkbox au6;
    private java.awt.Checkbox au7;
    private java.awt.Checkbox bouche;
    private javax.swing.JCheckBox connexion;
    private java.awt.Checkbox gaze;
    private java.awt.Checkbox head_x;
    private java.awt.Checkbox head_y;
    private java.awt.Checkbox head_z;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JPanel jPanel4;
    private javax.swing.JCheckBox perform;
    private java.awt.TextField port;
    // End of variables declaration//GEN-END:variables



    
      private String readDataCol(String key, String[] cols, Map<String, Integer> set) {
        if (isFeatureSelected(key)) {
            if (set.containsKey(key)) {
                return cols[set.get(key)];
            } else {
                LOGGER.warning(String.format("Map doesn't contains key: %s", key));
            }
        }
        return "0";
    }
      
    private boolean isFeatureSelected(String feature) {
        return selectedFeatures == null || selectedFeatures.contains(feature);
    }

    private double readAUDataCol(String key, String[] cols, Map<String, AUFeature> set) {
        double d = 0.;
        if (isFeatureSelected(key)) {
            d = Double.parseDouble(cols[set.get(key).index]);
        }
        return d;
    }

    
     public void readDataLine(String data) {

        String[] outputs = data.split(separator);

        frameNumber = Integer.parseInt(readDataCol("frame", outputs, preAUFeatureKeysMap));
        faceId      = Integer.parseInt(readDataCol("face_id", outputs, preAUFeatureKeysMap));
        timestamp   = Double.parseDouble(readDataCol("timestamp", outputs, preAUFeatureKeysMap));
        confidence  = Double.parseDouble(readDataCol("confidence", outputs, preAUFeatureKeysMap));
        success     = Integer.parseInt(readDataCol("success", outputs, preAUFeatureKeysMap)) == 1;

        gaze0.set(Double.parseDouble(readDataCol("gaze_0_x", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("gaze_0_y", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("gaze_0_z", outputs, preAUFeatureKeysMap)));
        gaze1.set(Double.parseDouble(readDataCol("gaze_1_x", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("gaze_1_y", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("gaze_1_z", outputs, preAUFeatureKeysMap)));

        gazeAngleX  = Double.parseDouble(readDataCol("gaze_angle_x", outputs, preAUFeatureKeysMap));
        gazeAngleY  = Double.parseDouble(readDataCol("gaze_angle_y", outputs, preAUFeatureKeysMap));

        headPoseT.set(Double.parseDouble(readDataCol("pose_Tx", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("pose_Ty", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("pose_Tz", outputs, preAUFeatureKeysMap)));
        headPoseR.set(Double.parseDouble(readDataCol("pose_Rx", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("pose_Ry", outputs, preAUFeatureKeysMap)),
                Double.parseDouble(readDataCol("pose_Rz", outputs, preAUFeatureKeysMap)));

        int i = 0;
        for (String key : auFeaturesMap.keySet()) {
            if(i>= MAX_AUS){
                //LOGGER.warning(String.format("AU[%d] %s is ignored, expected %d AUs maximum",i, key,MAX_AUS));                
                break;
            }
            aus[i] = readAUDataCol(key, outputs, auFeaturesMap) / 5.0; // AU**_r are between 0-5.0
            
            if (BLINK_AU.equals(key)) {
                blink = aus[i];
                //System.out.println(" DONNNEES RECU " + blink);
                
            }
            ++i;
        }
        i = 0;
        for (String key : auFeatureMasksMap.keySet()) {
            if(i < MAX_AUS)
                auMasks[i++] = readAUDataCol(key, outputs, auFeatureMasksMap);
        }
        isNull = false;
    }
     
      public void copy(ASAPFrame f) {
        frameNumber    = f.frameNumber;
        faceId         = f.faceId;
        timestamp      = f.timestamp;
        confidence     = f.confidence;
        success        = f.success;
        gaze0          = f.gaze0.clone();
        gaze1          = f.gaze1.clone();
        gazeAngleX     = f.gazeAngleX;
        gazeAngleY     = f.gazeAngleY;
        headPoseT      = f.headPoseT.clone();
        headPoseR      = f.headPoseR.clone();
        isNull      = f.isNull;
        System.arraycopy(f.aus,         0, aus,         0, f.aus.length);
        System.arraycopy(f.auMasks,     0, auMasks,     0, f.auMasks.length);
        System.arraycopy(f.intensity,   0, intensity,   0, f.intensity.length);
    }
      public boolean isPerforming() {
        return perform.isSelected();
    }
      
      public int loadASAP(String line_data) {
          
        //System.out.println("ENTERED HERE !!!!!");
         
        Logs.info(String.format(this.getClass().getSimpleName() + ".loadOpenFace(%s)",line_data));
       // System.out.println("line");
       
        String header_au="timestamp, gaze_0_x, gaze_0_y, gaze_0_z, gaze_1_x, gaze_1_y, gaze_1_z, gaze_angle_x, gaze_angle_y, pose_Tx, pose_Ty, pose_Tz, pose_Rx, pose_Ry, pose_Rz, AU01_r, AU02_r, AU04_r, AU05_r, AU06_r, AU07_r, AU09_r, AU10_r, AU12_r, AU14_r, AU15_r, AU17_r, AU20_r, AU23_r, AU25_r, AU26_r, AU45_r";
        isPerforming = true;
        au_frames.clear();
        bap_frames.clear();

        double prev_rot_X = 0.0;
        double prev_rot_Y = 0.0;
        double prev_rot_Z = 0.0;

        // open the file
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        
        double min_time = Double.MAX_VALUE;
        double max_time = 0.0;
        int cpt = 1;
            int[] au_correspondance = {1, 2, 4, 5, 6, 7, 9, 10, 12, 14, 15, 17, 20, 23, 25, 26, 45}; // 24
            double[] prev_value_au = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};

            double prev_gaze_x = 0.0;
            double prev_gaze_y = 0.0;

            double prev_blink = 0.0;
            int col_blink = 412;
            double alpha = 0.75;//1.0;

            

            List<Double> list_val = new ArrayList<>();
            //lecture header
                String[] header = header_au.split(cvsSplitBy);

                for (int h = 0; h < header.length; h++) {
                    String value = header[h];
                    value = value.replace(" ", "");

                    if ("AU45_r".equals(value)) {
                        col_blink = h;
                    }

                    //System.out.println("header[" + h + "] = " + value);
                }

                    //check if read all the frames
                    //System.out.println(iii);
                    //iii++;
                    String[] values = line_data.split(cvsSplitBy);
                    double time;
                    String val = values[0];
                    //System.out.println("time: " + val);
                    time = Double.parseDouble(val);

                    list_val.add(time);
                    

                    if (timeConstantFrame != 0) {
                        if (time > max_time) {
                            max_time = time;
                        }
                        if (time < min_time) {
                            min_time = time;
                        }
                        AUAPFrame au_frame = new AUAPFrame();
                        au_frame.setFrameNumber((int) (time * timeConstantFrame) + 1);

                        // be carefull: if the format of the excel file will change, also this parameter should change
                        int indice_intensity = 15;

                        for (int au = 0; au < au_correspondance.length; au++) {
                            if (au_correspondance[au] == 1 || au_correspondance[au] == 2 || au_correspondance[au] == 4 || au_correspondance[au] == 5 || au_correspondance[au] == 6 || au_correspondance[au] == 7 || au_correspondance[au] == 12 || au_correspondance[au] == 10
                                    || au_correspondance[au] == 14 || au_correspondance[au] == 15 || au_correspondance[au] == 17 || au_correspondance[au] == 20 || au_correspondance[au] == 23 || au_correspondance[au] == 25 || au_correspondance[au] == 26) {
                                String value = values[au + indice_intensity];
                                if (isNumeric(value)) {
                                    double intensity = alpha * (Double.parseDouble(value) / 3.5) + (1 - alpha) * prev_value_au[au];
                                    //System.out.println("AU["+au_correspondance[au]+"]: "+intensity+ " cpt "+ cpt);
                                    au_frame.setAUAPboth(au_correspondance[au], intensity);
                                    prev_value_au[au] = intensity;
                                }
                            }
                        }
                        cpt++;

                        //gaze
                        double gaze_x = alpha * (0.5 * (Double.parseDouble(values[4]) + Double.parseDouble(values[7]))) + (1 - alpha) * prev_gaze_x;
                        double gaze_y = alpha * (0.5 * (Double.parseDouble(values[5]) + Double.parseDouble(values[8]))) + (1 - alpha) * prev_gaze_y;
                        if (gaze_x < 0) {
                            au_frame.setAUAPboth(62, gaze_x);
                        } else {
                            au_frame.setAUAPboth(61, gaze_x);
                        }

                        if (gaze_y < 0) {
                            au_frame.setAUAPboth(64, gaze_y);
                        } else {
                            au_frame.setAUAPboth(63, gaze_y);
                        }
                        prev_gaze_x = gaze_x;
                        prev_gaze_y = gaze_y;

                        //blink
                        // double blink = alpha*(Double.parseDouble(values[col_blink].replace(',', '.'))/5.0)+(1-alpha)*prev_blink;
                        double blink = Double.parseDouble(values[col_blink].replace(',', '.')) / 3.0;
                        au_frame.setAUAPboth(43, blink);
                        prev_blink = blink;

                        au_frames.add(au_frame);
                        BAPFrame hmFrame = new BAPFrame();
                        hmFrame.setFrameNumber((int) (time * timeConstantFrame) + 1);

                        double rot_X_rad = -1.0 * Double.parseDouble(values[12]);

                        double rot_Y_rad = -1.0 * Double.parseDouble(values[13]);

                        double rot_Z_rad = -1.0 * Double.parseDouble(values[14]);

                        double rot_X_deg = -rot_X_rad * 180 / Math.PI;
                        double rot_Y_deg = rot_Y_rad * 180 / Math.PI;
                        double rot_Z_deg = rot_Z_rad * 180 / Math.PI;

                        rot_X_deg = alpha * (rot_X_deg) + (1 - alpha) * prev_rot_X;
                        rot_Y_deg = alpha * (rot_Y_deg) + (1 - alpha) * prev_rot_Y;
                        rot_Z_deg = alpha * (rot_Z_deg) + (1 - alpha) * prev_rot_Z;

                        hmFrame.setDegreeValue(BAPType.vc3_tilt, rot_X_deg);
                        hmFrame.setDegreeValue(BAPType.vc3_torsion, rot_Y_deg);
                        hmFrame.setDegreeValue(BAPType.vc3_roll, rot_Z_deg);

                        //System.out.println("BAP["+time+"]: ["+rot_X_rad+"; "+rot_Y_rad+"; "+rot_Z_rad+"]");
                        prev_rot_X = rot_X_deg;
                        prev_rot_Y = rot_Y_deg;
                        prev_rot_Z = rot_Z_deg;

                        bap_frames.add(hmFrame);
                    }
                
            
            //System.out.println("Program fini finifffffffffffffffffffffffffffffff ");

            
        

        int length = (int) ((int) max_time - min_time);

        send(length);

        //ID id = IDProvider.createID("From_AU_Parser");
        return length;
    }

    //Format based on https://github.com/TadasBaltrusaitis/OpenFace
    public int loadOpenFacesave(String csvFile) {
        Logs.info(String.format(this.getClass().getSimpleName() + ".loadOpenFace(%s)", csvFile));

        isPerforming = true;
        au_frames.clear();
        bap_frames.clear();

        // open the file
        BufferedReader br = null;
        String line = "";
        String cvsSplitBy = ",";

        double min_time = Double.MAX_VALUE;
        double max_time = 0.0;

        try {
            br = new BufferedReader(new FileReader(csvFile));
            //int [] au_correspondance = {0, 1, 2, 4,5,6,9,12,15,17,20,25,26};
            //double [] prev_value_au = {0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0};
            //String[] header_string = { "timestamp", " AU01_r", " AU02_r", " AU04_r", " AU05_r", " AU06_r", " AU07_r", " AU09_r", " AU10_r", " AU12_r", " AU14_r", " AU15_r", " AU17_r", " AU20_r", " AU23_r", " AU25_r", " AU26_r", " AU45_r"};
            String[] header_string = {"timestamp", "AU01_r", "AU02_r", "AU04_r", "AU05_r", "AU06_r", "AU07_r", "AU09_r", "AU10_r", "AU12_r", "AU14_r", "AU15_r", "AU17_r", "AU20_r", "AU23_r", "AU25_r", "AU26_r"};
            // String[] header_string = {"time",	"1",	"2",	"4",	"5",	"6",	"9",	"12",	"15",	"17",	"20",	"25",	"26"};
            List<String> header_list = Arrays.asList(header_string);
            int[] au_to_col = new int[header_string.length];
            int[] au_correspondance = new int[header_string.length - 1];
            double[] prev_value_au = new double[header_string.length];

            double prev_gaze_x = 0.0;
            double prev_gaze_y = 0.0;

            double prev_blink = 0.0;
            int col_blink = 412;

            for (int autc = 0; autc < au_to_col.length; autc++) {
                au_to_col[autc] = -1;
                prev_value_au[autc] = 0.0;
            }

            for (int auc = 0; auc < au_correspondance.length; auc++) {
                String val = header_string[auc + 1].replace(" AU0", "");
                val = val.replace("AU", "");
                val = val.replace("_r", "");
                //System.out.println("auc="+auc+" -> "+val);
                au_correspondance[auc] = Integer.parseInt(val);
            }

            double alpha = 0.75;//1.0;

            //lecture header
            if ((line = br.readLine()) != null) {
                String[] header = line.split(cvsSplitBy);

                for (int h = 0; h < header.length; h++) {
                    String value = header[h];
                    value = value.replace(" ", "");
                    int index = header_list.indexOf(value);
                    if (index != -1 & index < au_to_col.length) {
                        au_to_col[index] = h;
                    }

                    if ("AU45_r".equals(value)) {
                        col_blink = h;
                    }

                    Logs.debug("header[" + h + "] = " + value);
                }

                while ((line = br.readLine()) != null) {
                    String[] values = line.split(cvsSplitBy);

                    double time;
                    //String val = values[1].replace(',', '.');
                    String val = values[1];
                    Logs.debug("time: " + val);

                    time = Double.parseDouble(val);

                    if (time > max_time) {
                        max_time = time;
                    }
                    if (time < min_time) {
                        min_time = time;
                    }
                    AUAPFrame au_frame = new AUAPFrame();
                    au_frame.setFrameNumber((int) (time * Constants.FRAME_PER_SECOND) + 1);

                    for (int au = 1; au < au_to_col.length; au++) {
                        String value = values[au_to_col[au]];
                        if (isNumeric(value)) {
                            double intensity = alpha * (Double.parseDouble(value) / 5.0) + (1 - alpha) * prev_value_au[au];
                            //       System.out.println("AU["+au_correspondance[au]+"]: "+intensity);
                            au_frame.setAUAPboth(au_correspondance[au - 1], intensity);
                            prev_value_au[au] = intensity;
                        }
                    }

                    //gaze
                    double gaze_x = alpha * (0.5 * (Double.parseDouble(values[4]) + Double.parseDouble(values[7]))) + (1 - alpha) * prev_gaze_x;
                    double gaze_y = alpha * (0.5 * (Double.parseDouble(values[5]) + Double.parseDouble(values[8]))) + (1 - alpha) * prev_gaze_y;
                    if (gaze_x < 0) {
                        au_frame.setAUAPboth(62, gaze_x);
                    } else {
                        au_frame.setAUAPboth(61, gaze_x);
                    }

                    if (gaze_y < 0) {
                        au_frame.setAUAPboth(64, gaze_y);
                    } else {
                        au_frame.setAUAPboth(63, gaze_y);
                    }
                    prev_gaze_x = gaze_x;
                    prev_gaze_y = gaze_y;

                    //blink
                    double blink = alpha * (Double.parseDouble(values[col_blink].replace(',', '.')) / 5.0) + (1 - alpha) * prev_blink;
                    au_frame.setAUAPboth(43, blink);
                    prev_blink = blink;

                    au_frames.add(au_frame);

                    BAPFrame hmFrame = new BAPFrame();
                    hmFrame.setFrameNumber((int) (time * Constants.FRAME_PER_SECOND) + 1);

                    double rot_X_deg = 0.0;//alpha*Math.toDegrees(rot_X_rad)+(1-alpha)*prev_rot_X;

                    double rot_Y_deg = 0.0;//alpha*Math.toDegrees(rot_Y_rad)+(1-alpha)*prev_rot_Y;

                    //double rot_Z_rad = 1.0*Double.parseDouble(values[13]);
                    double rot_Z_deg = 0.0;//alpha*Math.toDegrees(rot_Z_rad)+(1-alpha)*prev_rot_Z;

                    hmFrame.setDegreeValue(BAPType.vc3_roll, rot_X_deg);
                    hmFrame.setDegreeValue(BAPType.vc3_torsion, rot_Y_deg);
                    hmFrame.setDegreeValue(BAPType.vc3_tilt, rot_Z_deg);

                    bap_frames.add(hmFrame);
                }

            }

        } catch (IOException ex) {
            Logs.error(ex.getMessage());
            LOGGER.log(Level.SEVERE, ex.toString(), ex);
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException ex) {
                    Logs.error(ex.getMessage());
                    LOGGER.log(Level.SEVERE, ex.toString(), ex);
                }
            }
        }

        int length = (int) ((int) max_time - min_time);
        send(length);

        
        
        return length;
    }
    
    private void send(int length) {
        ID id = IDProvider.createID("From_AU_Parser");

        int timer = (int) (Timer.getTime() * Constants.FRAME_PER_SECOND);
        Logs.debug("Send");
        double absoluteStartTime = greta.core.util.time.Timer.getTime();

        for (AUAPFrame frame : au_frames) {
            int time_frame = timer + frame.getFrameNumber();
            //System.out.println("AUAPFrame: "+time_frame);
            frame.setFrameNumber(time_frame);
            for (AUPerformer performer : au_perfomers) {
                // CHANGES OF NAZEH HERE ADD.ANIMATION into absolute time AFTER ADDING THE REALIZER VARIABLE THAT IS INITIALISED WHEN CALLING THE FML FILE READER to allow both AUperform and Fml Reading and synchronize them through absolute time
                if (this.parent != null && this.IsRunning == Boolean.TRUE){
                    performer.performAUAPFrame(frame, id);
                    this.parent.addAnimation(id, absoluteStartTime, time_frame);
                    //System.out.println(" LE ADD ANIMATION EST PASSEee 2");
                }
                else{
                performer.performAUAPFrame(frame, id);
                }
                
                // Parent 
                
            }
        }
        //performBAPFrames(bap_frames, id);
        ArrayList<BAPFrame> curr_bap_frames = new ArrayList<BAPFrame>();
        for (BAPFrame frame : bap_frames) {
            int time_frame = timer + frame.getFrameNumber();
            frame.setFrameNumber(time_frame);
            curr_bap_frames.add(frame);

        }

        int p = 0;
        for (BAPFramePerformer performer : bap_perfomers) {
            //System.out.println("[INFO]:greta.auxiliary.asap.AUParserFilesReader.send()");
            performer.performBAPFrames(curr_bap_frames, id);
        }
        bapFrameEmitterImpl.sendBAPFrames(id, curr_bap_frames);

        //System.out.println("--Post BAP");

        isPerforming = true;
    }
    
        public boolean isNumeric(String s) {
        return s.matches("[-+ ]?\\d*\\.?\\d+");
    }
        
           public void performFeedback(ID AnimId, String type, SpeechSignal speechSignal, TimeMarker tm){
    performFeedback(type);
   };

   public void performFeedback(ID AnimId, String type, List<Temporizable> listTmp){
       performFeedback(type);
   };

   public void performFeedback(Callback callback){
   performFeedback(callback.type());
};
   public void setDetailsOption(boolean detailed){
       
   };

   public boolean areDetailedFeedbacks(){
     return true  ;
   };

   public void setDetailsOnFace(boolean detailsOnFace){
     
   };

   public boolean areDetailsOnFace(){
       return false;  
   };

   public void setDetailsOnGestures(boolean detailsOnGestures){
       
   };

   public boolean areDetailsOnGestures(){
       return false;  
   };
   
   public void performFeedback(String type){
       
        if (type == "end"){
            this.IsRunning = Boolean.FALSE;
            
            }
        if (type == "start"){
            this.IsRunning = Boolean.TRUE;
                 }

    }

}
