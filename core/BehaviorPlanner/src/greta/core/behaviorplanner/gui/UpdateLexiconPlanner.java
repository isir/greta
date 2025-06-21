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
package greta.core.behaviorplanner.gui;

import greta.core.behaviorplanner.Planner;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;

/**
 *
 * @author donat
 */
public class UpdateLexiconPlanner extends JFrame implements CharacterDependent {

    private CharacterManager cm;
    //private Planner plan;
    private JLabel LexiconUpdate;
    private JButton Update;
    private Planner parent;

    public UpdateLexiconPlanner(){

        this.cm = CharacterManager.getStaticInstance();

        LexiconUpdate = new JLabel();
        Update = new JButton();

        LexiconUpdate.setText("Lexicon");
        LexiconUpdate.setToolTipText("");

        Update.setText("Update");
        Update.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                UpdateActionPerformed(evt);
            }
        });

        //Layout
        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(LexiconUpdate, javax.swing.GroupLayout.PREFERRED_SIZE, 68, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(42, 42, 42)
                        .addComponent(Update)))
                .addContainerGap(47, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(23, 23, 23)
                .addComponent(LexiconUpdate)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(Update)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
        }

    // action performed when click the button
    private void UpdateActionPerformed(java.awt.event.ActionEvent evt) {
       parent.UpdateLexicon();
    }

    // update lexicon in planner in order to do not restart the platform
    public void UpdateLexicon(Planner parent) {
        this.parent = parent;
    }

    @Override
    public void onCharacterChanged() {
        // to do
    }

    @Override
    public CharacterManager getCharacterManager() {
        return this.cm;
    }

    @Override
    public void setCharacterManager(CharacterManager cm) {
        this.cm = cm;
    }
}
