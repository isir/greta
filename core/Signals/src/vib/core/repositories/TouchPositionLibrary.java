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
package vib.core.repositories;

import java.util.ArrayList;
import java.util.List;
import vib.core.signals.gesture.TouchPosition;
import vib.core.util.CharacterDependent;
import vib.core.util.CharacterManager;
import vib.core.util.math.Vec3d;
import vib.core.util.parameter.ParameterSet;
import vib.core.util.xml.XML;
import vib.core.util.xml.XMLParser;
import vib.core.util.xml.XMLTree;

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
    public TouchPositionLibrary() {
        //get the default library :
        super(CharacterManager.getDefaultValueString(CHARACTER_PARAMETER_TOUCHPOINT));

        //load additionnal library :
        for(String filename : CharacterManager.getAllValuesString(CHARACTER_PARAMETER_TOUCHPOINT)) {
            addDefinition(filename);
        }

        //set the current library to use :
        setDefinition(CharacterManager.getValueString(CHARACTER_PARAMETER_TOUCHPOINT));

        //to be notify when the character change :
        CharacterManager.add(this);
    }


    @Override
    protected List<TouchPosition> load(String string) {
        List<TouchPosition> listPosition = new ArrayList<TouchPosition>();
        try {
            XMLParser xmlparser = XML.createParser();
            xmlparser.setValidating(false);
            String touchFile = CharacterManager.getValueString("TOUCHPOINT");
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
        setDefinition(CharacterManager.getValueString(CHARACTER_PARAMETER_TOUCHPOINT));
    }
}
