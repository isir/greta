/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package greta.core.behaviorrealizer.gui;

import greta.core.util.CharacterManager;
import greta.core.behaviorrealizer.ClientPhoneme;
import greta.core.behaviorrealizer.Realizer;
import greta.core.keyframes.Keyframe;
import greta.core.keyframes.KeyframeEmitter;
import greta.core.keyframes.KeyframePerformer;
import greta.core.keyframes.PhonemSequence;
import greta.core.util.id.ID;
import greta.core.util.id.IDProvider;
import greta.core.util.Mode;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import java.util.ArrayList;
import greta.core.keyframes.Keyframe;
import javax.swing.SwingUtilities;
import java.net.ServerSocket;
import java.net.Socket;
import java.io.DataOutputStream;
import java.io.DataInputStream;
import java.io.IOException;

/**
 *
 * @author Donatella Simonetti
 */
public class RealizerGui extends JFrame{

    private CharacterManager cm;
    private Realizer parent;
    // Variables declaration
    private JLabel AULibrary;
    private JLabel FaceLibrary;
    private JLabel GestureLibrary;
    private JLabel HeadLibrary;
    private JLabel Libraries;
    private JLabel ShoulderLibrary;
    private JLabel TorsoLibrary;
    private JLabel SendPhoneme;
    
    private JButton UpdateAU;
    private JButton UpdateAll;
    private JButton UpdateFace;
    private JButton UpdateGestures;
    private JButton UpdateHand;
    private JButton UpdateHead;
    private JButton UpdateSh;
    private JButton UpdateTorso;
    private javax.swing.JToggleButton togglePhonemeServer;
    private boolean Phoneme_send = false ;
    
    private JLabel jLabel1;
    // End of variables declaration

