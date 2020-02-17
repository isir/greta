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
package greta.application.modular.tools;

import greta.application.modular.ModularXMLFile;
import greta.core.util.xml.XMLTree;
import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.ItemEvent;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/**
 *
 * @author Andre-Marie Pez
 */
public class ParameterPanel extends javax.swing.JPanel {

    private ModuleEditor parentEditor;
    private XMLTree paramTree;
    private XMLTree object;
    private XMLTree frame;

    /**
     * Creates new form ParameterFrame
     */
    public ParameterPanel(XMLTree paramTree, ModuleEditor parentFrame) {
        initComponents();
        this.paramTree = paramTree;
        this.parentEditor = parentFrame;
        nameTextField.setText(paramTree.getAttribute("name"));

        nameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateParameterName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateParameterName();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateParameterName();
            }

        });
        nameTextField.addFocusListener(new FocusAdapter() {

            @Override
            public void focusGained(FocusEvent e) {
                checkParameterNameColor();
            }

        });
        defaultValueTextField.setText(paramTree.getAttribute("default"));
        defaultValueTextField.getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void changedUpdate(DocumentEvent e) {
                applyDefaultValue();
                checkDefaultValueColor();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                applyDefaultValue();
                checkDefaultValueColor();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                applyDefaultValue();
                checkDefaultValueColor();
            }

        });

        typeComboBox.setModel(new javax.swing.DefaultComboBoxModel(PrimitiveTypes.getNames()));
        typeComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (typeComboBox.isValid() && typeComboBox.getSelectedItem() != null && evt.getStateChange() == ItemEvent.SELECTED) {
                    ParameterPanel.this.paramTree.setAttribute("type", typeComboBox.getSelectedItem().toString());
                    updateSetMethod();
                    updateGetMethod();
                    checkDefaultValueColor();
                }
            }
        });
        typeComboBox.setSelectedItem(paramTree.getAttribute("type"));

        setMethodComboBox.setModel(new DefaultComboBoxModel<MethodItem>());
        setMethodComboBox.setRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component toRet = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
                if(value instanceof NotFoundMethodItem){
                    toRet.setForeground(Colors.error);
                }
                return toRet;
            }

        });
        setMethodComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (setMethodComboBox.isValid() && setMethodComboBox.getSelectedItem() !=null) {
                    ((MethodItem)(evt.getItem())).appply();
                    setMethodComboBox.setForeground(setMethodComboBox.getSelectedItem() instanceof NotFoundMethodItem ? Colors.error : Colors.normal);
                }
            }
        });

        getMethodComboBox.setModel(new DefaultComboBoxModel<MethodItem>());
        getMethodComboBox.setRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component toRet = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
                if(value instanceof NotFoundMethodItem){
                    toRet.setForeground(Colors.error);
                }
                return toRet;
            }

        });
        getMethodComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (getMethodComboBox.isValid() && getMethodComboBox.getSelectedItem() !=null) {
                    ((MethodItem)(evt.getItem())).appply();
                    getMethodComboBox.setForeground(getMethodComboBox.getSelectedItem() instanceof NotFoundMethodItem ? Colors.error : Colors.normal);
                }
            }
        });
        checkParameterNameColor();
        checkDefaultValueColor();
    }

    public void setObject(XMLTree obj) {
        this.object = obj;
        updateSetMethod();
        updateGetMethod();
    }

    public void setFrame(XMLTree frame) {
        this.frame = frame;
        updateSetMethod();
        updateGetMethod();
    }

    private void applyDefaultValue(){
        paramTree.setAttribute("default", defaultValueTextField.getText());
    }
    private void checkDefaultValueColor() {
        if (PrimitiveTypes.isCorrectValue(paramTree.getAttribute("type"), defaultValueTextField.getText())) {
            defaultValueTextField.setForeground(Colors.normal);
            defaultValueTextField.setBackground(Color.white);
        } else {
            defaultValueTextField.setForeground(Colors.error);
            defaultValueTextField.setBackground(defaultValueTextField.getText().isEmpty()? Colors.error : Color.white);
        }
    }
    private void checkParameterNameColor() {
        if (ModularXMLFile.checkParameterName(paramTree)) {
            nameTextField.setForeground(Colors.normal);
            nameTextField.setBackground(Color.white);
        } else {
            nameTextField.setForeground(Colors.error);
            nameTextField.setBackground(nameTextField.getText().isEmpty()? Colors.error : Color.white);
        }
    }

    private void updateSetMethod() {
        setMethodComboBox.removeAllItems();
        LinkedList<MethodItem> items = new LinkedList<MethodItem>();

        if (object != null) {
            List<Method> methods = ToolBox.getMethodsUsing(object, PrimitiveTypes.getClassOf((String) typeComboBox.getSelectedItem()));
            for (Method s : methods) {
                items.add(new ExistingSetMethodItem(2, s, false));
            }
        }
        if (frame != null) {
            List<Method> methods = ToolBox.getMethodsUsing(frame, PrimitiveTypes.getClassOf((String) typeComboBox.getSelectedItem()));
            for (Method s : methods) {
                items.add(new ExistingSetMethodItem(3, s, true));
            }
        }

        MethodItem current = getSetMethodItemOfCurrentLink();
        if (!items.contains(current)) {
            items.add(current);
        }

        Collections.sort(items);
        for (MethodItem item : items) {
            setMethodComboBox.addItem(item);
        }

        updateSetSelection();
    }

    private void updateSetSelection() {
        setMethodComboBox.setSelectedItem(getSetMethodItemOfCurrentLink());
        setMethodComboBox.setForeground(setMethodComboBox.getSelectedItem() instanceof NotFoundMethodItem ? Colors.error : Colors.normal);
    }


    private MethodItem getSetMethodItemOfCurrentLink() {
        boolean fromFrame = paramTree.getAttribute("set_on").equals("frame");
        String methodName = paramTree.getAttribute("set_method");
        return new NotFoundSetMethodItem(1, methodName, fromFrame);
    }

    private void updateGetMethod() {
        getMethodComboBox.removeAllItems();
        LinkedList<MethodItem> items = new LinkedList<MethodItem>();

        if (object != null) {
            List<Method> methods = ToolBox.getMethodsReturned(object, PrimitiveTypes.getClassOf((String) typeComboBox.getSelectedItem()));
            for (Method s : methods) {
                items.add(new ExistingGetMethodItem(2, s, false));
            }
        }
        if (frame != null) {
            List<Method> methods = ToolBox.getMethodsReturned(frame, PrimitiveTypes.getClassOf((String) typeComboBox.getSelectedItem()));
            for (Method s : methods) {
                items.add(new ExistingGetMethodItem(3, s, true));
            }
        }

        MethodItem current = getGetMethodItemOfCurrentLink();
        if (!items.contains(current)) {
            items.add(current);
        }

        Collections.sort(items);
        for (MethodItem item : items) {
            getMethodComboBox.addItem(item);
        }

        updateGetSelection();
    }

    private void updateGetSelection() {
        getMethodComboBox.setSelectedItem(getGetMethodItemOfCurrentLink());
        getMethodComboBox.setForeground(getMethodComboBox.getSelectedItem() instanceof NotFoundMethodItem ? Colors.error : Colors.normal);
    }

    private MethodItem getGetMethodItemOfCurrentLink() {
        boolean fromFrame = paramTree.getAttribute("get_on").equals("frame");
        String methodName = paramTree.getAttribute("get_method");
        return new NotFoundGetMethodItem(1, methodName, fromFrame);
    }

    private class ExistingSetMethodItem extends ExistingMethodItem {

        public ExistingSetMethodItem(int order, Method m, boolean fromFrame) {
            super(order, "set_method", "set_on", m, fromFrame);
        }

    }

    private class ExistingGetMethodItem extends ExistingMethodItem {

        public ExistingGetMethodItem(int order, Method m, boolean fromFrame) {
            super(order, "get_method", "get_on", m, fromFrame);
        }

    }

    private class NotFoundSetMethodItem extends NotFoundMethodItem {

        public NotFoundSetMethodItem(int order, String m, boolean fromFrame) {
            super(order, "set_method", "set_on", m, fromFrame);
        }
    }

    private class NotFoundGetMethodItem extends NotFoundMethodItem {

        public NotFoundGetMethodItem(int order, String m, boolean fromFrame) {
            super(order, "get_method", "get_on", m, fromFrame);
        }

        @Override
        public String getMethodParam() {
            return "";
        }
    }

    private class NotFoundMethodItem extends BaseMethodItem {

        String method;

        public NotFoundMethodItem(int order, String methodAttr, String onAttr, String m, boolean fromFrame) {
            super(order, methodAttr, onAttr, fromFrame);
            method = m;
            this.fromFrame = fromFrame;
        }

        @Override
        public String getMethodName() {
            return method;
        }

        @Override
        public String getMethodParam() {
            return "???";
        }
    };

    private class ExistingMethodItem extends BaseMethodItem {

        Method method;

        ExistingMethodItem(int order, String methodAttr, String onAttr, Method m, boolean fromFrame) {
            super(order, methodAttr, onAttr, fromFrame);
            this.method = m;
        }

        @Override
        public String getMethodName() {
            return method.getName();
        }

        @Override
        public String getMethodParam() {
            if(method.getParameterTypes().length>0)
                return method.getParameterTypes()[0].getName();
            return "";
        }
    }

    private abstract class BaseMethodItem extends MethodItem {

        String methodAttr;
        String onAttr;
        boolean fromFrame;

        public BaseMethodItem(int preferedOrder, String methodAttr, String onAttr, boolean fromFrame) {
            super(preferedOrder);
            this.methodAttr = methodAttr;
            this.onAttr = onAttr;
            this.fromFrame = fromFrame;
        }

        @Override
        public String getMethodCaller() {
            return fromFrame ? "frame" : "object";
        }

        public void appply() {
            paramTree.setAttribute(methodAttr, getMethodName());
            paramTree.setAttribute(onAttr, getMethodCaller());
        }
    }

    private void updateParameterName(){
        paramTree.setAttribute("name", nameTextField.getText());
        checkParameterNameColor();
    }
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        nameTextField = new javax.swing.JTextField();
        nameLabel = new javax.swing.JLabel();
        typeLabel = new javax.swing.JLabel();
        typeComboBox = new javax.swing.JComboBox();
        setMethodLabel = new javax.swing.JLabel();
        setMethodComboBox = new javax.swing.JComboBox();
        getMethodLabel = new javax.swing.JLabel();
        getMethodComboBox = new javax.swing.JComboBox();
        deleteButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.delete")  ;
        defaultValueLabel = new javax.swing.JLabel();
        defaultValueTextField = new javax.swing.JTextField();

        setBorder(javax.swing.BorderFactory.createTitledBorder(""));

        nameTextField.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                nameTextFieldActionPerformed(evt);
            }
        });

        nameLabel.setText("Name");

        typeLabel.setText("Type");

        typeComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                typeComboBoxActionPerformed(evt);
            }
        });

        setMethodLabel.setText("Set");

        getMethodLabel.setText("Get");

        deleteButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteButtonActionPerformed(evt);
            }
        });

        defaultValueLabel.setText("Default");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(nameLabel)
                    .addComponent(typeLabel)
                    .addComponent(setMethodLabel)
                    .addComponent(getMethodLabel))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(nameTextField)
                        .addGap(18, 18, 18)
                        .addComponent(deleteButton))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(defaultValueLabel)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(defaultValueTextField))
                    .addComponent(setMethodComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(getMethodComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(nameLabel)
                    .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(deleteButton))
                .addGap(12, 12, 12)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(typeLabel)
                    .addComponent(typeComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(defaultValueLabel)
                    .addComponent(defaultValueTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(setMethodLabel)
                    .addComponent(setMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(getMethodLabel)
                    .addComponent(getMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void typeComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_typeComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_typeComboBoxActionPerformed

    private void nameTextFieldActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_nameTextFieldActionPerformed
        updateParameterName();
    }//GEN-LAST:event_nameTextFieldActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed
        paramTree.getParent().removeChild(paramTree);
        parentEditor.updateParametersList();
    }//GEN-LAST:event_deleteButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel defaultValueLabel;
    private javax.swing.JTextField defaultValueTextField;
    private javax.swing.JButton deleteButton;
    private javax.swing.JComboBox getMethodComboBox;
    private javax.swing.JLabel getMethodLabel;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JComboBox setMethodComboBox;
    private javax.swing.JLabel setMethodLabel;
    private javax.swing.JComboBox typeComboBox;
    private javax.swing.JLabel typeLabel;
    // End of variables declaration//GEN-END:variables
}
