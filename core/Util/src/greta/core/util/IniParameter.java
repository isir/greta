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
package greta.core.util;

import greta.core.util.parameter.Parameter;

/**
 * This class contains the name and value of a single IniParameter.<br/>
 * The IniParameter has been read from an ini file by the class IniManager
 * @see greta.core.util.IniManager IniManager
 * @see greta.core.util.parameter.Parameter Parameter
 * @author Andre-Marie Pez
 */
public class IniParameter implements Parameter<IniParameter>{
    private String name;
    private String value;

    /**
     * Constructor
     * @param name the name of the parameter
     * @param value the value of the parameter
     */
    public IniParameter(String name, String value){
        setParamName(name);
        setParamValue(value);
    }

    /**
     * Returns the value of this parameter.
     * @return the value of the parameter
     */
    public String getParamValue(){
        return value;
    }

    /**
     * Sets the specified value to this parameter.
     * @param value the value to set
     */
    public void setParamValue(String value){
        this.value = value;
    }

    @Override
    public String getParamName() {
        return name;
    }

    @Override
    public void setParamName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(IniParameter other) {
        return name.equalsIgnoreCase(other.name) && value.equals(other.value);
    }
}
