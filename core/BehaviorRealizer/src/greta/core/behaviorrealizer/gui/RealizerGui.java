/*
 * This file is part of Greta.
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
package greta.core.behaviorrealizer.gui;

import greta.core.behaviorrealizer.Realizer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author Donatella Simonetti
 */
public class RealizerGui extends JFrame{

    private Realizer parent;
    // Variables declaration
    private JLabel AULibrary;
    private JLabel FaceLibrary;
    private JLabel GestureLibrary;
    private JLabel HeadLibrary;
    private JLabel Libraries;
    private JLabel ShoulderLibrary;
    private JLabel TorsoLibrary;
    private JButton UpdateAU;
    private JButton UpdateAll;
    private JButton UpdateFace;
    private JButton UpdateGestures;
    private JButton UpdateHand;
    private JButton UpdateHead;
    private JButton UpdateSh;
    private JButton UpdateTorso;
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

        setMaximumSize(new java.awt.Dimension(213, 392));

        AULibrary.setText("AU Library");

        UpdateAU.setText("Update");
        UpdateAU.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                //UpdateAUActionPerformed(evt);
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
                    //.addComponent(jLabel1))
                .addGap(32, 32, 32)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            //.addComponent(UpdateHand)
                            .addComponent(UpdateTorso))
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
                    .addComponent(UpdateTorso))
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

}
