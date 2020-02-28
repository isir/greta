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
package greta.core.repositories;

import greta.core.signals.gesture.TouchPosition;
import greta.core.util.CharacterDependent;
import greta.core.util.CharacterManager;
import greta.core.util.math.Vec3d;
import greta.core.util.parameter.ParameterSet;
import greta.core.util.xml.XML;
import greta.core.util.xml.XMLParser;
import greta.core.util.xml.XMLTree;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Brian Ravenet
 */
public class TouchPositionLibrary extends ParameterSet<TouchPosition> implements CharacterDependent {


    public static final String CHARACTER_PARAMETER_TOUCHPOINT = "TOUCHPOINT";
    public static TouchPositionLibrary globalTouchPositionlibrary;
    static{
        globalTouchPositionlibrary = new TouchPositionLibrary();
    }

    private CharacterManager characterManager;

    /**
     * @return the characterManager
     */
    @Override
    public CharacterManager getCharacterManager() {
        if(characterManager==null)
            characterManager = CharacterManager.getStaticInstance();
        return characterManager;
    }

    /**
     * @param characterManager the characterManager to set
     */
    @Override
    public void setCharacterManager(CharacterManager characterManager) {
        this.characterManager = characterManager;
    }

    public TouchPositionLibrary() {
        //get the default library :
        super();
        setDefaultDefinition(getCharacterManager().getDefaultValueString(CHARACTER_PARAMETER_TOUCHPOINT));
        //load additionnal library :
        for(String fileName : getCharacterManager().getAllValuesString(CHARACTER_PARAMETER_TOUCHPOINT)) {
            addDefinition(fileName);
        }

        //set the current library to use :
        setDefinition(getCharacterManager().getValueString(CHARACTER_PARAMETER_TOUCHPOINT));

        //to be notify when the character change :
        //CharacterManager.add(this);
        //Phil removed to be handled in the tree construction
    }


    @Override
    protected List<TouchPosition> load(String string) {
        List<TouchPosition> listPosition = new ArrayList<TouchPosition>();
        try {
            XMLParser xmlparser = XML.createParser();
            xmlparser.setValidating(false);
            String touchFile = getCharacterManager().getValueString("TOUCHPOINT");
            XMLTree _tree = xmlparser.parseFile(touchFile);
            XMLTree rootNodeBase = _tree.getRootNode();
            List<XMLTree> listNode = rootNodeBase.getChildrenElement();
            for (int inod = 0; inod < listNode.size(); inod++) {
                XMLTree node = listNode.get(inod);
                if (node.getName().equalsIgnoreCase("touchpoint")) {
                    TouchPosition p = new TouchPosition();
                    p.setId(node.getAttribute("id"));
                    p.setReference(node.getAttribute("reference"));
                    List<XMLTree> offsetNodes = node.getChildrenElement();
                    for (int ioffs = 0; ioffs < offsetNodes.size(); ioffs++) {
                        XMLTree off = offsetNodes.get(ioffs);
                        if (off.getName().equalsIgnoreCase("posOffset")) {
                            p.setPosOffset(new Vec3d(Float.valueOf(off.getAttribute("x")), Float.valueOf(off.getAttribute("y")), Float.valueOf(off.getAttribute("z"))));
                        }
                        if (off.getName().equalsIgnoreCase("rotOffset")) {
                            p.setRotOffset(new Vec3d(Float.valueOf(off.getAttribute("x")), Float.valueOf(off.getAttribute("y")), Float.valueOf(off.getAttribute("z"))));
                        }
                    }
                    listPosition.add(p);
                }
            }
        } catch (Exception e) {
            System.err.println("Problem while opening TouchPoint description file");
        }
        return listPosition;
    }

    @Override
    protected void save(String string, List<TouchPosition> list) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void onCharacterChanged() {
        //set the current library to use :
        setDefinition(getCharacterManager().getValueString(CHARACTER_PARAMETER_TOUCHPOINT));
    }
}
