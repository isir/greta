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

import greta.application.modular.compilation.JarMaker;
import greta.application.modular.compilation.ZipMaker;
import greta.application.modular.modules.Library;
import greta.application.modular.modules.Style;
import greta.application.modular.tools.ConnectorEditor;
import greta.application.modular.tools.EditorFrame;
import greta.application.modular.tools.LibEditor;
import greta.application.modular.tools.MenuEditor;
import greta.application.modular.tools.ModuleEditor;
import greta.application.modular.tools.StyleEditor;
import greta.core.util.IniManager;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JOptionPane;

/**
 *
 * @author Andre-Marie Pez
 */
public class ModularWindow extends javax.swing.JFrame {

    private ModuleGraph moduleGraph;
    private String title = "Modular";
    private File configsDirectory = new File("./Configurations");
    private File currentConfigFile;
    private String currentConfigSimpleName = "";
    private EditorFrame editor;

    private int valid_x;
    private int valid_y;
    private int valid_w;
    private int valid_h;


    /** Creates new form ModularWindow */
    public ModularWindow() {
        initComponents();
        moduleGraph.setInternalFrame(internalFrame);
        openDialog.removeChoosableFileFilter(openDialog.getAcceptAllFileFilter());
        openDialog.setAcceptAllFileFilterUsed(false);
        openDialog.addChoosableFileFilter(new ModularXMLFileFilter());
        setIconImage(Modular.icon);
        compileConfigItem.setEnabled(JarMaker.isCompilationEnabled());
        jMenuBar1.add(buildOptionMenu());
        moduleGraph.parentFrame = this;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                ModularWindow.this.savePositions();
            }
        });

        addComponentListener(new ComponentListener() {
            @Override
            public void componentResized(ComponentEvent e) {
                if(getExtendedState()==0){
                    valid_w = getWidth();
                    valid_h = getHeight();
                }
            }
            @Override
            public void componentMoved(ComponentEvent e) {
                if(getExtendedState()==0){
                    valid_x = getX();
                    valid_y = getY();
                }
            }
            @Override
            public void componentShown(ComponentEvent e) {}
            @Override
            public void componentHidden(ComponentEvent e) {}
        });


