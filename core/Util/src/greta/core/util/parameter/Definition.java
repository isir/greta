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

import java.util.Collection;
import java.util.HashMap;

/**
 * This class a definition of parameters of the ParameterSet class.
 * @see greta.core.util.parameter.ParameterSet ParameterSet
 * @see greta.core.util.parameter.Parameter Parameter
 * @author Andre-Marie Pez
 *
 * the following tags generate a warning in Javadoc generation because
 * they are UmlGraph tags, not javadoc tags.
 * @composed - - * greta.core.util.parameter.Parameter
 */
public final class Definition <P extends Parameter>{

    private String name;
    private HashMap<String, P> paramMap;

    /**
     * Constructor.
     * @param name name of this definition
     * @param parameters collection of paraters of this definition
     */
    public Definition(String name, Collection<P> parameters){
        this.name = name;
        this.paramMap = new HashMap<String, P>(parameters.size());
        addParameters(parameters);
    }

    /**
     * Returns the name of the definition.
     * @return the name of the definition
     */
    public String getName(){
        return name;
    }

    /**
     * change the name of this {@code Definition}.
     * @param name the new name
     */
    public void setName(String name){
        this.name = name;
    }

    /**
     * Returns the list of the parameters defined.
     * @return the list of parameters
     */
    public Collection<P> getParameters(){
        return paramMap.values();
    }

    /**
     * Finds and returns a specified parameter.<br/>
     * If the parameter is not found, it returns {@code null}
     * @param parameterName the name of the parameter
     * @return the target parameter
     */
    public P getParameter(String parameterName){
        if(parameterName!=null) {
            return paramMap.get(parameterName.toUpperCase());
        }
        return null;
    }

    /**
     * Check if this {@code Definition} contains a specific parameter.
     * @param parameterName the name of the parameter
     * @return {@code true} if this {@code Definition} contains the specified parameter, {@code false} otherwise.
     */
    public boolean contains(String parameterName){
        return parameterName!=null && paramMap.containsKey(parameterName.toUpperCase());
    }

    /**
     * Adds a {@code Parameter} in this {@code Definition}.<br/>
     * if the {@code Parameter} is already added (identified by {@code Parameter.getParamName()}), it will be replaced by the new value.
     * @param param the {@code Parameter} to add
     */
    public void addParameter(P param){
        if(param.getParamName()!=null) {
            paramMap.put(param.getParamName().toUpperCase(), param);
        }
    }

    /**
     * Adds a collection of {@code Parameters} in this {@code Definition}.<br/>
     * The {@code Parameters} already added (identified by {@code Parameter.getParamName()}) will be replaced by the new value.
     * @param params the collection of {@code Parameters} to add
     */
    public void addParameters(Collection<P> params){
        //we add each Parameter in the list
        for(P param : params) {
            addParameter(param);
        }
    }

    public void clear(){
        paramMap.clear();
    }
}
