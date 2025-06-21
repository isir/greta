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
package greta.auxiliary.activemq.semaine;

import greta.auxiliary.activemq.TextReceiver;
import greta.auxiliary.activemq.WhiteBoard;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class SemaineCharacterChangeReceiver extends TextReceiver implements CharacterDependent{
    XMLParser stateParser;

    public SemaineCharacterChangeReceiver(){
        this(WhiteBoard.DEFAULT_ACTIVEMQ_HOST,
             WhiteBoard.DEFAULT_ACTIVEMQ_PORT,
             "semaine.data.state.context");
    }

    public SemaineCharacterChangeReceiver(String host, String port, String topic){
        super(host, port, topic);
        stateParser = XML.createParser();
        stateParser.setValidating(false);
    }


    @Override
    protected void onMessage(String content, Map<String, Object> properties) {
        XMLTree state = stateParser.parseBuffer(content.toString());
        XMLTree character = state.findNodeCalled("character");
        if(character!=null) {
            CharacterManager.getStaticInstance().setCharacter(character.getAttribute("name"));
        }
    }


    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        this.characterManager = characterManager;
    }

    private CharacterManager characterManager;

    @Override
    public void onCharacterChanged() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
}
