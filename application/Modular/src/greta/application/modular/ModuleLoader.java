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

import greta.application.modular.MultiLineToolTip.JMultiLineToolTip;
import greta.application.modular.modules.Connector;
import greta.application.modular.modules.Library;
import greta.application.modular.modules.Module;
import greta.application.modular.modules.ModuleFactory;
import greta.application.modular.modules.ModuleFactory.ModuleInfo;
import greta.application.modular.modules.Style;
import greta.application.modular.tools.PrimitiveTypes;
import greta.core.util.log.LogPrinter;
import greta.core.util.log.Logs;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JComponent;
import javax.swing.JMenuBar;
import javax.swing.JToolTip;

/**
 *
 * @author Andre-Marie
 */
public class ModuleLoader {

    /**
     * Loads the Modular features.
     * @param modularWindow
     * @param moduleGraph the target graph
     */
    public static void loadModularXML(ModularWindow modularWindow, ModuleGraph moduleGraph) {
        LogPrinter lp = new LogPrinter();
        Logs.add(lp);//XXX this line is for debuging XML
        List<String> libLoaded = new ArrayList<String>();
        //load style
        loadStyles();
        moduleGraph.loadSylesInTheGraph();

        //load module
        loadModules(libLoaded);

        //load connection rules
        loadConnectionRules(libLoaded);

        //load menu
        loadMenus(modularWindow, moduleGraph);

        Logs.remove(lp);
    }

    public static URL getURLFromURI(URI uri)
            throws MalformedURLException {
        return uri.toURL();
    }

    public static URL getURLFromFile(File f)
            throws MalformedURLException, IOException {
        return getURLFromURI(f.getCanonicalFile().toURI());
    }

    public static URL getURLFromPath(String path)
            throws MalformedURLException, IOException {
        return getURLFromFile(new File(path));
    }

    private static void loadLib(String lib_id, List<String> libLoaded) {
        loadLib(ModularXMLFile.getLibs(), lib_id, new ArrayList<String>(), libLoaded);
    }

    private static void loadLib(XMLTree libs, String lib_id, List<String> callers, List<String> libLoaded) {
        if (callers.contains(lib_id)) {
            return;
        } else {
            callers.add(lib_id);
        }

        for (XMLTree lib : libs.getChildrenElement()) {
            if(! libLoaded.contains(lib_id)){
                if (lib.getAttribute("id").equals(lib_id)) {
                    String libpath = lib.getAttribute("path");
                    Library library = Library.createLibrary(lib_id, libpath);
                    for (XMLTree depends : lib.getChildrenElement()) {
                        if(depends.isNamed("depends")){
                            String dependsId = depends.getAttribute("lib_id");
                            loadLib(libs, dependsId, callers, libLoaded);
                            library.addDependency(Library.getLibrary(dependsId));
                        }
                        if(depends.isNamed("needs")){
                            library.addFile(depends.getAttribute("path"));
                        }
                    }
                    library.load();
                    libLoaded.add(lib_id);
                }
            }
        }
    }

    private static Class getClassFromObject(XMLTree object, List<String> libLoaded) throws ClassNotFoundException {
        loadLib(object.getAttribute("lib_id"), libLoaded);
        return ClassLoader.getSystemClassLoader().loadClass(object.getAttribute("class"));
    }

    private static Object[] getconnectionMethod(XMLTree methodTree, Class in, Class out) throws Exception {
        String from = methodTree.getAttribute("from");
        String to = methodTree.getAttribute("to");
        String methodName = methodTree.getAttribute("method");
        if (from.equals("null") || methodName.isEmpty()) {
            return new Object[]{null, true, true};
        }
        boolean mode = true;
        if (from.equals("output")) {
            mode = false;
        }
        if (from.equals(to)) {
            throw new Exception("\"from\" and \"to\" can not have the same value : \""+from+"\"");
        }
        Method method =  findMethod( (mode ? in : out) , methodName, (mode ? out : in) );
        return new Object[]{method, mode, to.equals("null")};
    }

