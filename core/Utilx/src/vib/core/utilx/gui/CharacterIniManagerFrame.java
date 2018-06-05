/* This file is part of Greta.
 * Greta is free software: you can redistribute it and / or modify
* it under the terms of the GNU General Public License as published by
* the Free Software Foundation, either version 3 of the License, or
* (at your option) any later version.
* 
* Greta is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.See the
* GNU General Public License for more details.
* 
* You should have received a copy of the GNU General Public License
* along with Greta.If not, see <http://www.gnu.org/licenses/>.
*//*
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
