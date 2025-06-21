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
