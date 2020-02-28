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
package greta.core.util.parameter;

import greta.core.util.IniManager;
import greta.core.util.log.Logs;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLTree;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * This class is a set of {@code EngineParameterSet}.<br/>
 * It can load all {@code EngineParameterSet} from one XML file.<br/>
 * You can add or set a definition to all {@code EngineParameterSet}
 * using {@code add(String)} and {@code set(String)} functions with the corresponding XML file
 * in argument.
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @has - - * greta.core.util.parameter.EngineParameterSet
 */
public class EngineParameterSetOfSet {

//private constants :
    /** the XML schema file to validate XML files */
    public static final String XSDFile = IniManager.getGlobals().getValueString("XSD_PROFILES");

//private members :
    /** contains the name of all known/loaded definitions */
    private ArrayList<String> knownDefinition;
    /** the list of all contained EngineParameterSets */
    private ArrayList<EngineParameterSet> engineSets;


//public methods :
    public EngineParameterSetOfSet(){
        knownDefinition = new ArrayList<String>();
        engineSets = new ArrayList<EngineParameterSet>();
    }
    /**
     * Constructs an {@code EngineParameterSetOfSet} with a default definition for all contained {@code EngineParameterSet}.
     * @param fileName the name of the file of the default definition.
     */
    public EngineParameterSetOfSet(String fileName){
        this();
        set(fileName);
    }

    /**
     * Finds a specific {@code EngineParameterSet} by its name.
     * @param name the name of the {@code EngineParameterSet} to find.
     * @return the target {@code EngineParameterSet}, or {@code null} if not found
     */
    public EngineParameterSet find(String name){
        for(EngineParameterSet  engineSet : engineSets) {
            if(engineSet.getName().equalsIgnoreCase(name)) {
                return engineSet;
            }
        }
        return null;
    }

    /**
     * Sets as current definition to all contained {@code EngineParameterSets}.<br/>
     * Here, the name of the definition is also the name of the file.<br/>
     * If the definition is not already known, this function tries to load it.<br/>
     * @param fileName the name of the difinition and the file
     * @see #add(java.lang.String) add(String)
     */
    public void set(String fileName){
        add(fileName); //Try to add if the definition is unknown;
        for(EngineParameterSet  engineSet : engineSets) {
            engineSet.setDefinition(fileName);
        }
    }

    /**
     * Adds definition to all contained {@code EngineParameterSets}.<br/>
     * Here, the name of the definition is also the name of the file.<br/>
     * If the definition is not already known, it will be loaded from the specified file.<br/>
     * @param fileName the name of the difinition and the file
     */
    public void add(String fileName){
        if( ! knownDefinition.contains(fileName)){ //if the definition is unknown
            load(fileName); //we try to load it
        }
    }

//private methods :
    /**
     * Loads an XML file as definition for all contained {@code EngineParameterSet}.<br/>
     * If the {@code EngineParameterSet} (needed by the file) does not exist, it will be created and the definition
     * currently loaded will be be its default definition.
     * @param definition the definition file name
     */
    private void load(String definition) {
        knownDefinition.add(definition);
        boolean obsolet = false;//check for backward compatibility
        if((new File(definition)).exists()){
            XMLTree tree = XML.createParser().parseFileWithXSD(definition, XSDFile);
            for(XMLTree paramset : tree.getChildrenElement()){
                String name = paramset.getAttribute("name");
                EngineParameterSet engineSet = find(name);
                if(engineSet == null){
                    engineSet = new EngineParameterSet(name, definition);
                    engineSets.add(engineSet);
                }
                else {
                    engineSet.addDefinition(definition);
                }

                ArrayList<EngineParameter> paramsToAdd = new ArrayList<EngineParameter>();
                for(XMLTree param : paramset.getChildrenElement()){
                    String paramName = param.getAttribute("name");
                    if(paramName.toLowerCase().endsWith(".value") || paramName.toLowerCase().endsWith(".min") || paramName.toLowerCase().endsWith(".max")){//backward compatibility : must be removed
                        obsolet = true;
                        //get the name and the type of the parameter in the XML file
                        String[] nameDotType = paramName.split("\\.");
                        //search the EngineParameter with the same name in the list paramsToAdd
                        EngineParameter engineParam = null;
                        for(EngineParameter tmpEngineParam : paramsToAdd) {
                            if(tmpEngineParam.getParamName().equalsIgnoreCase(nameDotType[0])){
                                engineParam = tmpEngineParam;
                                break;
                            }
                        }
                        //if not found we create it
                        if(engineParam == null){
                            engineParam = new EngineParameter(nameDotType[0]);
                            paramsToAdd.add(engineParam);
                        }
                        //get the value
                        double value = param.getAttributeNumber("value");
                        //then we can set the value to its right place
                        if(nameDotType[1].equalsIgnoreCase("value")) {
                            engineParam.setValue(value);
                        }
                        else{
                            if(nameDotType[1].equalsIgnoreCase("min")) {
                                engineParam.setMin(value);
                            }
                            else{
                                if(nameDotType[1].equalsIgnoreCase("max")) {
                                    engineParam.setMax(value);
                                }
                            }
                        }
                    }//end backward compatibility
                    else{
                        EngineParameter engineParam = null;
                        for(EngineParameter tmpEngineParam : paramsToAdd) {
                            if(tmpEngineParam.getParamName().equalsIgnoreCase(paramName)){
                                engineParam = tmpEngineParam;
                                break;
                            }
                        }
                        //if not found we create it
                        if(engineParam == null){
                            engineParam = new EngineParameter(paramName);
                            paramsToAdd.add(engineParam);
                        }

                        engineParam.setValue(param.getAttributeNumber("value"));
                        if(param.hasAttribute("min")){
                            engineParam.setMin(param.getAttributeNumber("min"));
                        }
                        else{
                            engineParam.setMin(0);
                        }
                        if(param.hasAttribute("max")){
                            engineParam.setMax(param.getAttributeNumber("max"));
                        }
                        else{
                            engineParam.setMax(1);
                        }
                    }
                }

                engineSet.addParameters(definition,paramsToAdd);
            }
        }
        if(obsolet){
            Logs.warning(this.getClass().getSimpleName()+": old syntax found in "+definition+". Use attributes value/min/max instead of suffix \".value\"/\".min\"/\".max\" in the paramter name.");
        }
    }

    /**
     * Returns the list of all contained {@code EngineParameterSet}.
     * @return the list of all {@code EngineParameterSet}
     */
    public List<EngineParameterSet> getAllSets(){
        return engineSets;
    }

}
