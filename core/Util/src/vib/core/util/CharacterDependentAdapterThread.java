/*
 *  This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.util;

/**
 *
 * @author Philippe Gauthier <philippe.gauthier@upmc.fr>
 */
public abstract class CharacterDependentAdapterThread extends Thread implements CharacterDependent{
    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager=CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        if(this.characterManager!=null)
            this.characterManager.remove(this);
        this.characterManager = characterManager;
        characterManager.add(this);
    }
    
    public static CharacterManager getCharacterManagerStatic(){
        return CharacterManager.getStaticInstance();
    }
}
