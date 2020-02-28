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
package greta.application.modular.modules;

import com.mxgraph.model.mxCell;
import greta.application.modular.Modular;
import greta.core.util.CharacterManager;
import java.util.Map;
import javax.swing.JFrame;

/**
 *
 * @author Andre-Marie Pez
 */
public class Module {

    private Object object;
    private mxCell cell;
    private JFrame controlFrame;
    private String id;
    private ModuleFactory.ModuleInfo type;
    private Module parent;

    public Module(ModuleFactory.ModuleInfo type, String id, Object object, mxCell cell, JFrame control) {
        this.type = type;
        this.id = id;
        this.cell = cell;
        this.object = object;
        this.controlFrame = control;
        if (controlFrame != null) {
            controlFrame.setIconImage(Modular.icon);
            if(type.windowedOnly){
                controlFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
                controlFrame.setVisible(true);
            }
            else {
                controlFrame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);//DO NOT STOP ALL PROCESS !!!
            }
        }
    }

    public boolean hasParent(){
        return parent!=null;
    }

    public CharacterManager getCharacterManager(){
        if(parent!=null && parent.getObject() instanceof CharacterManager){
            return (CharacterManager)parent.getObject();
        }
        else
            return CharacterManager.getStaticInstance();
    }

    public Module(ModuleFactory.ModuleInfo type, String id, Object object, mxCell cell) {
        this(type, id, object, cell, null);
    }


    /**
     * @return the parent
     */
    public Module getParent() {
        return parent;
    }

    /**
     * @param parent the parent to set
     */
    public void setParent(Module parent) {
        this.parent = parent;
    }

    public Object getObject() {
        return object;
    }

    public mxCell getCell() {
        return cell;
    }

    public String getId(){
        return id;
    }

    public String getType(){
        return type.name;
    }

    public ModuleFactory.ModuleInfo getInfo(){
        return type;
    }

    public Map<String,String> getParams(){
        return null;
    }

    public JFrame getFrame(){
        return controlFrame;
    }

    public String getObjectVariableName(){
        return Modular.createRegularVariableName(id);
    }

    public String getFrameVariableName(){
        if(controlFrame == null){
            return null;
        }
        if(controlFrame == object){
            return getObjectVariableName();
        }
        return Modular.createRegularVariableName(id)+"_ModuleFrame";
    }
}
