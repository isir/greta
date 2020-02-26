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

/**
 * Update 2018-08-13 : now we keep a reference to the CharacterManager Instance
 * @author Philippe Gauthier <philippe.gauthier@sorbonne-universite.fr>
 *
 * This interface describes an object that depends of the character displaying,
 * and that wants to know if the character change.<br/>
 * when the current character is changed, the function {@code onCharacterChanged()} of all
 * {@code CharacterDependent} added in the {@code CharacterManager} will be called.<br/>
 * It is recommended to call {@code CharacterManager.add(this)} in the constructor, and get the values needed
 * (until the character changes, {@code onCharacterChanged()} is not called).
 * @author Andre-Marie Pez
 * @see greta.core.util.CharacterManager CharacterManager
 */
public interface CharacterDependent {

    /**
     * This function will be call when the chararter is changed.<br/>
     * In this function, the {@code CharacterDependent} must updates its parameters
     * calling the {@code CharacterManager}.
     */
    public void onCharacterChanged();

    public CharacterManager getCharacterManager();

    public void setCharacterManager(CharacterManager characterManager);

    public static CharacterManager getCharacterManagerStatic() {
        return CharacterManager.getStaticInstance();
    }

}
