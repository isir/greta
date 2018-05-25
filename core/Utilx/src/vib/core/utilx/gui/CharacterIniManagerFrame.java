/*
 * This file is part of VIB (Virtual Interactive Behaviour).
 */
package vib.core.utilx.gui;

import vib.core.util.CharacterManager;

/**
 *
 * @author Andre-Marie Pez
 */
public class CharacterIniManagerFrame extends IniManagerFrame{
    /**
     * Default constructor
     */
    public CharacterIniManagerFrame(){
        setIniManager(CharacterManager.getIniManager());
        connect(new CharacterIniLoader());
    }

    public String getCurrentCaracter(){
        return CharacterManager.getCurrentCharacterName();
    }

    public void setCurrentCaracter(String name){
        if(name!=null && !name.isEmpty())
        CharacterManager.setCharacter(name);
    }
}
