/*
 * This file is a part of the Modular application.
 */

package vib.application.modular.tools;

import java.util.LinkedList;
import java.util.List;
import vib.application.modular.ModularXMLFile;
import vib.core.util.xml.XMLTree;

/**
 *
 * @author Andre-Marie Pez
 */
public class NameChanger {
    private XMLTree tree;
    private String attribute;
    List<NameChanger> refs;

    public NameChanger(XMLTree tree, String attribute, boolean applyToReferences){
        this.tree = tree;
        this.attribute = attribute;
        if(applyToReferences){
            if(tree.isNamed("lib")){
                refs = findReferencesToLib(tree.getAttribute(attribute));
            }
            if(tree.isNamed("module")){
                refs = findReferencesToModule(tree.getAttribute(attribute));
            }
            if(tree.isNamed("style")){
                refs = findReferencesToStyle(tree.getAttribute(attribute));
            }
        }
    }

    public void ApplyNewName(String newName){
        tree.setAttribute(attribute, newName);
        if(refs != null){
            for(NameChanger nc : refs){
                nc.ApplyNewName(newName);
            }
        }
    }

    private static List<NameChanger> findReferencesToLib(String libName){
        List<NameChanger> refs = new LinkedList<NameChanger>();
        //find in modules
        for(XMLTree module : ModularXMLFile.getModules().getChildrenElement()){
            if(module.isNamed("module")){
                for(XMLTree object : module.getChildrenElement()){
                    if( (object.isNamed("object") || (object.isNamed("frame") && object.getAttribute("type").equals("frame")))
                         && object.getAttribute("lib_id").equals(libName)){
                        refs.add(new NameChanger(object, "lib_id", false));
                    }
                }
            }
        }
        //find in lib dependencies
        for(XMLTree lib : ModularXMLFile.getLibs().getChildrenElement()){
            if(lib.isNamed("lib")){
                for(XMLTree depends : lib.getChildrenElement()){
                    if(depends.isNamed("depends") && depends.getAttribute("lib_id").equals(libName)){
                        refs.add(new NameChanger(depends, "lib_id", false));
                    }
                }
            }
        }
        //find in connectors
        for(XMLTree connector : ModularXMLFile.getConnectors().getChildrenElement()){
            if(connector.isNamed("connector")){
                for(XMLTree io : connector.getChildrenElement()){
                    if( (io.isNamed("input") || io.isNamed("output"))
                         && io.getAttribute("lib_id").equals(libName)){
                        refs.add(new NameChanger(io, "lib_id", false));
                    }
                }
            }
        }
        return refs;
    }


    private static void findReferencesToModuleInMenu(String moduleName, XMLTree menu, List<NameChanger> refsToFill){
         for(XMLTree child : menu.getChildrenElement()){
            if(child.isNamed("menu")){
                findReferencesToModuleInMenu(moduleName, child, refsToFill);
            }
            if(child.isNamed("item") && child.getAttribute("module").equals(moduleName)){
                refsToFill.add(new NameChanger(child, "module", false));
            }
         }
    }

    private static List<NameChanger> findReferencesToModule(String moduleName){
        List<NameChanger> refs = new LinkedList<NameChanger>();
        findReferencesToModuleInMenu(moduleName, ModularXMLFile.getMenus(), refs);
        return refs;
    }



    private static List<NameChanger> findReferencesToStyle(String styleName){
        List<NameChanger> refs = new LinkedList<NameChanger>();
        //find in modules
        for(XMLTree module : ModularXMLFile.getModules().getChildrenElement()){
            if(module.isNamed("module") && module.getAttribute("style").equals(styleName)){
                refs.add(new NameChanger(module, "style", false));
            }
        }
        //find in connectors
        for(XMLTree connector : ModularXMLFile.getConnectors().getChildrenElement()){
            if(connector.isNamed("connector") && connector.getAttribute("style").equals(styleName)){
                refs.add(new NameChanger(connector, "style", false));
            }
        }
        return refs;
    }
}
