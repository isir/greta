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

import greta.core.util.log.Logs;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

/**
 * This class manages a set of multi-defined parameters.<br/>
 * The contructor loads the default definition of parameters.
 * To add a new definition of parameters, call the {@code addDefinition(String)} method,
 * and to set the definition to use, call the {@code setDefinition(String)} method.<br/>
 * Call {@code get(String)} method to get a specific parameter,
 * if the parameter is not defined in the current definition, {@code get(String)} returns the default value.
 * @see greta.core.util.parameter.Parameter Parameter
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @composed - - + greta.core.util.parameter.Definition
 */
public abstract class ParameterSet<P extends Parameter> {

    protected Definition<P> defaultDefinition;
    protected ArrayList<Definition<P>> otherDefinition;
    protected Definition<P> currentDefinition;

//public methods :
    public ParameterSet() {
        otherDefinition = new ArrayList<Definition<P>>();
    }
    /**
     * Contructor.
     * @param defaultDefinitionName the name of the default definition to use
     */
    public ParameterSet(String defaultDefinitionName) {
        this();
        setDefaultDefinition(defaultDefinitionName);
    }

    /**
     * Sets the target definition as the definition to use.<br/>
     * If the definition is not found, it tries to add it or uses the default one.
     * @param definitionName the definition name
     * @see #load(String)
     */
    public void setDefaultDefinition(String defaultDefinitionName) {
         defaultDefinition = new Definition<P>(defaultDefinitionName, load(defaultDefinitionName));
    }

    /**
     * Sets the target definition as the definition to use.<br/>
     * If the definition is not found, it tries to add it or uses the default one.
     * @param definitionName the definition name
     * @see #load(String)
     */
    public void setDefinition(String definitionName) {
        if (isDefaultDefinition(definitionName)) {
            currentDefinition = null;
        } else {
            currentDefinition = findDefinition_(definitionName);
            if (currentDefinition == null) {
                currentDefinition = addDefinition_(definitionName);
            }
        }
    }

    /**
     * Loads and adds a new definition of parameters.<br/>
     * If the definition is already added, it does nothing.
     * @param definitionName the definition name
     * @see #load(String)
     */
    public void addDefinition(String definitionName) {
        if (findDefinition(definitionName) == null) {
            addDefinition_(definitionName);
        }
    }

    /**
     * Returns a target parameter of the current definition.
     * @param paramName name of the parameter
     * @return the parameter
     */
    public P get(String paramName) {
        return get(paramName, currentDefinition);
    }

    /**
     * Returns a target parameter of a specified definition.
     * @param paramName name of the parameter
     * @param definitionName name of the definition
     * @return the parameter
     */
    public P get(String paramName, String definitionName) {
        addDefinition(definitionName); //try to add a new definition
        return get(paramName, findDefinition(definitionName));
    }

    /**
     * Returns all parameters of the current definition.
     * @return the parameters
     */
    public List<P> getAll() {
        ArrayList<P> params = new ArrayList<P>(defaultDefinition.getParameters().size());
        //first : add all parameters of the current definition
        if (currentDefinition != null) {
            params.addAll(currentDefinition.getParameters());
        }

        //second : search parameters in the default definition
        if(currentDefinition!=defaultDefinition){
            for (P param : defaultDefinition.getParameters()) {
                int i = 0;
                //search if the parameter is already added
                while (i < params.size() && !params.get(i).getParamName().equalsIgnoreCase(param.getParamName())) {
                    ++i;
                }
                //if not found, then add it
                if (i == params.size()) {
                    params.add(param);
                }
            }
        }
        return params;
    }

    /**
     * Checks if a definition is already exist.
     * @param definitionName the name of the definition
     * @return {@code true} if the definition is known, {@code false} otherwise
     */
    public boolean hasDefinition(String definitionName) {
        return findDefinition(definitionName) != null;
    }

    /**
     * Returns all {@code Parameter} from all {@code Definition} which match to given name.
     * @param paramName the name of parameter to search
     * @return the list of matching {@code Parameters}
     */
    public List<P> getAllFromOne(String paramName) {
        List<P> allValues = new ArrayList<P>();
        P param = defaultDefinition.getParameter(paramName);
        if (param != null) {
            allValues.add(param);
        }
        for (Definition<P> def : otherDefinition) {
            param = def.getParameter(paramName);
            if (param != null) {
                allValues.add(param);
            }
        }
        return allValues;
    }

    /**
     * Returns a target parameter of the default definition.
     * @param paramName name of the parameter
     * @return the default parameter
     */
    public P getDefault(String paramName) {
        return defaultDefinition.getParameter(paramName);
    }

