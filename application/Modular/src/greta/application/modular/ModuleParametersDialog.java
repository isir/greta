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
package greta.application.modular;

import greta.application.modular.modules.Module;
import greta.application.modular.modules.ModuleFactory;
import greta.application.modular.modules.ModuleFactory.ParameterInfo;
import greta.core.util.IniManager;
import greta.core.utilx.gui.ToolBox;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

/**
 *
 * @author Andre-Marie Pez
 */
public class ModuleParametersDialog extends javax.swing.JDialog {

    Module module;
    ArrayList<ParameterField> fields;

    /**
     * Creates new form ModuleParametersDialog
     */
    public ModuleParametersDialog(java.awt.Frame parent, boolean modal, Module m, List<ModuleFactory.ParameterInfo> parameters) {
        super(parent, modal);
        module = m;
        fields = new ArrayList<ParameterField>(parameters.size());
        initComponents();

        // Close the dialog when Esc is pressed
        String cancelName = "cancel";
        InputMap inputMap = getRootPane().getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), cancelName);
        ActionMap actionMap = getRootPane().getActionMap();
        actionMap.put(cancelName, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });


        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        javax.swing.GroupLayout.SequentialGroup vertical = jPanel1Layout.createSequentialGroup();
        javax.swing.GroupLayout.ParallelGroup horizontal = jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
        boolean first = true;
        for (ModuleFactory.ParameterInfo parameter : parameters) {
            ParameterField paramField = null;
            if (parameter.getType().equalsIgnoreCase("boolean")) {
                paramField = new BooleanField(parameter);
            }
            if (parameter.getType().equalsIgnoreCase("string")) {
                paramField = new StringField(parameter);
            }
            if (parameter.getType().equalsIgnoreCase("byte")) {
                paramField = new ByteField(parameter);
            }
            if (parameter.getType().equalsIgnoreCase("short")) {
                paramField = new ShortField(parameter);
            }
            if (parameter.getType().equalsIgnoreCase("integer")) {
                paramField = new IntegerField(parameter);
            }
            if (parameter.getType().equalsIgnoreCase("long")) {
                paramField = new LongField(parameter);
            }
            if (parameter.getType().equalsIgnoreCase("float")) {
                paramField = new FloatField(parameter);
            }
            if (parameter.getType().equalsIgnoreCase("double")) {
                paramField = new NumberField(parameter);
            }

            if (paramField != null) {
                fields.add(paramField);
                horizontal = horizontal.addComponent(paramField);
                vertical = (first ? vertical.addContainerGap() : vertical.addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED))
                        .addComponent(paramField);
            }
            first = first && !first;
        }

        jPanel1Layout.setHorizontalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(jPanel1Layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(horizontal)
                .addContainerGap()));
        jPanel1Layout.setVerticalGroup(
                jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                .addGroup(vertical
                .addContainerGap()));
        pack();

    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        CancelButton = new javax.swing.JButton();
        okButton = new javax.swing.JButton();
        jScrollPane1 = new javax.swing.JScrollPane();
        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);

        CancelButton.setText(IniManager.getLocaleProperty("GUI.Cancel"));
        CancelButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                CancelButtonActionPerformed(evt);
            }
        });

        okButton.setText(IniManager.getLocaleProperty("GUI.Ok"));
        okButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                okButtonActionPerformed(evt);
            }
        });

        jScrollPane1.setBorder(null);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 457, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 218, Short.MAX_VALUE)
        );

        jScrollPane1.setViewportView(jPanel1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(0, 0, Short.MAX_VALUE)
                        .addComponent(okButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(CancelButton))
                    .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 382, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 235, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(CancelButton)
                    .addComponent(okButton))
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void CancelButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_CancelButtonActionPerformed
        dispose();
    }//GEN-LAST:event_CancelButtonActionPerformed

    private void okButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_okButtonActionPerformed
        for (ParameterField field : fields) {
            field.apply();
        }
        dispose();
    }//GEN-LAST:event_okButtonActionPerformed

    private abstract class ParameterField extends JPanel {

        ModuleFactory.ParameterInfo parameterInfo;

        ParameterField(ModuleFactory.ParameterInfo parameterInfo) {
            this.parameterInfo = parameterInfo;
        }

        abstract Object getValue();

        Object get() {
            try {
                return parameterInfo.getGetMethod().invoke(
                        parameterInfo.getGetOn().equalsIgnoreCase("object") ? module.getObject() : module.getFrame(),
                        new Object[]{});
            } catch (Exception ex) {
                return ModuleFactory.castStringToTypedObject(parameterInfo.getType(), parameterInfo.getDefaultvalue());
            }
        }

        void apply() {
            try {
                parameterInfo.getSetMethod().invoke(
                        parameterInfo.getSetOn().equalsIgnoreCase("object") ? module.getObject() : module.getFrame(),
                        new Object[]{getValue()});
            } catch (Exception ex) {
            }
        }
    }

    private class BooleanField extends ParameterField {

        javax.swing.JCheckBox jCheckBox1;

        BooleanField(ModuleFactory.ParameterInfo parameterInfo) {
            super(parameterInfo);
            jCheckBox1 = new javax.swing.JCheckBox();
            jCheckBox1.setText(parameterInfo.getName());
            jCheckBox1.setSelected((Boolean) get());

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);

            this.setLayout(layout);

            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addComponent(jCheckBox1)));

            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addComponent(jCheckBox1)));

        }

        @Override
        Object getValue() {
            return Boolean.valueOf(jCheckBox1.isSelected());
        }

        void setValue(boolean b) {
            jCheckBox1.setSelected(b);
        }
    }

    private class StringField extends ParameterField {

        javax.swing.JTextField jTextField1;
        javax.swing.JLabel jLabel1;

        StringField(ModuleFactory.ParameterInfo parameterInfo) {
            super(parameterInfo);

            jTextField1 = new javax.swing.JTextField();
            jLabel1 = new javax.swing.JLabel();

            jTextField1.setText(get().toString());
            jLabel1.setText(parameterInfo.getName());

            javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
            this.setLayout(layout);
            layout.setHorizontalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addComponent(jLabel1)
                    .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                    .addComponent(jTextField1)));
            layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jTextField1))));
        }

        @Override
        Object getValue() {
            return jTextField1.getText();
        }
    }

    private class NumberField extends StringField {

        public NumberField(ParameterInfo parameterInfo) {
            super(parameterInfo);
            jTextField1.addKeyListener(new java.awt.event.KeyAdapter() {
                @Override
                public void keyTyped(java.awt.event.KeyEvent evt) {
                    check(evt);
                }
            });
        }

        void check(java.awt.event.KeyEvent evt) {
            ToolBox.checkDouble(evt, jTextField1);
        }

        @Override
        Object getValue() {
            return Double.parseDouble(jTextField1.getText());
        }
    }

    private class ByteField extends NumberField {

        public ByteField(ParameterInfo parameterInfo) {
            super(parameterInfo);
        }

        @Override
        void check(java.awt.event.KeyEvent evt) {
            ToolBox.checkByte(evt, jTextField1);
        }

        @Override
        Object getValue() {
            return Byte.parseByte(jTextField1.getText());
        }
    }

    private class ShortField extends NumberField {

        public ShortField(ParameterInfo parameterInfo) {
            super(parameterInfo);
        }

        @Override
        void check(java.awt.event.KeyEvent evt) {
            ToolBox.checkShort(evt, jTextField1);
        }

        @Override
        Object getValue() {
            return Short.parseShort(jTextField1.getText());
        }
    }

    private class IntegerField extends NumberField {

        public IntegerField(ParameterInfo parameterInfo) {
            super(parameterInfo);
        }

        @Override
        void check(java.awt.event.KeyEvent evt) {
            ToolBox.checkInteger(evt, jTextField1);
        }

        @Override
        Object getValue() {
            return Integer.parseInt(jTextField1.getText());
        }
    }

    private class LongField extends NumberField {

        public LongField(ParameterInfo parameterInfo) {
            super(parameterInfo);
        }

        @Override
        void check(java.awt.event.KeyEvent evt) {
            ToolBox.checkLong(evt, jTextField1);
        }

        @Override
        Object getValue() {
            return Long.parseLong(jTextField1.getText());
        }
    }

    private class FloatField extends NumberField {

        public FloatField(ParameterInfo parameterInfo) {
            super(parameterInfo);
        }

        @Override
        Object getValue() {
            return Float.parseFloat(jTextField1.getText());
        }
    }
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton CancelButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JButton okButton;
    // End of variables declaration//GEN-END:variables
}
