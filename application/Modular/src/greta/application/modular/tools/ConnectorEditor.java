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
import greta.core.util.IniManager;
import greta.core.util.xml.XMLTree;
import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JList;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Andre-Marie Pez
 */
public class ConnectorEditor extends javax.swing.JPanel implements Updatable{

    ConnectorListModel model  = new ConnectorListModel();
    ConnectorListModel.ConnectorElement connector;

    /**
     * Creates new form ModuleEditor
     */
    public ConnectorEditor() {
        initComponents();
        setName(IniManager.getLocaleProperty("modular.edit.connector"));
        jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if( ! nameTextField.hasFocus()){
                    ConnectorListModel.ConnectorElement newSelection = (ConnectorListModel.ConnectorElement)jList1.getSelectedValue();
                    if(newSelection != connector){
                        setCurrentModule(newSelection);
                    }
                }
            }
        });

        nameTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                updateModuleName();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updateModuleName();
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                updateModuleName();
            }

        });

        updateStyleCombobox();

        styleComboBox.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    if(connector !=null){
                        connector.setStyle(evt.getItem().toString());
                    }
                }
            }

        });
        jList1.setCellRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component toReturn = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null || ! (value instanceof ConnectorListModel.ConnectorElement)){
                    return toReturn;
                }
                ConnectorListModel.ConnectorElement element = (ConnectorListModel.ConnectorElement)value;
                boolean unicname = true;
                if(index > 0){
                    unicname = ! (model.getElementAt(index-1).getName().equals(element.getName()));
                }
                if(index < model.getSize()-1){
                    unicname = unicname && ! (model.getElementAt(index+1).getName().equals(element.getName()));
                }
                boolean valid = ModularXMLFile.checkOneConnector(element.connector);
                toReturn.setForeground(valid ? (unicname ? toReturn.getForeground() : Colors.warning) : Colors.error);
                return toReturn;
            }

        });

        objectPanel1.addPropertyChangeListener(ObjectPanel.CLASS_CHANGED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateConnectionMethods();
            }
        });
        objectPanel1.setShowInterfaces(true);
        objectPanel1.setCheckConstructor(false);
        objectPanel2.addPropertyChangeListener(ObjectPanel.CLASS_CHANGED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateConnectionMethods();
            }
        });
        objectPanel2.setShowInterfaces(true);
        objectPanel2.setCheckConstructor(false);


        connectionMethodComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if(connectionMethodComboBox.isValid() && connectionMethodComboBox.getSelectedItem() !=null){
                    ((MethodItem)connectionMethodComboBox.getSelectedItem()).appply();
                    connectionMethodComboBox.setForeground(connectionMethodComboBox.getSelectedItem() instanceof NotFoundMethodItem ? Colors.error : Colors.normal);
                }
            }
        });

        disconnectionMethodComboBox.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if(disconnectionMethodComboBox.isValid() && disconnectionMethodComboBox.getSelectedItem() !=null){
                    ((MethodItem)disconnectionMethodComboBox.getSelectedItem()).appply();
                    disconnectionMethodComboBox.setForeground(disconnectionMethodComboBox.getSelectedItem() instanceof NotFoundMethodItem ? Colors.error : Colors.normal);
                }
            }
        });
        setCurrentModule(null);
    }

    private void updateModuleName() {
        if(connector != null){
            connector.setName(nameTextField.getText());
            jList1.setSelectedValue(connector, true);
        }
    }

    private void updateStyleCombobox(){
        LinkedList<String> styleNames = new LinkedList<String>();
        for (XMLTree styles : ModularXMLFile.getStyles().getChildrenElement()) {
            if (styles.isNamed("style")) {
                styleNames.add(styles.getAttribute("name"));
            }
        }
        Collections.sort(styleNames, String.CASE_INSENSITIVE_ORDER);
        styleNames.addFirst("");
        styleComboBox.removeAllItems();

        for (String styleName : styleNames) {
            styleComboBox.addItem(styleName);
        }
        updateStyleSelection();
    }

    private void updateStyleSelection(){

        if (connector != null && ((DefaultComboBoxModel)styleComboBox.getModel()).getIndexOf(connector.getStyle())>=0) {
            styleComboBox.setSelectedItem(connector.getStyle());
        }
        else{
            styleComboBox.setSelectedIndex(0);
        }
    }

    private void setCurrentModule(ConnectorListModel.ConnectorElement connector){
        this.connector = null;

        boolean enable = connector!=null;
        nameLabel.setEnabled(enable);
        styleLabel.setEnabled(enable);
        inputPanel.setEnabled(enable);
        outputPanel.setEnabled(enable);
        nameTextField.setEnabled(enable);
        objectPanel1.setEnabled(enable);
        objectPanel2.setEnabled(enable);
        connectPanel.setEnabled(enable);
        disconnectPanel.setEnabled(enable);
        connectionMethodComboBox.setEnabled(enable);
        disconnectionMethodComboBox.setEnabled(enable);
        deleteConnectorButton.setEnabled(enable);
        styleComboBox.setEnabled(enable);
        uniqueCheckBox.setEnabled(enable);
        if(connector==null){
            nameTextField.setText("");
            uniqueCheckBox.setSelected(false);
            objectPanel1.setObject(null);
            objectPanel2.setObject(null);
        }
        else{
            nameTextField.setText(connector.getName());
            uniqueCheckBox.setSelected(connector.isUnique());
            objectPanel1.setObject(connector.getInput());
            objectPanel2.setObject(connector.getOutput());
        }
        disconnectionMethodComboBox.setEnabled(enable);
        this.connector = connector;
        updateStyleSelection();
        updateConnectionMethods();
    }

    private void updateConnectionMethods() {
        updateConnectMethod();
        updateDisconnectMethod();
    }

    private void updateConnectMethod() {
        boolean enable = connector !=null;
        connectionMethodComboBox.setEnabled(enable);
        connectionMethodComboBox.removeAllItems();

        if(enable){

            java.util.List<java.util.List<Method>> methods = ToolBox.getMethodBeetween(objectPanel2.object,objectPanel1.object);

            LinkedList<MethodItem> items = new LinkedList<MethodItem>();
            for(Method s : methods.get(0)){
                items.add(new ExistingMethodItem(2, s, true, connector.getConnect(), false));
            }
            for(Method s : methods.get(1)){
                items.add(new ExistingMethodItem(3, s, false, connector.getConnect(), false));
            }


//            items.add(nothingToConnect);

            MethodItem current = getMethodItemOfConnect();
            if( ! items.contains(current)){
                items.add(current);
            }

            Collections.sort(items);

            for(MethodItem item : items){
                connectionMethodComboBox.addItem(item);
            }
            updateConnectSelection();
        }
    }

    private void updateConnectSelection(){
        connectionMethodComboBox.setSelectedItem(getMethodItemOfConnect());
        connectionMethodComboBox.setForeground(connectionMethodComboBox.getSelectedItem() instanceof NotFoundMethodItem ? Colors.error : Colors.normal);
    }

    private MethodItem getMethodItemOfConnect(){
        XMLTree Method = connector.getConnect();
        boolean fromOutput = Method.getAttribute("from").equals("output");
        String methodName = Method.getAttribute("method");
        boolean toNull = Method.getAttribute("to").equals("null");
        return new NotFoundMethodItem(1, methodName, fromOutput, connector.getConnect(), toNull);
    }

    private void updateDisconnectMethod() {
        boolean enable = connector !=null;
        disconnectionMethodComboBox.setEnabled(enable);
        disconnectionMethodComboBox.removeAllItems();

        if(enable){

            java.util.List<java.util.List<Method>> methods = ToolBox.getMethodBeetween(objectPanel2.object,objectPanel1.object);

            LinkedList<MethodItem> items = new LinkedList<MethodItem>();
            for(Method s : methods.get(0)){
                items.add(new ExistingMethodItem(2, s, true, connector.getDisconnect(), false));
                items.add(new ExistingMethodItem(2, s, true, connector.getDisconnect(), true));
            }
            for(Method s : methods.get(1)){
                items.add(new ExistingMethodItem(3, s, false, connector.getDisconnect(), false));
                items.add(new ExistingMethodItem(3, s, false, connector.getDisconnect(), true));
            }


            items.add(nothingToDisconnect);

            MethodItem current = getMethodItemOfDisconnect();
            if( ! items.contains(current)){
                items.add(current);
            }

            Collections.sort(items);

            for(MethodItem item : items){
                disconnectionMethodComboBox.addItem(item);
            }
            updateDisconnectSelection();
        }
    }

    private void updateDisconnectSelection(){
        disconnectionMethodComboBox.setSelectedItem(getMethodItemOfDisconnect());
        disconnectionMethodComboBox.setForeground(disconnectionMethodComboBox.getSelectedItem() instanceof NotFoundMethodItem ? Colors.error : Colors.normal);
    }

    private MethodItem getMethodItemOfDisconnect(){
        XMLTree Method = connector.getDisconnect();
        if(Method.getAttribute("from").equals("null")){
            return nothingToDisconnect;
        }
        boolean fromOutput = Method.getAttribute("from").equals("output");
        String methodName = Method.getAttribute("method");
        boolean toNull = Method.getAttribute("to").equals("null");
        return new NotFoundMethodItem(1, methodName, fromOutput, connector.getDisconnect(), toNull);
    }

    private MethodItem nothingToDisconnect = new NotFoundMethodItem(0, "", false, null, true){

        @Override
        public String toString() {
            return "No method";
        }

        @Override
        public void appply(){
            connector.getDisconnect().setAttribute("method", "");
            connector.getDisconnect().setAttribute("from", "null");
            connector.getDisconnect().setAttribute("to", "null");
        }
    };


    private class NotFoundMethodItem extends MethodItem{

        boolean fromOutput;
        String method;
        XMLTree methodXML;
        boolean nullArg;


        public NotFoundMethodItem(int order, String m, boolean fromOutput, XMLTree methodXML, boolean nullArg) {
            super(order);
            method = m;
            this.fromOutput = fromOutput;
            this.methodXML = methodXML;
            this.nullArg = nullArg;
        }

        @Override
        public String getMethodName() {
            return method;
        }

        @Override
        public String getMethodParam() {
            return nullArg?"null" : fromOutput?"input":"output";
        }

        @Override
        public String getMethodCaller(){
            return fromOutput?"output":"input";
        }

        public void appply(){
            methodXML.setAttribute("method", getMethodName());
            methodXML.setAttribute("from", getMethodCaller());
            methodXML.setAttribute("to", nullArg?"null" : fromOutput?"input":"output");
        }
    };


    private class ExistingMethodItem extends MethodItem{

        boolean fromOutput;
        Method method;
        XMLTree methodXML;
        boolean nullArg;

        ExistingMethodItem(int order, Method m, boolean fromOutput, XMLTree methodXML, boolean nullArg){
            super(order);
            this.fromOutput = fromOutput;
            this.method = m;
            this.methodXML = methodXML;
            this.nullArg = nullArg;
        }

        @Override
        public String getMethodName(){
            return method.getName();
        }

        @Override
        public String getMethodParam(){
            return nullArg? "null" : method.getParameterTypes()[0].getSimpleName();
        }

        @Override
        public String getMethodCaller(){
            return fromOutput?"output":"input";
        }

        @Override
        public String toString() {
            return method.getDeclaringClass().getSimpleName()+ "." + getMethodName() + "(" + getMethodParam() + ")";
        }


        public void appply(){
            methodXML.setAttribute("method", getMethodName());
            methodXML.setAttribute("from", getMethodCaller());
            methodXML.setAttribute("to", nullArg?"null" : fromOutput?"input":"output");
        }
    }

    @Override
    public void update() {
        jList1.clearSelection();
        setCurrentModule(null);
        updateStyleCombobox();
        objectPanel1.update();
        objectPanel2.update();
    }

    @Override
    public void reload() {
        jList1.setEnabled(false);
        update();
        model.reload();
        jList1.setEnabled(true);
    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        newConnectorButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("modular.edit.connector.new")  ;
        deleteConnectorButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("modular.edit.connector.delete")  ;
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        nameLabel = new javax.swing.JLabel();
        nameTextField = new javax.swing.JTextField();
        styleComboBox = new javax.swing.JComboBox();
        styleLabel = new javax.swing.JLabel();
        inputPanel = new javax.swing.JPanel();
        objectPanel1 = new greta.application.modular.tools.ObjectPanel();
        outputPanel = new javax.swing.JPanel();
        objectPanel2 = new greta.application.modular.tools.ObjectPanel();
        connectPanel = new javax.swing.JPanel();
        connectionMethodComboBox = new javax.swing.JComboBox();
        uniqueCheckBox = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBox("modular.edit.connector.unique")
        ;
        disconnectPanel = new javax.swing.JPanel();
        disconnectionMethodComboBox = new javax.swing.JComboBox();

        newConnectorButton.setText("New");
        newConnectorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newConnectorButtonActionPerformed(evt);
            }
        });

        deleteConnectorButton.setText("Delete");
        deleteConnectorButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteConnectorButtonActionPerformed(evt);
            }
        });

        jList1.setModel(model);
        jScrollPane1.setViewportView(jList1);

        nameLabel.setText("Name");

        styleComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        styleComboBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                styleComboBoxActionPerformed(evt);
            }
        });

        styleLabel.setText("Style");

        inputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Input"));

        javax.swing.GroupLayout inputPanelLayout = new javax.swing.GroupLayout(inputPanel);
        inputPanel.setLayout(inputPanelLayout);
        inputPanelLayout.setHorizontalGroup(
            inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(objectPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        inputPanelLayout.setVerticalGroup(
            inputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(inputPanelLayout.createSequentialGroup()
                .addComponent(objectPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        outputPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Output"));

        javax.swing.GroupLayout outputPanelLayout = new javax.swing.GroupLayout(outputPanel);
        outputPanel.setLayout(outputPanelLayout);
        outputPanelLayout.setHorizontalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(objectPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        outputPanelLayout.setVerticalGroup(
            outputPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(outputPanelLayout.createSequentialGroup()
                .addComponent(objectPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        connectPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Connection"));

        connectionMethodComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout connectPanelLayout = new javax.swing.GroupLayout(connectPanel);
        connectPanel.setLayout(connectPanelLayout);
        connectPanelLayout.setHorizontalGroup(
            connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(connectionMethodComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        connectPanelLayout.setVerticalGroup(
            connectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(connectPanelLayout.createSequentialGroup()
                .addComponent(connectionMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        uniqueCheckBox.setText("unique connection");
        uniqueCheckBox.setToolTipText("");
        uniqueCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                uniqueCheckBoxActionPerformed(evt);
            }
        });

        disconnectPanel.setBorder(javax.swing.BorderFactory.createTitledBorder("Disconnection"));

        disconnectionMethodComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout disconnectPanelLayout = new javax.swing.GroupLayout(disconnectPanel);
        disconnectPanel.setLayout(disconnectPanelLayout);
        disconnectPanelLayout.setHorizontalGroup(
            disconnectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(disconnectPanelLayout.createSequentialGroup()
                .addContainerGap()
                .addComponent(disconnectionMethodComboBox, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addContainerGap())
        );
        disconnectPanelLayout.setVerticalGroup(
            disconnectPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(disconnectPanelLayout.createSequentialGroup()
                .addComponent(disconnectionMethodComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap())
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(nameTextField)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(nameLabel)
                                        .addGap(0, 0, Short.MAX_VALUE)))
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(styleLabel)
                                    .addComponent(styleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(inputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(outputPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(uniqueCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addComponent(connectPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(disconnectPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newConnectorButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteConnectorButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newConnectorButton)
                    .addComponent(deleteConnectorButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane1)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nameLabel)
                            .addComponent(styleLabel))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(nameTextField, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(styleComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(uniqueCheckBox)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(inputPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(outputPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(connectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(disconnectPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 19, Short.MAX_VALUE)))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newConnectorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newConnectorButtonActionPerformed
        ConnectorListModel.ConnectorElement module = model.createConnector();
        jList1.setSelectedValue(module, true);
        setCurrentModule(module);
        nameTextField.requestFocusInWindow();
    }//GEN-LAST:event_newConnectorButtonActionPerformed

    private void styleComboBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_styleComboBoxActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_styleComboBoxActionPerformed

    private void uniqueCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_uniqueCheckBoxActionPerformed
        if(connector != null){
            connector.setUnique(uniqueCheckBox.isSelected());
        }
    }//GEN-LAST:event_uniqueCheckBoxActionPerformed

    private void deleteConnectorButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteConnectorButtonActionPerformed
        if(connector != null){
            model.deleteConnector(connector);
        }
    }//GEN-LAST:event_deleteConnectorButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel connectPanel;
    private javax.swing.JComboBox connectionMethodComboBox;
    private javax.swing.JButton deleteConnectorButton;
    private javax.swing.JPanel disconnectPanel;
    private javax.swing.JComboBox disconnectionMethodComboBox;
    private javax.swing.JPanel inputPanel;
    private javax.swing.JList jList1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JLabel nameLabel;
    private javax.swing.JTextField nameTextField;
    private javax.swing.JButton newConnectorButton;
    private greta.application.modular.tools.ObjectPanel objectPanel1;
    private greta.application.modular.tools.ObjectPanel objectPanel2;
    private javax.swing.JPanel outputPanel;
    private javax.swing.JComboBox styleComboBox;
    private javax.swing.JLabel styleLabel;
    private javax.swing.JCheckBox uniqueCheckBox;
    // End of variables declaration//GEN-END:variables

}
