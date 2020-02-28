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
import greta.application.modular.tools.ModuleListModel.ModuleElement;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.swing.AbstractListModel;

/**
 *
 * @author Andre-Marie Pez
 */
public class ModuleListModel extends AbstractListModel<ModuleElement>{


    ArrayList<ModuleElement> modules;

    public ModuleListModel(){
        modules = new ArrayList<ModuleElement>();
        reload();
    }

    public void reload(){
        modules.clear();
        List<XMLTree> modulesXML = ModularXMLFile.getModules().getChildrenElement();
        modules.ensureCapacity(modulesXML.size());
        for(XMLTree moduleXML : modulesXML){
            this.modules.add(new ModuleElement(moduleXML));
        }
        Collections.sort(modules);
    }

    @Override
    public int getSize() {
        return modules.size();
    }

    @Override
    public ModuleElement getElementAt(int index) {
        return modules.get(index);
    }

    public ModuleElement createModule(){
        XMLTree moduleXML = ModularXMLFile.getModules().createChild("module");
        moduleXML.createChild("object");
        ModuleElement module = new ModuleElement(moduleXML);
        modules.add(module);
        Collections.sort(modules);
        int index = modules.indexOf(module);
        fireIntervalAdded(this, index, index);
        return module;
    }

    public void deleteModule(ModuleElement module){
        module.module.getParent().removeChild(module.module);
        int index = modules.indexOf(module);
        modules.remove(index);
        fireIntervalRemoved(this, index, index);
    }

    public void moduleNameChanged(ModuleElement module){
//        int prev_index = modules.indexOf(module);
//
        Collections.sort(modules);
//
//        int new_index = modules.indexOf(module);
//
//        if(prev_index==new_index){
//            fireContentsChanged(this, new_index, new_index);
//        }
//        else{
//            fireIntervalRemoved(this, prev_index, prev_index);
//            fireIntervalAdded(this, new_index, new_index);
//        }
    }


    public class ModuleElement implements Comparable<ModuleElement>{

        XMLTree module;
        NameChanger nc;

        private ModuleElement(XMLTree module){
            this.module = module;
            updateNameChanger(false);
        }

        @Override
        public String toString() {
            return getName();
        }

        @Override
        public int compareTo(ModuleElement o) {
            return String.CASE_INSENSITIVE_ORDER.compare(getName(), o.getName());
        }

        public void setName(String name){
            nc.ApplyNewName(name);
            moduleNameChanged(this);
        }

        public String getName(){
            return module.getAttribute("name");
        }

        public void setFrameType(int i) {
            if(i == 0){
                removeFrame();
            }
            if(i == 1){
                XMLTree frameXML = getOrCreateFrame();
                frameXML.setAttribute("type", "object");
                frameXML.setAttribute("class", null);
                frameXML.setAttribute("lib_id", null);
            }
            if(i == 2){
                getOrCreateFrame().setAttribute("type", "frame");
            }
        }

        public int getFrameType(){
            XMLTree frameXML = getFrame();
            if(frameXML != null){
                String type = frameXML.getAttribute("type");
                if(type.equals("object")){
                    return 1;
                }
                if(type.equals("frame")){
                    return 2;
                }
            }
            return 0;
        }

        public XMLTree getOrCreateFrame(){
            XMLTree frameXML = getFrame();
            if(frameXML == null){
                frameXML = module.createChild("frame");
            }
            return frameXML;
        }

        public XMLTree getFrame(){
            return module.findNodeCalled("frame");
        }

        void removeFrame(){
            XMLTree frameXML = getFrame();
            if(frameXML != null){
                module.removeChild(frameXML);
            }
        }

        public void setStyle(String style) {
            module.setAttribute("style", style==null ||style.isEmpty() ? null : style);
        }

        public String getStyle() {
            return  module.getAttribute("style");
        }

        public boolean isWindowed() {
            XMLTree frameXML = getFrame();
            return frameXML != null && frameXML.getAttribute("windowed_only").equalsIgnoreCase("true");
        }

        public void setWindowed(boolean windowed){
            XMLTree frameXML = getFrame();
            if(frameXML == null){
                return ;
            }
            frameXML.setAttribute("windowed_only", windowed ? "true" : null);
        }

        XMLTree getObject() {
            return module.findNodeCalled("object");
        }

        void updateNameChanger(boolean reference) {
            nc = new NameChanger(module, "name", reference);
        }
    }
}
