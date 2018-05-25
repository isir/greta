/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.tools.editors.facelibrary;

import vib.tools.editors.SliderAndText;
import vib.core.repositories.AUAP;
import vib.core.repositories.AUAPFrame;
import vib.core.util.enums.Side;
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