    /**
     * Returns the default {@code Definition}.
     * @return the default {@code Definition}
     */
    public Definition<P> getDefaultDefinition() {
        return defaultDefinition;
    }

    /**
     * Returns the current {@code Definition}.
     * @return the current {@code Definition}
     */
    public Definition<P> getCurrentDefinition() {
        return currentDefinition==null ? defaultDefinition : currentDefinition;
    }

    /**
     * Creates an empty {@code definition}.
     * @param definitionName the name of the {@code Definition}
     * @return the new {@code Definition}
     */
    public Definition<P> createEmptyDefinition(String definitionName){
        Definition<P> definition = new Definition<P>(definitionName, new ArrayList<P>());
        otherDefinition.add(definition);
        return definition;
    }

    public void saveCurrentDefinition(){
        saveCurrentDefinition(true);
    }

    public void saveCurrentDefinition(boolean optimize){
        if(currentDefinition==null){
            saveDefaultDefinition();
        }
        else {
            ArrayList<P> toSave = new ArrayList<P>(currentDefinition.getParameters());
            if(optimize){
                ListIterator<P> iterator = toSave.listIterator();
                while(iterator.hasNext()){
                    P param = iterator.next();
                    P inDefault = defaultDefinition.getParameter(param.getParamName());
                    if(inDefault==null){
                        Logs.warning("default values for "+param.getParamName()+" does not exist");
                    }
                    else{
                        if(param.equals(inDefault)){
                            iterator.remove();
                        }
                    }
                }
            }
            save(currentDefinition.getName(), toSave);
        }
    }

    public void saveDefaultDefinition(){
        save(defaultDefinition.getName(), new ArrayList(defaultDefinition.getParameters()));
    }

    public void refreshAll(){
        refreshDefaultDefinition();
        for(Definition<P> def : otherDefinition){
            refresh(def);
        }
    }

    public void refreshDefaultDefinition(){
        refresh(defaultDefinition);
    }

    public void refreshCurrentDefinition(){
        refresh(currentDefinition);
    }

    public void refreshDefinition(String definition){
        refresh(findDefinition(definition));
    }

//abstract methods :
    /**
     * Loads a definition.<br/>
     * If the doading failed, it returns an empty List.<br/>
     * Typically, the name of the definition is the name of a file.
     * @param definition the name of the definition
     * @return the list of the parameters of this definition
     */
    protected abstract List<P> load(String definition);

    protected abstract void save(String definition, List<P> paramToSave);

//protected methods :
    /**
     * Search a definition in all definition.
     * @param definitionName name of the target definition
     * @return the target definition
     */
    protected Definition<P> findDefinition(String definitionName) {
        Definition<P> definition = findDefinition_(definitionName);
        if (definition == null && isDefaultDefinition(definitionName)) {
            definition = defaultDefinition;
        }
        return definition;
    }

//private methods :
    /**
     * Search a definition in all definition (without the default definition).
     * @param definitionName name of the target definition
     * @return the target definition
     */
    private Definition<P> findDefinition_(String definitionName) {
        for (Definition<P> definition : otherDefinition) {
            if (definition.getName().equalsIgnoreCase(definitionName)) {
                return definition;
            }
        }
        return null;
    }

    /**
     * Loads and adds a new Definition.
     * @param definitionName the name of the definition
     * @return the created definition
     * @see greta.core.util.parameter.Definition Definition
     */
    private Definition<P> addDefinition_(String definitionName) {
        Definition<P> newDefinition = new Definition<P>(definitionName, load(definitionName));
        otherDefinition.add(newDefinition);
        return newDefinition;
    }

    /**
     * Checks if the definition name corresponds to the default definition.
     * @param definitionName the name of the definition
     * @return {@code true} if definitionName equals {@code null}, empty string, {@code "default"} or the name of the default definition. {@code false} otherwise.
     */
    private boolean isDefaultDefinition(String definitionName) {
        return definitionName == null || definitionName.equalsIgnoreCase(defaultDefinition.getName()) || definitionName.equalsIgnoreCase("default") || definitionName.isEmpty();
    }

    private P get(String paramName, Definition<P> def) {
        P found = null;
        if (def != null) {
            found = def.getParameter(paramName);
        }
        if (found == null) {
            found = defaultDefinition.getParameter(paramName);
        }
        return found;
    }

    private void refresh(Definition<P> def){
        if(def!=null){
            def.clear();
            def.addParameters(load(def.getName()));
        }
    }
}