    private static void loadConnectionRules(List<String> libLoaded) {
        for (XMLTree connection : ModularXMLFile.getConnectors().getChildrenElement()) {
            try {
                Class inputclass = getClassFromObject(
                        connection.findNodeCalled("input"),
                        libLoaded);
                Class outputclass = getClassFromObject(
                        connection.findNodeCalled("output"),
                        libLoaded);
                Object[] cennectMethod = getconnectionMethod(
                        connection.findNodeCalled("connect"),
                        inputclass,
                        outputclass);
                Object[] disconnectMethod = getconnectionMethod(
                        connection.findNodeCalled("disconnect"),
                        inputclass,
                        outputclass);
                Connector.createConnector(
                        connection.getAttribute("id"),
                        Boolean.parseBoolean(connection.getAttribute("unique")),
                        inputclass,
                        outputclass,
                        (Method) cennectMethod[0],
                        ((Boolean) cennectMethod[1]).booleanValue(),
                        ((Boolean) cennectMethod[2]).booleanValue(),
                        (Method) disconnectMethod[0],
                        ((Boolean) disconnectMethod[1]).booleanValue(),
                        ((Boolean) disconnectMethod[2]).booleanValue(),
                        connection.getAttribute("style"));
            } catch (Throwable ex) {
                printError(ex, "Failed to load rule: \"%s\" (%s)\n", connection.getAttribute("id"), ex.getLocalizedMessage());
            }
        }
    }

    private static void loadModules(List<String> libLoaded) {
        XMLTree modules = ModularXMLFile.getModules();
        for (XMLTree module : modules.getChildrenElement()) {
            if (module.isNamed("module")) {
                try {
                    ModuleInfo moduleInfo = new ModuleInfo();
                    moduleInfo.name = module.getAttribute("name");
                    moduleInfo.style = module.getAttribute("style");
                    moduleInfo.restriction = 0;
                    if(module.hasAttribute("restrict")){
                        try {
                            moduleInfo.restriction = Integer.parseInt(module.getAttribute("restrict"));
                        } catch (Exception ex) {
                            printError(ex, "The restrict attribute must be an integer value.\n");
                        }
                    }

                    XMLTree description = module.findNodeCalled("description");
                    if (description != null) {
                        moduleInfo.description = formatTextFromXML(description.findNodeCalled(XML.TEXT_NODE_NAME).getTextValue());
                    }

                    moduleInfo.objectClass = getClassFromObject( module.findNodeCalled("object"), libLoaded);
                    moduleInfo.objectLib = Library.getLibrary(module.findNodeCalled("object").getAttribute("lib_id"));

                    XMLTree frame = module.findNodeCalled("frame");
                    if (frame != null) {
                        moduleInfo.frameType = frame.getAttribute("type");
                        moduleInfo.windowedOnly = frame.hasAttribute("windowed_only") && Boolean.parseBoolean(frame.getAttribute("windowed_only"));
                    } else {
                        moduleInfo.frameType = "noFrame";
                    }
                    if (moduleInfo.frameType.equals("frame")) {
                        moduleInfo.frameClass = getClassFromObject(frame, libLoaded);
                        moduleInfo.frameLib = Library.getLibrary(frame.getAttribute("lib_id"));
                        XMLTree link = frame.findNodeCalled("link");
                        if(link!=null){
                            try{
                                String linkOn = link.getAttribute("on");
                                moduleInfo.linkMethod = findMethod(
                                        (linkOn.equals("object")? moduleInfo.objectClass : moduleInfo.frameClass),
                                        link.getAttribute("method"),
                                        (linkOn.equals("object")?  moduleInfo.frameClass : moduleInfo.objectClass));
                                moduleInfo.linkOn = linkOn;
                            }catch(Exception e){
                                printError(e, "Method %s not found: %s\n", link.getAttribute("method"), e.getLocalizedMessage());
                            }
                        }
                    } else {
                        moduleInfo.frameClass = null;
                    }

                    for (XMLTree parameterNode : module.getChildrenElement()) {
                        if (parameterNode.isNamed("parameter")) {
                            try {
                                String parameterType = parameterNode.getAttribute("type");
                                String parameterName = parameterNode.getAttribute("name");
                                String defaultValue = parameterNode.getAttribute("default");
                                String set_on = parameterNode.getAttribute("set_on");
                                String get_on = parameterNode.getAttribute("get_on");
                                Method getMethod = findMethod(
                                    set_on.equals("object")?
                                        moduleInfo.objectClass : moduleInfo.frameClass,
                                    parameterNode.getAttribute("set_method"), PrimitiveTypes.getClassOf(parameterType)
                                );
                                Method setMethod = findMethod(
                                    get_on.equals("object")?
                                        moduleInfo.objectClass : moduleInfo.frameClass,
                                    parameterNode.getAttribute("get_method"),
                                    null);

                                moduleInfo.addParameter(parameterType, parameterName, defaultValue, set_on, getMethod, get_on, setMethod);

                            } catch (Exception e) {
                                printError(e, "Failed to load parameter in module: \"%s\" (%s)\n", module.getAttribute("name"), e.getLocalizedMessage());
                            }
                        }
                    }
                    ModuleFactory.moduleInfos.add(moduleInfo);
                    System.out.printf("Loaded module: \"%s\"\n", moduleInfo.name);
                } catch (Throwable ex) {
                    printError(ex, "Failed to load module: \"%s\" (%s)\n", module.getAttribute("name"), ex.getLocalizedMessage());
                }
            }
        }
    }


