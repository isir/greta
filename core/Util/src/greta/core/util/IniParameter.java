/*
 * Copyright 2025 Greta Modernization Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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
