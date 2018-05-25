/*
 * This file is a part of the Modular application.
 */

package vib.application.modular.modules;

import com.mxgraph.model.mxCell;
import com.mxgraph.view.mxGraph;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.swing.JFrame;

/**
 *
 * @author Andre-Marie Pez
 */
public class ModuleFactory {

    public static List<ModuleInfo> moduleInfos = new ArrayList<ModuleInfo>();

    public static Module create(mxGraph graph, String moduleType){
        return create(graph, moduleType, moduleType,moduleType+"-"+System.currentTimeMillis()+"-"+(int)(Math.random()*1000.0), 15, 15, 80, 50, null);
    }

    public static Module create(mxGraph graph, String moduleType, String cellName, String id, double x, double y, double w, double h, Map<String,String> params){
        //Create an empty Map
        if(params==null) {
            params = new HashMap<String,String>();
        }
        ModuleInfo moduleInfo = findModuleInfo(moduleType);

        if(moduleInfo!=null){
            try {
                Object object = moduleInfo.objectClass.newInstance();
                for(ParameterInfo parameterInfo : moduleInfo.parameterInfos){
                    if(parameterInfo.setOn.equals("object")){
                        String value = parameterInfo.defaultvalue;
                        if(params.containsKey(parameterInfo.name)) {
                            value = params.get(parameterInfo.name);
                        }
                        try{
                            parameterInfo.setMethod.invoke(
                                object,
                                new Object[]{castStringToTypedObject(
                                    parameterInfo.type,
                                    value)});
                        }
                        catch(Exception e){
                            e.printStackTrace();
                            //TODO say something
                        }
                    }
                }

                JFrame jFrame = null;
                try{
                    if(moduleInfo.frameType.equals("frame") && moduleInfo.frameClass!=null){
                        jFrame = (JFrame)moduleInfo.frameClass.newInstance();
                        if(moduleInfo.linkOn!=null){
                            moduleInfo.linkMethod.invoke(
                                moduleInfo.linkOn.equals("object")? object : jFrame,
                                    new Object[]{
                                        moduleInfo.linkOn.equals("object")? jFrame : object
                                    });
                        }
                    }
                    else if(moduleInfo.frameType.equals("object")) {
                        jFrame = (JFrame)object;
                    }
                }
                catch(Exception e){System.err.println("Wrong type for frame in module "+moduleInfo.name);}

                for(ParameterInfo parameterInfo : moduleInfo.parameterInfos){
                    if( ! parameterInfo.setOn.equals("object")){
                        String value = parameterInfo.defaultvalue;
                        if(params.containsKey(parameterInfo.name)) {
                            value = params.get(parameterInfo.name);
                        }
                        try{
                            parameterInfo.setMethod.invoke(
                                jFrame,
                                new Object[]{castStringToTypedObject(
                                    parameterInfo.type,
                                    value)});
                        }
                        catch(Exception e){
                            e.printStackTrace();
                            //TODO say something
                        }
                    }
                }

                return new Module(
                    moduleInfo,
                    id,
                    object,
                    (mxCell)(graph.insertVertex(null, id, cellName, x, y , w, h, Style.getMapper().getVertexStyle(moduleInfo.style))),
                    jFrame){
                        @Override
                        public Map<String, String> getParams() {
                            Map<String,String> parameters = new HashMap<String,String>();
                            ModuleInfo moduleInfo = getInfo();
                            for(ParameterInfo parameterInfo : moduleInfo.parameterInfos){
                                try{
                                    Object value = parameterInfo.getMethod.invoke(
                                        parameterInfo.getOn.equals("object")?
                                            getObject() : getFrame(),
                                        new  Object[]{});
                                    parameters.put(parameterInfo.name, value.toString());
                                }
                                catch(Exception e){
                                    e.printStackTrace();
                                    //TODO say something
                                }
                            }
                            return parameters;
                        }
                    };
            }
            catch (Throwable ex) {
                while(ex.getCause() != null){
                    ex.printStackTrace();
                    ex = ex.getCause();
                }
                ex.printStackTrace();
            }
        }
        return null;
    }

    public static Object castStringToTypedObject(String type, String value){
        if(type.equals("boolean")) {
            return Boolean.valueOf(value);
        }
        try{
            if(type.equals("byte")) {
                return Byte.valueOf(value);
            }
            if(type.equals("short")) {
                return Short.valueOf(value);
            }
            if(type.equals("integer")) {
                return Integer.valueOf(value);
            }
            if(type.equals("long")) {
                return Long.valueOf(value);
            }
            if(type.equals("float")) {
                return Float.valueOf(value);
            }
            if(type.equals("double")) {
                return Double.valueOf(value);
            }
        }
        catch(Exception e){
            System.err.println("value \""+value+"\" is wrong with respect to type "+type);
            return castStringToTypedObject(type,"0");
        }

        return value;
    }

    public static ModuleInfo findModuleInfo(String name){
        for(ModuleInfo moduleInfo : moduleInfos) {
            if(moduleInfo.name.equals(name)) {
                return moduleInfo;
            }
        }
        return null;
    }

    /**
     * Structure containning informations about one module
     */
    public static class ModuleInfo{
        public String name;
        public String style;
        public int restriction;
        public Class  objectClass;
        public String frameType;
        public Class  frameClass;
        public Method linkMethod;
        public String linkOn;
        public String description;
        public boolean windowedOnly;
        public ArrayList<ParameterInfo> parameterInfos = new ArrayList<ParameterInfo>();
        public Library objectLib;
        public Library frameLib;

        public void addParameter(String type, String name, String defaultvalue, String setOn, Method setMethod, String getOn, Method getMethod){
            parameterInfos.add(new ParameterInfo(type, name, defaultvalue, setOn, setMethod, getOn, getMethod));
        }

        protected ParameterInfo getParameterInfo(String parameterName){
            for(ParameterInfo parameterInfo : parameterInfos) {
                if(parameterInfo.name.equals(parameterName)) {
                    return parameterInfo;
                }
            }
            return null;
        }

    }

    /**
     * Structure containning informations about one module parameter
     */
    public static class ParameterInfo{
        private String type;
        private String name;
        private String defaultvalue;
        private String setOn;
        private Method setMethod;
        private String getOn;
        private Method getMethod;

        ParameterInfo(String type, String name, String defaultvalue, String setOn, Method setMethod, String getOn, Method getMethod){
            this.type = type;
            this.name = name;
            this.defaultvalue = defaultvalue;
            this.setOn = setOn;
            this.setMethod = setMethod;
            this.getOn = getOn;
            this.getMethod = getMethod;
        }

        public String getType() {
            return type;
        }

        public String getName() {
            return name;
        }

        public String getDefaultvalue() {
            return defaultvalue;
        }

        public String getSetOn() {
            return setOn;
        }

        public Method getSetMethod() {
            return setMethod;
        }

        public String getGetOn() {
            return getOn;
        }

        public Method getGetMethod() {
            return getMethod;
        }

    }
}
