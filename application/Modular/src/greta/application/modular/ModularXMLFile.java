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

import greta.application.modular.modules.Library;
import greta.application.modular.tools.PrimitiveTypes;
import greta.application.modular.tools.ToolBox;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.io.FileWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarFile;

/**
 *
 * @author Andre-Marie Pez
 */
public class ModularXMLFile {

    public static final String MODULAR_XSD;
    public static final String MODULAR_XML;

    private static XMLTree modularTree;

    static {
        MODULAR_XSD = "./Modular.xsd";
        MODULAR_XML = "./Modular.xml";

        reload();
    }

    public static XMLTree getRoot() {
        return modularTree;
    }

    public static void reload() {
        modularTree = XML.createParser().parseFileWithXSD(MODULAR_XML, MODULAR_XSD);
    }

    public static void save() {
        try {
            //this part is for writing the XML file in a hybrid format (pretty-print but not so much)
            //the default pretty print makes a correct markup indentation but also indents attributs inside markups
            //this second kind of indentation makes the file quite ugly...

            //read the pretty print returned by modularTree.toString()
            javax.xml.parsers.DocumentBuilderFactory dbFactory = javax.xml.parsers.DocumentBuilderFactory.newInstance();
            javax.xml.parsers.DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            org.w3c.dom.Document doc = dBuilder.parse(new org.xml.sax.InputSource(new java.io.StringReader(modularTree.toString())));

            //save whitout pretty print. the indentation inside markups will disappear
            org.w3c.dom.DOMImplementation implementation = org.w3c.dom.bootstrap.DOMImplementationRegistry.newInstance().getDOMImplementation("XML 3.0");
            org.w3c.dom.ls.DOMImplementationLS domImplLS = (org.w3c.dom.ls.DOMImplementationLS) implementation.getFeature("LS", "3.0");
            org.w3c.dom.ls.LSOutput output = domImplLS.createLSOutput();
            output.setEncoding("UTF-8");
            java.io.StringWriter buf = new java.io.StringWriter();
            output.setCharacterStream(buf);
            domImplLS.createLSSerializer().write(doc, output);
            FileWriter out = new FileWriter(MODULAR_XML);
            out.write(new String(buf.toString().getBytes("UTF-8")));
            out.close();
        } catch (Throwable e) {
            //default save
            modularTree.save(MODULAR_XML);
        }
    }


    public static boolean checkOject(XMLTree object) {
        XMLTree lib = getLib(object.getAttribute("lib_id"));
        if (lib == null) {
            return false;
        }
        if (!Library.jarContainsClass(lib.getAttribute("path"), object.getAttribute("class"))) {
            return false;
        }
        return true;
    }

    //<editor-fold defaultstate="collapsed" desc="menu">
    public static XMLTree getMenus() {
        return modularTree.findNodeCalled("menus");
    }

