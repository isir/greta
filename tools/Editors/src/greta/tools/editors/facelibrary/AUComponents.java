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
