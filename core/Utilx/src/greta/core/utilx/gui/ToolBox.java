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
package greta.core.utilx.gui;

import greta.core.util.IniManager;
import java.util.Locale;

/**
 *
 * @author Andre-Marie Pez
 */
public class ToolBox {

    public static class LocalizedJMenu extends javax.swing.JMenu {

        String LocalizedParam;

        public LocalizedJMenu(String param) {
            super(IniManager.getLocaleProperty(param));
            setLocaleParameter(param);
        }

        @Override
        public void setLocale(Locale l) {
            super.setLocale(l);
            super.setText(IniManager.getLocaleProperty(LocalizedParam, l));
        }

        public void setLocaleParameter(String param){
            LocalizedParam = param;
            super.setText(IniManager.getLocaleProperty(LocalizedParam));
        }

        /**
         * Does nothing, please use setLocaleParameter method
         * @param text
         */
        @Override
        public void setText(String text) { }
    }

    public static class LocalizedJMenuItem extends javax.swing.JMenuItem {

        String LocalizedParam;

        public LocalizedJMenuItem(String param) {
            super(IniManager.getLocaleProperty(param));
            setLocaleParameter(param);
        }

        @Override
        public void setLocale(Locale l) {
            super.setLocale(l);
            super.setText(IniManager.getLocaleProperty(LocalizedParam, l));
        }

        public void setLocaleParameter(String param){
            LocalizedParam = param;
            super.setText(IniManager.getLocaleProperty(LocalizedParam));
        }

        /**
         * Does nothing, please use setLocaleParameter method
         * @param text
         */
        @Override
        public void setText(String text) { }
    }

    public static class LocalizedJCheckBoxMenuItem extends javax.swing.JCheckBoxMenuItem {

        String LocalizedParam;

        public LocalizedJCheckBoxMenuItem(String param) {
            super(IniManager.getLocaleProperty(param));
            setLocaleParameter(param);
        }

        @Override
        public void setLocale(Locale l) {
            super.setLocale(l);
            super.setText(IniManager.getLocaleProperty(LocalizedParam, l));
        }

        public void setLocaleParameter(String param){
            LocalizedParam = param;
            super.setText(IniManager.getLocaleProperty(LocalizedParam));
        }

        /**
         * Does nothing, please use setLocaleParameter method
         * @param text
         */
        @Override
        public void setText(String text) { }
    }

    public static class LocalizedJLabel extends javax.swing.JLabel {

        String LocalizedParam;

        public LocalizedJLabel(String param) {
            super(IniManager.getLocaleProperty(param));
            setLocaleParameter(param);
        }

        @Override
        public void setLocale(Locale l) {
            super.setLocale(l);
            super.setText(IniManager.getLocaleProperty(LocalizedParam, l));
        }

        public void setLocaleParameter(String param){
            LocalizedParam = param;
            super.setText(IniManager.getLocaleProperty(LocalizedParam));
        }

        /**
         * Does nothing, please use setLocaleParameter method
         * @param text
         */
        @Override
        public void setText(String text) { }
    }

    public static class LocalizedJButton extends javax.swing.JButton {

        String LocalizedParam;

        public LocalizedJButton(String param) {
            super(IniManager.getLocaleProperty(param));
            setLocaleParameter(param);
        }

        @Override
        public void setLocale(Locale l) {
            super.setLocale(l);
            super.setText(IniManager.getLocaleProperty(LocalizedParam, l));
        }

        public void setLocaleParameter(String param){
            LocalizedParam = param;
            super.setText(IniManager.getLocaleProperty(LocalizedParam));
        }

        /**
         * Does nothing, please use setLocaleParameter method
         * @param text
         */
        @Override
        public void setText(String text) { }
    }

    public static class LocalizedJCheckBox extends javax.swing.JCheckBox {

        String LocalizedParam;

        public LocalizedJCheckBox(String param) {
            super(IniManager.getLocaleProperty(param));
            setLocaleParameter(param);
        }

        @Override
        public void setLocale(Locale l) {
            super.setLocale(l);
            super.setText(IniManager.getLocaleProperty(LocalizedParam, l));
        }

        public void setLocaleParameter(String param){
            LocalizedParam = param;
            super.setText(IniManager.getLocaleProperty(LocalizedParam));
        }

        /**
         * Does nothing, please use setLocaleParameter method
         * @param text
         */
        @Override
        public void setText(String text) { }
    }

    public static void checkDouble(java.awt.event.KeyEvent evt, javax.swing.JTextField field) {
        checkDoubleInRange(evt, field, -Double.MAX_VALUE, Double.MAX_VALUE);
    }

    public static void checkDoubleInRange(java.awt.event.KeyEvent evt, javax.swing.JTextField field, double min, double max) {
        char pressed = evt.getKeyChar();
        if (Character.isDigit(pressed) || //any digit is ok
                (pressed == '.' && ((!field.getText().contains(".")) || (field.getSelectedText() != null && field.getSelectedText().contains(".")))) || //dot is ok if it is the only one
                (pressed == '-' && ((field.getCaretPosition() == 0 && (!field.getText().contains("-"))) || (field.getSelectionStart() == 0 && field.getSelectionEnd() != 0)))) { //minus is ok if it is at the first position and the only one
            String beforeselect = field.getText().substring(0, field.getSelectionStart());
            String afterSelect = field.getText().substring(field.getSelectionEnd());
            String valueAsString = beforeselect + pressed + afterSelect;
            try {
                double valueAsDouble = Double.parseDouble(valueAsString);
                if (min <= valueAsDouble && valueAsDouble <= max) {
                    return;//it's ok
                }
            } catch (Throwable t) {
                if (valueAsString.equals(".") || valueAsString.equals("-")) {
                    return;//it's ok
                }
            }
        }
        evt.consume(); //we don't ant it
    }

    public static void checkByte(java.awt.event.KeyEvent evt, javax.swing.JTextField field) {
        checkIntegerInRange(evt, field, Byte.MIN_VALUE, Byte.MAX_VALUE);
    }

    public static void checkShort(java.awt.event.KeyEvent evt, javax.swing.JTextField field) {
        checkIntegerInRange(evt, field, Short.MIN_VALUE, Short.MAX_VALUE);
    }

    public static void checkInteger(java.awt.event.KeyEvent evt, javax.swing.JTextField field) {
        checkIntegerInRange(evt, field, Integer.MIN_VALUE, Integer.MAX_VALUE);
    }

    public static void checkLong(java.awt.event.KeyEvent evt, javax.swing.JTextField field) {
        checkIntegerInRange(evt, field, Long.MIN_VALUE, Long.MAX_VALUE);
    }

    public static void checkIntegerInRange(java.awt.event.KeyEvent evt, javax.swing.JTextField field, long min, long max) {
        char pressed = evt.getKeyChar();
        if (Character.isDigit(pressed) || //any digit is ok
                (pressed == '-' && ((field.getCaretPosition() == 0 && (!field.getText().contains("-"))) || (field.getSelectionStart() == 0 && field.getSelectionEnd() != 0)))) { //minus is ok if it is at the first position and the only one
            String beforeselect = field.getText().substring(0, field.getSelectionStart());
            String afterSelect = field.getText().substring(field.getSelectionEnd());
            String valueAsString = beforeselect + pressed + afterSelect;
            try {
                long valueAsLong = Long.parseLong(valueAsString);
                if (min <= valueAsLong && valueAsLong <= max) {
                    return;//it's ok
                }
            } catch (Throwable t) {
                if (valueAsString.equals("-")) {
                    return;//it's ok
                }
            }
        }
        evt.consume(); //we don't want it
    }
}
