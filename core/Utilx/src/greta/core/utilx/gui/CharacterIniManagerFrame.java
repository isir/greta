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
package greta.core.utilx.gui;

import greta.core.util.CharacterManager;

/**
 *
 * @author Andre-Marie Pez
 */
public class CharacterIniManagerFrame extends IniManagerFrame{
    private CharacterManager cm;

    /**
     * Default constructor
     */
    public CharacterIniManagerFrame(){

    }

    public void setCharacterManager(CharacterManager cm){
        this.cm = cm;
        setIniManager(cm.getIniManager());
        connect(new CharacterIniLoader(cm), new LanguageIniLoader(cm));
//        connect(new CharacterIniLoader(cm));
    }

    public CharacterManager getCharacterManager(){
        return cm;
    }

    public String getCharacterName(){
        return cm.getCurrentCharacterName();
    }

    public void setCharacterName(String name){
        cm.setCharacter(name);
    }
}