    public RealizerGui() {

        AULibrary = new javax.swing.JLabel();
        UpdateAU = new javax.swing.JButton();
        FaceLibrary = new javax.swing.JLabel();
        UpdateFace = new javax.swing.JButton();
        GestureLibrary = new javax.swing.JLabel();
        UpdateGestures = new javax.swing.JButton();
        Libraries = new javax.swing.JLabel();
        HeadLibrary = new javax.swing.JLabel();
        UpdateHead = new javax.swing.JButton();
        ShoulderLibrary = new javax.swing.JLabel();
        UpdateSh = new javax.swing.JButton();
        TorsoLibrary = new javax.swing.JLabel();
        UpdateTorso = new javax.swing.JButton();
        jLabel1 = new javax.swing.JLabel();
        UpdateHand = new javax.swing.JButton();
        UpdateAll = new javax.swing.JButton();
        togglePhonemeServer = new javax.swing.JToggleButton();

        setMaximumSize(new java.awt.Dimension(213, 392));

        AULibrary.setText("AU Library");

        UpdateAU.setText("Update");
        UpdateAU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //UpdateAUActionPerformed(evt);
            }
        });
        
        
        //SendPhoneme.setText("Send Phoneme");
        
        togglePhonemeServer.setText("Enable Phoneme Server");
            togglePhonemeServer.addActionListener(new java.awt.event.ActionListener() {
                    public void actionPerformed(java.awt.event.ActionEvent evt) {
                    togglePhonemeServerActionPerformed(evt);
    }
});

        FaceLibrary.setText("Face Library");

        UpdateFace.setText("Update");
        UpdateFace.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateFaceActionPerformed(evt);
            }
        });

        GestureLibrary.setText("Gesture Library");

        UpdateGestures.setText("Update");
        UpdateGestures.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateGesturesActionPerformed(evt);
            }
        });

        Libraries.setText("LIBRARIES");

        HeadLibrary.setText("Head Library");

        UpdateHead.setText("Update");
        UpdateHead.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateHeadActionPerformed(evt);
            }
        });

        ShoulderLibrary.setText("Shoulders Library");

        UpdateSh.setText("Update");
        UpdateSh.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //UpdateShActionPerformed(evt);
            }
        });

        TorsoLibrary.setText("Torso Library");

        UpdateTorso.setText("Update");
        UpdateTorso.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateTorsoActionPerformed(evt);
            }
        });

        jLabel1.setText("Hand Shape Library");

        UpdateHand.setText("Update");
        UpdateHand.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateHandActionPerformed(evt);
            }
        });

        UpdateAll.setText("Update All");
        UpdateAll.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateAllActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    //.addComponent(AULibrary, javax.swing.GroupLayout.PREFERRED_SIZE, 76, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(FaceLibrary)
                    .addComponent(GestureLibrary)
                    .addComponent(HeadLibrary)
                    //.addComponent(ShoulderLibrary)
                    .addComponent(TorsoLibrary))
                    .addComponent(togglePhonemeServer)
                    //.addComponent(SendPhoneme)
                    //.addComponent(jLabel1))
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            //.addComponent(UpdateHand)
                            .addComponent(UpdateTorso))
                            //.addComponent(togglePhonemeServer)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            //.addComponent(UpdateAU)
                            .addComponent(UpdateFace)
                            .addComponent(UpdateHead)
                            //.addComponent(UpdateSh)
                            .addComponent(UpdateGestures))
                        .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))))
            .addGroup(layout.createSequentialGroup()
                .addGap(61, 61, 61)
                .addComponent(Libraries, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(0, 0, Short.MAX_VALUE))
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(UpdateAll, javax.swing.GroupLayout.PREFERRED_SIZE, 134, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(28, 28, 28))
        );
        
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(Libraries)
                //.addGap(26, 26, 26)
                //.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    //.addComponent(UpdateAU, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    //.addGroup(layout.createSequentialGroup()
                        //.addComponent(AULibrary)
                        //.addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(FaceLibrary)
                    .addComponent(UpdateFace, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(UpdateGestures, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(GestureLibrary))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(HeadLibrary)
                    .addComponent(UpdateHead, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                //.addGap(18, 18, 18)
                //.addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    //.addComponent(UpdateSh, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    //.addGroup(layout.createSequentialGroup()
                        //.addComponent(ShoulderLibrary)
                        //.addGap(0, 0, Short.MAX_VALUE)))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(TorsoLibrary)
                    .addComponent(UpdateTorso)
                    .addComponent(togglePhonemeServer))
                .addGap(18, 18, 18)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING))
                    //.addComponent(jLabel1)
                    //.addComponent(UpdateHand))
                .addGap(16, 16, 16)
                .addComponent(UpdateAll, javax.swing.GroupLayout.PREFERRED_SIZE, 37, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        pack();
    }

    public void UpdateLibraries(Realizer parent) {
        this.parent = parent;
    }

    /*private void UpdateAUActionPerformed(java.awt.event.ActionEvent evt) {
        this.parent.UpdateAULibrary();
    }*/

    private void UpdateFaceActionPerformed(java.awt.event.ActionEvent evt) {
        this.parent.UpdateFaceLibrary();
    }

    private void UpdateGesturesActionPerformed(java.awt.event.ActionEvent evt) {
        this.parent.UpdateGestureLibrary();
    }

    private void UpdateHeadActionPerformed(java.awt.event.ActionEvent evt) {
        this.parent.UpdateHeadLibrary();
    }

    /*private void UpdateShActionPerformed(java.awt.event.ActionEvent evt) {
        this.parent.UpdateShoulderLibrary();
    }*/

    private void UpdateTorsoActionPerformed(java.awt.event.ActionEvent evt) {
        this.parent.UpdateTorsoLibrary();
    }

    private void UpdateHandActionPerformed(java.awt.event.ActionEvent evt) {
        // TODO add your handling code here:
    }

    private void UpdateAllActionPerformed(java.awt.event.ActionEvent evt) {
        //this.parent.UpdateAULibrary();
        this.parent.UpdateFaceLibrary();
        this.parent.UpdateGestureLibrary();
        this.parent.UpdateHeadLibrary();
        //this.parent.UpdateShoulderLibrary();
        this.parent.UpdateTorsoLibrary();
        this.parent.UpdateHandLibrary();
    }
    
    private void togglePhonemeServerActionPerformed(java.awt.event.ActionEvent evt) {   
        
    if (togglePhonemeServer.isSelected()) {        
       

            new Thread(() -> {
            // Initial delay to avoid checking immediately.
            try { Thread.sleep(500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
            //this.parent.characterManager.Phoneme_manager(); is the cm boolean for phoneme sending
           /* while (!this.Phoneme_send) {
                System.out.println("Waiting for launch");
                try { Thread.sleep(1000); } catch (InterruptedException e) { Thread.currentThread().interrupt(); break; }
            }
            
            if (this.Phoneme_send) {
                // Runs on the Swing event dispatch thread to ensure thread-safety for UI updates.
                SwingUtilities.invokeLater(this::startPhonemeServer);
            }*/
           startClient();
           SwingUtilities.invokeLater(this::startPhonemeServer);
        }).start();
    } else {
        // Stop the phoneme server
        System.out.println("Phoneme server stopped.");
        // Call stop method on your phoneme client instance
    }
}

    private void startPhonemeServer() {
        
        System.out.println("Phoneme server started.");
        List<Keyframe> keyframes = new ArrayList<Keyframe>();
        ID id = IDProvider.createID("UniqueId");
        Mode mode = new Mode("Mode@1835238");
        double lastKeyFrameTime = 0.0;
        double absoluteStartTime = greta.core.util.time.Timer.getTime();
        // Start the phoneme server
        // Set the shared state based on the toggle button
        ClientPhoneme client = new ClientPhoneme();
        client.clearLastReceivedSequence(); // Ensure no previous data is present
        Thread clientThread = new Thread(client::startClient);
        clientThread.start();
        //System.out.println("curPhoneme is : " +visemes.get(i).curPho.getPhonemeType() );

        try {
            clientThread.join(); // Wait for the client thread to finish
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return;
        }

        PhonemSequence receivedSequence = client.getLastReceivedSequence();
        if (receivedSequence != null) {
            // If a PhonemeSequence was received, add it to the list of keyframes to be processed.
            keyframes.add(receivedSequence);
            //code ajouté pour syncrhoniser les animation avec greta absolute time
            for (Keyframe keyframe : keyframes) {
            keyframe.setOnset(keyframe.getOnset() + absoluteStartTime);
            keyframe.setOffset(keyframe.getOffset() + absoluteStartTime);
            
            if (keyframe instanceof PhonemSequence) {
                PhonemSequence phonems = (PhonemSequence) keyframe;
                if (lastKeyFrameTime < phonems.getOffset() + phonems.getDuration()) {
                    lastKeyFrameTime = phonems.getOffset() + phonems.getDuration();
                }
            }
            }
            
            this.parent.sendKeyframes(keyframes, id, mode);
            //code ajouté pour syncrhoniser les animation avec greta absolute time
            this.parent.addAnimation(id, absoluteStartTime, lastKeyFrameTime);
    
        } }
    
    public void startClient() {
    int attempts = 0;
    boolean connected = false;
    while (attempts < 10 && !connected) {
        try (Socket socket = new Socket("localhost", 12345);
             DataInputStream in = new DataInputStream(socket.getInputStream())) {

            boolean received = in.readBoolean();
            if (received) {
                System.out.println("Boolean received, proceeding...");
                this.Phoneme_send = received;
                startPhonemeServer();
                connected = true; // Exit loop on successful connection
            }
        } catch (IOException e) {
            attempts++;
            System.out.println("Connection attempt " + attempts + " failed, retrying...");
            try {
                Thread.sleep(2000); // Wait for 1 second before retrying
            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
            }
        }
    }
    if (!connected) {
        System.out.println("Failed to connect to server after " + attempts + " attempts.");
    }
}
}
