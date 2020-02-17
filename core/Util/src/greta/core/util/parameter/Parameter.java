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

/**
 * This interface describes a parameter to use with the ParameterSet class.<br/>
 * An implementation {@code MyParameterImpl} of this interface should implements {@code Parameter<MyParameterImpl>}.
 * @see greta.core.util.parameter.ParameterSet ParameterSet
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
