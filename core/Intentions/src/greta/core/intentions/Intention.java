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
