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
package greta.tools.editors;

import greta.core.util.math.Functions;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.text.DecimalFormat;
import javax.swing.JFormattedTextField;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.NumberFormatter;

/**
 *
 * @author Andre-Marie Pez
 */
public abstract class SliderAndText {

    public final JSlider slider;
    public final JFormattedTextField field;
    private int min;
    private int max;

    public SliderAndText(int min, int max) {
        this.min = min;
        this.max = max;
        slider = new JSlider(min, max, 0);
        NumberFormatter formatter = new NumberFormatter(new DecimalFormat("###"));
        formatter.setMaximum(max);
        formatter.setMinimum(min);
        formatter.setAllowsInvalid(false);
        formatter.setCommitsOnValidEdit(true);
        field = new JFormattedTextField(formatter);
        field.setColumns(3);
        field.setValue(slider.getValue());
        field.setHorizontalAlignment(JTextField.TRAILING);
        slider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                field.setValue(slider.getValue());
            }
        });
        field.addPropertyChangeListener("value", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                slider.setValue((int) Integer.parseInt(field.getValue().toString()));
                onModification();
            }
        });
    }

    public void setValue(int value) {
        slider.setValue(value);
    }

    public void setNormalisedValue(double normalised) {
        setValue((int) Functions.changeInterval(normalised, 0, 1, min, max));
    }

    public int getValue() {
        return slider.getValue();
    }

    public double getNormlizedValue() {
        return Functions.changeInterval(getValue(), min, max, 0, 1);
    }

    public void setEnabled(boolean enabled) {
        slider.setEnabled(enabled);
        field.setEnabled(enabled);
    }

    protected abstract void onModification();
}
