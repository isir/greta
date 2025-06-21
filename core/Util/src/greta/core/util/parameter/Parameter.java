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
