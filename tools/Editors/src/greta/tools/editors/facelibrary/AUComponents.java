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

import greta.core.repositories.AUAP;
import greta.core.repositories.AUAPFrame;
import greta.core.util.enums.Side;
import greta.tools.editors.SliderAndText;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JCheckBox;
import javax.swing.JLabel;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class AUComponents {

    public final int which;
    public final AUAPFrame frame;
    public final JLabel auNameLabel;
    public final SliderAndText left;
    public final SliderAndText right;
    public final JCheckBox assymetryCheckBox;

    public AUComponents(int which, AUAPFrame frame) {
        this.which = which;
        this.frame = frame;
        auNameLabel = new JLabel("AU " + which);
        left = new SliderAndText(0, 100) {
            @Override
            protected void onModification() {
                modifyLeft();
            }
        };
        right = new SliderAndText(0, 100) {
            @Override
            protected void onModification() {
                modifyRigth();
            }
        };
        assymetryCheckBox = new JCheckBox();
        assymetryCheckBox.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setAssymetric(assymetryCheckBox.isSelected());
            }
        });

        setAssymetric(false);
    }

    public final void setAssymetric(boolean isAssimetric) {
        assymetryCheckBox.setSelected(isAssimetric);
        left.setEnabled(assymetryCheckBox.isEnabled() && isAssimetric);
        if (!isAssimetric) {
            left.setValue(right.getValue());
        }
    }

    private void modifyLeft() {
        //change frame for left
        AUAP leftForThisAU = frame.getAUAP(which, Side.LEFT);
        leftForThisAU.setMask(true);
        leftForThisAU.setValue(left.getNormlizedValue());

        //send
        send();
    }

    private void modifyRigth() {
        //change frame for right
        AUAP rightForThisAU = frame.getAUAP(which, Side.RIGHT);
        rightForThisAU.setMask(true);
        rightForThisAU.setValue(right.getNormlizedValue());

        if (assymetryCheckBox.isSelected()) {
            //send
            send();
        } else {
            left.setValue(right.getValue());
            //no need to send, left does it
        }
    }

    public void setEnabled(boolean enabled){
        auNameLabel.setEnabled(enabled);
        assymetryCheckBox.setEnabled(enabled);
        left.setEnabled(enabled);
        right.setEnabled(enabled);
    }

    protected abstract void send();
}
