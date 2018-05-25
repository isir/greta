/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */

package vib.core.util;

/**
 * This interface describes an object that depends of the character displaying,
 * and that wants to know if the character change.<br/>
 * when the current character is changed, the function {@code onCharacterChanged()} of all
 * {@code CharacterDependent} added in the {@code CharacterManager} will be called.<br/>
 * It is recommended to call {@code CharacterManager.add(this)} in the constructor, and get the values needed
 * (until the character changes, {@code onCharacterChanged()} is not called).
 * @author Andre-Marie Pez
 * @see vib.core.util.CharacterManager CharacterManager
 */
public interface CharacterDependent {

    /**
     * This function will be call when the chararter is changed.<br/>
     * In this function, the {@code CharacterDependent} must updates its parameters
     * calling the {@code CharacterManager}.
     */
    public void onCharacterChanged();

}