    private static void loadMenus(final ModularWindow modularWindow, ModuleGraph moduleGraph) {
        JMenuBar jMenuBar = modularWindow.getJMenuBar();
        XMLTree menus = ModularXMLFile.getMenus();
        fillMenu(menus, jMenuBar, moduleGraph);

        //create help menu
        javax.swing.JMenu helpMenu = new greta.core.utilx.gui.ToolBox.LocalizedJMenu("GUI.help");
        javax.swing.JMenuItem item = new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem("modular.help.connections");
        item.addActionListener(new ActionListener() {
            ConnectionPossibilities possibilities;
            @Override
            public void actionPerformed(ActionEvent e) {
                if(possibilities==null){
                    possibilities = new ConnectionPossibilities(modularWindow, false, "modular.help.connections");
                }
                possibilities.setLocale(Locale.getDefault());
                possibilities.setVisible(true);
            }
        });
        javax.swing.JMenuItem jvmItem = new javax.swing.JMenuItem("JVM infos");
        jvmItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                java.lang.management.RuntimeMXBean runtimeMxBean = java.lang.management.ManagementFactory.getRuntimeMXBean();
                java.util.List<String> arguments = runtimeMxBean.getInputArguments();
                Date d = new Date(runtimeMxBean.getStartTime());
                String spec =
                        "Java Specification "+runtimeMxBean.getSpecVersion()+"\n"+
                        runtimeMxBean.getVmName()+" "+runtimeMxBean.getVmVersion()+" by "+runtimeMxBean.getVmVendor()+"\n\n"+
                        "Starting at "+d.toString()+"\n"+
                        "PID: "+runtimeMxBean.getName()+"\n"+
                        "args:";
                for(String s : arguments){
                    spec += " "+s;
                }
                javax.swing.JOptionPane.showMessageDialog(modularWindow, spec, "JVM infos", javax.swing.JOptionPane.PLAIN_MESSAGE);
            }
        });
        helpMenu.add(item);
        helpMenu.add(jvmItem);
        jMenuBar.add(helpMenu);

    }

    private static void fillMenu(XMLTree menu, JComponent jMenu, ModuleGraph moduleGraph){
        for(XMLTree child : menu.getChildrenElement()){
            if(child.isNamed("item")){
                String localeName = parseMenuName(child.getAttribute("name"));
                javax.swing.JMenuItem item = (localeName==null)?
                        new javax.swing.JMenuItem(child.getAttribute("name")){
                            @Override
                            public JToolTip createToolTip() {
                                JMultiLineToolTip tip = new JMultiLineToolTip();
                                tip.setComponent(this);
                                tip.setFixedWidth(300);
                                return tip;
                            }
                        }:
                        new greta.core.utilx.gui.ToolBox.LocalizedJMenuItem(localeName){
                            @Override
                            public JToolTip createToolTip() {
                                JMultiLineToolTip tip = new JMultiLineToolTip();
                                tip.setComponent(this);
                                tip.setFixedWidth(300);
                                return tip;
                            }
                        }
                        ;

                ModuleInfo moduleInfo = null;
                for(ModuleInfo pi : ModuleFactory.moduleInfos){
                    if(pi.name.equals(child.getAttribute("module"))){
                        moduleInfo=pi;
                        break;
                    }
                }
                if(moduleInfo==null){
                    item.setEnabled(false);
                }
                else{
                    item.setEnabled(true);
                    item.addActionListener(new MenuItemListener(moduleInfo, moduleGraph));
                    if(moduleInfo.description!=null) {
                        item.setToolTipText(moduleInfo.description);
                    }
                }
                jMenu.add(item);
            }
            if(child.isNamed("menu")){
                String localeName = parseMenuName(child.getAttribute("name"));
                javax.swing.JMenu newJMenu = (localeName==null)?
                        new javax.swing.JMenu(child.getAttribute("name")):
                        new greta.core.utilx.gui.ToolBox.LocalizedJMenu(localeName);
                fillMenu(child, newJMenu, moduleGraph);
                jMenu.add(newJMenu);
            }
        }
    }

    private static String parseMenuName(String name){
        //try to find locale sementic : ${something}
        Pattern p = Pattern.compile("\\$\\{([^\\}]+?)\\}");
        Matcher m = p.matcher(name);
//        String result = "";
        //int lastend = 0;
        if(m.find()){
            String match = m.group();
            return match.substring(2,match.length()-1);
//            result += name.substring(0, m.start())+IniManager.getLocaleProperty(match.substring(2,match.length()-1));
//            lastend = m.end();
        }
//        if(lastend!=name.length()) {
//            result+=name.substring(lastend);
//        }
//        return result;
        return null;
    }

    private static void loadStyles() {
        XMLTree styles = ModularXMLFile.getStyles();
        for(XMLTree style : styles.getChildrenElement()) {
            //fix : some times the jvm does not load default values from xsd properly
            String name = "default";
            if(style.hasAttribute("name")){
                name = style.getAttribute("name");
            }
            else{
                //only the default style can be in this condition
                System.err.println("JVM doesn't setup XML correctely you may have some mistakes during this execution.");
            }
            String color = Style.DEFAULT_COLOR;
            if(style.hasAttribute("color")){
                color = style.getAttribute("color");
            }
            String edge_color = "";
            if(style.hasAttribute("edge-color")){
                edge_color = style.getAttribute("edge-color");
            }
            String edge_dash = "";
            if(style.hasAttribute("edge-dash")){
                edge_dash = style.getAttribute("edge-dash");
            }

            String edge_start = "none";
            if(style.hasAttribute("edge-start")){
                edge_start = style.getAttribute("edge-start");
            }
            String edge_end = "arrow";
            if(style.hasAttribute("edge-end")){
                edge_end = style.getAttribute("edge-end");
            }
            String vAlign = "center";
            if(style.hasAttribute("v-align")){
                vAlign = style.getAttribute("v-align");
            }

            Style.createNewStyle(name, color, edge_color, edge_dash, edge_start, edge_end, vAlign);

        }
    }

    private static Method findMethod(Class<?> origine, String methodName, Class argue) throws NoSuchMethodException, SecurityException{
        if(argue==null) {
            return origine.getMethod(methodName);
        }
        for (Method method : origine.getMethods()) {
            if (method.getName().equals(methodName)) {
                if (method.getParameterTypes().length==1 && method.getParameterTypes()[0].isAssignableFrom(argue)) {
                    return method;
                }
            }
        }
        throw new NoSuchMethodException(origine.getCanonicalName()+"."+methodName+"("+argue.getCanonicalName()+")");
    }

    private static class MenuItemListener implements java.awt.event.ActionListener{

        ModuleInfo moduleInfo;
        ModuleGraph moduleGraph;

        /**
         * Constructor
         * @param moduleInfo informations about Module instanciation
         * @param moduleGraph the graph where the Module must be added
         */
        MenuItemListener(ModuleInfo moduleInfo, ModuleGraph moduleGraph){
            this.moduleInfo = moduleInfo;
            this.moduleGraph = moduleGraph;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if(moduleInfo.restriction<=0 || moduleGraph.countModuleByType(moduleInfo.name)<moduleInfo.restriction) {
                Module parent = moduleGraph.getSelectedModule();
                moduleGraph.addModule(moduleInfo.name, parent);
            }
            else{
                Logs.warning("Can not add module "+moduleInfo.name+": restriction="+moduleInfo.restriction+" number of instances="+moduleGraph.countModuleByType(moduleInfo.name));
            }
        }

    }


    private static String formatTextFromXML(String textInXML){
        //clean spaces
        textInXML = textInXML.replaceAll("\\s+"," ");

        //line breaks
        textInXML = textInXML.replaceAll("\\s*\\\\n\\s*","\n");

        //dont start with space or line break
        while(textInXML.startsWith(" ")||textInXML.startsWith("\n")) {
            textInXML = textInXML.substring(1);
        }

        //tabulations
        textInXML = textInXML.replaceAll("\\\\t","\t");

        //dont end with space, line break or tabulations
        while(textInXML.endsWith(" ")||textInXML.endsWith("\n")||textInXML.endsWith("\t")) {
            textInXML = textInXML.substring(0, textInXML.length()-1);
        }

        return textInXML;
    }


    private static void printError(Throwable error, String formatedText, Object ... args){
        System.err.printf(formatedText,args);
       // error.printStackTrace(); //usefull to debug
    }


}