//        editConnectorMenuItem.setEnabled(false);
//        editLibMenuItem.setEnabled(false);
//        editMenuMenuItem.setEnabled(false);
//        editModuleMenuItem.setEnabled(false);
//        editStyleMenuItem.setEnabled(false);
    }

    public ModuleGraph getGraph(){
        return moduleGraph;
    }

    protected void savePositions(){
        ModularSateIO.setPosition(valid_x, valid_y);
        ModularSateIO.setDimention(valid_w, valid_h);
        ModularSateIO.setDividerPosition(jSplitPane1.getDividerLocation());
        int state = ModularWindow.this.getExtendedState();
        ModularSateIO.setVerticalMaximized((state & java.awt.Frame.MAXIMIZED_VERT) == java.awt.Frame.MAXIMIZED_VERT);
        ModularSateIO.setHorizontalMaximized((state & java.awt.Frame.MAXIMIZED_HORIZ) == java.awt.Frame.MAXIMIZED_HORIZ);
        if(currentConfigFile!=null) {
            ModularSateIO.setLastFile(currentConfigFile.getAbsolutePath());
        }
        else{
            ModularSateIO.setLastFile("");
        }
    }

    protected void loadPositions(){
        //load states
        java.awt.Point p = ModularSateIO.getPosition();
        if(p!=null) { setLocation(p); valid_x=p.x; valid_y=p.y; }
        java.awt.Dimension d = ModularSateIO.getDimension();
        if(d!=null) { setSize(d); valid_w=d.width; valid_h=d.height; }
        int divPos = ModularSateIO.getDividerPosition();
        int state =
                (ModularSateIO.isHorizontalMaximized() ? java.awt.Frame.MAXIMIZED_HORIZ:0)+
                (ModularSateIO.isVerticalMaximized() ? java.awt.Frame.MAXIMIZED_VERT:0);
        setExtendedState(state);
        if(divPos!=0){ jSplitPane1.setDividerLocation(divPos); }
    }

    private javax.swing.JMenu buildLAFMenu(){
        javax.swing.JMenu looks = new greta.core.utilx.gui.ToolBox.LocalizedJMenu("modular.look");
        javax.swing.JMenu styles = new greta.core.utilx.gui.ToolBox.LocalizedJMenu("Graph");
        final List<JCheckBoxMenuItem> mapperItems = new ArrayList<JCheckBoxMenuItem>();
        for(Style.Mapper mapper : Style.getMappers()){
            final Style.Mapper finalMapper = mapper;
            final javax.swing.JCheckBoxMenuItem mapperItem = new javax.swing.JCheckBoxMenuItem(finalMapper.getName());
            mapperItems.add(mapperItem);
            mapperItem.addActionListener(new java.awt.event.ActionListener(){

                @Override
                public void actionPerformed(ActionEvent e) {
                    Style.setMapper(finalMapper);
                    getGraph().updateStyles();
                    ModularSateIO.setStyleMapper(finalMapper);
                    for(JCheckBoxMenuItem item : mapperItems){
                        item.setSelected(mapperItem==item);
                    }
                }

            });
            mapperItem.setSelected(Style.getMapper()==finalMapper);
            styles.add(mapperItem);
        }

        looks.add(styles);
        javax.swing.JMenu lafs = new greta.core.utilx.gui.ToolBox.LocalizedJMenu("Java");
        final List<JCheckBoxMenuItem> lafItems = new ArrayList<JCheckBoxMenuItem>();
        for (final javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            final javax.swing.JCheckBoxMenuItem laf = new javax.swing.JCheckBoxMenuItem(info.getName());
            lafItems.add(laf);
            laf.addActionListener(new java.awt.event.ActionListener(){
                @Override
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        javax.swing.UIManager.setLookAndFeel(info.getClassName());
                        javax.swing.SwingUtilities.updateComponentTreeUI(ModularWindow.this);
                        internalFrame.setBorder(null);
                        internalFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
                        internalFrame.setDoubleBuffered(true);
                        internalFrame.setFrameIcon(null);
                        for(greta.application.modular.modules.Module module : moduleGraph.getModules()){
                            if(module.getFrame()!=null){
                                Dimension lastSize = module.getFrame().getSize();
                                javax.swing.SwingUtilities.updateComponentTreeUI(module.getFrame());
                                module.getFrame().pack();
                                module.getFrame().setSize(lastSize);
                            }
                        }
                        ModularSateIO.setLookAndFeel(info.getClassName());
                        for(JCheckBoxMenuItem item : lafItems){
                            item.setSelected(item == laf);
                        }
                    }
                    catch (Exception ex) {ex.printStackTrace();}
                }
            });
            laf.setSelected(info.getClassName().equals(ModularSateIO.getLookAndFeel()));
            laf.setEnabled( ! Modular.isProblematicLAF(info));
            lafs.add(laf);
        }
        looks.add(lafs);
        return looks;
    }

    private javax.swing.JMenu buildOptionMenu(){
        javax.swing.JMenu option = new greta.core.utilx.gui.ToolBox.LocalizedJMenu("GUI.options");

        option.add(new LanguageMenu(this));
        option.add(buildLAFMenu());
        final JCheckBoxMenuItem loadLast = new greta.core.utilx.gui.ToolBox.LocalizedJCheckBoxMenuItem("modular.load.last.file");
        loadLast.setToolTipText(IniManager.getLocaleProperty("modular.load.last.file.full"));
        loadLast.setSelected(ModularSateIO.isLoadLastFile());
        loadLast.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ModularSateIO.setLoadLastFile(loadLast.isSelected());
            }
        });
        option.add(loadLast);
        return option;
    }

    @Override
    public void setLocale(Locale l) {
        super.setLocale(l);
        if(openDialog!=null) {
            openDialog.setLocale(l);
        }
    }

    protected void setCurrentConfigFile(File config){
        currentConfigFile = config;
        if(currentConfigFile==null){
            currentConfigSimpleName = "";
            setTitle(title);
        }
        else{
            if( ! currentConfigFile.getAbsolutePath().toLowerCase().endsWith(".xml")){
                currentConfigFile = new File(currentConfigFile.getAbsolutePath()+".xml");
            }
            currentConfigSimpleName = currentConfigFile.getName().substring(0, currentConfigFile.getName().length()-4);
            setTitle(title+" - "+currentConfigSimpleName);
        }

    }

    protected void openGraph(String graphFileName){
        openGraph(new File(graphFileName));
    }

    protected void openGraph(File graphFile){
        setCurrentConfigFile(graphFile);
        moduleGraph.clearGraph();
        moduleGraph.loadGraph(currentConfigFile.getAbsolutePath());
        ModularSateIO.setLastFile(currentConfigFile.getAbsolutePath());
    }

    public void checkValidity(){
        checkMenus();
        checkModules();
        checkLibs();
        checkConnectors();
        checkStyles();
    }

    private void checkMenus(){
        editMenuMenuItem.setIcon(ModularXMLFile.checkMenus() ? null : new ImageIcon(Modular.warnIcon));
    }
    private void checkModules(){
        editModuleMenuItem.setIcon(ModularXMLFile.checkModules()? null : new ImageIcon(Modular.warnIcon));
    }
    private void checkLibs(){
        editLibMenuItem.setIcon(ModularXMLFile.checkLibs()? null : new ImageIcon(Modular.warnIcon));
    }

    private void checkStyles(){
        editStyleMenuItem.setIcon(ModularXMLFile.checkStyles()? null : new ImageIcon(Modular.warnIcon));
    }

    private void checkConnectors(){
        editConnectorMenuItem.setIcon(ModularXMLFile.checkConnectors()? null : new ImageIcon(Modular.warnIcon));
    }

    private void showEditors(int paneIndex){
        if(editor == null){
            editor = new EditorFrame(this, new MenuEditor(), new ModuleEditor(), new LibEditor(), new ConnectorEditor(), new StyleEditor());
        }
        editor.selectPane(paneIndex);
        if( ! editor.isVisible()){
            Point p = this.getLocationOnScreen();
            Dimension d = this.getSize();
            Dimension d2 = editor.getSize();
            editor.setLocation(Math.max(0, p.x + (d.width-d2.width)/2), Math.max(0, p.y + (d.height-d2.height)/2));
        }
        editor.setVisible(true);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        openDialog = new javax.swing.JFileChooser();
        openDialog.setCurrentDirectory(new File("./"));
        jSplitPane1 = new javax.swing.JSplitPane();
        moduleGraph = new greta.application.modular.ModuleGraph();
        jScrollPane1 = new javax.swing.JScrollPane(moduleGraph);
        jScrollPane2 = new javax.swing.JScrollPane();
        desktopPane = new javax.swing.JDesktopPane();
        internalFrame = new javax.swing.JInternalFrame();
        jMenuBar1 = new javax.swing.JMenuBar();
        fileMenu = new greta.core.utilx.gui.ToolBox.LocalizedJMenu("GUI.file");
        newConfigItem = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("GUI.new");
        openConfigItem = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("GUI.open");
        saveConfigItem = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("GUI.save");
        saveAsConfigItem = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("GUI.saveAs");
        jSeparator1 = new javax.swing.JPopupMenu.Separator();
        compileConfigItem = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("GUI.compile");
        jSeparator2 = new javax.swing.JPopupMenu.Separator();
        exitMenuItem = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("GUI.exit");
        editMenu = new greta.core.utilx.gui.ToolBox.LocalizedJMenu("GUI.edit");
        editMenuMenuItem = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("modular.edit.menu");
        editModuleMenuItem = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("modular.edit.module");
        editLibMenuItem = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("modular.edit.lib");
        editConnectorMenuItem = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("modular.edit.connector");
        editStyleMenuItem = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("modular.edit.style");

        setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
        setTitle(title);
        setMinimumSize(new java.awt.Dimension(400, 300));

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(300);
        jSplitPane1.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);
        jSplitPane1.setResizeWeight(1.0);
        jSplitPane1.setDoubleBuffered(true);
        jSplitPane1.setLeftComponent(jScrollPane1);

        desktopPane.setDesktopManager(null);

        internalFrame.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        internalFrame.setDoubleBuffered(true);
        internalFrame.setFrameIcon(null);
        internalFrame.setPreferredSize(new java.awt.Dimension(10, 10));
        internalFrame.setVisible(true);
        internalFrame.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                internalFramePropertyChange(evt);
            }
        });

        javax.swing.GroupLayout internalFrameLayout = new javax.swing.GroupLayout(internalFrame.getContentPane());
        internalFrame.getContentPane().setLayout(internalFrameLayout);
        internalFrameLayout.setHorizontalGroup(
            internalFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );
        internalFrameLayout.setVerticalGroup(
            internalFrameLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 0, Short.MAX_VALUE)
        );

        desktopPane.add(internalFrame);
        internalFrame.setBounds(0, 0, 10, 10);
        try {
            internalFrame.setMaximum(true);
        } catch (java.beans.PropertyVetoException e1) {
            e1.printStackTrace();
        }

        jScrollPane2.setViewportView(desktopPane);

        jSplitPane1.setRightComponent(jScrollPane2);

        newConfigItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        newConfigItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                newConfigItemActionPerformed(evt);
            }
        });
        fileMenu.add(newConfigItem);

        openConfigItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        openConfigItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                openConfigItemActionPerformed(evt);
            }
        });
        fileMenu.add(openConfigItem);

        saveConfigItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.CTRL_MASK));
        saveConfigItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveConfigItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveConfigItem);

        saveAsConfigItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        saveAsConfigItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                saveAsConfigItemActionPerformed(evt);
            }
        });
        fileMenu.add(saveAsConfigItem);
        fileMenu.add(jSeparator1);

        compileConfigItem.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_C, java.awt.event.InputEvent.ALT_MASK | java.awt.event.InputEvent.CTRL_MASK));
        compileConfigItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                compileConfigItemActionPerformed(evt);
            }
        });
        fileMenu.add(compileConfigItem);
        fileMenu.add(jSeparator2);

        exitMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                exitMenuItemActionPerformed(evt);
            }
        });
        fileMenu.add(exitMenuItem);

        jMenuBar1.add(fileMenu);

        editMenuMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editMenuMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(editMenuMenuItem);

        editModuleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editModuleMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(editModuleMenuItem);

        editLibMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editLibMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(editLibMenuItem);

        editConnectorMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editConnectorMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(editConnectorMenuItem);

        editStyleMenuItem.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                editStyleMenuItemActionPerformed(evt);
            }
        });
        editMenu.add(editStyleMenuItem);

        jMenuBar1.add(editMenu);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 417, Short.MAX_VALUE)
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jSplitPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 378, Short.MAX_VALUE)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void newConfigItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newConfigItemActionPerformed
        setCurrentConfigFile(null);
        this.moduleGraph.clearGraph();
    }//GEN-LAST:event_newConfigItemActionPerformed

    private void openConfigItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_openConfigItemActionPerformed
        if(currentConfigFile!=null) {
            openDialog.setCurrentDirectory(currentConfigFile);
        } else if(configsDirectory.exists()) {
            openDialog.setCurrentDirectory(configsDirectory);
        }
        openDialog.updateUI();
        if(openDialog.showOpenDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION){
            openGraph(openDialog.getSelectedFile());
        }
    }//GEN-LAST:event_openConfigItemActionPerformed

    private void saveAsConfigItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveAsConfigItemActionPerformed
        if(currentConfigFile!=null) {
            openDialog.setCurrentDirectory(currentConfigFile);
        } else if(configsDirectory.exists()) {
            openDialog.setCurrentDirectory(configsDirectory);
        }
        openDialog.updateUI();
        if(openDialog.showSaveDialog(this) == javax.swing.JFileChooser.APPROVE_OPTION){
            setCurrentConfigFile(openDialog.getSelectedFile());
            ModularSateIO.setLastFile(currentConfigFile.getAbsolutePath());
            moduleGraph.saveGraph(currentConfigFile.getAbsolutePath());
        }
    }//GEN-LAST:event_saveAsConfigItemActionPerformed

    private void saveConfigItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_saveConfigItemActionPerformed
        if(currentConfigFile!=null) {
            moduleGraph.saveGraph(currentConfigFile.getAbsolutePath());
        }
        else {
            saveAsConfigItemActionPerformed(evt);
        }
    }//GEN-LAST:event_saveConfigItemActionPerformed

    private void internalFramePropertyChange(java.beans.PropertyChangeEvent evt) {//GEN-FIRST:event_internalFramePropertyChange
        if("maximum".equals(evt.getPropertyName()) && evt.getNewValue().equals(Boolean.FALSE)){
            moduleGraph.extenalize();
            try {
                this.internalFrame.setMaximum(true);
            } catch (Exception ex) {}
        }
    }//GEN-LAST:event_internalFramePropertyChange

    private void compileConfigItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_compileConfigItemActionPerformed
        String choosenName = Modular.cleanName(JOptionPane.showInputDialog(this, IniManager.getLocaleProperty("modular.needs.a.name"), currentConfigSimpleName));
        if(choosenName == null || choosenName.isEmpty()){
            return;
        }

        if(new File(choosenName+".jar").exists() || new File(choosenName+".zip").exists()){
            if(JOptionPane.showConfirmDialog(this, IniManager.getLocaleProperty("modular.exiting.files"),"",
                    JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE)!=JOptionPane.OK_OPTION){
                return ;
            }
        }

        String className = Modular.createRegularClassName(choosenName);
        String packageName = "greta.generated";


        if(className!=null && !className.isEmpty()) {
            ModuleGraph.SourceCode generatedSourceCode = moduleGraph.generateSourceCode(className, packageName, choosenName);
            if(generatedSourceCode==null){
                //the user cancel the code generation
                return ;
            }
//            System.out.println("-------------------------------------------\n"+generatedSourceCode.getSourceCode()+"\n-------------------------------------------");

//            String fullClassName = packageName+"."+className;
            JarMaker jar = new JarMaker();
            ZipMaker zip = new ZipMaker();

            jar.addSource(generatedSourceCode);
            jar.setMainClass(generatedSourceCode.getQualifiedName());
            for(Library lib : generatedSourceCode.getLibrariesNeeded()){
                jar.addLib(lib.getFileName());
                for(String neededFileName : lib.getAllNeededFilesAndLibs()) {
                    zip.addFile(neededFileName);
                }
            }
//            jar.setStandAlone(true);
            if(generatedSourceCode.useIniManagerFile()){
                zip.addFile(generatedSourceCode.getIniFileName());
            }
            jar.doJar("./"+choosenName+".jar", true);
            if(generatedSourceCode.useIniManagerFile()){
                generatedSourceCode.getIniManager().saveDefaultDefinition();
                zip.addFile(generatedSourceCode.getIniFileName());
            }
            zip.addFile("./"+choosenName+".jar");
            zip.doZip("./"+choosenName+".zip", true, this);
        }
    }//GEN-LAST:event_compileConfigItemActionPerformed

    private void exitMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_exitMenuItemActionPerformed
        this.getToolkit().getSystemEventQueue().postEvent(new WindowEvent(this, WindowEvent.WINDOW_CLOSING));
    }//GEN-LAST:event_exitMenuItemActionPerformed

    private void editMenuMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editMenuMenuItemActionPerformed
        showEditors(0);
    }//GEN-LAST:event_editMenuMenuItemActionPerformed

    private void editModuleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editModuleMenuItemActionPerformed
        showEditors(1);
    }//GEN-LAST:event_editModuleMenuItemActionPerformed

    private void editLibMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editLibMenuItemActionPerformed
        showEditors(2);
    }//GEN-LAST:event_editLibMenuItemActionPerformed

    private void editConnectorMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editConnectorMenuItemActionPerformed
        showEditors(3);
    }//GEN-LAST:event_editConnectorMenuItemActionPerformed

    private void editStyleMenuItemActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_editStyleMenuItemActionPerformed
        showEditors(4);
    }//GEN-LAST:event_editStyleMenuItemActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenuItem compileConfigItem;
    private javax.swing.JDesktopPane desktopPane;
    private javax.swing.JMenuItem editConnectorMenuItem;
    private javax.swing.JMenuItem editLibMenuItem;
    private javax.swing.JMenu editMenu;
    private javax.swing.JMenuItem editMenuMenuItem;
    private javax.swing.JMenuItem editModuleMenuItem;
    private javax.swing.JMenuItem editStyleMenuItem;
    private javax.swing.JMenuItem exitMenuItem;
    private javax.swing.JMenu fileMenu;
    private javax.swing.JInternalFrame internalFrame;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JPopupMenu.Separator jSeparator1;
    private javax.swing.JPopupMenu.Separator jSeparator2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JMenuItem newConfigItem;
    private javax.swing.JMenuItem openConfigItem;
    private javax.swing.JFileChooser openDialog;
    private javax.swing.JMenuItem saveAsConfigItem;
    private javax.swing.JMenuItem saveConfigItem;
    // End of variables declaration//GEN-END:variables
}
