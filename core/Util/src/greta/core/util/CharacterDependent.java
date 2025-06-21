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
