/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util.parameter;

/**
 * This interface describes a parameter to use with the ParameterSet class.<br/>
 * An implementation {@code MyParameterImpl} of this interface should implements {@code Parameter<MyParameterImpl>}.
 * @see vib.core.util.parameter.ParameterSet ParameterSet
 * @author Andre-Marie Pez
 */
public interface Parameter<P extends Parameter>{

    /**
     * Returns the name of the parameter.<br/>
     * It's used by the ParameterSet.
     * @return the name of the parameter
     */
    public String getParamName();

    /**
     * Sets the name of the parameter.
     * @param name the name of the parameter
     */
    public void setParamName(String name);


    /**
     * Test the equality of two {@code Parameter}s.
     * @param other an other {@code Parameter}
     * @return {@code true} if {@code this} and the {@code other} {@code Parameter} are equal, {@code false} otherwise.
     */
    public boolean equals(P other);

}
