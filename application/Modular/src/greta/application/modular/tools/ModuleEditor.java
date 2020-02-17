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
import java.awt.Color;
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
import javax.swing.GroupLayout;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *
 * @author Andre-Marie Pez
 */
public class ModuleEditor extends javax.swing.JPanel implements Updatable{

    private Color originalButtonColor;
    ModuleListModel model  = new ModuleListModel();
    ModuleListModel.ModuleElement module;

    /**
     * Creates new form ModuleEditor
     */
    public ModuleEditor() {
        initComponents();
        setName(IniManager.getLocaleProperty("modular.edit.module"));
        jList1.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        jList1.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                if( ! jTextField1.hasFocus()){
                    ModuleListModel.ModuleElement newSelection = (ModuleListModel.ModuleElement)jList1.getSelectedValue();
                    if(newSelection != module){
                        setCurrentModule(newSelection);
                    }
                }
            }
        });

        jTextField1.getDocument().addDocumentListener(new DocumentListener() {
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

        updateCombobox();

        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    if(module !=null){
                        module.setStyle(evt.getItem().toString());
                    }
                }
            }

        });
        jList1.setCellRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component toReturn = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value == null || ! (value instanceof ModuleListModel.ModuleElement)){
                    return toReturn;
                }
                ModuleListModel.ModuleElement element = (ModuleListModel.ModuleElement)value;
                boolean unicname = true;
                if(index > 0){
                    unicname = ! (model.getElementAt(index-1).getName().equals(element.getName()));
                }
                if(index < model.getSize()-1){
                    unicname = unicname && ! (model.getElementAt(index+1).getName().equals(element.getName()));
                }
                boolean valid = ModularXMLFile.checkOneModule(element.module);
                toReturn.setForeground(valid ? (unicname ? toReturn.getForeground() : Colors.warning) : Colors.error);
                return toReturn;
            }

        });
        objectPanel2.setCheckForJFrame(true);
        objectPanel1.addPropertyChangeListener(ObjectPanel.CLASS_CHANGED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                jRadioButton2.setEnabled(objectPanel1.isCurrentIsJFrame());
                updateFrameLinkMethod();
            }
        });
        objectPanel2.addPropertyChangeListener(ObjectPanel.CLASS_CHANGED, new PropertyChangeListener() {

            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updateFrameLinkMethod();
            }
        });

        jComboBox2.setModel(new DefaultComboBoxModel<MethodItem>());
        jComboBox2.setRenderer(new DefaultListCellRenderer(){

            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component toRet = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus); //To change body of generated methods, choose Tools | Templates.
                if(value instanceof NotFoundMethodItem){
                    toRet.setForeground(Colors.error);
                }
                return toRet;
            }

        });
        jComboBox2.addItemListener(new ItemListener() {

            @Override
            public void itemStateChanged(ItemEvent e) {
                if(jComboBox2.isValid() && jComboBox2.getSelectedItem() !=null){
                    ((MethodItem)jComboBox2.getSelectedItem()).appply();
                    jComboBox2.setForeground(jComboBox2.getSelectedItem() instanceof NotFoundMethodItem ? Colors.error : Colors.normal);
                }
            }
        });
        originalButtonColor = jRadioButton2.getForeground();
        jSpinner1.setModel(new SpinnerNumberModel(0, 0, 100, 1));
        jSpinner1.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                if(module != null){
                    int value = ((Integer)jSpinner1.getValue()).intValue();
                    if(value<=0){
                        module.module.setAttribute("restrict", null);
                    }
                    else{
                        module.module.setAttribute("restrict", ""+value);
                    }
                }
            }
        });
        setCurrentModule(null);
    }

    private void updateModuleName() {
        if(module != null){
            module.setName(jTextField1.getText());
            jList1.setSelectedValue(module, true);
        }
    }

    private void updateCombobox(){
        LinkedList<String> styleNames = new LinkedList<String>();
        for (XMLTree styles : ModularXMLFile.getStyles().getChildrenElement()) {
            if (styles.isNamed("style")) {
                styleNames.add(styles.getAttribute("name"));
            }
        }
        Collections.sort(styleNames, String.CASE_INSENSITIVE_ORDER);
        styleNames.addFirst("");
        jComboBox1.removeAllItems();

        for (String styleName : styleNames) {
            jComboBox1.addItem(styleName);
        }
        updateComboboxSelection();
    }

    private void updateComboboxSelection(){

        if (module != null && ((DefaultComboBoxModel)jComboBox1.getModel()).getIndexOf(module.getStyle())>=0) {
            jComboBox1.setSelectedItem(module.getStyle());
        }
        else{
            jComboBox1.setSelectedIndex(0);
        }
    }

    private void setCurrentModule(ModuleListModel.ModuleElement module){
        this.module = null;

        boolean enable = module!=null;
        jLabel1.setEnabled(enable);
        jLabel2.setEnabled(enable);
        jPanel1.setEnabled(enable);
        jPanel2.setEnabled(enable);
        jTextField1.setEnabled(enable);
        objectPanel1.setEnabled(enable);
        jRadioButton1.setEnabled(enable);
        jRadioButton2.setEnabled(enable);
        jRadioButton3.setEnabled(enable);
        deleteModuleButton.setEnabled(enable);
        jComboBox1.setEnabled(enable);
        jLabel4.setEnabled(enable);
        jSpinner1.setEnabled(enable);
        jCheckBox2.setEnabled(enable);
        if(module==null){
            jTextField1.setText("");

            jRadioButton1.setSelected(false);
            objectPanel2.setEnabled(false);
            jCheckBox1.setEnabled(false);
            jCheckBox1.setSelected(false);
            objectPanel1.setObject(null);
            objectPanel2.setObject(null);
            jSpinner1.setValue(0);
        }
        else{
            jTextField1.setText(module.getName());
            module.updateNameChanger(jCheckBox2.isSelected());
            int frameType = module.getFrameType();
            jRadioButton1.setSelected(frameType==0);
            jRadioButton2.setSelected(frameType==1);
            jRadioButton3.setSelected(frameType==2);
            objectPanel2.setEnabled(frameType==2);
            jCheckBox1.setEnabled(frameType!=0);
            jCheckBox1.setSelected(module.isWindowed());
            objectPanel1.setObject(module.getObject());
            objectPanel2.setObject(frameType==2 ? module.getFrame() : null);
            jRadioButton2.setEnabled(objectPanel1.isCurrentIsJFrame());
            jRadioButton2.setForeground(jRadioButton2.isSelected() && !jRadioButton2.isEnabled() ? Colors.error : originalButtonColor);
            String restrict = module.module.getAttribute("restrict");
            jSpinner1.setValue(restrict==null || restrict.isEmpty()? 0 : Integer.parseInt(restrict));
        }
        this.module = module;
        updateComboboxSelection();
        updateFrameLinkMethod();
        updateParametersList();
    }

    public void updateParametersList(){
        boolean enable = module!=null;
        jPanel3.setEnabled(enable);
        jButton1.setEnabled(enable);
        JPanel content = new JPanel();
        if(enable){
            LinkedList<ParameterPanel> parameters = new LinkedList<ParameterPanel>();
            for(XMLTree child : module.module.getChildrenElement()){
                if(child.isNamed("parameter")){
                    ParameterPanel paramPanel = new ParameterPanel(child, this);
                    paramPanel.setObject(module.getObject());
                    paramPanel.setFrame(module.getFrame());
                    parameters.add(paramPanel);

                }
            }
            if( ! parameters.isEmpty()){
                javax.swing.GroupLayout layout = new javax.swing.GroupLayout(content);
                content.setLayout(layout);
                GroupLayout.ParallelGroup horizontal = layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING);
                GroupLayout.SequentialGroup vertical = layout.createSequentialGroup();
                for(ParameterPanel param : parameters){
                    horizontal = horizontal.addComponent(param, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE);
                    vertical = vertical.addComponent(param, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED);
                }
                layout.setHorizontalGroup(horizontal);
                layout.setVerticalGroup(
                    layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(vertical)
                );
            }
        }

        jScrollPane2.setViewportView(content);
    }

    public void updateFrameLinkMethod(){
        jComboBox2.setEnabled(objectPanel2.isCurrentIsJFrame());
        jLabel3.setEnabled(jComboBox2.isEnabled());

        jComboBox2.removeAllItems();

        if(jComboBox2.isEnabled()){

            java.util.List<java.util.List<Method>> methods = ToolBox.getMethodBeetween(objectPanel2.object,objectPanel1.object);

            LinkedList<MethodItem> items = new LinkedList<MethodItem>();
            for(Method s : methods.get(0)){
                items.add(new ExistingMethodItem(2, s, true));
            }
            for(Method s : methods.get(1)){
                items.add(new ExistingMethodItem(3, s, false));
            }


            items.add(nothing);

            MethodItem current = getMethodItemOfCurrentLink();
            if( ! items.contains(current)){
                items.add(current);
            }

            Collections.sort(items);

            for(MethodItem item : items){
                jComboBox2.addItem(item);
            }
            updateSelection();
        }

    }

    private void updateSelection(){
        jComboBox2.setSelectedItem(getMethodItemOfCurrentLink());
        jComboBox2.setForeground(jComboBox2.getSelectedItem() instanceof NotFoundMethodItem ? Colors.error : Colors.normal);
    }

    private MethodItem getMethodItemOfCurrentLink(){
        XMLTree link = objectPanel2.object.findNodeCalled("link");
        if(link == null){
            return nothing;
        }
        else{
            boolean fromFrame = link.getAttribute("on").equals("frame");
            String methodName = link.getAttribute("method");
            return new NotFoundMethodItem(1, methodName, fromFrame);
        }
    }

    private MethodItem nothing = new NotFoundMethodItem(0, "", false){

        @Override
        public String toString() {
            return " ";
        }

        @Override
        public void appply() {
            XMLTree link = objectPanel2.object.findNodeCalled("link");
            if(link != null){
                objectPanel2.object.removeChild(link);
            }
        }
    };

    private class NotFoundMethodItem extends MethodItem{

        boolean fromFrame;
        String method;

        public NotFoundMethodItem(int order, String m, boolean fromFrame) {
            super(order);
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

        @Override
        public String getMethodCaller() {
            return fromFrame?"frame":"object";
        }

        public void appply(){
            XMLTree link = objectPanel2.object.findNodeCalled("link");
            if(link == null){
                link = objectPanel2.object.createChild("link");
            }
            link.setAttribute("method", getMethodName());
            link.setAttribute("on", getMethodCaller());
        }
    };


    private class ExistingMethodItem extends MethodItem{

        boolean fromFrame;
        Method method;

        ExistingMethodItem(int order, Method m, boolean fromFrame){
            super(order);
            this.fromFrame = fromFrame;
            this.method = m;
        }

        @Override
        public String getMethodName(){
            return method.getName();
        }

        @Override
        public String getMethodParam(){
            return method.getParameterTypes()[0].getName();
        }

        @Override
        public String getMethodCaller(){
            return fromFrame?"frame":"object";
        }

        public void appply(){
            XMLTree link = objectPanel2.object.findNodeCalled("link");
            if(link == null){
                link = objectPanel2.object.createChild("link");
            }
            link.setAttribute("method", getMethodName());
            link.setAttribute("on", getMethodCaller());
        }
    }

    @Override
    public void update() {
        jList1.clearSelection();
        setCurrentModule(null);
        updateCombobox();
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

        buttonGroup1 = new javax.swing.ButtonGroup();
        newModuleButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("modular.edit.module.new")  ;
        deleteModuleButton = new greta.core.utilx.gui.ToolBox.LocalizedJButton("modular.edit.module.delete")  ;
        jScrollPane1 = new javax.swing.JScrollPane();
        jList1 = new javax.swing.JList();
        jLabel1 = new javax.swing.JLabel();
        jTextField1 = new javax.swing.JTextField();
        jComboBox1 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        objectPanel1 = new greta.application.modular.tools.ObjectPanel();
        jPanel2 = new javax.swing.JPanel();
        jRadioButton1 = new javax.swing.JRadioButton();
        jRadioButton2 = new javax.swing.JRadioButton();
        jRadioButton3 = new javax.swing.JRadioButton();
        objectPanel2 = new greta.application.modular.tools.ObjectPanel();
        jCheckBox1 = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBox("modular.edit.module.windowed")
        ;
        jLabel3 = new javax.swing.JLabel();
        jComboBox2 = new javax.swing.JComboBox();
        jPanel3 = new javax.swing.JPanel();
        jButton1 = new greta.core.utilx.gui.ToolBox.LocalizedJButton("GUI.add");
        jScrollPane2 = new javax.swing.JScrollPane();
        jLabel4 = new greta.core.utilx.gui.ToolBox.LocalizedJLabel("modular.edit.module.restrict");
        jSpinner1 = new javax.swing.JSpinner();
        jCheckBox2 = new javax.swing.JCheckBox();

        newModuleButton.setText("New");
        newModuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newModuleButtonActionPerformed(evt);
            }
        });

        deleteModuleButton.setText("Delete");
        deleteModuleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                deleteModuleButtonActionPerformed(evt);
            }
        });

        jList1.setModel(model);
        jScrollPane1.setViewportView(jList1);

        jLabel1.setText("Name");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        jComboBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jComboBox1ActionPerformed(evt);
            }
        });

        jLabel2.setText("Style");

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder(greta.core.util.IniManager.getLocaleProperty("word.object")
        ));

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(objectPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(objectPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        jPanel2.setBorder(javax.swing.BorderFactory.createTitledBorder(greta.core.util.IniManager.getLocaleProperty("word.window")
        ));

        buttonGroup1.add(jRadioButton1);
        jRadioButton1.setSelected(true);
        jRadioButton1.setText("None");
        jRadioButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton1ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton2);
        jRadioButton2.setText("Object");
        jRadioButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton2ActionPerformed(evt);
            }
        });

        buttonGroup1.add(jRadioButton3);
        jRadioButton3.setText("Other");
        jRadioButton3.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jRadioButton3ActionPerformed(evt);
            }
        });

        jCheckBox1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox1ActionPerformed(evt);
            }
        });

        jLabel3.setText("link method");

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        javax.swing.GroupLayout jPanel2Layout = new javax.swing.GroupLayout(jPanel2);
        jPanel2.setLayout(jPanel2Layout);
        jPanel2Layout.setHorizontalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jRadioButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton2)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jRadioButton3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jCheckBox1))
            .addComponent(objectPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addComponent(jLabel3)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
        );
        jPanel2Layout.setVerticalGroup(
            jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel2Layout.createSequentialGroup()
                .addGroup(jPanel2Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jRadioButton1)
                    .addComponent(jRadioButton2)
                    .addComponent(jRadioButton3)
                    .addComponent(jCheckBox1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(objectPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(jLabel3)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
        );

        jPanel3.setBorder(javax.swing.BorderFactory.createTitledBorder(greta.core.util.IniManager.getLocaleProperty("word.parameter")));

        jButton1.setText("Add");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jScrollPane2.setBorder(null);

        javax.swing.GroupLayout jPanel3Layout = new javax.swing.GroupLayout(jPanel3);
        jPanel3.setLayout(jPanel3Layout);
        jPanel3Layout.setHorizontalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jButton1)
                .addGap(0, 0, Short.MAX_VALUE))
            .addComponent(jScrollPane2)
        );
        jPanel3Layout.setVerticalGroup(
            jPanel3Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel3Layout.createSequentialGroup()
                .addComponent(jButton1)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jScrollPane2))
        );

        jLabel4.setText("Restriction");

        jCheckBox2.setSelected(true);
        jCheckBox2.setText("Apply rename everywhere");
        jCheckBox2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jCheckBox2ActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 200, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel1)
                                    .addGroup(layout.createSequentialGroup()
                                        .addComponent(jTextField1)
                                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                        .addComponent(jCheckBox2)))
                                .addGap(18, 18, 18)
                                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addComponent(jLabel2)
                                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                            .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel2, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(jLabel4)
                                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                                .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                                .addGap(0, 0, Short.MAX_VALUE))))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(newModuleButton)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(deleteModuleButton)
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(newModuleButton)
                    .addComponent(deleteModuleButton))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jCheckBox2))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jLabel4)
                            .addComponent(jSpinner1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(jPanel3, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                    .addComponent(jScrollPane1))
                .addContainerGap())
        );
    }// </editor-fold>//GEN-END:initComponents

    private void newModuleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newModuleButtonActionPerformed
        ModuleListModel.ModuleElement module = model.createModule();
        jList1.setSelectedValue(module, true);
        setCurrentModule(module);
        jTextField1.requestFocusInWindow();
    }//GEN-LAST:event_newModuleButtonActionPerformed

    private void jRadioButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton1ActionPerformed
        if(module != null){
            module.setFrameType(0);
        }
        jRadioButton2.setForeground(jRadioButton2.isSelected() && !jRadioButton2.isEnabled() ? Colors.error : originalButtonColor);
        objectPanel2.setEnabled(false);
        jCheckBox1.setEnabled(false);
        jCheckBox1.setSelected(false);
        objectPanel2.setObject(null);
        updateFrameLinkMethod();
    }//GEN-LAST:event_jRadioButton1ActionPerformed

    private void jRadioButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton2ActionPerformed
        if(module != null){
            module.setFrameType(1);
        }
        jRadioButton2.setForeground(jRadioButton2.isSelected() && !jRadioButton2.isEnabled() ? Colors.error : originalButtonColor);
        objectPanel2.setEnabled(false);
        jCheckBox1.setEnabled(true);
        objectPanel2.setObject(null);
        updateFrameLinkMethod();
    }//GEN-LAST:event_jRadioButton2ActionPerformed

    private void jRadioButton3ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jRadioButton3ActionPerformed
        if(module != null){
            module.setFrameType(2);
        }
        jRadioButton2.setForeground(jRadioButton2.isSelected() && !jRadioButton2.isEnabled() ? Colors.error : originalButtonColor);
        objectPanel2.setEnabled(true);
        jCheckBox1.setEnabled(true);
        objectPanel2.setObject(module.getFrame());
        updateFrameLinkMethod();
    }//GEN-LAST:event_jRadioButton3ActionPerformed

    private void jComboBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jComboBox1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jComboBox1ActionPerformed

    private void jCheckBox1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox1ActionPerformed
        if(module != null){
            module.setWindowed(jCheckBox1.isSelected());
        }
    }//GEN-LAST:event_jCheckBox1ActionPerformed

    private void deleteModuleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteModuleButtonActionPerformed
        if(module != null){
            model.deleteModule(module);
        }
    }//GEN-LAST:event_deleteModuleButtonActionPerformed

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        if(module != null){
            int paramCount = 1;
            for(XMLTree child : module.module.getChildrenElement()){
                if(child.isNamed("parameter")){
                    paramCount ++;
                }
            }
            XMLTree param = module.module.createChild("parameter");
            param.setAttribute("name", "parameter "+paramCount);
            param.setAttribute("type", PrimitiveTypes.getNameOf(String.class)); //default type ?
            param.setAttribute("default", "");
            param.setAttribute("set_on", "object");
            param.setAttribute("set_method", "");
            param.setAttribute("get_on", "object");
            param.setAttribute("get_method", "");

            updateParametersList();
        }
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jCheckBox2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jCheckBox2ActionPerformed
        if(module != null){
            module.updateNameChanger(jCheckBox1.isSelected());
        }
    }//GEN-LAST:event_jCheckBox2ActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.ButtonGroup buttonGroup1;
    private javax.swing.JButton deleteModuleButton;
    private javax.swing.JButton jButton1;
    private javax.swing.JCheckBox jCheckBox1;
    private javax.swing.JCheckBox jCheckBox2;
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JLabel jLabel3;
    private javax.swing.JLabel jLabel4;
    private javax.swing.JList jList1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JPanel jPanel3;
    private javax.swing.JRadioButton jRadioButton1;
    private javax.swing.JRadioButton jRadioButton2;
    private javax.swing.JRadioButton jRadioButton3;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSpinner jSpinner1;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JButton newModuleButton;
    private greta.application.modular.tools.ObjectPanel objectPanel1;
    private greta.application.modular.tools.ObjectPanel objectPanel2;
    // End of variables declaration//GEN-END:variables

}