    private static boolean modulesHaveItem() {
        XMLTree menus = getMenus();
        for (XMLTree moduleTree : getModules().getChildrenElement()) {
            if (moduleTree.isNamed("module")) {
                String moduleName = moduleTree.getAttribute("name");
                if (!moduleHasItem(menus, moduleName)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean moduleHasItem(XMLTree menu, String moduleName) {
        for (XMLTree child : menu.getChildrenElement()) {
            if (child.isNamed("menu")) {
                if (moduleHasItem(child, moduleName)) {
                    return true;
                }
            }
            if (child.isNamed("item")) {
                if (child.getAttribute("module").equals(moduleName)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static boolean itemsHaveModule() {
        return itemHasModule(getMenus(), getModules().getChildrenElement());
    }

    public static boolean itemHasModule(XMLTree menu) {
        return itemHasModule(menu, getModules().getChildrenElement());
    }

    private static boolean itemHasModule(XMLTree menu, List<XMLTree> modules) {
        if (menu.isNamed("item")) {
            String name = menu.getAttribute("module");
            boolean found = false;
            for (XMLTree module : modules) {
                if (module.isNamed("module") && module.getAttribute("name").equals(name)) {
                    found = true;
                    break;
                }
            }
            return found;
        }
        if (menu.isNamed("menu")) {
            for (XMLTree child : menu.getChildrenElement()) {
                if (!itemHasModule(child, modules)) {
                    return false;
                }
            }
        }
        return true;
    }

    public static boolean checkMenus() {
        return modulesHaveItem() && itemsHaveModule();
    }
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="modules">
    public static XMLTree getModules() {
        return modularTree.findNodeCalled("modules");
    }

    public static boolean modulesHaveUniqueNames() {
        List<XMLTree> modules = getModules().getChildrenElement();
        for (int i = 0; i < modules.size() - 1; ++i) {
            XMLTree module = modules.get(i);

            if (module.isNamed("module")) {
                String name = module.getAttribute("name");

                for (int j = i + 1; j < modules.size(); ++j) {
                    XMLTree module2 = modules.get(j);
                    if (module2.isNamed("module")) {
                        String name2 = module2.getAttribute("name");
                        if (name.equals(name2)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkOneModule(XMLTree module) {
        XMLTree object = module.findNodeCalled("object");
        if(object == null){
            return false;
        }
        if (!checkOject(object)) {
            return false;
        }
        XMLTree frame = module.findNodeCalled("frame");
        if(frame != null){
            if(frame.getAttribute("type").equals("frame")){
                if (!checkOject(frame)) {
                    return false;
                }
                if (!ToolBox.inheritsFromJFrame(frame)) {
                    return false;
                }
                XMLTree link = frame.findNodeCalled("link");
                if(link != null){
                    if(link.getAttribute("on").equals("object") && !(ToolBox.containsMethodUsing(object, link.getAttribute("method"), frame))){
                        return false;
                    }
                    if(link.getAttribute("on").equals("frame") && !(ToolBox.containsMethodUsing(frame, link.getAttribute("method"), object))){
                        return false;
                    }
                }
            }
            if(frame.getAttribute("type").equals("object")){
                if(!ToolBox.inheritsFromJFrame(object)){
                    return false;
                }
            }
        }
        for(XMLTree child : module.getChildrenElement()){
            if(child.isNamed("parameter") && ! checkParameter(child, object, frame)){
                return false;
            }
        }
        return containsValidStyle(module);
    }

    public static boolean checkParameter(XMLTree param, XMLTree object, XMLTree frame){
        if(object == null){
            return false;
        }
        //check type and default
        if( ! PrimitiveTypes.isCorrectValue(param.getAttribute("type"), param.getAttribute("default"))){
            return false;
        }
        //check methods
        if( ! ToolBox.containsMethodUsing(
                param.getAttribute("set_on").equals("object") ? object : frame,
                param.getAttribute("set_method"),
                PrimitiveTypes.getClassOf(param.getAttribute("type")))){
            return false;
        }
        if( ! ToolBox.containsMethodReturning(
                param.getAttribute("get_on").equals("object") ? object : frame,
                param.getAttribute("get_method"),
                PrimitiveTypes.getClassOf(param.getAttribute("type")))){
            return false;
        }
        //check name
        return checkParameterName(param);
    }

    public static boolean checkParameterName(XMLTree param){
        for(XMLTree brother : param.getParent().getChildrenElement()){
            if(brother.isNamed("parameter") && (! brother.equals(param)) && brother.getAttribute("name").equals(param.getAttribute("name"))){
                return false;
            }
        }
        return true;
    }
    public static boolean checkParameter(XMLTree param){
        return checkParameter(param, param.getParent().findNodeCalled("object"), param.getParent().findNodeCalled("frame"));
    }

    public static boolean checkModules() {
        for (XMLTree module : getModules().getChildrenElement()) {
            if (module.isNamed("module")) {
                if (!checkOneModule(module)) {
                    return false;
                }
            }
        }
        return modulesHaveUniqueNames();
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="libs">
    public static XMLTree getLibs() {
        return modularTree.findNodeCalled("libs");
    }

    public static XMLTree getLib(String id) {
        for (XMLTree lib : getLibs().getChildrenElement()) {
            if (lib.isNamed("lib")) {
                if (lib.getAttribute("id").equals(id)) {
                    return lib;
                }
            }
        }
        return null;
    }

    public static boolean libsHaveUniqueIds() {
        List<XMLTree> libs = getLibs().getChildrenElement();
        for (int i = 0; i < libs.size() - 1; ++i) {
            XMLTree lib = libs.get(i);

            if (lib.isNamed("lib")) {
                String id = lib.getAttribute("id");

                for (int j = i + 1; j < libs.size(); ++j) {
                    XMLTree lib2 = libs.get(j);
                    if (lib2.isNamed("lib")) {
                        String id2 = lib2.getAttribute("id");
                        if (id.equals(id2)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static List<String> getLibPaths(String... libIDs){
        List<String> paths = new LinkedList<String>();
        for(String libID : libIDs){
            getlibPaths(getLib(libID), paths);
        }
        return paths;
    }

    private static void getlibPaths(XMLTree lib, List<String> pathToLoad) {
        if(lib == null){
            return ;
        }
        String libPath = lib.getAttribute("path");
        if(pathToLoad.contains(libPath)){
            return ;
        }
        pathToLoad.add(libPath);
        for(XMLTree dep : lib.getChildrenElement()){
            if(dep.isNamed("depends")){
                getlibPaths(ModularXMLFile.getLib(dep.getAttribute("lib_id")), pathToLoad);
            }
        }
    }

    public static boolean checkOneLib(XMLTree lib) {
        try {
            JarFile jar = new JarFile(lib.getAttribute("path"));
            jar.getName();
        } catch (Exception ex) {
            return false;
        }

        return true;
    }

    public static boolean checkLibs() {
        for (XMLTree lib : getLibs().getChildrenElement()) {
            if (lib.isNamed("lib")) {
                if (!checkOneLib(lib)) {
                    return false;
                }
            }
        }
        return libsHaveUniqueIds();
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="connectors">
    public static XMLTree getConnectors() {
        return modularTree.findNodeCalled("connectors");
    }

    public static boolean checkOneConnector(XMLTree connector) {
        if(connector.hasAttribute("style") && !styleExists(connector.getAttribute("style"))){
            return false;
        }
        XMLTree in = connector.findNodeCalled("input");
        if(in==null || ! checkOject(in)){
            return false;
        }
        XMLTree out = connector.findNodeCalled("output");
        if(out==null || ! checkOject(out)){
            return false;
        }
        return true;
    }


    public static boolean connectorsHaveUniqueNames() {
        List<XMLTree> connectors = getConnectors().getChildrenElement();
        for (int i = 0; i < connectors.size() - 1; ++i) {
            XMLTree connector = connectors.get(i);

            if (connector.isNamed("connector")) {
                String name = connector.getAttribute("id");

                for (int j = i + 1; j < connectors.size(); ++j) {
                    XMLTree connector2 = connectors.get(j);
                    if (connector2.isNamed("connector")) {
                        String name2 = connector2.getAttribute("id");
                        if (name.equals(name2)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkConnectors(){
        for (XMLTree connector : getConnectors().getChildrenElement()) {
            if (connector.isNamed("connector")) {
                if (!checkOneConnector(connector)) {
                    return false;
                }
            }
        }
        return connectorsHaveUniqueNames();
    }

    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="styles">
    public static XMLTree getStyles() {
        return modularTree.findNodeCalled("styles");
    }

    private static boolean checkEdgeBound(String bound){
        return bound.equals("none") || bound.equals("arrow") || bound.equals("oval") || bound.equals("diamond");
    }

    public static boolean checkOneStyle(XMLTree style) {

        if(style.hasAttribute("edge-start") && !checkEdgeBound(style.getAttribute("edge-start"))){
            return false;
        }
        if(style.hasAttribute("edge-end") && !checkEdgeBound(style.getAttribute("edge-end"))){
            return false;
        }
        return true;
    }

    public static boolean stylesHaveUniqueNames() {
        List<XMLTree> styles = getStyles().getChildrenElement();
        for (int i = 0; i < styles.size() - 1; ++i) {
            XMLTree style = styles.get(i);

            if (style.isNamed("style")) {
                String name = style.getAttribute("name");

                for (int j = i + 1; j < styles.size(); ++j) {
                    XMLTree style2 = styles.get(j);
                    if (style2.isNamed("style")) {
                        String name2 = style2.getAttribute("name");
                        if (name.equals(name2)) {
                            return false;
                        }
                    }
                }
            }
        }
        return true;
    }

    public static boolean checkStyles() {
        for (XMLTree style : getStyles().getChildrenElement()) {
            if (style.isNamed("style")) {
                if (!checkOneStyle(style)) {
                    return false;
                }
            }
        }

        return stylesHaveUniqueNames();
    }
    public static boolean containsValidStyle(XMLTree styledObject){
        return !styledObject.hasAttribute("style") || styleExists(styledObject.getAttribute("style"));
    }

    public static boolean styleExists(String styleName) {
        for(XMLTree style : getStyles().getChildrenElement()){
            if(style.getAttribute("name").equals(styleName)){
                return true;
            }
        }
        return false;
    }

    //</editor-fold>

}
