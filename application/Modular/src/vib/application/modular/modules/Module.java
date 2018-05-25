/*
 * This file is a part of the Modular application.
 */


package vib.application.modular.modules;

import com.mxgraph.model.mxCell;
import java.util.Map;
import javax.swing.JFrame;
import vib.application.modular.Modular;

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

    public Module(ModuleFactory.ModuleInfo type, String id, Object object, mxCell cell) {
        this(type, id, object, cell, null);
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
