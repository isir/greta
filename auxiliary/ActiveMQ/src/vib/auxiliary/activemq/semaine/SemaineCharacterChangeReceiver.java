/*
 *  This file is part of the auxiliaries of VIB (Virtual Interactive Behaviour).
 */
package vib.auxiliary.activemq.semaine;

import vib.auxiliary.activemq.TextReceiver;
import vib.auxiliary.activemq.WhiteBoard;
import vib.core.util.CharacterManager;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;
import java.util.Map;

/**
 *
 * @author Andre-Marie Pez
 */
public class SemaineCharacterChangeReceiver extends TextReceiver{
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
            CharacterManager.setCharacter(character.getAttribute("name"));
        }
    }
}
