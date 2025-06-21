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
package greta.tools.editors.facelibrary;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.GroupLayout.ParallelGroup;
import javax.swing.GroupLayout.SequentialGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.LayoutStyle;

/**
 *
 * @author Andre-Marie Pez
 */
public class AUsPanel extends JPanel {
    private JLabel assymLabel;
    private JLabel leftLabel;
    private JLabel righLabel;

    /**
     * Creates new form AUsPanel
     */
    public AUsPanel(AUComponents... aus) {

        assymLabel = new JLabel("Assymetry"); //TODO : use greta.core.utilx.gui.ToolBox.LocalizedJLabel
        righLabel = new JLabel("Right"); //TODO : use greta.core.utilx.gui.ToolBox.LocalizedJLabel
        leftLabel = new JLabel("Left"); //TODO : use greta.core.utilx.gui.ToolBox.LocalizedJLabel

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        ParallelGroup names = layout.createParallelGroup();
        ParallelGroup assyms = layout.createParallelGroup(Alignment.CENTER).addComponent(assymLabel);
        ParallelGroup rSliders = layout.createParallelGroup().addComponent(righLabel);
        ParallelGroup rFields = layout.createParallelGroup();
        ParallelGroup lSliders = layout.createParallelGroup().addComponent(leftLabel);
        ParallelGroup lFields = layout.createParallelGroup();
        ParallelGroup legend = layout.createParallelGroup()
                .addComponent(assymLabel)
                .addComponent(righLabel)
                .addComponent(leftLabel);
        SequentialGroup vertical = layout.createSequentialGroup().addContainerGap().addGroup(legend);

        int p = GroupLayout.PREFERRED_SIZE;
        for(AUComponents au : aus){
            names = names.addComponent(au.auNameLabel);
            assyms = assyms.addComponent(au.assymetryCheckBox);
            rSliders = rSliders.addComponent(au.right.slider);
            rFields = rFields.addComponent(au.right.field, p,p,p);
            lFields = lFields.addComponent(au.left.field, p,p,p);

            lSliders = lSliders.addComponent(au.left.slider);
            vertical = vertical.addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                    .addGroup(layout.createParallelGroup(Alignment.CENTER)
                        .addComponent(au.auNameLabel)
                        .addComponent(au.assymetryCheckBox)
                        .addComponent(au.right.slider)
                        .addComponent(au.right.field, p,p,p)
                        .addComponent(au.left.slider)
                        .addComponent(au.left.field, p,p,p));
        }

        layout.setVerticalGroup(vertical.addContainerGap());

        layout.setHorizontalGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(names)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(assyms)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(rSliders)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(rFields)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(lSliders)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(lFields)
                .addContainerGap());
    }
}
