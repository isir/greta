/*
 * This file is part of the auxiliaries of Greta.
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
