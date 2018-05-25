/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util;

import vib.core.util.parameter.Parameter;

/**
 * This class contains the name and value of a single IniParameter.<br/>
 * The IniParameter has been read from an ini file by the class IniManager
 * @see vib.core.util.IniManager IniManager
 * @see vib.core.util.parameter.Parameter Parameter
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
