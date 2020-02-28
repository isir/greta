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
import java.awt.event.ItemEvent;
import java.io.File;
import java.lang.reflect.Modifier;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JFrame;
import javax.swing.JList;

/**
 *
 * @author Andre-Marie Pez
 */
public class ObjectPanel extends javax.swing.JPanel implements Updatable{

    static String CLASS_CHANGED = "modular.ObjectPanel.class.change";
    static String LIBRARY_CHANGED = "modular.ObjectPanel.lib.change";

    XMLTree object;
    boolean checkForJFrame = false;
    boolean showInterfaces = false;
    boolean checkConstructor = true;

    ClassLoader cl;
    /**
     * Creates new form ObjectPanel
     */
    public ObjectPanel() {
        initComponents();
        jComboBox2.setModel(new DefaultComboBoxModel<ClassElement>());
        updateLibCombobox();
        jComboBox1.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    if(object !=null){
                        String oldLib = object.getAttribute("lib_id");
                        String newLib = jComboBox1.getSelectedItem().toString();
                        object.setAttribute("lib_id", jComboBox1.getSelectedItem().toString());
                        updateLibComboboxSelection();
                        firePropertyChange(LIBRARY_CHANGED, oldLib, newLib);
                    }
                }
            }
        });

        jComboBox2.addItemListener(new java.awt.event.ItemListener() {
            @Override
            public void itemStateChanged(java.awt.event.ItemEvent evt) {
                if (evt.getStateChange() == ItemEvent.SELECTED) {
                    if(object !=null){
                        String oldClass = object.getAttribute("class");
                        String newClass = ((ClassElement)jComboBox2.getSelectedItem()).getFullClassName();
                        object.setAttribute("class", newClass);
                        updateClassComboboxSelection();
                        firePropertyChange(CLASS_CHANGED, oldClass, newClass);
                    }
                }
            }
        });
        jComboBox2.setRenderer(new DefaultListCellRenderer(){
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
                Component toReturn = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if(value instanceof ClassElement){
                    toReturn.setForeground(((ClassElement)value).getColor());
                }
                return toReturn;
            }
        });
    }

    public void setCheckForJFrame(boolean checkForJFrame){
        this.checkForJFrame = checkForJFrame;
        updateClassCombobox();
    }

    public void setCheckConstructor(boolean checkConstructor){
        this.checkConstructor = checkConstructor;
        updateClassCombobox();
    }

    public void setShowInterfaces(boolean showInterfaces){
        this.showInterfaces = showInterfaces;
        updateClassCombobox();
    }

    public boolean isCurrentIsJFrame(){
        if(object == null || jComboBox2.getSelectedItem() == null){
            return false;
        }
        return ((ClassElement)jComboBox2.getSelectedItem()).isJFrame;
    }

    public void setObject(XMLTree object){
        this.object = object;
        this.setEnabled(object != null);
        updateLibComboboxSelection();
    }

    private void updateLibCombobox(){
        jComboBox1.removeAllItems();

        if(ModularXMLFile.getRoot()==null){
            return ;
        }
        LinkedList<String> libIds = new LinkedList<String>();
        for (XMLTree lib : ModularXMLFile.getLibs().getChildrenElement()) {
            if (lib.isNamed("lib")) {
                libIds.add(lib.getAttribute("id"));
            }
        }

        Collections.sort(libIds, String.CASE_INSENSITIVE_ORDER);
        libIds.addFirst("");

        for (String lib_id : libIds) {
            jComboBox1.addItem(lib_id);
        }
        updateLibComboboxSelection();
    }

    private void updateLibComboboxSelection(){

        if (object != null){
            if(((DefaultComboBoxModel)jComboBox1.getModel()).getIndexOf(object.getAttribute("lib_id"))>=0) {
                jComboBox1.setSelectedItem(object.getAttribute("lib_id"));
                jLabel1.setForeground(Colors.normal);

                loadLibrary(object.getAttribute("lib_id"));
            }
            else{
                cl = null;
                jComboBox1.setSelectedIndex(0);
                jLabel1.setForeground(Colors.error);
            }
        }
        else{
            cl = null;
            jComboBox1.setSelectedIndex(0);
            jLabel1.setForeground(Colors.normal);
        }
        updateClassCombobox();

    }

    private void updateClassCombobox(){
        jComboBox2.removeAllItems();
        if(object!=null){
            ClassElement elem = null;
            String className = object.getAttribute("class");
            XMLTree lib = ModularXMLFile.getLib(object.getAttribute("lib_id"));
            if(lib!=null && new File(lib.getAttribute("path")).exists()){
                LinkedList<ClassElement> classes = new LinkedList<ClassElement>();

                try {
                    JarFile jar = new JarFile(lib.getAttribute("path"));
                    Enumeration<JarEntry> entries = jar.entries();
                    while(entries.hasMoreElements()){
                        JarEntry entry = entries.nextElement();
                        if(entry.getName().endsWith(".class") &&  ! entry.getName().contains("$")){
                            String classNameInJar = entry.getName().substring(0, entry.getName().length()-".class".length()).replace("/", ".");
                            ClassElement element = new ClassElement(classNameInJar, true);
                            if(elem==null && classNameInJar.equalsIgnoreCase(className)){
                                elem = element;
                            }
                            if(element.mustBeShown() || elem==element){
                                classes.add(element);
                            }
                        }

                    }
                } catch (Exception ex) {}

                if(elem==null){
                    elem = new ClassElement(className, false);
                    classes.add(elem);
                }

                Collections.sort(classes);

                for (ClassElement element : classes) {
                    jComboBox2.addItem(element);
                }

                jComboBox2.setSelectedItem(elem);
            }
            else{
                elem = new ClassElement(className, false);
                jComboBox2.addItem(elem);
                jComboBox2.setSelectedItem(elem);
            }
            updateClassComboboxSelection();
        }
    }

    private void updateClassComboboxSelection(){
        jComboBox2.setForeground(((ClassElement)jComboBox2.getSelectedItem()).getColor());
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        jComboBox1.setEnabled(enabled);
        jComboBox2.setEnabled(enabled);
        jLabel1.setEnabled(enabled);
        jLabel2.setEnabled(enabled);
    }

    private void loadLibrary(String libId) {
        cl = ToolBox.getClassLoaderForLib(libId);
    }


    private class ClassElement implements Comparable<ClassElement>{
        String package_;
        String class_;

        boolean inLib;
        boolean isLoadable;
        boolean isJFrame;
        boolean hasConstructor;
        boolean isInterface;
        boolean isAbstract;

        public ClassElement(String fullClassName, boolean inlib) {
            int lastDot = fullClassName.lastIndexOf(".");
            if(lastDot>0){
                package_ = fullClassName.substring(0, lastDot);
            }
            class_ = fullClassName.substring(lastDot+1);
            this.inLib = inlib;
            isLoadable = false;
            isJFrame = false;
            hasConstructor = false;
            isInterface = false;
            isAbstract = false;
            try{
                Class clazz = cl.loadClass(fullClassName);
                isLoadable = true;
                isJFrame = JFrame.class.isAssignableFrom(clazz);
                isInterface = clazz.isInterface();
                isAbstract = Modifier.isAbstract(clazz.getModifiers());
                if(!isInterface && !isAbstract && checkConstructor){
                    //search a public empty constructor
                    int modifiers = clazz.getConstructor().getModifiers();
                    hasConstructor = Modifier.isPublic(modifiers);
                }
            } catch(Throwable e){}
        }

        public boolean mustBeShown(){
            return ((isInterface || isAbstract)&& showInterfaces) || (((!checkConstructor) || hasConstructor) && ((!checkForJFrame) || isJFrame));
        }

        @Override
        public String toString() {
            String desc = getDescription();
            String mess = getMessage();
            if(mess!=null){
                desc += " - "+mess;
            }
            return desc;
        }

        String getDescription(){
            if(package_==null){
                return class_;
            }
            return class_+" ("+package_+")";
        }

        public String getFullClassName(){
            if(package_==null){
                return class_;
            }
            return package_+"."+class_;
        }

        @Override
        public int compareTo(ClassElement o) {
            if(isInterface && !o.isInterface){
                return -1;
            }
            if(!isInterface && o.isInterface){
                return 1;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(class_, o.class_);
        }

        public Color getColor(){
            if(checkForJFrame && !isJFrame){
                return Colors.error;
            }
            if(!isLoadable){
                if(!inLib){
                    return Colors.error;
                }
                return Colors.warning;
            }
            if(!isInterface && checkConstructor && !hasConstructor){
                return Colors.error;
            }
            if(!inLib){
                return Colors.warning;
            }
            if(isInterface){
                return Colors.interface_;
            }
            if(isAbstract){
                return Colors.abstract_;
            }
            return Colors.normal_;
        }

        public String getMessage(){
            if(!isLoadable && !inLib){
                return "doesn't exist";
            }
            if(checkForJFrame && !isJFrame){
                return "is not a JFrame";
            }
            if(!isLoadable){
                return "can not be loaded";
            }
            if(!isInterface && checkConstructor && !hasConstructor){
                return "needs an empty and public constructor";
            }
            if(!inLib){
                return "is not in this library";
            }
            return null;
        }
    }

    @Override
    public void update() {
        updateLibCombobox();
    }

    @Override
    public void reload() {

    }


    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jLabel1 = new javax.swing.JLabel();
        jComboBox1 = new javax.swing.JComboBox();
        jComboBox2 = new javax.swing.JComboBox();
        jLabel2 = new javax.swing.JLabel();

        jLabel1.setText("Lib");

        jComboBox1.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jComboBox2.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));

        jLabel2.setText("Class");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jLabel1))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jLabel2)
                        .addGap(0, 0, Short.MAX_VALUE))
                    .addComponent(jComboBox2, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jLabel1)
                    .addComponent(jLabel2))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jComboBox1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jComboBox2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, Short.MAX_VALUE))
        );
    }// </editor-fold>//GEN-END:initComponents


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox jComboBox1;
    private javax.swing.JComboBox jComboBox2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    // End of variables declaration//GEN-END:variables
}
