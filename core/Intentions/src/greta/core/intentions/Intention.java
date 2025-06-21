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
package greta.core.intentions;

import greta.core.util.time.Temporizable;

/**
 * Contains informations on a single {@code Intention}.<br>
 * The communicative intention can have a unique ID and the name of the class of communicative intentions
 * it belongs to and/or the name of the instance. The representation class-instance is typical
 * of languages like FML-APML. The other fields contain information on the communicative intention level
 * of importance.
 * @author Andre-Marie Pez
 */
public interface Intention extends Temporizable{

    /**
     * Returns the name of the class of the Intention.
     * @return the name of the class of the Intention
     */
    public String getName();

    /**
     * Every Intention has a type attribute.
     * @return the type attribute
     */
    public String getType();

    /**
     * Returns the importance of the Intention.<br>
     * It has to be a floating point number between 0 and 1.
     * @return the importance of the Intention
     */
    public double getImportance();


    public boolean hasCharacter();

    public String getCharacter();

    public String getTarget ();

}
